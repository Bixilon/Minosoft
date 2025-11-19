/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.operation.LogOp
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
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
import java.time.Instant
import java.util.stream.Collectors


plugins {
    kotlin("jvm") version "2.2.20"
    `jvm-test-suite`
    application
    id("org.ajoberstar.grgit.service") version "5.3.3"
    id("com.github.ben-manes.versions") version "0.53.0"
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

val updates = properties["minosoft.updates"]?.toBoolean() ?: false

val os = properties["platform"]?.let { OSTypes[it] } ?: PlatformInfo.OS
val architecture = properties["architecture"]?.let { Architectures[it] } ?: PlatformInfo.ARCHITECTURE

logger.info("Building for ${os.name.lowercase()}, ${architecture.name.lowercase()}")

repositories {
    mavenCentral()
}

buildscript {
    dependencies {
        classpath("de.bixilon:kutil:1.30.1")
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
                zstdNatives += "_x86"
                javafxNatives += "-x86"
            }

            Architectures.AARCH64, Architectures.ARM -> {
                lwjglNatives += "-arm64"
                zstdNatives += "_aarch64"
                // TODO: javafx for Windows on arm is not yet supported
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
            useJUnitJupiter("5.9.2")

            dependencies {
                implementation(project())
                implementation("de.bixilon:kutil:$kutilVersion")
                implementation("org.jetbrains.kotlin:kotlin-test:2.2.0")
                implementation("com.github.ajalt.clikt:clikt:5.0.3")
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
            useTestNG("7.7.1")

            dependencies {
                implementation(project())

                implementation("org.objenesis:objenesis:3.4")

                // ToDo: Include dependencies from project
                implementation("de.bixilon:kutil:$kutilVersion")
                implementation("it.unimi.dsi:fastutil-core:8.5.18")

                implementation("de.bixilon:mbf-kotlin:1.0.3") { exclude("com.github.luben", "zstd-jni") }

                // netty
                netty("buffer")
                netty("handler")

                jacksonCore("core")
                jacksonCore("databind")
                jackson("module", "kotlin")
                jackson("datatype", "jsr310")
            }

            targets {
                all {
                    testTask.configure {
                        maxHeapSize = "2G"
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
            useTestNG("7.7.1")

            dependencies {
                implementation(project())

                implementation("org.objenesis:objenesis:3.4")

                // ToDo: Include dependencies from project
                implementation("de.bixilon:kutil:$kutilVersion")
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

fun DependencyHandler.javafx(name: String) {
    if (javafxNatives == "") {
        logger.error("JavaFX does not have natives for windows. You must use a JRE that bundles these or disable eros.")
        compileOnly("org.openjfx", "javafx-$name", javafxVersion, classifier = "win") {
            version { strictly(javafxVersion) }
        }
    } else {
        implementation("org.openjfx", "javafx-$name", javafxVersion, classifier = javafxNatives) {
            version { strictly(javafxVersion) }
        }
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

fun JvmComponentDependencies.netty(name: String) {
    implementation("io.netty:netty-$name:$nettyVersion")
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
    implementation("org.slf4j", "slf4j-api", "2.0.17")
    implementation("dnsjava", "dnsjava", "3.6.3")
    implementation("com.github.ajalt.clikt", "clikt", "5.0.3")
    implementation("org.jline", "jline", "3.30.6")
    implementation("org.l33tlabs.twl", "pngdecoder", "1.0")
    implementation("com.github.oshi", "oshi-core", "6.9.1")
    implementation("com.github.luben", "zstd-jni", "1.5.7-6", classifier = zstdNatives)
    implementation("org.kamranzafar", "jtar", "2.3")
    implementation("it.unimi.dsi", "fastutil-core", "8.5.16")
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
    implementation("de.bixilon", "mbf-kotlin", "1.0.3") { exclude("com.github.luben", "zstd-jni") }
    implementation("de.bixilon.javafx", "javafx-svg", "0.3.1") { exclude("org.openjfx", "javafx-controls") } // TODO: remove this, it is really large

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
    implementation(kotlin("reflect", "2.2.20"))


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
    try {
        git = Grgit.open(mapOf("currentDir" to project.rootDir))
    } catch (error: Throwable) {
        logger.warn("Can not open git folder: $error")
        return
    }
    val git = git!!
    commit = git.log { LogOp(git.repository).apply { maxCommits = 1 } }.first()
    val commit = commit!!
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
                "date" to Instant.now().toEpochMilli() / 1000,
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
        val file = project.layout.buildDirectory.get().asFile.resolve("resources/main/assets/minosoft/version.json")
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

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        languageVersion.set(KotlinVersion.KOTLIN_2_1)
        freeCompilerArgs.add("-Xskip-prerelease-check")
        freeCompilerArgs.add("-Xallow-unstable-dependencies")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = StandardCharsets.UTF_8.name()
}

application {
    mainClass.set("de.bixilon.minosoft.Minosoft")
}

var destination: File? = null

val fatJar = tasks.register("fatJar", fun Jar.() {
    destination = destinationDirectory.get().asFile
    archiveBaseName.set("${project.name}-fat-${os.name.lowercase()}-${architecture.name.lowercase()}")
    manifest {
        attributes["Implementation-Title"] = project.name.replaceFirstChar { it.uppercaseChar() }
        attributes["Implementation-Version"] = project.version
        attributes["Main-Class"] = application.mainClass
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/maven/**")


    exclude("com/sun/jna/*sparc*/**")
    exclude("com/sun/jna/*ppc*/**")
    exclude("com/sun/jna/*mips*/**")
    exclude("com/sun/jna/*riscv*/**")
    exclude("com/sun/jna/*s390x*/**")

    if (PlatformInfo.OS != OSTypes.WINDOWS) {
        exclude("com/sun/jna/win32*/**")
        exclude("com/sun/jna/platform/win32/**")
        exclude("com/sun/jna/platform/wince/**")
        exclude("org/lwjgl/system/windows/**")
        exclude("oshi/software/os/windows/**")
        exclude("oshi/hardware/platform/windows/**")
        exclude("oshi/driver/windows/**")
    }
    if (PlatformInfo.OS != OSTypes.MAC) {
        exclude("com/sun/jna/darwin*/**")
        exclude("com/sun/jna/platform/mac/**")
        exclude("org/lwjgl/system/macosx/**")
        exclude("oshi/software/os/mac/**")
        exclude("oshi/hardware/platform/mac/**")
        exclude("oshi/driver/mac/**")
    }
    if (PlatformInfo.OS != OSTypes.UNIX) {
        exclude("com/sun/jna/aix*/**")
        exclude("com/sun/jna/sunos*/**")
        exclude("com/sun/jna/freebsd*/**")
        exclude("com/sun/jna/openbsd*/**")
        exclude("com/sun/jna/dragonflybsd*/**")
        exclude("com/sun/jna/platform/unix/**")
        exclude("com/sun/jna/platform/bsd/**")
        exclude("oshi/software/os/unix/**")
        exclude("oshi/hardware/platform/unix/**")
        exclude("oshi/driver/unix/**")
        exclude("org/lwjgl/system/freebsd/**")
    }
    if (PlatformInfo.OS != OSTypes.LINUX) {
        exclude("com/sun/jna/platform/linux/**")
        exclude("com/sun/jna/linux*/**")
        exclude("org/lwjgl/system/linux/**")
        exclude("oshi/software/os/linux/**")
        exclude("oshi/hardware/platform/linux/**")
        exclude("oshi/driver/linux/**")
    }

    exclude("com/sun/jna/*loongarch64/**")
    exclude("com/sun/jna/*armel/**")
    if (PlatformInfo.ARCHITECTURE != Architectures.AMD64) {
        exclude("com/sun/jna/*x86-64/**")
    }
    if (PlatformInfo.ARCHITECTURE != Architectures.X86) {
        exclude("com/sun/jna/*x86/**")
    }
    if (PlatformInfo.ARCHITECTURE != Architectures.ARM) {
        exclude("com/sun/jna/*arm/**")
    }
    if (PlatformInfo.ARCHITECTURE != Architectures.AARCH64) {
        exclude("com/sun/jna/*aarch64/**")
    }


    // exclude("it/unimi/dsi/fastutil/doubles/**")
    exclude("it/unimi/dsi/fastutil/longs/**")
    exclude("it/unimi/dsi/fastutil/io/**")
    exclude("it/unimi/dsi/fastutil/bytes/**")
    exclude("it/unimi/dsi/fastutil/booleans/**")
    exclude("it/unimi/dsi/fastutil/chars/**")
    exclude("it/unimi/dsi/fastutil/shorts/**")


    // TODO: This is bad! dnsjava is a multi release jar, and that a class is only present with java>18. See https://github.com/dnsjava/dnsjava/issues/329 and https://github.com/Bixilon/Minosoft/issues/33
    exclude("META-INF/services/java.net.spi.InetAddressResolverProvider")
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)
})


tasks.register("assetsProperties", fun JavaExec.() {
    dependsOn("processResources", "compileKotlin", "compileJava")
    classpath(project.configurations.runtimeClasspath.get(), tasks["jar"])
    standardOutput = System.out
    mainClass.set("de.bixilon.minosoft.assets.properties.version.generator.AssetsPropertiesGenerator")
})

tasks.register("upload") {
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
