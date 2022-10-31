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
import de.bixilon.minosoft.assets.directory.DirectoryAssetsManager
import de.bixilon.minosoft.assets.file.ZipAssetsManager
import de.bixilon.minosoft.assets.util.FileUtil.readJson
import de.bixilon.minosoft.modding.loader.LoaderUtil.load
import de.bixilon.minosoft.modding.loader.error.*
import de.bixilon.minosoft.modding.loader.mod.MinosoftMod
import de.bixilon.minosoft.modding.loader.mod.ModMain
import de.bixilon.minosoft.modding.loader.mod.manifest.load.LoadM
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
    private const val MANIFEST = "manifest.json"
    private var latch: CountUpAndDownLatch? = null
    val mods = ModList()
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


    private fun MinosoftMod.scanDirectory(base: File, file: File) {
        if (file.isFile) {
            this.classLoader.load(file.path.removePrefix(base.path).removePrefix("/"), FileInputStream(file).readAllBytes())
        }
        if (file.isDirectory) {
            for (sub in file.listFiles()!!) {
                scanDirectory(base, sub)
            }
        }
    }

    private fun MinosoftMod.processDirectory(file: File) {
        val files = file.listFiles()!!
        val assets = DirectoryAssetsManager(file.path)
        assets.load(CountUpAndDownLatch(0))

        for (sub in files) {
            if (sub.isDirectory && sub.name == "assets") {
                continue
            }
            if (sub.isFile && sub.name == MANIFEST) {
                manifest = FileInputStream(sub).readJson()
            }
            scanDirectory(file, sub)
        }

        this.assetsManager = assets
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
            } else if (entry.name == MANIFEST) {
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
        val manifest = manifest ?: throw NoManifestError(path)
        val mainClass = Class.forName(manifest.main, true, classLoader)
        val main = mainClass.kotlin.objectInstance ?: throw NoObjectMainError(mainClass)
        if (main !is ModMain) {
            throw NoModMainError(mainClass)
        }
        main.assets = assetsManager!!
        this.main = main
        main.init()
    }

    private fun MinosoftMod.postInit() {
        main!!.postInit()
    }

    private fun inject(list: ModList, file: File, phase: LoadingPhases, latch: CountUpAndDownLatch) {
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
            val manifest = mod.manifest!!
            val name = manifest.name
            if (name in list || name in this.mods) {
                throw DuplicateModError(name)
            }
            manifest.packages?.provides?.let {
                for (providedName in it) {
                    val provided = list[providedName] ?: this.mods[providedName]
                    if (provided != null) {
                        throw DuplicateProvidedError(mod, provided, providedName)
                    }
                }
            }
            list += mod
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
            if (phase == LoadingPhases.POST_BOOT) {
                Log.log(LogMessageType.MOD_LOADING, LogLevels.INFO) { "Mod loading completed!" }
            }
            state = PhaseStates.COMPLETE
            return
        }
        val list = ModList()

        state = PhaseStates.INJECTING
        var worker = UnconditionalWorker()
        for (file in files) {
            worker += add@{ inject(list, file, phase, inner) }
        }
        worker.work(inner)

        state = PhaseStates.CONSTRUCTING
        worker = UnconditionalWorker()
        for (mod in list) {
            worker += {
                try {
                    mod.construct()
                    mod.latch.dec()
                } catch (error: Throwable) {
                    mod.latch.count = 0
                    error.printStackTrace()
                    list -= mod
                }
            }
        }
        worker.work(inner)

        state = PhaseStates.POST_INIT
        worker = UnconditionalWorker()
        for (mod in list) {
            worker += {
                try {
                    mod.postInit()
                    mod.latch.dec()
                } catch (error: Throwable) {
                    mod.latch.count = 0
                    error.printStackTrace()
                    list -= mod
                }
            }
        }
        worker.work(inner)


        this.mods += list
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

    private fun LoadM.checkDependencies(mods: List<MinosoftMod>) {

    }
}
