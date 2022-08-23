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

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes

open class GenericColorMesh(renderWindow: RenderWindow, primitiveType: PrimitiveTypes = renderWindow.renderSystem.preferredPrimitiveType) : Mesh(renderWindow, GenericColorMeshStruct, primitiveType) {

    fun addVertex(position: Vec3, color: RGBColor?) {
        data.add(position.x)
        data.add(position.y)
        data.add(position.z)
        data.add(Float.fromBits((color ?: ChatColors.WHITE).rgba))
    }

    fun addVertex(position: Vec3, color: Float) {
        data.add(position.x)
        data.add(position.y)
        data.add(position.z)
        data.add(color)
    }

    data class GenericColorMeshStruct(
        val position: Vec3,
        val color: RGBColor,
    ) {
        companion object : MeshStruct(GenericColorMeshStruct::class)
    }
}
