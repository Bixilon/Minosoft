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

package de.bixilon.minosoft.gui.rendering.world.entities.renderer.storage

import de.bixilon.minosoft.data.entities.block.container.storage.StorageBlockEntity
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.world.entities.BlockEntityRenderer
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

abstract class StorageBlockEntityRenderer<E : StorageBlockEntity>(
    protected val modelName: ResourceLocation,
) : BlockEntityRenderer<E> {
    private lateinit var model: BakedSkeletalModel
    private lateinit var blockPosition: Vec3i
    private var delta = 0.0f
    private var rotating: Boolean = false

    override fun init(renderWindow: RenderWindow, state: BlockState, blockPosition: Vec3i) {
        this.blockPosition = blockPosition
        this.model = renderWindow.modelLoader.blockModels["minecraft:models/block/entities/single_chest.bbmodel".toResourceLocation()]!!
    }


    override fun draw(renderWindow: RenderWindow) {
        val shader = renderWindow.shaderManager.skeletalShader
        shader.use()
        shader.setMat4("uSkeletalTransforms[0]", Mat4(1).translate(Vec3(blockPosition)).rotate(delta, Vec3(0, 1, 0)))
        if (rotating) {
            delta += 0.03f
        }
        model.mesh.draw()
    }

    fun open() {
        rotating = true
    }

    fun close() {
        rotating = false
    }
}
