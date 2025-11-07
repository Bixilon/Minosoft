/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.redstone

import de.bixilon.minosoft.data.entities.block.redstone.piston.PistonBlockEntity
import de.bixilon.minosoft.data.registries.blocks.factory.PixLyzerBlockFactory
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.PixLyzerBlockWithEntity
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

open class PistonBlock(identifier: ResourceLocation, registries: Registries, data: Map<String, Any>) : PixLyzerBlockWithEntity<PistonBlockEntity>(identifier, registries, data) {

    override fun createBlockEntity(session: PlaySession, position: BlockPosition, state: BlockState) = PistonBlockEntity(session, position, state) // TODO: sticky

    companion object : PixLyzerBlockFactory<PistonBlock> {
        override fun build(identifier: ResourceLocation, registries: Registries, data: Map<String, Any>): PistonBlock {
            return PistonBlock(identifier, registries, data)
        }
    }
}

