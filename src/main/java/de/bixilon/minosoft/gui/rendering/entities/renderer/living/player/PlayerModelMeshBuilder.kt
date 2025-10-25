/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.entities.renderer.living.player

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.entities.entities.player.SkinParts
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.block.element.FaceVertexData
import de.bixilon.minosoft.gui.rendering.skeletal.mesh.AbstractSkeletalMeshBuilder
import de.bixilon.minosoft.gui.rendering.skeletal.mesh.SkeletalMeshUtil
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.UnpackedUV

open class PlayerModelMeshBuilder(context: RenderContext, initialCacheSize: Int = 1000) : AbstractSkeletalMeshBuilder(context, PlayerMeshStruct, initialCacheSize = initialCacheSize) {
    override val order = context.system.quadOrder

    private fun addVertex(position: FaceVertexData, positionOffset: Int, uv: UnpackedUV, uvOffset: Int, partTransformNormal: Float) {
        data.add(
            position[positionOffset + 0], position[positionOffset + 1], position[positionOffset + 2],
        )
        data.add(
            uv.raw[uvOffset + 0], uv.raw[uvOffset + 1],
            partTransformNormal,
        )
    }

    override fun addQuad(positions: FaceVertexData, uv: UnpackedUV, transform: Int, normal: Vec3f, texture: ShaderTexture, path: String) {
        val part = path.getSkinPart()?.ordinal?.inc() ?: 0x00
        val partTransformNormal = ((part shl 19) or (transform shl 12) or SkeletalMeshUtil.encodeNormal(normal)).buffer()

        order.iterate { position, uvIndex ->
            addVertex(positions, position * Vec3f.LENGTH, uv, uvIndex * Vec2f.LENGTH, partTransformNormal)
        }
    }

    private fun String.getSkinPart(): SkinParts? = when (this) {
        "head.hat" -> SkinParts.HAT
        "body.jacket" -> SkinParts.JACKET
        "left_leg.pants" -> SkinParts.LEFT_PANTS
        "right_leg.pants" -> SkinParts.RIGHT_PANTS
        "left_arm.sleeve" -> SkinParts.LEFT_SLEEVE
        "right_arm.sleeve" -> SkinParts.RIGHT_SLEEVE
        else -> null
    }


    data class PlayerMeshStruct(
        val position: Vec3f,
        val uv: UnpackedUV,
        val partTransformNormal: Int,
    ) {
        companion object : MeshStruct(PlayerMeshStruct::class)
    }
}
