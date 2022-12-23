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

package de.bixilon.minosoft.config.profile

import de.bixilon.minosoft.config.profile.profiles.audio.AudioProfile
import de.bixilon.minosoft.config.profile.profiles.block.BlockProfile
import de.bixilon.minosoft.config.profile.profiles.connection.ConnectionProfile
import de.bixilon.minosoft.config.profile.profiles.controls.ControlsProfile
import de.bixilon.minosoft.config.profile.profiles.entity.EntityProfile
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfile
import de.bixilon.minosoft.config.profile.profiles.gui.GUIProfile
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfile
import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfile
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfile
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile

object ProfileTestUtil {

    fun createProfiles(): ConnectionProfiles {
        return ConnectionProfiles(
            eros = ErosProfile(),
            particle = ParticleProfile(),
            audio = AudioProfile(),
            entity = EntityProfile(),
            resources = ResourcesProfile(),
            rendering = RenderingProfile(),
            block = BlockProfile(),
            connection = ConnectionProfile(),
            gui = GUIProfile(),
            controls = ControlsProfile(),
            other = OtherProfile(),
        )
    }
}