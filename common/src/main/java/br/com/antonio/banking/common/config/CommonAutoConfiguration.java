package br.com.antonio.banking.common.config;

import br.com.antonio.banking.common.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration entry point for the common module.
 * Automatically imported by any Spring Boot service that has 'common' on the classpath
 * via META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 */
@AutoConfiguration
@Import(GlobalExceptionHandler.class)
public class CommonAutoConfiguration {
}