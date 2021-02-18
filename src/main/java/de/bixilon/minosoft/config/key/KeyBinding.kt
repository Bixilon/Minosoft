package de.bixilon.minosoft.config.key

import de.bixilon.minosoft.data.mappings.ModIdentifier

class KeyBinding(
    val action: MutableMap<KeyAction, MutableSet<KeyCodes>>,
    val `when`: MutableSet<MutableSet<ModIdentifier>>,
)
