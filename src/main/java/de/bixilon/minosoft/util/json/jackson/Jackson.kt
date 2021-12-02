package de.bixilon.minosoft.util.json.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.module.kotlin.KotlinModule

object Jackson {
    val MAPPER = ObjectMapper()
        .registerModule(KotlinModule())
        .registerModule(ResourceLocationSerializer)


    val JSON_MAP_TYPE: MapType = MAPPER.typeFactory.constructMapType(HashMap::class.java, Any::class.java, Any::class.java)

    init {
        MAPPER.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }

}
