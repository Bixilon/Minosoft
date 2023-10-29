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

package de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage.shulker

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.colors.DyeColors
import de.bixilon.minosoft.data.entities.block.container.storage.ShulkerBoxBlockEntity
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties.getFacing
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.entities.EntityRendererRegister
import de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage.StorageBlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.models.loader.ModelLoader
import de.bixilon.minosoft.gui.rendering.models.loader.SkeletalLoader.Companion.sModel
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rad

class ShulkerBoxRenderer(
    val entity: ShulkerBoxBlockEntity,
    context: RenderContext,
    state: BlockState,
    position: Vec3i,
    model: BakedSkeletalModel,
    light: Int,
) : StorageBlockEntityRenderer<ShulkerBoxBlockEntity>(state, model.createInstance(context)) {
    val animation = skeletal?.let { ShulkerAnimation(it) }

    init {
        update(position, state, light)
    }

    override fun update(position: BlockPosition, state: BlockState, light: Int) {
        super.update(position, state, light)
        val facing = state.getFacing()
        val rotation = ROTATIONS[facing.ordinal]
        skeletal?.update(position, rotation)
    }

    override fun open() {
        animation?.open()
    }

    override fun close() {
        animation?.close()
    }

    companion object : EntityRendererRegister {
        val TEMPLATE = minecraft("block/entities/shulker_box").sModel()
        val NAME = minecraft("block/entities/shulker_box")
        val NAME_COLOR = Array(DyeColors.VALUES.size) { minecraft("entity/shulker/shulker_box/${DyeColors[it].name.lowercase()}") }

        private val named = minecraft("shulker")
        private val texture = minecraft("entity/shulker/shulker").texture()
        private val colored = Array(DyeColors.VALUES.size) { minecraft("entity/shulker/shulker_${DyeColors[it].name.lowercase()}").texture() }

        private val ROTATIONS = arrayOf(
            Vec3(180, 0, 0).rad,
            Vec3(0, 0, 0).rad,
            Vec3(270, 180, 0).rad,
            Vec3(90, 0, 0).rad,
            Vec3(90, 0, 90).rad,
            Vec3(90, 0, 270).rad,
        )

        override fun register(loader: ModelLoader) {
            val texture = loader.context.textures.staticTextures.createTexture(texture)
            loader.skeletal.register(NAME, TEMPLATE, override = mapOf(this.named to texture))

            for (color in DyeColors) {
                val texture = loader.context.textures.staticTextures.createTexture(colored[color.ordinal])
                loader.skeletal.register(NAME_COLOR[color.ordinal], TEMPLATE, override = mapOf(this.named to texture))
            }
        }
    }
}
