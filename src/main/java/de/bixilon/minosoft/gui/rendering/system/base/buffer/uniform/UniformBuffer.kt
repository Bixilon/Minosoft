package de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform

import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderBuffer

interface UniformBuffer : RenderBuffer {
    val bindingIndex: Int
}