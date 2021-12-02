package de.bixilon.minosoft.util.json.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object ResourceLocationSerializer : SimpleModule() {
    init {
        addDeserializer(ResourceLocation::class.java, Deserializer)
        addSerializer(ResourceLocation::class.java, Serializer)
    }

    object Deserializer : StdDeserializer<ResourceLocation>(ResourceLocation::class.java) {

        override fun deserialize(parser: JsonParser, context: DeserializationContext?): ResourceLocation {
            return parser.valueAsString.toResourceLocation()
        }
    }

    object Serializer : StdSerializer<ResourceLocation>(ResourceLocation::class.java) {

        override fun serialize(value: ResourceLocation?, generator: JsonGenerator, provider: SerializerProvider?) {
            generator.writeString(value?.full)
        }
    }
}
