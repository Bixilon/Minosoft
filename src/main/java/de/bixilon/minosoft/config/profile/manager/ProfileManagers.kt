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

package de.bixilon.minosoft.config.profile.manager

import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.kutil.file.watcher.FileWatcherService
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager
import de.bixilon.minosoft.config.profile.profiles.audio.AudioProfileManager
import de.bixilon.minosoft.config.profile.profiles.block.BlockProfileManager
import de.bixilon.minosoft.config.profile.profiles.connection.ConnectionProfileManager
import de.bixilon.minosoft.config.profile.profiles.controls.ControlsProfileManager
import de.bixilon.minosoft.config.profile.profiles.entity.EntityProfileManager
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.config.profile.profiles.gui.GUIProfileManager
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfileManager
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfileManager
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager
import de.bixilon.minosoft.config.profile.storage.ProfileIOManager
import de.bixilon.minosoft.config.profile.storage.StorageProfileManager
import de.bixilon.minosoft.data.registries.factory.DefaultFactory
import de.bixilon.minosoft.terminal.RunConfiguration

object ProfileManagers : DefaultFactory<StorageProfileManager<*>>(
    ErosProfileManager,
    ParticleProfileManager,
    AudioProfileManager,
    EntityProfileManager,
    ResourcesProfileManager,
    AccountProfileManager,
    RenderingProfileManager,
    BlockProfileManager,
    ConnectionProfileManager,
    GUIProfileManager,
    ControlsProfileManager,
    OtherProfileManager,
) {


    fun load(latch: AbstractLatch?) {
        if (RunConfiguration.PROFILES_HOT_RELOADING) {
            FileWatcherService.start() // TODO: kutil 1.25: remove kutil race condition
        }
        val worker = UnconditionalWorker()
        for (manager in ProfileManagers) {
            worker += { manager.load() }
        }
        worker.work(latch)
        ProfileIOManager.init()
    }
}
