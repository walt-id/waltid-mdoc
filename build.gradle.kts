import com.github.jk1.license.filter.DependencyFilter
import com.github.jk1.license.filter.LicenseBundleNormalizer
import com.github.jk1.license.render.InventoryHtmlReportRenderer
import com.github.jk1.license.render.ReportRenderer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.20"
    id("io.ktor.plugin") version "2.2.4"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.20"
    id("org.owasp.dependencycheck") version "6.5.3"
    id("com.github.jk1.dependency-license-report") version "2.0"
    application
    `maven-publish`
}

group = "id.walt"
version = "1.SNAPSHOT"

repositories {
    mavenCentral()
    //jcenter()
    maven("https://jitpack.io")
    maven("https://maven.walt.id/repository/waltid/")
    mavenLocal()
}

dependencies {

    /* -- KTOR -- */

    // Ktor server
    implementation("io.ktor:ktor-server-core-jvm:2.2.4")
    implementation("io.ktor:ktor-server-auth-jvm:2.2.4")
    implementation("io.ktor:ktor-server-sessions-jvm:2.2.4")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:2.2.4")
    implementation("io.ktor:ktor-server-auto-head-response-jvm:2.2.4")
    implementation("io.ktor:ktor-server-double-receive-jvm:2.2.4")
    implementation("io.ktor:ktor-server-host-common-jvm:2.2.4")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.2.4")
    implementation("io.ktor:ktor-server-compression-jvm:2.2.4")
    implementation("io.ktor:ktor-server-cors-jvm:2.2.4")
    implementation("io.ktor:ktor-server-forwarded-header-jvm:2.2.4")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.2.4")
    implementation("io.ktor:ktor-server-call-id-jvm:2.2.4")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.2.4")
    implementation("io.ktor:ktor-server-cio-jvm:2.2.4")

    // Ktor server external libs
    implementation("io.github.smiley4:ktor-swagger-ui:1.4.0")

    // Ktor client
    implementation("io.ktor:ktor-client-core:2.2.4")
    implementation("io.ktor:ktor-client-serialization:2.2.4")
    implementation("io.ktor:ktor-client-content-negotiation:2.2.4")
    implementation("io.ktor:ktor-client-json:2.2.4")
    implementation("io.ktor:ktor-client-cio:2.2.4")
    implementation("io.ktor:ktor-client-logging-jvm:2.2.4")


    /* -- Kotlin -- */

    // Kotlinx.serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.2.4")

    // Date
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")


    /* -- Database --*/

    // DB
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.xerial:sqlite-jdbc:3.41.2.1")
    //implementation("org.postgresql:postgresql:42.5.4")

    /* -- Misc --*/

    // Config
    implementation("com.sksamuel.hoplite:hoplite-core:2.7.3")
    implementation("com.sksamuel.hoplite:hoplite-hocon:2.7.3")

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:4.0.0-beta-22")
    implementation("org.slf4j:slf4j-simple:2.0.7")
    implementation("org.slf4j:jul-to-slf4j:2.0.7")
    implementation("io.ktor:ktor-client-cio-jvm:2.2.4")

    // Test
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.8.20")

    testImplementation("io.kotest:kotest-runner-junit5:5.5.5")
    testImplementation("io.kotest:kotest-assertions-core:5.5.5")
    testImplementation("io.kotest.extensions:kotest-assertions-ktor:2.0.0")
    testImplementation("io.ktor:ktor-server-tests-jvm:2.2.4")

    // CLI
    implementation("com.github.ajalt.clikt:clikt-jvm:3.5.0")
    implementation("com.github.ajalt.clikt:clikt:3.5.0")

}

tasks.withType<Test> {
    useJUnitPlatform()

    // Use the following condition to optionally run the integration tests:
    // > gradle build -PrunIntegrationTests
    if (!project.hasProperty("runIntegrationTests")) {
        exclude("id/walt/test/integration/**")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.named<CreateStartScripts>("startScripts") {
    doLast {
        windowsScript.writeText(
            windowsScript.readText().replace(Regex("set CLASSPATH=.*"), "set CLASSPATH=%APP_HOME%\\\\lib\\\\*")
        )
    }
}

application {
    mainClass.set("id.walt.MainCliKt")
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            pom {
                name.set("walt.id XYZ Kit")
                description.set(
                    """
                    Kotlin/Java library for XYZ core services
                    """.trimIndent()
                )
                url.set("https://walt.id")
            }
            from(components["java"])
        }
    }

    repositories {
        maven {
            url = uri("https://maven.walt.id/repository/waltid/")
            val envUsername = System.getenv("MAVEN_USERNAME")
            val envPassword = System.getenv("MAVEN_PASSWORD")

            val usernameFile = File("secret_maven_username.txt")
            val passwordFile = File("secret_maven_password.txt")

            val secretMavenUsername = envUsername ?: usernameFile.let { if (it.isFile) it.readLines().first() else "" }
            val secretMavenPassword = envPassword ?: passwordFile.let { if (it.isFile) it.readLines().first() else "" }

            credentials {
                username = secretMavenUsername
                password = secretMavenPassword
            }
        }
    }
}

licenseReport {
    renderers = arrayOf<ReportRenderer>(InventoryHtmlReportRenderer("xyzkit-licenses-report.html","XYZ Kit"))
    filters = arrayOf<DependencyFilter>(LicenseBundleNormalizer())
}
