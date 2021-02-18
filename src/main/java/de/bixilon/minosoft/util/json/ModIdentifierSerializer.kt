package de.bixilon.minosoft.util.json

import com.squareup.moshi.*
import de.bixilon.minosoft.data.mappings.ModIdentifier

class ModIdentifierSerializer : JsonAdapter<ModIdentifier>() {
    @FromJson
    override fun fromJson(jsonReader: JsonReader): ModIdentifier? {
        if (jsonReader.peek() == JsonReader.Token.NULL) {
            return null
        }
        return ModIdentifier.getIdentifier(jsonReader.nextString())
    }

    @ToJson
    override fun toJson(jsonWriter: JsonWriter, modIdentifier: ModIdentifier?) {
        if (modIdentifier == null) {
            jsonWriter.nullValue()
            return
        }
        jsonWriter.value(modIdentifier.fullIdentifier)
    }
}
