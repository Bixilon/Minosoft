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
import de.bixilon.minosoft.data.entities.block.container.storage.ChestBlockEntity
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec3.Vec3i

class ChestBlockEntityRenderer(
    val entity: ChestBlockEntity,
    renderWindow: RenderWindow,
    blockState: BlockState,
    blockPosition: Vec3i,
) : StorageBlockEntityRenderer<ChestBlockEntity>(
    blockState,
    SkeletalInstance(renderWindow, blockPosition, renderWindow.modelLoader.blockModels["minecraft:models/block/entities/single_chest.bbmodel".toResourceLocation()]!!, (blockState.properties[BlockProperties.FACING]?.nullCast() ?: Directions.NORTH).rotatedMatrix))
