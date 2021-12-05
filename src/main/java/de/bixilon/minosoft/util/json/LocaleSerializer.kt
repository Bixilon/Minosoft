package de.bixilon.minosoft.util.json

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
            var locale = Locale.forLanguageTag(parser.valueAsString)
            if (locale.language.isEmpty()) {
                locale = Locale.US
            }
            return locale
        }
    }

    object Serializer : StdSerializer<Locale>(Locale::class.java) {

        override fun serialize(value: Locale?, generator: JsonGenerator, provider: SerializerProvider?) {
            if (value?.language?.isEmpty() == true) {
                generator.writeString(Locale.US.fullName)
            } else {
                generator.writeString(value?.fullName)
            }
        }
    }
}
