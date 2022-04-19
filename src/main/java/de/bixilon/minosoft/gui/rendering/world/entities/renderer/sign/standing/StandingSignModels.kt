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

package de.bixilon.minosoft.gui.rendering.world.entities.renderer.sign.standing

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.blocks.types.entity.sign.StandingSignBlock
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.models.ModelLoader
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.world.entities.EntityRendererRegister
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object StandingSignModels {

    fun register(renderWindow: RenderWindow, modelLoader: ModelLoader, textureName: ResourceLocation, block: ResourceLocation) {
        val block = renderWindow.connection.registries.blockRegistry[block].nullCast<StandingSignBlock>() ?: return
        val texture = renderWindow.textureManager.staticTextures.createTexture(textureName)
        val signModel = StandingSignModel(texture)
        block.model = signModel
    }

    object Acacia : EntityRendererRegister {
        val TEXTURE = "minecraft:entity/signs/acacia".toResourceLocation().texture()

        override fun register(renderWindow: RenderWindow, modelLoader: ModelLoader) {
            register(renderWindow, modelLoader, TEXTURE, MinecraftBlocks.ACACIA_SIGN)
        }
    }

    object Birch : EntityRendererRegister {
        val TEXTURE = "minecraft:entity/signs/birch".toResourceLocation().texture()

        override fun register(renderWindow: RenderWindow, modelLoader: ModelLoader) {
            register(renderWindow, modelLoader, TEXTURE, MinecraftBlocks.BIRCH_SIGN)
        }
    }

    object Crimson : EntityRendererRegister {
        val TEXTURE = "minecraft:entity/signs/crimson".toResourceLocation().texture()

        override fun register(renderWindow: RenderWindow, modelLoader: ModelLoader) {
            register(renderWindow, modelLoader, TEXTURE, MinecraftBlocks.CRIMSON_SIGN)
        }
    }

    object DarkOak : EntityRendererRegister {
        val TEXTURE = "minecraft:entity/signs/dark_oak".toResourceLocation().texture()

        override fun register(renderWindow: RenderWindow, modelLoader: ModelLoader) {
            register(renderWindow, modelLoader, TEXTURE, MinecraftBlocks.DARK_OAK_SIGN)
        }
    }

    object Jungle : EntityRendererRegister {
        val TEXTURE = "minecraft:entity/signs/jungle".toResourceLocation().texture()

        override fun register(renderWindow: RenderWindow, modelLoader: ModelLoader) {
            register(renderWindow, modelLoader, TEXTURE, MinecraftBlocks.JUNGLE_SIGN)
        }
    }

    object Oak : EntityRendererRegister {
        val TEXTURE = "minecraft:entity/signs/oak".toResourceLocation().texture()

        override fun register(renderWindow: RenderWindow, modelLoader: ModelLoader) {
            register(renderWindow, modelLoader, TEXTURE, MinecraftBlocks.OAK_SIGN)
        }
    }

    object Spruce : EntityRendererRegister {
        val TEXTURE = "minecraft:entity/signs/spruce".toResourceLocation().texture()

        override fun register(renderWindow: RenderWindow, modelLoader: ModelLoader) {
            register(renderWindow, modelLoader, TEXTURE, MinecraftBlocks.SPRUCE_SIGN)
        }
    }

    object WarpedSign : EntityRendererRegister {
        val TEXTURE = "minecraft:entity/signs/warped".toResourceLocation().texture()

        override fun register(renderWindow: RenderWindow, modelLoader: ModelLoader) {
            register(renderWindow, modelLoader, TEXTURE, MinecraftBlocks.WARPED_SIGN)
        }
    }
}
