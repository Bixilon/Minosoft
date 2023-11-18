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

package de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage.chest

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.time.DateUtil
import de.bixilon.minosoft.data.entities.block.container.storage.StorageBlockEntity
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.entities.EntityRendererRegister
import de.bixilon.minosoft.gui.rendering.models.loader.ModelLoader
import de.bixilon.minosoft.gui.rendering.models.loader.SkeletalLoader.Companion.sModel
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture

class DoubleChestRenderer(
    val entity: StorageBlockEntity,
    context: RenderContext,
    state: BlockState,
    position: Vec3i,
    model: BakedSkeletalModel,
    light: Int,
) : ChestRenderer(state, model.createInstance(context), position, light) {

    companion object {
        private val MODEL = minecraft("block/entities/chest/double").sModel()
        private val MODEL_5 = minecraft("block/entities/chest/double_5").sModel()

        private val TEXTURE = minecraft("chest")
        private val TEXTURE_5 = arrayOf(minecraft("left"), minecraft("right"))

        private fun register(loader: ModelLoader, name: ResourceLocation, texture: ResourceLocation) {
            val static = loader.context.textures.staticTextures
            val override = mapOf(TEXTURE to static.createTexture(texture))
            loader.skeletal.register(name, MODEL, override)
        }

        private fun register5(loader: ModelLoader, name: ResourceLocation, textures: Array<ResourceLocation>) {
            if (textures.size != 2) throw IllegalStateException("Textures must be left and right!")
            val static = loader.context.textures.staticTextures
            val override = mapOf(
                TEXTURE_5[0] to static.createTexture(textures[0]),
                TEXTURE_5[1] to static.createTexture(textures[1]),
            )
            loader.skeletal.register(name, MODEL_5, override)
        }
    }

    object NormalChest : EntityRendererRegister {
        val NAME = minecraft("block/entities/chest/double")
        private val TEXTURE = minecraft("entity/chest/normal_double").texture()
        private val TEXTURE_5 = arrayOf(minecraft("entity/chest/normal_left").texture(), minecraft("entity/chest/normal_right").texture())
        private val CHRISTMAS = minecraft("entity/chest/christmas_double").texture()
        private val CHRISTMAS_5 = arrayOf(minecraft("entity/chest/christmas_left").texture(), minecraft("entity/chest/christmas_right").texture())

        override fun register(loader: ModelLoader) {
            val christmas = DateUtil.christmas

            if (loader.packFormat < 5) {
                register(loader, NAME, if (christmas) CHRISTMAS else TEXTURE)
            } else {
                register5(loader, NAME, if (christmas) CHRISTMAS_5 else TEXTURE_5)
            }
        }
    }

    object TrappedChest : EntityRendererRegister {
        val NAME = minecraft("block/entities/chest/double_trapped")
        private val TEXTURE = minecraft("entity/chest/trapped_double").texture()
        private val TEXTURE_5 = arrayOf(minecraft("entity/chest/trapped_left").texture(), minecraft("entity/chest/trapped_right").texture())

        override fun register(loader: ModelLoader) {
            if (loader.packFormat < 5) {
                register(loader, NAME, TEXTURE)
            } else {
                register5(loader, NAME, TEXTURE_5)
            }
        }
    }
}
