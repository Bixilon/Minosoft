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

package de.bixilon.minosoft.modding.loader.phase

import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.latch.AbstractLatch.Companion.child
import de.bixilon.kutil.latch.ParentLatch
import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.modding.loader.ModList
import de.bixilon.minosoft.modding.loader.ModLoader
import de.bixilon.minosoft.modding.loader.ModLoadingUtil.construct
import de.bixilon.minosoft.modding.loader.ModLoadingUtil.postInit
import de.bixilon.minosoft.modding.loader.ModLoadingUtil.validate
import de.bixilon.minosoft.modding.loader.error.DuplicateModError
import de.bixilon.minosoft.modding.loader.error.DuplicateProvidedError
import de.bixilon.minosoft.modding.loader.mod.MinosoftMod
import de.bixilon.minosoft.modding.loader.mod.source.ModSource
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType


class LoadingPhase(val name: String) {
    private val path = ModLoader.BASE_PATH.resolve(name.lowercase()).toFile()

    private var latch = SimpleLatch(1)
    var state by observed(PhaseStates.WAITING)
        private set

    private fun inject(list: ModList, source: ModSource, latch: AbstractLatch) {
        val mod = MinosoftMod(source, this, ParentLatch(4, latch))
        Log.log(LogMessageType.MOD_LOADING, LogLevels.VERBOSE) { "Injecting $source" }
        try {
            source.process(mod)
            val manifest = mod.manifest!!
            val name = manifest.name

            if (name in RunConfiguration.MOD_PARAMETERS.ignoreMods) {
                mod.latch.count = 0
                return
            }
            if (name in list || name in ModLoader.mods) {
                throw DuplicateModError(name)
            }
            manifest.packages?.provides?.let {
                // ToDO: possible race condition?
                for (providedName in it) {
                    val provided = list[providedName] ?: ModLoader.mods[providedName]
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

    fun load(latch: AbstractLatch? = null) {
        if (this.latch.count == 0) throw IllegalStateException("Phase is already loaded!")
        if (RunConfiguration.IGNORE_MODS || this.name in RunConfiguration.MOD_PARAMETERS.ignorePhases) {
            this.latch.dec()
            return
        }
        Log.log(LogMessageType.MOD_LOADING, LogLevels.VERBOSE) { "Starting mod load: ${this.name}" }

        val inner = latch.child(1)
        this.state = PhaseStates.LISTING
        if (!path.exists()) {
            path.mkdirs()
            Log.log(LogMessageType.MOD_LOADING, LogLevels.VERBOSE) { "Created mod folders: $path" }
        }

        val files = path.listFiles() ?: emptyArray()
        val additionalSources = RunConfiguration.MOD_PARAMETERS.additionalSources[this.name] ?: emptySet()
        if (files.isEmpty() && additionalSources.isEmpty()) {
            // no mods to load
            state = PhaseStates.COMPLETE
            inner.dec(); this.latch.dec()
            return
        }
        val list = ModList()

        state = PhaseStates.INJECTING
        var worker = UnconditionalWorker()
        for (file in files) {
            worker += add@{ inject(list, ModSource.detect(file) ?: return@add, inner) }
        }
        for (source in additionalSources) {
            worker += add@{ inject(list, source, inner) }
        }
        worker.work(inner)

        state = PhaseStates.VALIDATING
        worker = UnconditionalWorker()
        for (mod in list) {
            worker += {
                try {
                    mod.validate()
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


        ModLoader.mods += list
        state = PhaseStates.COMPLETE
        inner.dec()
        this.latch.dec()
    }

    fun await() {
        if (RunConfiguration.IGNORE_MODS) return
        latch.await()
    }
}
