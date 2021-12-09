package de.bixilon.minosoft.util.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.PostChatFormattingCodes
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
