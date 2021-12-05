package de.bixilon.minosoft.util.json.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import de.bixilon.minosoft.util.KUtil.fullName
import java.util.*

object LocaleSerializer : SimpleModule() {
    init {
        addDeserializer(Locale::class.java, Deserializer)
        addSerializer(Locale::class.java, Serializer)
    }

    object Deserializer : StdDeserializer<Locale>(Locale::class.java) {

        override fun deserialize(parser: JsonParser, context: DeserializationContext?): Locale {
            return Locale.forLanguageTag(parser.valueAsString)
        }
    }

    object Serializer : StdSerializer<Locale>(Locale::class.java) {

        override fun serialize(value: Locale?, generator: JsonGenerator, provider: SerializerProvider?) {
            generator.writeString(value?.fullName)
        }
    }
}
