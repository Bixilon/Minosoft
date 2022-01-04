package de.bixilon.minosoft.gui.rendering.system.opengl

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystemError
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.GL_INVALID_FRAMEBUFFER_OPERATION
import org.lwjgl.opengl.GL45.GL_CONTEXT_LOST

class OpenGLError(
    val id: Int,
) : RenderSystemError {
    val type = OpenGLErrors.CODES[id]
    override val printMessage = ChatComponent.of("§cOpenGL error: §e$id ($type)")

    enum class OpenGLErrors(val code: Int) {
        INVALID_ENUM(GL_INVALID_ENUM),
        INVALID_VALUE(GL_INVALID_VALUE),
        INVALID_OPERATION(GL_INVALID_OPERATION),
        STACK_OVERFLOW(GL_STACK_OVERFLOW),
        STACK_UNDERFLOW(GL_STACK_UNDERFLOW),
        OUT_OF_MEMORY(GL_OUT_OF_MEMORY),
        INVALID_FRAMEBUFFER_OPERATION(GL_INVALID_FRAMEBUFFER_OPERATION),
        CONTEXT_LOST(GL_CONTEXT_LOST),
        ;

        companion object : ValuesEnum<OpenGLErrors> {
            override val VALUES: Array<OpenGLErrors> = values()
            override val NAME_MAP: Map<String, OpenGLErrors> = EnumUtil.getEnumValues(VALUES)
            val CODES: Map<Int, OpenGLErrors>

            init {
                val codes: MutableMap<Int, OpenGLErrors> = mutableMapOf()

                for (value in VALUES) {
                    codes[value.code] = value
                }
                this.CODES = codes
            }
        }
    }
}
