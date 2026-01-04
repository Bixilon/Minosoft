/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.window.glfw

import de.bixilon.kutil.cast.CastUtil.cast
import de.bixilon.kutil.concurrent.queue.Queue
import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.kutil.os.OSTypes
import de.bixilon.kutil.os.PlatformInfo
import de.bixilon.minosoft.gui.rendering.system.base.buffer.AsyncBufferLoader
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.VertexBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.vertex.OpenGlVertexBuffer
import de.bixilon.minosoft.gui.rendering.system.window.glfw.GlfwUtil.glfw
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.system.MemoryUtil
import kotlin.time.Duration.Companion.seconds

class GlfwGlBufferLoader : AsyncBufferLoader {
    private val latch = SimpleLatch(1)
    private val queue = Queue()
    var window = -1L
        private set

    init {
        Thread({ init(); latch.dec(); loop() }, "loader").start()
    }

    fun init() {
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        if (PlatformInfo.OS == OSTypes.MAC) {
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, true.glfw)
        }
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

        glfwWindowHint(GLFW_VISIBLE, false.glfw)
        if (PlatformInfo.OS == OSTypes.MAC) {
            glfwWindowHint(GLFW_COCOA_MENUBAR, false.glfw) // https://www.glfw.org/docs/3.3/context_guide.html
        }

        window = glfwCreateWindow(1, 1, "", MemoryUtil.NULL, MemoryUtil.NULL)
        glfwMakeContextCurrent(window)

        GL.createCapabilities()
    }

    fun loop() {
        while (true) {
            queue.workBlocking(1.seconds)
            glfwPollEvents()
        }
    }

    fun await() = latch.await()


    operator fun plusAssign(item: () -> Unit) = queue.plusAssign(item)

    override fun load(buffer: VertexBuffer, callback: () -> Unit) {
        buffer.cast<OpenGlVertexBuffer>().initAsync(callback)
    }
}
