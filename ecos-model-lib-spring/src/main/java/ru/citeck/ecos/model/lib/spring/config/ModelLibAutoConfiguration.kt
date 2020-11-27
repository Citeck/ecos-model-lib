package ru.citeck.ecos.model.lib.spring.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

/**
 * Auto configuration to initialize model-lib beans.
 */
@Configuration
@ComponentScan(basePackages = ["ru.citeck.ecos.model.lib.spring"])
open class ModelLibAutoConfiguration
