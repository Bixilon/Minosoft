package de.bixilon.minosoft.util.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule

object Jackson {
    val MAPPER = ObjectMapper()
        .registerModule(KotlinModule.Builder()
            .withReflectionCacheSize(512)
            .configure(KotlinFeature.NullToEmptyCollection, false)
            .configure(KotlinFeature.NullToEmptyMap, false)
            .configure(KotlinFeature.NullIsSameAsDefault, false)
            .configure(KotlinFeature.SingletonSupport, false)
            .configure(KotlinFeature.StrictNullChecks, false)
            .build())
        .registerModule(ResourceLocationSerializer)
        .registerModule(RGBColorSerializer)
        .registerModule(ChatComponentColorSerializer)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setDefaultMergeable(true)


    val JSON_MAP_TYPE: MapType = MAPPER.typeFactory.constructMapType(HashMap::class.java, Any::class.java, Any::class.java)

    init {
        MAPPER.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }
}
