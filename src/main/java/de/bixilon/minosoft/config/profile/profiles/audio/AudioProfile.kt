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

package de.bixilon.minosoft.config.profile.profiles.audio

import de.bixilon.minosoft.config.profile.ProfileLock
import de.bixilon.minosoft.config.profile.ProfileType
import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.audio.gui.GuiC
import de.bixilon.minosoft.config.profile.profiles.audio.types.TypesC
import de.bixilon.minosoft.config.profile.profiles.audio.volume.VolumeC
import de.bixilon.minosoft.config.profile.storage.ProfileStorage
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid

/**
 * Profile for audio
 */
class AudioProfile(
    override val storage: ProfileStorage? = null,
) : Profile {
    override val lock = ProfileLock()

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
        return storage?.toString() ?: super.toString()
    }

    companion object : ProfileType<AudioProfile> {
        override val identifier = minosoft("audio")
        override val clazz = AudioProfile::class.java
        override val icon: Ikon get() = FontAwesomeSolid.USER_CIRCLE

        override fun create(storage: ProfileStorage?) = AudioProfile(storage)
    }
}
