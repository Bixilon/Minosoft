/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *  
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *  
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.mappings.tweaker

import de.bixilon.minosoft.data.mappings.ModIdentifier
import de.bixilon.minosoft.data.mappings.blocks.Block
import de.bixilon.minosoft.data.mappings.blocks.BlockProperties
import de.bixilon.minosoft.data.mappings.blocks.BlockState

object TweakBlocks {
    val GRASS_BLOCK_SNOWY_YES = BlockState(Block(ModIdentifier("grass")), setOf(BlockProperties.GRASS_SNOWY_YES))
    val GRASS_BLOCK_SNOWY_NO = BlockState(Block(ModIdentifier("grass")), setOf(BlockProperties.GRASS_SNOWY_NO))

    val SNOW_IDENTIFIER = ModIdentifier("snow")
    val SNOW_LAYER_IDENTIFIER = ModIdentifier("snow_layer")
}
