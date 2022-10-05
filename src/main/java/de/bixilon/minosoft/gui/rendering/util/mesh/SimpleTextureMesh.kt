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

package de.bixilon.minosoft.gui.rendering.util.mesh

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture

open class SimpleTextureMesh(renderWindow: RenderWindow, primitiveType: PrimitiveTypes = renderWindow.renderSystem.preferredPrimitiveType) : Mesh(renderWindow, SimpleTextureMeshStruct, primitiveType, initialCacheSize = 2 * 3 * SimpleTextureMeshStruct.FLOATS_PER_VERTEX) {

    fun addVertex(position: Vec3, texture: AbstractTexture, uv: Vec2, tintColor: RGBColor?) {
        data.add(position.x)
        data.add(position.y)
        data.add(position.z)
        data.add(uv.x)
        data.add(uv.y)
        data.add(texture.renderData.shaderTextureId.buffer())
        data.add((tintColor?.rgba ?: Colors.WHITE).buffer())
    }


    data class SimpleTextureMeshStruct(
        val position: Vec3,
        val uv: Vec2,
        val indexLayerAnimation: Int,
        val tint: RGBColor,
    ) {
        companion object : MeshStruct(SimpleTextureMeshStruct::class)
    }
}
