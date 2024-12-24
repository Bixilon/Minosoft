/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

import de.bixilon.kutil.array.ByteArrayUtil.toHex
import de.bixilon.kutil.base64.Base64Util.fromBase64
import de.bixilon.kutil.base64.Base64Util.toBase64
import de.bixilon.kutil.hash.HashUtil
import de.bixilon.kutil.os.Architectures
import de.bixilon.kutil.os.OSTypes
import de.bixilon.kutil.os.PlatformInfo
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.stream.InputStreamUtil.copy
import de.bixilon.kutil.time.TimeUtil
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.operation.LogOp
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.util.stream.Collectors


plugins {
    kotlin("jvm") version "2.1.0"
    `jvm-test-suite`
    application
    id("org.ajoberstar.grgit.service") version "5.3.0"
    id("com.github.ben-manes.versions") version "0.51.0"
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

val updates = properties["minosoft.updates"]?.toBoolean() ?: false

val os = properties["platform"]?.let { OSTypes[it] } ?: PlatformInfo.OS
val architecture = properties["architecture"]?.let { Architectures[it] } ?: PlatformInfo.ARCHITECTURE

logger.info("Building for ${os.name.lowercase()}, ${architecture.name.lowercase()}")

repositories {
    mavenCentral()
    maven(url = "https://s01.oss.sonatype.org/content/repositories/releases/")
}

buildscript {
    dependencies {
        classpath("de.bixilon", "kutil", "1.26.5")
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
            /*
            Architectures.ARM -> {
                lwjglNatives += "-arm64"
                zstdNatives += "-amd64"
                 // TODO: javafx for Windows on arm is not yet supported
            }
             */

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
                implementation("org.jetbrains.kotlin:kotlin-test:2.1.0")
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

                implementation("org.objenesis:objenesis:3.4")

                // ToDo: Include dependencies from project
                implementation("de.bixilon:kutil:$kutilVersion")
                implementation("de.bixilon:kotlin-glm:$glmVersion")
                implementation("it.unimi.dsi:fastutil-core:8.5.15")

                implementation("de.bixilon:mbf-kotlin:1.0.3") { exclude("com.github.luben", "zstd-jni") }

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
                            // options.excludeGroups("models", "mesher", "chunk", "input", "font", "command", "registry", "biome", "version", "fluid", "world", "raycasting", "pixlyzer", "item", "block", "physics", "light", "packet", "container", "item_stack", "signature", "private_key", "interaction", "item_digging", "chunk_renderer", "rendering", "texture", "atlas", "gui")
                            //   options.excludeGroups("models", "chunk", "input", "font", "command", "registry", "biome", "version", "fluid", "world", "raycasting", "pixlyzer", "item", "physics", "light", "packet", "container", "item_stack", "signature", "private_key", "interaction", "item_digging", "chunk_renderer", "texture", "atlas", "gui")
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

                implementation("org.objenesis:objenesis:3.4")

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
    implementation("org.slf4j", "slf4j-api", "2.0.16")
    implementation("com.google.guava", "guava", "33.3.1-jre")
    implementation("dnsjava", "dnsjava", "3.6.2")
    implementation("net.sourceforge.argparse4j", "argparse4j", "0.9.0")
    implementation("org.jline", "jline", "3.28.0")
    implementation("org.l33tlabs.twl", "pngdecoder", "1.0")
    implementation("com.github.oshi", "oshi-core", "6.6.5")
    implementation("com.github.luben", "zstd-jni", "1.5.6-8", classifier = zstdNatives)
    implementation("org.apache.commons", "commons-lang3", "3.17.0")
    implementation("org.kamranzafar", "jtar", "2.3")
    implementation("org.reflections", "reflections", "0.10.2")
    implementation("it.unimi.dsi", "fastutil-core", "8.5.15")
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
    implementation("de.bixilon", "mbf-kotlin", "1.0.3") { exclude("com.github.luben", "zstd-jni") }
    implementation("de.bixilon.javafx", "javafx-svg", "0.3.1") { exclude("org.openjfx", "javafx-controls") }

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
    implementation(kotlin("reflect", "2.1.0"))


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


fun Commit.shortId() = id.substring(0, 10)

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
        commit.shortId()
    }
    val status = git.status()
    if (!status.isClean) {
        nextVersion += "-dirty"
        println(status)
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
            val status = git.status()
            return mapOf(
                "branch" to (System.getenv()["CI_COMMIT_BRANCH"] ?: git.branch.current().name),
                "commit" to commit.id,
                "commit_short" to commit.shortId(),
                "dirty" to !status.isClean,
            )
        }

        val versionInfo: MutableMap<String, Any> = mutableMapOf(
            "general" to mutableMapOf(
                "name" to project.version,
                "date" to TimeUtil.seconds(),
                "stable" to stable,
                "updates" to updates,
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
    kotlinOptions { freeCompilerArgs += "-Xskip-prerelease-check"; freeCompilerArgs += "-Xallow-unstable-dependencies" }
}

tasks.withType<JavaCompile> {
    options.encoding = StandardCharsets.UTF_8.name()
}

application {
    mainClass.set("de.bixilon.minosoft.education.MinosoftEducation")
}

var destination: File? = null

val fatJar = task("fatJar", type = Jar::class) {
    destination = destinationDirectory.get().asFile
    archiveBaseName.set("${project.name}-fat-${os.name.lowercase()}-${architecture.name.lowercase()}")
    manifest {
        attributes["Implementation-Title"] = project.name.capitalized()
        attributes["Implementation-Version"] = project.version
        attributes["Main-Class"] = application.mainClass
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/maven/**")
    // TODO: This is bad! dnsjava is a multi release jar, and that a class is only present with java>18. See https://github.com/dnsjava/dnsjava/issues/329 and https://github.com/Bixilon/Minosoft/issues/33
    exclude("META-INF/services/java.net.spi.InetAddressResolverProvider")
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)

    // TODO: exclude a lot of unneeded files

    // remote other platforms from com.sun.jna
    // remove most of it.unimi.fastutil classes
}


task("assetsProperties", type = JavaExec::class) {
    dependsOn("processResources", "compileKotlin", "compileJava")
    classpath(project.configurations.runtimeClasspath.get(), tasks["jar"])
    standardOutput = System.out
    mainClass.set("de.bixilon.minosoft.assets.properties.version.generator.AssetsPropertiesGenerator")
}

task("upload") {
    dependsOn("fatJar")
    doLast {
        val base = (destination ?: File("build/libs"))
        val file = base.resolve("${project.name}-fat-${os.name.lowercase()}-${architecture.name.lowercase()}-${project.version}.jar")
        if (!file.exists()) throw FileNotFoundException("Release file to upload was not found???: $file, available: ${base.listFiles()}")
        val key = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(System.getenv("RELEASE_KEY").fromBase64()))

        val digest = MessageDigest.getInstance(HashUtil.SHA_512)
        val sign = Signature.getInstance("SHA512withRSA")
        sign.initSign(key)
        val stream = FileInputStream(file)
        stream.copy(digest = digest, signature = sign)

        val sha512 = digest.digest().toHex()
        val signature = sign.sign().toBase64()

        stream.close()

        val data = mapOf(
            "size" to file.length(),
            "sha512" to sha512,
            "signature" to signature,
            "os" to os.name.lowercase(),
            "architecture" to architecture.name.lowercase(),
        )
        val url = System.getenv("MINOSOFT_API") + "/api/v1/releases/upload/${project.version}?"
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(data.entries.stream().map { it.key + "=" + URLEncoder.encode(it.value.toString(), StandardCharsets.UTF_8) }.collect(Collectors.joining("&", url, ""))))
            .header("Authorization", "Bearer " + System.getenv("MINOSOFT_TOKEN"))
            .PUT(BodyPublishers.ofFile(file.toPath()))

        val response = client.send(request.build(), HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) throw IOException("Could not upload: $response")
    }
}
