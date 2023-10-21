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

class SingleChestRenderer(
    val entity: StorageBlockEntity,
    context: RenderContext,
    state: BlockState,
    position: Vec3i,
    model: BakedSkeletalModel,
    light: Int,
) : ChestRenderer(state, model.createInstance(context), position, light) {

    companion object {
        val SINGLE_MODEL = minecraft("block/entities/chest/single").sModel()
        private val named = minecraft("chest")

        fun register(loader: ModelLoader, name: ResourceLocation, texture: ResourceLocation) {
            val texture = loader.context.textures.staticTextures.createTexture(texture)
            loader.skeletal.register(name, SINGLE_MODEL, mapOf(named to texture))
        }
    }

    object NormalChest : EntityRendererRegister {
        val NAME = minecraft("block/entities/chest/single")
        val TEXTURE = minecraft("entity/chest/normal").texture()
        val TEXTURE_CHRISTMAS = minecraft("entity/chest/christmas").texture()

        override fun register(loader: ModelLoader) {
            register(loader, NAME, if (DateUtil.christmas) TEXTURE_CHRISTMAS else TEXTURE)
        }
    }

    object TrappedChest : EntityRendererRegister {
        val NAME = minecraft("block/entities/chest/trapped")
        val TEXTURE = minecraft("entity/chest/trapped").texture()

        override fun register(loader: ModelLoader) {
            register(loader, NAME, TEXTURE)
        }
    }

    object EnderChest : EntityRendererRegister {
        val NAME = minecraft("block/entities/chest/ender")
        val TEXTURE = minecraft("entity/chest/ender").texture()

        override fun register(loader: ModelLoader) {
            register(loader, NAME, TEXTURE)
        }
    }
}
