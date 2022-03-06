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

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.container.storage.StorageBlockEntity
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.models.ModelLoader
import de.bixilon.minosoft.gui.rendering.models.ModelLoader.Companion.bbModel
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.world.entities.EntityRendererRegister
import de.bixilon.minosoft.util.DateUtil
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec3.Vec3i

class DoubleChestRenderer(
    val entity: StorageBlockEntity,
    renderWindow: RenderWindow,
    blockState: BlockState,
    blockPosition: Vec3i,
    model: BakedSkeletalModel,
) : StorageBlockEntityRenderer<StorageBlockEntity>(
    blockState,
    SkeletalInstance(renderWindow, blockPosition, model, (blockState.properties[BlockProperties.FACING]?.nullCast() ?: Directions.NORTH).rotatedMatrix)) {


    companion object {
        val DOUBLE_MODEL = "minecraft:block/entities/double_chest".toResourceLocation().bbModel()

        fun register(renderWindow: RenderWindow, modelLoader: ModelLoader, textureName1: ResourceLocation, textureName2: ResourceLocation, model: ResourceLocation) {
            val texture1 = renderWindow.textureManager.staticTextures.createTexture(textureName1)
            val texture2 = renderWindow.textureManager.staticTextures.createTexture(textureName2)
            modelLoader.entities.loadModel(model, DOUBLE_MODEL, mutableMapOf(0 to texture1, 1 to texture2))
        }
    }

    object NormalChest : EntityRendererRegister {
        val MODEL = "minecraft:models/block/entities/double_chest".toResourceLocation()
        val TEXTURE1 = "minecraft:entity/chest/normal_left".toResourceLocation().texture()
        val TEXTURE2 = "minecraft:entity/chest/normal_right".toResourceLocation().texture()
        val TEXTURE_CHRISTMAS1 = "minecraft:entity/chest/christmas_left".toResourceLocation().texture()
        val TEXTURE_CHRISTMAS2 = "minecraft:entity/chest/christmas_right".toResourceLocation().texture()

        override fun register(renderWindow: RenderWindow, modelLoader: ModelLoader) {
            val christmas = DateUtil.christmas
            register(renderWindow, modelLoader, if (christmas) TEXTURE_CHRISTMAS1 else TEXTURE1, if (christmas) TEXTURE_CHRISTMAS2 else TEXTURE2, MODEL)
        }
    }

    object TrappedChest : EntityRendererRegister {
        val MODEL = "minecraft:models/block/entities/double_trapped_chest".toResourceLocation()
        val TEXTURE1 = "minecraft:entity/chest/trapped_left".toResourceLocation().texture()
        val TEXTURE2 = "minecraft:entity/chest/trapped_right".toResourceLocation().texture()

        override fun register(renderWindow: RenderWindow, modelLoader: ModelLoader) {
            register(renderWindow, modelLoader, TEXTURE1, TEXTURE2, MODEL)
        }
    }
}
