/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

import de.bixilon.kutil.os.Architectures
import de.bixilon.kutil.os.OSTypes
import de.bixilon.kutil.os.PlatformInfo
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.operation.LogOp
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.charset.StandardCharsets


plugins {
    kotlin("jvm") version "1.9.0"
    `jvm-test-suite`
    application
    id("org.ajoberstar.grgit.service") version "5.2.0"
    id("com.github.ben-manes.versions") version "0.47.0"
}

fun getProperty(name: String): String {
    val value = property(name) ?: throw NullPointerException("Can not find $name property")
    return value.toString()
}

group = "de.bixilon.minosoft"
version = "0.1-pre"
var stable = false

val javafxVersion = getProperty("javafx.version")
val lwjglVersion = getProperty("lwjgl.version")
val ikonliVersion = getProperty("ikonli.version")
val nettyVersion = getProperty("netty.version")
val jacksonVersion = getProperty("jackson.version")
val kutilVersion = getProperty("kutil.version")
val glmVersion = getProperty("glm.version")

val os = properties["platform"]?.let { OSTypes[it] } ?: PlatformInfo.OS
val architecture = properties["architecture"]?.let { Architectures[it] } ?: PlatformInfo.ARCHITECTURE

logger.info("Building for ${os.name.lowercase()}, ${architecture.name.lowercase()}")

repositories {
    mavenCentral()
    maven(url = "https://s01.oss.sonatype.org/content/repositories/releases/")
}

buildscript {
    dependencies {
        classpath("de.bixilon", "kutil", "1.21")
    }
}

var lwjglNatives = ""
var zstdNatives = ""
var javafxNatives = ""

when (os) {
    OSTypes.LINUX -> {
        lwjglNatives += "linux"
        zstdNatives += "linux"
        javafxNatives += "linux"

        when (architecture) {
            Architectures.AMD64 -> {
                zstdNatives += "_amd64"
            }

            Architectures.AARCH64, Architectures.ARM -> {
                lwjglNatives += "-arm64"
                zstdNatives += "_aarch64"
                javafxNatives += "-aarch64"
            }

            else -> throw IllegalArgumentException("Can not determinate linux natives on $architecture")
        }
    }

    OSTypes.MAC -> {
        lwjglNatives += "macos"
        zstdNatives += "darwin"
        javafxNatives += "mac"

        when (architecture) {
            Architectures.AMD64, Architectures.X86 -> {
                zstdNatives += "_x86_64"
            }

            Architectures.AARCH64, Architectures.ARM -> {
                lwjglNatives += "-arm64"
                zstdNatives += "_aarch64"
                javafxNatives += "-aarch64"
            }

            else -> throw IllegalArgumentException("Can not determinate macos natives on $architecture")
        }
    }

    OSTypes.WINDOWS -> {
        lwjglNatives += "windows"
        zstdNatives += "win"
        javafxNatives += "win"

        when (architecture) {
            Architectures.AMD64 -> {
                zstdNatives += "_amd64"
            }

            Architectures.X86 -> {
                lwjglNatives += "-x86"
                zstdNatives += "-x86"
                javafxNatives += "-x86"
            }

            else -> throw IllegalArgumentException("Can not determinate windows natives on $architecture")
        }
    }

    else -> {
        throw IllegalArgumentException("Can not determinate natives for $os on $architecture")
    }
}


testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            testType.set(TestSuiteType.UNIT_TEST)
            useJUnitJupiter("5.9.2")

            dependencies {
                implementation(project())
                implementation("de.bixilon:kutil:$kutilVersion")
                implementation("org.jetbrains.kotlin:kotlin-test:1.9.0")
            }

            targets {
                all {
                    testTask.configure {
                        filter {
                            isFailOnNoMatchingTests = true
                        }
                        testLogging {
                            exceptionFormat = TestExceptionFormat.FULL
                            showExceptions = true
                            showStandardStreams = true
                            events(
                                TestLogEvent.PASSED,
                                TestLogEvent.FAILED,
                                TestLogEvent.SKIPPED,
                                TestLogEvent.STANDARD_OUT,
                                TestLogEvent.STANDARD_ERROR,
                            )
                        }
                    }
                }
            }
            sources {
                kotlin {
                    setSrcDirs(listOf("src/test/java"))
                }
            }
        }


        val integrationTest by registering(JvmTestSuite::class) {
            testType.set(TestSuiteType.INTEGRATION_TEST)
            useTestNG("7.7.1")

            dependencies {
                implementation(project())

                implementation("org.objenesis:objenesis:3.3")

                // ToDo: Include dependencies from project
                implementation("de.bixilon:kutil:$kutilVersion")
                implementation("de.bixilon:kotlin-glm:$glmVersion")
                implementation("it.unimi.dsi:fastutil-core:8.5.12")

                implementation("de.bixilon:mbf-kotlin:1.0") { exclude("com.github.luben", "zstd-jni") }

                jacksonCore("core")
                jacksonCore("databind")
                jackson("module", "kotlin")
                jackson("datatype", "jsr310")
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                        filter {
                            isFailOnNoMatchingTests = true
                        }
                        testLogging {
                            exceptionFormat = TestExceptionFormat.FULL
                            showExceptions = true
                            showStandardStreams = true
                            events(
                                TestLogEvent.PASSED,
                                TestLogEvent.FAILED,
                                TestLogEvent.SKIPPED,
                                TestLogEvent.STANDARD_OUT,
                                TestLogEvent.STANDARD_ERROR,
                            )
                        }
                        options {
                            val options = this as TestNGOptions
                            options.preserveOrder = true
                            // options.excludeGroups("input", "font", "command", "registry", "biome", "version", "fluid", "world", "raycasting", "pixlyzer", "item", "physics", "packet", "container", "item_stack", "signature", "private_key", "interaction", "item_digging", "world_renderer", "rendering")
                        }
                    }
                }
            }
            sources {
                kotlin {
                    setSrcDirs(listOf("src/integration-test/kotlin"))
                }
                resources {
                    setSrcDirs(listOf("src/integration-test/resources"))
                }
            }
        }
        val benchmark by registering(JvmTestSuite::class) {
            testType.set(TestSuiteType.PERFORMANCE_TEST)
            useTestNG("7.7.1")

            dependencies {
                implementation(project())

                implementation("org.objenesis:objenesis:3.3")

                // ToDo: Include dependencies from project
                implementation("de.bixilon:kutil:$kutilVersion")
                implementation("de.bixilon:kotlin-glm:$glmVersion")
            }

            targets {
                all {
                    testTask.configure {
                        filter {
                            isFailOnNoMatchingTests = false
                        }
                        testLogging {
                            exceptionFormat = TestExceptionFormat.FULL
                            showExceptions = true
                            showStandardStreams = true
                            events(
                                TestLogEvent.PASSED,
                                TestLogEvent.FAILED,
                                TestLogEvent.SKIPPED,
                                TestLogEvent.STANDARD_OUT,
                                TestLogEvent.STANDARD_ERROR,
                            )
                        }
                    }
                }
            }
            sources {
                kotlin {
                    setSrcDirs(listOf("src/benchmark/kotlin"))
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}

fun DependencyHandler.javafx(name: String) {
    implementation("org.openjfx", "javafx-$name", javafxVersion, classifier = javafxNatives) {
        version { strictly(javafxVersion) }
    }
}

fun DependencyHandler.ikonli(name: String) {
    implementation("org.kordamp.ikonli", "ikonli-$name", ikonliVersion)
}

fun DependencyHandler.jacksonCore(name: String) {
    implementation("com.fasterxml.jackson.core", "jackson-$name", jacksonVersion)
}


fun DependencyHandler.jackson(group: String, name: String) {
    implementation("com.fasterxml.jackson.$group", "jackson-$group-$name", jacksonVersion)
}

fun JvmComponentDependencies.jacksonCore(name: String) {
    implementation("com.fasterxml.jackson.core:jackson-$name:$jacksonVersion")
}

fun JvmComponentDependencies.jackson(group: String, name: String) {
    implementation("com.fasterxml.jackson.$group:jackson-$group-$name:$jacksonVersion")
}

fun DependencyHandler.netty(name: String) {
    implementation("io.netty", "netty-$name", nettyVersion)
}

fun DependencyHandler.lwjgl(name: String? = null) {
    var artifactId = "lwjgl"
    if (name != null) {
        artifactId += "-$name"
    }
    implementation("org.lwjgl", artifactId, lwjglVersion)
    runtimeOnly("org.lwjgl", artifactId, lwjglVersion, classifier = "natives-$lwjglNatives")
}

dependencies {
    implementation("org.slf4j", "slf4j-api", "2.0.7")
    implementation("com.google.guava", "guava", "32.1.1-jre")
    implementation("dnsjava", "dnsjava", "3.5.2")
    implementation("net.sourceforge.argparse4j", "argparse4j", "0.9.0")
    implementation("org.jline", "jline", "3.23.0")
    implementation("org.l33tlabs.twl", "pngdecoder", "1.0")
    implementation("com.github.oshi", "oshi-core", "6.4.4")
    implementation("com.github.luben", "zstd-jni", "1.5.5-5", classifier = zstdNatives)
    implementation("org.apache.commons", "commons-lang3", "3.12.0")
    implementation("org.kamranzafar", "jtar", "2.3")
    implementation("org.reflections", "reflections", "0.10.2")
    implementation("it.unimi.dsi", "fastutil-core", "8.5.12")
    implementation("org.xeustechnologies", "jcl-core", "2.8")


    // ikonli
    ikonli("fontawesome5-pack")
    ikonli("javafx")

    // jackson
    jacksonCore("core")
    jacksonCore("databind")
    jackson("module", "kotlin")
    jackson("datatype", "jsr310")


    // de.bixilon
    implementation("de.bixilon", "kutil", kutilVersion)
    implementation("de.bixilon", "jiibles", "1.1.1")
    implementation("de.bixilon", "kotlin-glm", glmVersion)
    implementation("de.bixilon", "mbf-kotlin", "1.0") { exclude("com.github.luben", "zstd-jni") }
    implementation("de.bixilon.javafx", "javafx-svg", "0.3") { exclude("org.openjfx", "javafx-controls") }

    // netty
    netty("buffer")
    netty("handler")


    // lwjgl
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    lwjgl()
    lwjgl("glfw")
    lwjgl("openal")
    lwjgl("opengl")
    lwjgl("stb")

    // kotlin
    implementation(kotlin("reflect", "1.9.0"))


    // platform specific
    if (os == OSTypes.LINUX) {
        val nettyNatives = when (architecture) {
            Architectures.AMD64, Architectures.X86 -> "x86_64"
            Architectures.ARM, Architectures.AARCH64 -> "aarch_64"
            else -> throw IllegalArgumentException("Can not determinate netty natives for $architecture")
        }
        implementation("io.netty", "netty-transport-native-epoll", nettyVersion, classifier = "linux-$nettyNatives")
    } else {
        compileOnly("io.netty", "netty-transport-native-epoll", nettyVersion)
    }

    // javafx
    javafx("base")
    javafx("graphics")
    javafx("controls")
    javafx("fxml")
}

tasks.test {
    useJUnitPlatform()
}

var git: Grgit? = null
var commit: Commit? = null

fun loadGit() {
    val git: Grgit
    try {
        git = Grgit.open(mapOf("currentDir" to project.rootDir))
    } catch (error: Throwable) {
        logger.warn("Can not open git folder: $error")
        return
    }
    this.git = git
    val commit = git.log { LogOp(git.repository).apply { maxCommits = 1 } }.first()
    this.commit = commit
    val tag = git.tag.list().find { it.commit == commit }
    var nextVersion = if (tag != null) {
        stable = true
        tag.name
    } else {
        commit.abbreviatedId
    }
    if (!git.status().isClean) {
        nextVersion += "-dirty"
    }
    if (project.version != nextVersion) {
        project.version = nextVersion
        logger.info("Version changed to ${project.version}")
    }
}
loadGit()


val versionJsonTask = tasks.register("versionJson") {
    outputs.upToDateWhen { false }

    doFirst {
        fun generateGit(git: Grgit, commit: Commit): Map<String, Any> {
            return mapOf(
                "branch" to git.branch.current().name,
                "commit" to commit.id,
                "commit_short" to commit.abbreviatedId,
                "dirty" to !git.status().isClean,
            )
        }

        val versionInfo: MutableMap<String, Any> = mutableMapOf(
            "general" to mutableMapOf(
                "name" to project.version,
                "stable" to stable,
            )
        )
        try {
            val git = git
            val commit = commit
            if (git != null && commit != null) {
                versionInfo["git"] = generateGit(git, commit)
            }
        } catch (exception: Throwable) {
            exception.printStackTrace()
        }
        val file = File(project.buildDir.path + "/resources/main/assets/minosoft/version.json")
        file.writeText(groovy.json.JsonOutput.toJson(versionInfo))
    }
}

tasks.getByName("processResources") {
    finalizedBy(versionJsonTask)
    // ToDo: verify and minify jsons
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.languageVersion = "2.0"
}

tasks.withType<JavaCompile> {
    options.encoding = StandardCharsets.UTF_8.name()
}

application {
    mainClass.set("de.bixilon.minosoft.Minosoft")
}

val fatJar = task("fatJar", type = Jar::class) {
    archiveBaseName.set("${project.name}-fat-${os.name.lowercase()}-${architecture.name.lowercase()}")
    manifest {
        attributes["Implementation-Title"] = project.name.capitalized()
        attributes["Implementation-Version"] = project.version
        attributes["Main-Class"] = application.mainClass
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)
}


task("assetsProperties", type = JavaExec::class) {
    dependsOn("processResources", "compileKotlin", "compileJava")
    classpath(project.configurations.runtimeClasspath.get(), tasks["jar"])
    standardOutput = System.out
    mainClass.set("de.bixilon.minosoft.assets.properties.version.generator.AssetsPropertiesGenerator")
}
