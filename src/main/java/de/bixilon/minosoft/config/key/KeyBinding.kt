package de.bixilon.minosoft.config.key

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.ModIdentifier

class KeyBinding(
    val identifier: ModIdentifier,
    val action: MutableMap<KeyAction, MutableSet<KeyCodes>>,
    val `when`: MutableSet<MutableSet<ModIdentifier>>,
) {

    fun serialize(): JsonObject {
        val ret = JsonObject()
        for ((keyEvent, keyCodes) in action) {
            val keyEventArray = JsonArray()
            for (keyCode in keyCodes) {
                keyEventArray.add(keyCode.keyName.toUpperCase().replace(' ', '_'))
            }
            ret.add(keyEvent.name.toLowerCase(), keyEventArray)
        }
        val `when` = JsonArray()
        for (whenIdentifierSet in this.`when`) {
            val whenIdentifierArray = JsonArray()
            for (whenIdentifier in whenIdentifierSet) {
                whenIdentifierArray.add(whenIdentifier.fullIdentifier)
            }
            `when`.add(whenIdentifierArray)
        }

        ret.add("when", `when`)
        return ret
    }

    companion object {
        fun deserialize(identifier: ModIdentifier, json: JsonObject): KeyBinding {
            val action: MutableMap<KeyAction, MutableSet<KeyCodes>> = mutableMapOf()
            for (keyAction in KeyAction.VALUES) {
                json[keyAction.name.toLowerCase()]?.asJsonArray?.let {
                    val keyCodes: MutableSet<KeyCodes> = mutableSetOf()
                    for (keyCode in it) {
                        keyCodes.add(KeyCodes.KEY_CODE_MAP[keyCode.asString.toUpperCase()]!!)
                    }
                    action[keyAction] = keyCodes
                }
            }
            val `when`: MutableSet<MutableSet<ModIdentifier>> = mutableSetOf()
            json["when"]?.asJsonArray?.let {
                val currentWhenIdentifierSet: MutableSet<ModIdentifier> = mutableSetOf()
                for (whenIdentifierJsonElementArray in it) {
                    val whenIdentifierArray = whenIdentifierJsonElementArray.asJsonArray
                    for (whenIdentifier in whenIdentifierArray) {
                        currentWhenIdentifierSet.add(ModIdentifier.getIdentifier(whenIdentifier.asString))
                    }
                }
                `when`.add(currentWhenIdentifierSet)
            }
            return KeyBinding(identifier, action, `when`)
        }
    }

    enum class KeyAction {
        MODIFIER,
        CHANGE,
        PRESS,
        RELEASE,
        DOUBLE_CLICK,
        TOGGLE,
        ;

        companion object {
            val VALUES = values()
        }
    }
}
