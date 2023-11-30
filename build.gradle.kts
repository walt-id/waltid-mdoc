plugins {
    kotlin("multiplatform") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    id("dev.petuska.npm.publish") version "3.3.1"
    `maven-publish`
}

group = "id.walt"
version = "1.SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        jvmToolchain(16)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        nodejs {
            generateTypeScriptDefinitions()
        }
        binaries.library()
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }
    val kryptoVersion = "4.0.1"


    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
                //implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.5.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("com.soywiz.korlibs.krypto:krypto:$kryptoVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.kotest:kotest-assertions-core:5.5.5")

                implementation("io.kotest:kotest-assertions-json:5.5.5")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("com.augustcellars.cose:cose-java:1.1.0")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("org.bouncycastle:bcprov-jdk15to18:1.72")
                implementation("org.bouncycastle:bcpkix-jdk15to18:1.72")
                implementation("io.mockk:mockk:1.13.2")

                implementation("io.kotest:kotest-runner-junit5:5.5.5")
                implementation("io.kotest:kotest-assertions-core:5.5.5")
                implementation("io.kotest:kotest-assertions-json:5.5.5")
                implementation(kotlin("reflect"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(npm("cose-js", "0.9.0"))
            }
        }
        val jsTest by getting {

        }
        val nativeMain by getting
        val nativeTest by getting
    }

    publishing {
        repositories {
            val envUsername = System.getenv("MAVEN_USERNAME")
            val envPassword = System.getenv("MAVEN_PASSWORD")
            val usernameFile = File("secret_maven_username.txt")
            val passwordFile = File("secret_maven_password.txt")
            val secretMavenUsername = envUsername ?: usernameFile.let { if (it.isFile) it.readLines().first() else "" }
            val secretMavenPassword = envPassword ?: passwordFile.let { if (it.isFile) it.readLines().first() else "" }
            val hasMavenAuth = secretMavenUsername.isNotEmpty() && secretMavenPassword.isNotEmpty()
            if (hasMavenAuth) {
                maven {
                    url = uri("https://maven.walt.id/repository/waltid/")
                    credentials {
                        username = secretMavenUsername
                        password = secretMavenPassword
                    }
                }
            }
        }
    }
}

npmPublish {
    registries {
        val envToken = System.getenv("NPM_TOKEN")
        val npmTokenFile = File("secret_npm_token.txt")
        val secretNpmToken = envToken ?: npmTokenFile.let { if (it.isFile) it.readLines().first() else "" }
        val hasNPMToken = secretNpmToken.isNotEmpty()
        val isReleaseBuild = Regex("\\d+.\\d+.\\d+").matches(version.get())
        println("NPM token: ${hasNPMToken}")
        println("Release build: ${isReleaseBuild}")
        if (isReleaseBuild && hasNPMToken) {
            readme.set(File("README.md"))
            register("npmjs") {
                uri.set(uri("https://registry.npmjs.org"))
                authToken.set(secretNpmToken)
            }
        }
    }
}
