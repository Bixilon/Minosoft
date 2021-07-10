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

import de.bixilon.minosoft.data.registries.MultiResourceLocationAble
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.nullCast
import de.bixilon.minosoft.util.KUtil.toResourceLocationList

class SignBlockEntity(connection: PlayConnection) : BlockEntity(connection) {
    var lines: Array<ChatComponent> = Array(ProtocolDefinition.SIGN_LINES) { ChatComponent.of("") }


    override fun updateNBT(nbt: Map<String, Any>) {
        for (i in 0 until ProtocolDefinition.SIGN_LINES) {
            val tag = nbt["Text$i"].nullCast<String>() ?: continue

            lines[i] = ChatComponent.of(tag, translator = connection.version.language)
        }
    }

    companion object : BlockEntityFactory<SignBlockEntity>, MultiResourceLocationAble {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("minecraft:sign")
        override val ALIASES: Set<ResourceLocation> = setOf("minecraft:Sign").toResourceLocationList()

        override fun build(connection: PlayConnection): SignBlockEntity {
            return SignBlockEntity(connection)
        }
    }
}
