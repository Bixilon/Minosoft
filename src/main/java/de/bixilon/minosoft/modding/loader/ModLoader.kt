/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.modding.loader

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.watcher.DataWatcher.Companion.watched
import de.bixilon.minosoft.assets.util.FileUtil.readJson
import de.bixilon.minosoft.modding.loader.LoaderUtil.load
import de.bixilon.minosoft.modding.loader.mod.MinosoftMod
import de.bixilon.minosoft.modding.loader.mod.ModMain
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.io.File
import java.io.FileInputStream
import java.util.jar.JarEntry
import java.util.jar.JarInputStream


object ModLoader {
    private val BASE_PATH = RunConfiguration.HOME_DIRECTORY + "mods/"
    private const val MANFIEST = "manifest.json"
    private val mods: MutableList<MinosoftMod> = mutableListOf()
    private var latch: CountUpAndDownLatch? = null
    var currentPhase by watched(LoadingPhases.PRE_BOOT)
        private set
    var state by watched(PhaseStates.WAITING)
        private set

    private val LoadingPhases.path: File get() = File(BASE_PATH + name.lowercase())

    private fun createDirectories() {
        val created: MutableList<LoadingPhases> = mutableListOf()
        for (phase in LoadingPhases.VALUES) {
            val path = phase.path
            if (!path.exists()) {
                path.mkdirs()
                created += phase
            }
        }
        if (created.isNotEmpty()) {
            Log.log(LogMessageType.MOD_LOADING, LogLevels.VERBOSE) { "Created mod folders: $created" }
        }
    }

    fun initModLoading() {
        DefaultThreadPool += { createDirectories() }
    }

    private fun MinosoftMod.processFile(path: String, data: ByteArray) {
        TODO("Directory")
    }

    private fun MinosoftMod.processJar(file: File) {
        val stream = JarInputStream(FileInputStream(file))
        while (true) {
            val entry = stream.nextEntry ?: break
            if (entry.isDirectory) {
                continue
            }

            if (entry.name.endsWith(".class") && entry is JarEntry) {
                this.classLoader.load(entry, stream)
            } else if (entry.name.startsWith("assets/")) {
                TODO("Assets")
            } else if (entry.name == MANFIEST) {
                manifest = stream.readJson(false)
            }
        }
        stream.close()
    }

    private fun MinosoftMod.construct() {
        val manifest = manifest ?: throw IllegalStateException("Mod $path has no manifest!")
        val mainClass = Class.forName(manifest.main, true, classLoader)
        val main = mainClass.kotlin.objectInstance ?: throw IllegalStateException("${manifest.main} is not an kotlin object!")
        if (main !is ModMain) {
            throw IllegalStateException("${manifest.main} does not inherit ModMain!")
        }
        this.main = main
        main.init()
    }

    private fun MinosoftMod.postInit() {
        main!!.postInit()
    }

    @Synchronized
    fun load(phase: LoadingPhases, latch: CountUpAndDownLatch) {
        if (RunConfiguration.IGNORE_MODS) {
            return
        }
        Log.log(LogMessageType.MOD_LOADING, LogLevels.VERBOSE) { "Starting mod load: $phase" }
        // ToDo: check phase
        this.currentPhase = phase

        val inner = CountUpAndDownLatch(1, latch)
        this.latch = inner
        this.state = PhaseStates.LISTING

        val path = phase.path
        val files = path.listFiles()
        if (files == null || files.isEmpty()) {
            // no mods to load
            inner.dec()
            state = PhaseStates.COMPLETE
            return
        }
        val mods: MutableList<MinosoftMod> = mutableListOf()

        state = PhaseStates.INJECTING
        for (file in files) {
            if (!file.isDirectory && !file.name.endsWith(".jar") && !file.name.endsWith(".zip")) {
                continue
            }
            val mod = MinosoftMod(file, phase, CountUpAndDownLatch(3, inner))
            Log.log(LogMessageType.MOD_LOADING, LogLevels.VERBOSE) { "Injecting $file" }
            try {
                if (file.isDirectory) {
                    TODO("Scanning directory")
                }
                mod.processJar(file)
                mods += mod
                mod.latch.dec()
            } catch (exception: Throwable) {
                Log.log(LogMessageType.MOD_LOADING, LogLevels.WARN) { "Error injecting mod: $file" }
                exception.printStackTrace()
                mod.latch.count = 0
            }
        }


        state = PhaseStates.CONSTRUCTING
        var worker = UnconditionalWorker()
        for (mod in mods) {
            worker += {
                try {
                    mod.construct()
                    mod.latch.dec()
                } catch (error: Throwable) {
                    mod.latch.count = 0
                    error.printStackTrace()
                    mods -= mod
                }
            }
        }
        worker.work(inner)

        state = PhaseStates.POST_INIT
        worker = UnconditionalWorker()
        for (mod in mods) {
            worker += {
                try {
                    mod.postInit()
                    mod.latch.dec()
                } catch (error: Throwable) {
                    mod.latch.count = 0
                    error.printStackTrace()
                    mods -= mod
                }
            }
        }
        worker.work(inner)


        this.mods += mods
        state = PhaseStates.COMPLETE
        inner.dec()
        if (phase == LoadingPhases.POST_BOOT) {
            Log.log(LogMessageType.MOD_LOADING, LogLevels.INFO) { "Mod loading completed!" }
        }
    }

    fun await(phase: LoadingPhases) {
        if (RunConfiguration.IGNORE_MODS) {
            return
        }
        val latch = this.latch
        val currentPhase = this.currentPhase
        val state = this.state
        if (currentPhase == phase) {
            if (state == PhaseStates.COMPLETE) {
                return
            }
            latch!!.await() // ToDo: What if phase has not started yet?
            return
        }
        if (phase.ordinal < currentPhase.ordinal) {
            // already done
            return
        }
        throw IllegalStateException("$phase has not started yet!")
    }
}
