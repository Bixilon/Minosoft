/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *  
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *  
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.mappings.tweaker

import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.WannabeBlockState
import de.bixilon.minosoft.data.mappings.blocks.properties.BlockProperties

object TweakBlocks {
    val GRASS_BLOCK_SNOWY_YES = WannabeBlockState(ResourceLocation("grass"), mapOf(BlockProperties.SNOWY to true))
    val GRASS_BLOCK_SNOWY_NO = WannabeBlockState(ResourceLocation("grass"), mapOf(BlockProperties.SNOWY to false))

    val SNOW_RESOURCE_LOCATION = ResourceLocation("snow")
    val SNOW_LAYER_RESOURCE_LOCAION = ResourceLocation("snow_layer")
}
