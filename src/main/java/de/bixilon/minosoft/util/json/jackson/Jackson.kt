package de.bixilon.minosoft.util.json.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.bixilon.minosoft.data.registries.ResourceLocation

object Jackson {
    val MAPPER = ObjectMapper()
        .registerModule(KotlinModule())
        .registerModule(SimpleModule().apply { addDeserializer(ResourceLocation::class.java, ResourceLocationSerializer) })


    val JSON_MAP_TYPE: MapType = MAPPER.typeFactory.constructMapType(HashMap::class.java, Any::class.java, Any::class.java)

    init {
        MAPPER.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }

}
