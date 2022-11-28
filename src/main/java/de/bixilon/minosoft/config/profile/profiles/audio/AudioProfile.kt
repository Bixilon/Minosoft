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

package de.bixilon.minosoft.config.profile.profiles.audio

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.delegate.types.StringDelegate
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.audio.AudioProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.audio.gui.GuiC
import de.bixilon.minosoft.config.profile.profiles.audio.types.TypesC
import de.bixilon.minosoft.config.profile.profiles.audio.volume.VolumeC

/**
 * Profile for audio
 */
class AudioProfile(
    description: String? = null,
) : Profile {
    override val manager: ProfileManager<Profile> = AudioProfileManager.unsafeCast()
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override var ignoreNextReload: Boolean = false
    override val version: Int = latestVersion
    override var description by StringDelegate(this, description ?: "")

    /**
     * Skips the loading od the AudioPlayer
     * Requires reloading of the whole audio subsystem to be applied
     */
    var skipLoading by BooleanDelegate(this, false, "profile.audio.loading.skip")

    /**
     * Enabled or disables all audio playing
     * Does not skip loading of audio
     */
    var enabled by BooleanDelegate(this, false, "profile.audio.enabled")

    val types = TypesC(this)
    val volume = VolumeC(this)

    val gui = GuiC(this)


    override fun toString(): String {
        return AudioProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
