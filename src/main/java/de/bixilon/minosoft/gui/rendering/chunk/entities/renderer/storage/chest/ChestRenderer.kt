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

import de.bixilon.minosoft.data.entities.block.container.storage.ChestBlockEntity
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties.getFacing
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage.StorageBlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance

abstract class ChestRenderer(
    state: BlockState,
    skeletal: SkeletalInstance?,
    blockPosition: BlockPosition,
    light: Int,
) : StorageBlockEntityRenderer<ChestBlockEntity>(state, skeletal) {

    init {
        update(blockPosition, state, light)
    }

    override fun update(position: BlockPosition, state: BlockState, light: Int) {
        skeletal?.update(position, state.getFacing())
    }
}
