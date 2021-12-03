package de.bixilon.minosoft.util.json.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import de.bixilon.minosoft.data.text.ChatCode.Companion.toColor
import de.bixilon.minosoft.data.text.RGBColor

object RGBColorSerializer : SimpleModule() {
    init {
        addDeserializer(RGBColor::class.java, Deserializer)
        addSerializer(RGBColor::class.java, Serializer)
    }

    object Deserializer : StdDeserializer<RGBColor>(RGBColor::class.java) {

        override fun deserialize(parser: JsonParser, context: DeserializationContext?): RGBColor {
            return when (parser.currentToken) {
                JsonToken.VALUE_NUMBER_INT -> RGBColor(parser.valueAsInt)
                JsonToken.VALUE_STRING -> parser.valueAsString.toColor()!!
                else -> TODO("Can not parse color!")
            }
        }
    }

    object Serializer : StdSerializer<RGBColor>(RGBColor::class.java) {

        override fun serialize(value: RGBColor?, generator: JsonGenerator, provider: SerializerProvider?) {
            generator.writeString(value?.toString())
        }
    }
}
