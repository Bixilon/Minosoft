package de.bixilon.minosoft.config.key

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
