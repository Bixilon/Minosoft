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
package de.bixilon.minosoft.data.entities.entities.vehicle

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil

class CommandBlockMinecart(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : AbstractMinecart(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val command: String
        get() = data.get(COMMAND_DATA, "")

    @get:SynchronizedEntityData
    val lastOutput: ChatComponent
        get() = data.getChatComponent(LAST_OUTPUT_DATA, "")


    companion object : EntityFactory<CommandBlockMinecart> {
        override val identifier: ResourceLocation = KUtil.minecraft("command_block_minecart")
        private val COMMAND_DATA = EntityDataField("MINECART_COMMAND_BLOCK_COMMAND")
        private val LAST_OUTPUT_DATA = EntityDataField("MINECART_COMMAND_BLOCK_LAST_OUTPUT")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): CommandBlockMinecart {
            return CommandBlockMinecart(connection, entityType, data, position, rotation)
        }
    }
}
