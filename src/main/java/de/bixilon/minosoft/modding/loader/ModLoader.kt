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
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.modding.loader.error.*
import de.bixilon.minosoft.modding.loader.mod.MinosoftMod
import de.bixilon.minosoft.modding.loader.mod.ModMain
import de.bixilon.minosoft.modding.loader.mod.logger.ModLogger
import de.bixilon.minosoft.modding.loader.mod.source.ModSource
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.io.File
import kotlin.reflect.jvm.javaField


object ModLoader {
    private val BASE_PATH = RunConfiguration.HOME_DIRECTORY + "mods/"
    private var latch: CountUpAndDownLatch? = null
    val mods = ModList()
    var currentPhase by observed(LoadingPhases.PRE_BOOT)
        private set
    var state by observed(PhaseStates.WAITING)
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


    private fun MinosoftMod.construct() {
        val manifest = manifest ?: throw NoManifestError(source)
        val mainClass = Class.forName(manifest.main, true, classLoader)
        val main = mainClass.kotlin.objectInstance ?: throw NoObjectMainError(mainClass)
        if (main !is ModMain) {
            throw NoModMainError(mainClass)
        }
        ASSETS_MANAGER_FIELD[main] = assetsManager!!
        LOGGER_FIELD[main] = ModLogger(manifest.name)
        this.main = main
        main.init()
    }

    private fun MinosoftMod.postInit() {
        main!!.postInit()
    }

    private fun inject(list: ModList, source: ModSource, phase: LoadingPhases, latch: CountUpAndDownLatch) {
        val mod = MinosoftMod(source, phase, CountUpAndDownLatch(4, latch))
        Log.log(LogMessageType.MOD_LOADING, LogLevels.VERBOSE) { "Injecting $source" }
        try {
            source.process(mod)
            val manifest = mod.manifest!!
            val name = manifest.name

            if (name in RunConfiguration.MOD_PARAMETERS.ignoreMods) {
                mod.latch.count = 0
                return
            }
            if (name in list || name in this.mods) {
                throw DuplicateModError(name)
            }
            manifest.packages?.provides?.let {
                // ToDO: possible race condition?
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
            Log.log(LogMessageType.MOD_LOADING, LogLevels.WARN) { "Error injecting mod: $source" }
            exception.printStackTrace()
            mod.latch.count = 0
        }
    }

    fun MinosoftMod.validate(mods: ModList) {
        val manifest = manifest!!
        val missingDependencies = manifest.packages?.getMissingDependencies(mods)
        if (missingDependencies != null) {
            throw MissingDependencyError(this, missingDependencies)
        }
    }

    @Synchronized
    fun load(phase: LoadingPhases, latch: CountUpAndDownLatch) {
        if (RunConfiguration.IGNORE_MODS || phase in RunConfiguration.MOD_PARAMETERS.ignorePhases) {
            return
        }
        Log.log(LogMessageType.MOD_LOADING, LogLevels.VERBOSE) { "Starting mod load: $phase" }
        // ToDo: check phase
        this.currentPhase = phase

        val inner = CountUpAndDownLatch(1, latch)
        this.latch = inner
        this.state = PhaseStates.LISTING

        val path = phase.path
        val files = path.listFiles() ?: emptyArray()
        val additionalSources = RunConfiguration.MOD_PARAMETERS.additionalSources[phase] ?: emptySet()
        if (files.isEmpty() && additionalSources.isEmpty()) {
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
            worker += add@{ inject(list, ModSource.detect(file) ?: return@add, phase, inner) }
        }
        for (source in additionalSources) {
            worker += add@{ inject(list, source, phase, inner) }
        }
        worker.work(inner)

        state = PhaseStates.VALIDATING
        worker = UnconditionalWorker()
        for (mod in list) {
            worker += {
                try {
                    mod.validate(mods)
                    mod.latch.dec()
                } catch (error: Throwable) {
                    mod.latch.count = 0
                    error.printStackTrace()
                    list -= mod
                }
            }
        }
        worker.work(inner)

        val sorted = list.sorted().toMutableList()
        state = PhaseStates.CONSTRUCTING
        worker = UnconditionalWorker()
        for (mod in sorted) {
            worker += {
                try {
                    mod.construct()
                    mod.latch.dec()
                } catch (error: Throwable) {
                    mod.latch.count = 0
                    error.printStackTrace()
                    list -= mod
                    sorted -= mod
                }
            }
        }
        worker.work(inner)

        state = PhaseStates.POST_INIT
        worker = UnconditionalWorker()
        for (mod in sorted) {
            worker += {
                try {
                    mod.postInit()
                    mod.latch.dec()
                } catch (error: Throwable) {
                    mod.latch.count = 0
                    error.printStackTrace()
                    list -= mod
                    sorted -= mod
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


    private val ASSETS_MANAGER_FIELD = ModMain::assets.javaField!!.apply { isAccessible = true }
    private val LOGGER_FIELD = ModMain::logger.javaField!!.apply { isAccessible = true }
}
