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

package de.bixilon.minosoft.data.entities.block

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil

class JigsawBlockEntity(connection: PlayConnection) : BlockEntity(connection) {
    var joint: String = "rollable"
        private set
    var name: ResourceLocation = minecraft("empty")
        private set
    var pool: ResourceLocation = minecraft("empty")
        private set
    var finalState: ResourceLocation = minecraft("empty")
        private set
    var target: ResourceLocation = minecraft("empty")
        private set


    override fun updateNBT(nbt: Map<String, Any>) {
        nbt["joint"]?.nullCast<String>()?.let { joint = it }
        nbt["name"]?.nullCast<String>()?.let { name = ResourceLocation.ofPath(it) }
        nbt["pool"]?.nullCast<String>()?.let { pool = ResourceLocation.ofPath(it) }
        nbt["finalState"]?.nullCast<String>()?.let { finalState = ResourceLocation.ofPath(it) }
        nbt["target"]?.nullCast<String>()?.let { target = ResourceLocation.ofPath(it) }
    }

    companion object : BlockEntityFactory<JigsawBlockEntity> {
        override val identifier: ResourceLocation = minecraft("jigsaw")

        override fun build(connection: PlayConnection): JigsawBlockEntity {
            return JigsawBlockEntity(connection)
        }
    }
}
