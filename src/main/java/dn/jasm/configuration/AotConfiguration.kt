package dn.jasm.configuration

import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportRuntimeHints
import org.springframework.web.util.pattern.PathPatternParser

@Configuration
@ImportRuntimeHints(AppRuntimeHints::class)
open class AotConfiguration

class AppRuntimeHints : RuntimeHintsRegistrar {
    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        hints.reflection().registerType(
            PathPatternParser::class.java
        ) { typeHint ->
            typeHint.withMembers()
        }
    }
}
