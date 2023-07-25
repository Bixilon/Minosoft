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

package de.bixilon.minosoft.gui.rendering.chunk.light.updater

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.chunk.light.LightmapBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import java.util.*

object FullbrightLightUpdater : LightmapUpdater {

    override fun update(force: Boolean, buffer: LightmapBuffer) {
        if (!force) {
            return
        }

        val random = Random(10000L)
        for (sky in 0 until ProtocolDefinition.LIGHT_LEVELS) {
            for (block in 0 until ProtocolDefinition.LIGHT_LEVELS) {
                buffer[sky, block] = Vec3(1.0f)
            }
        }
    }
}
