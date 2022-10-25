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

import de.bixilon.kutil.collections.CollectionUtil.synchronizedListOf
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.watcher.DataWatcher.Companion.watched
import de.bixilon.minosoft.assets.file.ZipAssetsManager
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

    private fun MinosoftMod.processDirectory(file: File) {
        TODO("Directory")
    }

    private fun MinosoftMod.processJar(file: File) {
        val stream = JarInputStream(FileInputStream(file))
        val assets = ZipAssetsManager(stream)
        while (true) {
            val entry = stream.nextEntry ?: break
            if (entry.isDirectory) {
                continue
            }

            if (entry.name.endsWith(".class") && entry is JarEntry) {
                this.classLoader.load(entry, stream)
            } else if (entry.name == MANFIEST) {
                manifest = stream.readJson(false)
            } else {
                assets.push(entry.name, stream)
            }
        }
        stream.close()
        this.assetsManager = assets
        assets.loaded = true
    }

    private fun MinosoftMod.construct() {
        val manifest = manifest ?: throw IllegalStateException("Mod $path has no manifest!")
        val mainClass = Class.forName(manifest.main, true, classLoader)
        val main = mainClass.kotlin.objectInstance ?: throw IllegalStateException("${manifest.main} is not an kotlin object!")
        if (main !is ModMain) {
            throw IllegalStateException("${manifest.main} does not inherit ModMain!")
        }
        main.assets = assetsManager!!
        this.main = main
        main.init()
    }

    private fun MinosoftMod.postInit() {
        main!!.postInit()
    }

    private fun inject(mods: MutableList<MinosoftMod>, file: File, phase: LoadingPhases, latch: CountUpAndDownLatch) {
        if (!file.isDirectory && !file.name.endsWith(".jar") && !file.name.endsWith(".zip")) {
            return
        }
        val mod = MinosoftMod(file, phase, CountUpAndDownLatch(3, latch))
        Log.log(LogMessageType.MOD_LOADING, LogLevels.VERBOSE) { "Injecting $file" }
        try {
            if (file.isDirectory) {
                mod.processDirectory(file)
            } else {
                mod.processJar(file)
            }
            mods += mod
            mod.latch.dec()
        } catch (exception: Throwable) {
            Log.log(LogMessageType.MOD_LOADING, LogLevels.WARN) { "Error injecting mod: $file" }
            exception.printStackTrace()
            mod.latch.count = 0
        }
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
        val mods: MutableList<MinosoftMod> = synchronizedListOf()

        state = PhaseStates.INJECTING
        var worker = UnconditionalWorker()
        for (file in files) {
            worker += add@{ inject(mods, file, phase, inner) }
        }
        worker.work(inner)

        state = PhaseStates.CONSTRUCTING
        worker = UnconditionalWorker()
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
