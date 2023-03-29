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

package de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.overlays.arm

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.entities.entities.player.Arms
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.skeletal.SkeletalVertexConsumer
import de.bixilon.minosoft.gui.rendering.skeletal.model.SkeletalModel
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.texture.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

open class ArmMesh(context: RenderContext, primitiveType: PrimitiveTypes = context.renderSystem.quadType) : Mesh(context, ArmMeshStruct, primitiveType, initialCacheSize = 2 * 3 * ArmMeshStruct.FLOATS_PER_VERTEX), SkeletalVertexConsumer {


    fun addVertex(position: FloatArray, uv: Vec2) {
        data.add(position)
        data.add(uv.array)
    }

    override fun addVertex(position: FloatArray, transformedUV: Vec2, transform: Float, textureShaderId: Float, flags: Float) {
        addVertex(position, transformedUV)
    }

    override fun addVertex(position: FloatArray, uv: Vec2, transform: Int, texture: ShaderTexture, flags: Int) {
        addVertex(position, uv)
    }


    fun addArm(model: SkeletalModel, arm: Arms, skin: ShaderTexture) {
        val elements = model.elements.filter {
            when (arm) {
                Arms.LEFT -> it.name == "LEFT_ARM" || it.name == "LEFT_SLEEVE"
                Arms.RIGHT -> it.name == "RIGHT_ARM" || it.name == "RIGHT_SLEEVE"
            }
        }
        val textures = Int2ObjectOpenHashMap<ShaderTexture>()
        textures[0] = skin
        for (element in elements) {
            element.bake(model, textures, emptyMap(), this)
        }
    }


    data class ArmMeshStruct(
        val position: Vec3,
        val uv: Vec2,
    ) {
        companion object : MeshStruct(ArmMeshStruct::class)
    }
}
