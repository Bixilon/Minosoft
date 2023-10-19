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

package de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.time.DateUtil
import de.bixilon.minosoft.data.entities.block.container.storage.StorageBlockEntity
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties.getFacing
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.entities.EntityRendererRegister
import de.bixilon.minosoft.gui.rendering.models.loader.ModelLoader
import de.bixilon.minosoft.gui.rendering.models.loader.SkeletalLoader.Companion.bbModel
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture

class DoubleChestRenderer(
    val entity: StorageBlockEntity,
    context: RenderContext,
    blockState: BlockState,
    blockPosition: Vec3i,
    model: BakedSkeletalModel,
    light: Int,
) : StorageBlockEntityRenderer<StorageBlockEntity>(
    blockState,
    model.createInstance(context),
    light,
) {

    init {
        update(blockPosition, state, light)
    }

    override fun update(position: BlockPosition, state: BlockState, light: Int) {
        skeletal?.update(position, state.getFacing())
    }

    companion object {
        val DOUBLE_MODEL = minecraft("block/entities/double_chest").bbModel()
        private val named = arrayOf(minecraft("left"), minecraft("right"))

        private fun register(loader: ModelLoader, name: ResourceLocation, textures: Array<ResourceLocation>) {
            if (textures.size != 2) throw IllegalStateException("Textures must be left and right!")
            val static = loader.context.textures.staticTextures
            val override = mapOf(
                named[0] to static.createTexture(textures[0]),
                named[1] to static.createTexture(textures[1]),
            )
            loader.skeletal.register(name, DOUBLE_MODEL, override)
        }
    }

    object NormalChest : EntityRendererRegister {
        val NAME = minecraft("block/entities/double_chest")
        private val textures = arrayOf(minecraft("entity/chest/normal_left").texture(), minecraft("entity/chest/normal_right").texture())
        private val christmas = arrayOf(minecraft("entity/chest/christmas_left").texture(), minecraft("entity/chest/christmas_right").texture())

        override fun register(loader: ModelLoader) {
            register(loader, NAME, if (DateUtil.christmas) christmas else textures)
        }
    }

    object TrappedChest : EntityRendererRegister {
        val NAME = minecraft("block/entities/double_trapped_chest")
        private val textures = arrayOf(minecraft("entity/chest/trapped_left").texture(), minecraft("entity/chest/trapped_right").texture())

        override fun register(loader: ModelLoader) {
            register(loader, NAME, textures)
        }
    }
}
