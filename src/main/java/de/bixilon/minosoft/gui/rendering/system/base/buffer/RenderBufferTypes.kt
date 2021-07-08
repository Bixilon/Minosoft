package de.bixilon.minosoft.gui.rendering.system.base.buffer

import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum

enum class RenderBufferTypes {
    UNIFORM_BUFFER,
    ARRAY_BUFFER,
    ;

    companion object : ValuesEnum<RenderBufferTypes> {
        override val VALUES: Array<RenderBufferTypes> = values()
        override val NAME_MAP: Map<String, RenderBufferTypes> = KUtil.getEnumValues(VALUES)
    }
}