/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

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
