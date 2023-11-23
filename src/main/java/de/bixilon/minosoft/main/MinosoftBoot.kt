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

package de.bixilon.minosoft.main

import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.worker.task.TaskWorker
import de.bixilon.kutil.concurrent.worker.task.WorkerTask
import de.bixilon.kutil.latch.CallbackLatch
import de.bixilon.minosoft.assets.IntegratedAssets
import de.bixilon.minosoft.assets.meta.MinosoftMeta
import de.bixilon.minosoft.assets.properties.version.AssetsVersionProperties
import de.bixilon.minosoft.config.profile.manager.ProfileManagers
import de.bixilon.minosoft.data.entities.event.EntityEvents
import de.bixilon.minosoft.data.registries.fallback.FallbackRegistries
import de.bixilon.minosoft.data.registries.fallback.tags.FallbackTags
import de.bixilon.minosoft.datafixer.DataFixer
import de.bixilon.minosoft.modding.loader.LoadingPhases
import de.bixilon.minosoft.modding.loader.ModLoader
import de.bixilon.minosoft.protocol.protocol.LANServerListener
import de.bixilon.minosoft.protocol.versions.VersionLoader
import de.bixilon.minosoft.terminal.cli.CLI
import de.bixilon.minosoft.util.yggdrasil.YggdrasilUtil

object MinosoftBoot {
    val LATCH = CallbackLatch(1)

    fun register(worker: TaskWorker) {
        worker += WorkerTask(identifier = BootTasks.VERSIONS, priority = ThreadPool.HIGHER, executor = VersionLoader::load)
        worker += WorkerTask(identifier = BootTasks.PROFILES, dependencies = arrayOf(BootTasks.VERSIONS), priority = ThreadPool.HIGHEST, executor = ProfileManagers::load) // servers might have a version set

        worker += WorkerTask(identifier = BootTasks.ASSETS_PROPERTIES, dependencies = arrayOf(BootTasks.VERSIONS), executor = AssetsVersionProperties::load)
        worker += WorkerTask(identifier = BootTasks.DEFAULT_REGISTRIES, dependencies = arrayOf(BootTasks.VERSIONS), executor = { MinosoftMeta.load(); FallbackTags.load(); FallbackRegistries.load(); EntityEvents.load() })


        worker += WorkerTask(identifier = BootTasks.LAN_SERVERS, dependencies = arrayOf(BootTasks.PROFILES), executor = LANServerListener::listen)

        worker += WorkerTask(identifier = BootTasks.YGGDRASIL, executor = { YggdrasilUtil.load() })

        worker += WorkerTask(identifier = BootTasks.ASSETS_OVERRIDE, executor = { IntegratedAssets.OVERRIDE.load(it) })
        worker += WorkerTask(identifier = BootTasks.MODS, executor = { ModLoader.load(LoadingPhases.BOOT, it) })
        worker += WorkerTask(identifier = BootTasks.DATA_FIXER, executor = { DataFixer.load() })
        worker += WorkerTask(identifier = BootTasks.CLI, priority = ThreadPool.LOW, executor = CLI::startThread)
    }
}
