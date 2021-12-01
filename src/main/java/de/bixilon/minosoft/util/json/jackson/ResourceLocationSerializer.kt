package de.bixilon.minosoft.util.json.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object ResourceLocationSerializer : StdDeserializer<ResourceLocation>(ResourceLocation::class.java) {

    override fun deserialize(parser: JsonParser, context: DeserializationContext?): ResourceLocation {
        return parser.valueAsString.toResourceLocation()
    }
}
