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

package de.bixilon.minosoft.gui.rendering.renderer.renderer

import de.bixilon.minosoft.gui.rendering.camera.arm.ArmRenderer
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import de.bixilon.minosoft.gui.rendering.chunk.border.WorldBorderRenderer
import de.bixilon.minosoft.gui.rendering.chunk.chunk.ChunkBorderRenderer
import de.bixilon.minosoft.gui.rendering.chunk.outline.BlockOutlineRenderer
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.particle.ParticleRenderer
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.sky.clouds.CloudRenderer

object DefaultRenderer {
    val list = mutableListOf(
        // order dependent (from back to front)
        SkyRenderer,
        ChunkRenderer,
        BlockOutlineRenderer,
        ParticleRenderer,

        EntitiesRenderer,
        CloudRenderer,
        ChunkBorderRenderer,
        WorldBorderRenderer,
        ArmRenderer,
        GUIRenderer,
    )
}
