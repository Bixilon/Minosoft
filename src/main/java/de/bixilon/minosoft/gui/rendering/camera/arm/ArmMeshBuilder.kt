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

package de.bixilon.minosoft.gui.rendering.camera.arm

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.entities.entities.player.Arms
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.player.PlayerModelMeshBuilder
import de.bixilon.minosoft.gui.rendering.models.block.element.FaceVertexData
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.UnpackedUV

class ArmMeshBuilder(
    context: RenderContext,
    val arm: Arms,
) : PlayerModelMeshBuilder(context) {


    override fun addQuad(positions: FaceVertexData, uv: UnpackedUV, transform: Int, normal: Vec3f, texture: ShaderTexture, path: String) {
        val arm = path.getArm() ?: return
        if (arm != this.arm) return
        super.addQuad(positions, uv, 0, normal, texture, path)
    }

    private fun String.getArm() = when {
        this.startsWith("left_arm") -> Arms.LEFT
        this.startsWith("right_arm") -> Arms.RIGHT
        else -> null
    }
}
