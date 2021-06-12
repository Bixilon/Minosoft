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

package de.bixilon.minosoft.data.entities.block

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.nullCast

class JigsawBlockEntity(connection: PlayConnection) : BlockEntity(connection) {
    var joint: String = "rollable"
        private set
    var name: ResourceLocation = ResourceLocation("minecraft:empty")
        private set
    var pool: ResourceLocation = ResourceLocation("minecraft:empty")
        private set
    var finalState: ResourceLocation = ResourceLocation("minecraft:empty")
        private set
    var target: ResourceLocation = ResourceLocation("minecraft:empty")
        private set


    override fun updateNBT(nbt: Map<String, Any>) {
        nbt["joint"]?.nullCast<String>()?.let { joint = it }
        nbt["name"]?.nullCast<String>()?.let { name = ResourceLocation.getPathResourceLocation(it) }
        nbt["pool"]?.nullCast<String>()?.let { pool = ResourceLocation.getPathResourceLocation(it) }
        nbt["finalState"]?.nullCast<String>()?.let { finalState = ResourceLocation.getPathResourceLocation(it) }
        nbt["target"]?.nullCast<String>()?.let { target = ResourceLocation.getPathResourceLocation(it) }
    }

    companion object : BlockEntityFactory<JigsawBlockEntity> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("minecraft:jigsaw")

        override fun build(connection: PlayConnection): JigsawBlockEntity {
            return JigsawBlockEntity(connection)
        }
    }
}
