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

package de.bixilon.minosoft.data.entities.block

import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class JigsawBlockEntity(session: PlaySession, position: BlockPosition, state: BlockState) : BlockEntity(session, position, state) {
    var joint: String = "rollable"
        private set
    var name = minecraft("empty")
        private set
    var pool = minecraft("empty")
        private set
    var finalState = minecraft("empty")
        private set
    var target = minecraft("empty")
        private set


    override fun updateNBT(nbt: Map<String, Any>) {
        nbt["joint"]?.let { joint = it.toString() }
        nbt["name"]?.let { name = it.toResourceLocation() }
        nbt["pool"]?.let { pool = it.toResourceLocation() }
        nbt["finalState"]?.let { finalState = it.toResourceLocation() }
        nbt["target"]?.let { target = it.toResourceLocation() }
    }

    companion object : BlockEntityFactory<JigsawBlockEntity> {
        override val identifier = minecraft("jigsaw")

        override fun build(session: PlaySession, position: BlockPosition, state: BlockState) = JigsawBlockEntity(session, position, state)
    }
}
