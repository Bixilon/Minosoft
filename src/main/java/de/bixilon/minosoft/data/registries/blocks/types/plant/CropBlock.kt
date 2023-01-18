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

package de.bixilon.minosoft.data.registries.blocks.types.plant

import de.bixilon.minosoft.data.registries.blocks.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.blocks.wawla.BlockWawlaProvider
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.world.chunk.light.SectionLight.Companion.BLOCK_LIGHT_MASK
import de.bixilon.minosoft.data.world.chunk.light.SectionLight.Companion.SKY_LIGHT_MASK
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

open class CropBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : PlantBlock(resourceLocation, registries, data), BlockWawlaProvider {

    override fun canPlaceOn(blockState: BlockState): Boolean {
        return blockState.block.identifier == MinecraftBlocks.FARMLAND
    }

    override fun getWawlaInformation(connection: PlayConnection, target: BlockTarget): ChatComponent {
        val light = connection.world.getLight(target.blockPosition)

        val blockLight = light and BLOCK_LIGHT_MASK
        val skyLight = (light and SKY_LIGHT_MASK) shr 4

        val component = BaseComponent("Light: ")

        component += if (blockLight < MIN_LIGHT_LEVEL) "§4$blockLight§r" else "§a$blockLight§r"

        component += " ($skyLight)"

        return component
    }

    companion object : BlockFactory<CropBlock> {
        const val MIN_LIGHT_LEVEL = 7

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): CropBlock {
            return CropBlock(resourceLocation, registries, data)
        }
    }
}
