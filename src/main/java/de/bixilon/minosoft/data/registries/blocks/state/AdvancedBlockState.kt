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

package de.bixilon.minosoft.data.registries.blocks.state

import de.bixilon.minosoft.data.registries.blocks.light.LightProperties
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.shapes.voxel.AbstractVoxelShape

open class AdvancedBlockState(
    block: Block,
    properties: Map<BlockProperties, Any>,
    luminance: Int,
    val collisionShape: AbstractVoxelShape?,
    val outlineShape: AbstractVoxelShape?,
    val lightProperties: LightProperties,
    @Deprecated("pixlyzer") val solidRenderer: Boolean,
) : PropertyBlockState(block, properties, luminance) {

    constructor(block: Block, settings: BlockStateSettings) : this(block, settings.properties ?: emptyMap(), settings.luminance, settings.collisionShape, settings.outlineShape, settings.lightProperties, settings.solidRenderer)
}
