package id.walt.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import id.walt.Values
import id.walt.config.ConfigManager
import id.walt.config.WebConfig
import id.walt.db.Db
import id.walt.web.UserApi.helloApi
import id.walt.web.plugins.*
import io.github.oshai.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

data class CliConfig(var dataDir: String, val properties: MutableMap<String, String>, var verbose: Boolean)

val log = KotlinLogging.logger { }

class Walt : CliktCommand(
    name = "walt",
    help = """XYZ Kit by walt.id

        The XYZ Kit by walt.id is a command line tool that allows you to ... 
        
        Example commands are:
        
        ./xyzkit.sh -h

        docker run -itv ${'$'}(pwd)/data:/app/data waltid/xyzkit -h
        
        """
) {
    init {
        versionOption(Values.version, message = {
            """
            XYZ Kit: $it${if (Values.isSnapshot) " - SNAPSHOT VERSION, use only for demo and testing purposes" else " - stable release"}
            Environment: ${System.getProperty("java.runtime.name")} of ${System.getProperty("java.vm.name")} (${
                System.getProperty(
                    "java.version.date"
                )
            })
            OS version:  ${System.getProperty("os.name")} ${System.getProperty("os.version")}
        """.trimIndent()
        })
    }

    val cliConfig: Map<String, String> by option(
        "-c",
        "--config",
        help = "Overrides a config key/value pair."
    ).associate()
    val verbose: Boolean by option("-v", "--verbose", help = "Enables verbose mode.")
        .flag()

    override fun run() {
        val config = CliConfig("data", HashMap(), verbose)

        config.properties.putAll(this.cliConfig)

        currentContext.obj = config

        if (config.verbose) {
            log.debug { "Config loaded: $config" }
        }
    }
}

object WaltCLI {

    fun start(args: Array<String>) {

        try {

            log.debug { "XYZ Kit CLI starting ..." }

            args.forEach { arg -> println(arg) }

            log.info("Reading configurations...")
            ConfigManager.loadConfigs(args)

            Db.start()

            val webConfig = ConfigManager.getConfig<WebConfig>()
            log.info("Starting web server (binding to ${webConfig.webHost}, listening on port ${webConfig.webPort})...")
            embeddedServer(CIO, port = webConfig.webPort, host = webConfig.webHost, module = Application::module)
                .start(wait = true)

        } catch (e: Exception) {
            println(e.message)

            if (log.isDebugEnabled)
                e.printStackTrace()
        }
    }
}

fun Application.configurePlugins() {
    configureHTTP()
    configureMonitoring()
    configureStatusPages()
    configureSerialization()
    configureRouting()
    configureOpenApi()
}

fun Application.module() {
    configurePlugins()
    helloApi()
}
