package ru.citeck.ecos.model.lib.workspace

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import com.github.benmanes.caffeine.cache.LoadingCache
import com.github.benmanes.caffeine.cache.Ticker
import java.time.Duration

/**
 * Cache for workspace identifier mappings: workspaceId -> workspace system id and back.
 *
 * A resolved mapping behaves as a plain `expireAfterAccess(accessTtl) + expireAfterWrite(resolvedTtl)`
 * cache. A mapping which resolved nothing is kept for `unresolvedTtl` only and is never prolonged by
 * reads, because such an answer doesn't have to mean that the workspace doesn't exist: it also
 * appears when the workspace was not created yet or could not be read at the moment of the lookup.
 * While an empty mapping is cached, every reference prefixed with that workspace system id resolves
 * to the global scope and all artifacts of the workspace are read as non-existent - COREDEV-514.
 */
internal class WsIdMappingCache private constructor(
    private val cache: LoadingCache<String, Entry>
) {

    companion object {

        /**
         * @param isResolved must return false for an answer which is not a real mapping
         *        (an empty string, a generated identifier and so on).
         * @param loader must throw when the mapping can't be read at all - nothing is cached when
         *        a Caffeine loader throws, so the next call will try again.
         */
        fun create(
            resolvedTtl: Duration,
            unresolvedTtl: Duration,
            accessTtl: Duration,
            maxSize: Long,
            isResolved: (String) -> Boolean,
            ticker: Ticker = Ticker.systemTicker(),
            loader: (String) -> String
        ): WsIdMappingCache {

            fun ttlOf(value: String): Long {
                return if (isResolved(value)) {
                    resolvedTtl.toNanos()
                } else {
                    unresolvedTtl.toNanos()
                }
            }

            val cache = Caffeine.newBuilder()
                .ticker(ticker)
                .maximumSize(maxSize)
                .expireAfter(
                    object : Expiry<String, Entry> {

                        override fun expireAfterCreate(key: String, value: Entry, currentTime: Long): Long {
                            return ttlOf(value.value)
                        }

                        override fun expireAfterUpdate(
                            key: String,
                            value: Entry,
                            currentTime: Long,
                            currentDuration: Long
                        ): Long {
                            return ttlOf(value.value)
                        }

                        override fun expireAfterRead(
                            key: String,
                            value: Entry,
                            currentTime: Long,
                            currentDuration: Long
                        ): Long {
                            // the entry dies at min(lastRead + accessTtl, loadedAt + its own ttl):
                            // reads prolong the life of a mapping, but never beyond the moment when
                            // it has to be loaded again anyway. currentDuration can't be used for
                            // the second part - after the first read it is the access-based one.
                            val timeLeft = value.loadedAt + ttlOf(value.value) - currentTime
                            return minOf(accessTtl.toNanos(), timeLeft.coerceAtLeast(0))
                        }
                    }
                ).build { key: String -> Entry(loader(key), ticker.read()) }

            return WsIdMappingCache(cache)
        }
    }

    fun get(key: String): String {
        return cache.get(key).value
    }

    private class Entry(
        val value: String,
        val loadedAt: Long
    )
}
