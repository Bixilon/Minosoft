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

package de.bixilon.minosoft.util.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.formatting.PostChatFormattingCodes
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

object ChatComponentColorSerializer : SimpleModule() {

    init {
        addDeserializer(ChatComponent::class.java, Deserializer)
        addSerializer(ChatComponent::class.java, Serializer)
    }

    object Deserializer : StdDeserializer<ChatComponent>(ChatComponent::class.java) {

        override fun deserialize(parser: JsonParser, context: DeserializationContext?): ChatComponent {
            return ChatComponent.of(parser.valueAsString) // ToDo: Allow json text component
        }
    }

    object Serializer : StdSerializer<ChatComponent>(ChatComponent::class.java) {

        override fun serialize(value: ChatComponent?, generator: JsonGenerator, provider: SerializerProvider?) {
            generator.writeString(value?.legacyText?.removeSuffix(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR.toString() + PostChatFormattingCodes.RESET.char.toString()))
        }
    }
}
