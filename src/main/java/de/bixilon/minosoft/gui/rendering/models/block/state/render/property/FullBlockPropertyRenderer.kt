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

package de.bixilon.minosoft.gui.rendering.models.block.state.render.property

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.SideProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY

object FullBlockPropertyRenderer : PropertyOnlyBlockRender {
    private val properties = SideProperties(arrayOf(FaceProperties(Vec2.EMPTY, Vec2(1.0f), TextureTransparencies.OPAQUE)), TextureTransparencies.OPAQUE)

    override fun getProperties(direction: Directions) = this.properties
}
