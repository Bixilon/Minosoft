/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.window

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.modding.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.modding.events.WindowCloseEvent
import de.bixilon.minosoft.gui.rendering.modding.events.WindowFocusChangeEvent
import de.bixilon.minosoft.gui.rendering.modding.events.WindowIconifyChangeEvent
import de.bixilon.minosoft.gui.rendering.modding.events.input.MouseMoveEvent
import de.bixilon.minosoft.gui.rendering.modding.events.input.MouseScrollEvent
import de.bixilon.minosoft.gui.rendering.modding.events.input.RawCharInputEvent
import de.bixilon.minosoft.gui.rendering.modding.events.input.RawKeyInputEvent
import de.bixilon.minosoft.gui.rendering.system.window.BaseWindow.Companion.DEFAULT_MAXIMUM_WINDOW_SIZE
import de.bixilon.minosoft.gui.rendering.system.window.BaseWindow.Companion.DEFAULT_MINIMUM_WINDOW_SIZE
import de.bixilon.minosoft.gui.rendering.system.window.BaseWindow.Companion.DEFAULT_WINDOW_SIZE
import de.bixilon.minosoft.modding.event.master.AbstractEventMaster
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer


class GLFWWindow(
    private val eventMaster: AbstractEventMaster,
) : BaseWindow {
    private var window = -1L

    override var cursorMode: CursorModes = CursorModes.NORMAL
        set(value) {
            if (field == value) {
                return
            }
            glfwSetInputMode(window, GLFW_CURSOR, value.glfw)
            field = value
        }

    private var _size = Vec2i(DEFAULT_WINDOW_SIZE)

    override var size: Vec2i
        get() = _size
        set(value) {
            glfwSetWindowSize(window, value.x, value.y)
            _size = size
        }

    override var minSize: Vec2i = DEFAULT_MINIMUM_WINDOW_SIZE
        set(value) {
            glfwSetWindowSizeLimits(window, value.x, value.y, maxSize.x, maxSize.y)
            field = value
        }

    override var maxSize: Vec2i = DEFAULT_MAXIMUM_WINDOW_SIZE
        set(value) {
            glfwSetWindowSizeLimits(window, minSize.x, minSize.y, value.x, value.y)
            field = value
        }

    override var visible: Boolean = false
        set(value) {
            if (field == value) {
                return
            }
            when (value) {
                true -> glfwShowWindow(window)
                false -> glfwHideWindow(window)
            }
            field = value
        }

    override var resizable: Boolean = true
        set(value) {
            if (field == value) {
                return
            }
            glfwWindowHint(GLFW_RESIZABLE, value.glfw)
            field = value
        }

    override var swapInterval: Int = -1
        set(value) {
            if (field == value) {
                return
            }
            glfwSwapInterval(value)
            field = value
        }

    override var clipboardText: String
        get() = glfwGetClipboardString(window) ?: ""
        set(value) {
            glfwSetClipboardString(window, value)
        }

    override val version: String
        get() = glfwGetVersionString()

    override val time: Double
        get() = glfwGetTime()

    override var title: String = "Window"
        set(value) {
            if (field == value) {
                return
            }
            glfwSetWindowTitle(window, value)
            field = value
        }

    override fun init() {
        GLFWErrorCallback.createPrint(System.err).set()
        check(glfwInit()) { "Unable to initialize GLFW" }

        glfwDefaultWindowHints()
        if (Minosoft.config.config.game.graphics.preferQuads) {
            setOpenGLVersion(3, 0, false)
        } else {
            setOpenGLVersion(3, 3, true)
        }
        glfwWindowHint(GLFW_VISIBLE, false.glfw)


        window = glfwCreateWindow(size.x, size.y, "Minosoft", MemoryUtil.NULL, MemoryUtil.NULL)
        if (window == MemoryUtil.NULL) {
            destroy()
            throw RuntimeException("Failed to create the GLFW window")
        }

        glfwMakeContextCurrent(window)

        super.init()

        val primaryMonitor = glfwGetPrimaryMonitor()
        if (primaryMonitor != 0L) {
            glfwGetVideoMode(primaryMonitor)?.let {
                glfwSetWindowPos(window, (it.width() - size.x) / 2, (it.height() - size.y) / 2)
            }
        }


        glfwSetKeyCallback(window, this::keyInput)
        glfwSetMouseButtonCallback(window, this::mouseKeyInput)

        glfwSetCharCallback(window, this::charInput)
        glfwSetCursorPosCallback(window, this::mouseMove)

        glfwSetWindowSizeCallback(window, this::onResize)

        glfwSetWindowCloseCallback(window, this::onClose)
        glfwSetWindowFocusCallback(window, this::onFocusChange)
        glfwSetWindowIconifyCallback(window, this::onIconify)
        glfwSetScrollCallback(window, this::onScroll)
    }

    override fun destroy() {
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        glfwTerminate()
        glfwSetErrorCallback(null)!!.free()
    }

    override fun close() {
        if (eventMaster.fireEvent(WindowCloseEvent(window = this))) {
            return
        }

        glfwSetWindowShouldClose(window, true)
    }

    override fun swapBuffers() {
        glfwSwapBuffers(window)
    }

    override fun pollEvents() {
        glfwPollEvents()
    }

    override fun setOpenGLVersion(major: Int, minor: Int, coreProfile: Boolean) {
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, major)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, minor)
        glfwWindowHint(GLFW_OPENGL_PROFILE, if (coreProfile) GLFW_OPENGL_CORE_PROFILE else GLFW_OPENGL_ANY_PROFILE)
    }

    private fun onFocusChange(window: Long, focused: Boolean) {
        if (window != this.window) {
            return
        }

        eventMaster.fireEvent(WindowFocusChangeEvent(window = this, focused = focused))
    }

    private fun onIconify(window: Long, iconified: Boolean) {
        if (window != this.window) {
            return
        }

        eventMaster.fireEvent(WindowIconifyChangeEvent(window = this, iconified = iconified))
    }

    private fun onClose(window: Long) {
        if (window != this.window) {
            return
        }
        val cancelled = eventMaster.fireEvent(WindowCloseEvent(window = this))

        if (cancelled) {
            glfwSetWindowShouldClose(window, false)
        }
    }

    private fun onResize(window: Long, width: Int, height: Int) {
        if (window != this.window) {
            return
        }
        val previousSize = Vec2i(_size)
        _size = Vec2i(width, height)
        eventMaster.fireEvent(ResizeWindowEvent(previousSize = previousSize, size = _size))
    }

    private fun mouseKeyInput(windowId: Long, button: Int, action: Int, modifierKey: Int) {
        keyInput(windowId, button, 0, action, modifierKey)
    }

    private fun keyInput(window: Long, key: Int, char: Int, action: Int, modifierKey: Int) {
        if (window != this.window) {
            return
        }
        val keyCode = KeyCodes.KEY_CODE_GLFW_ID_MAP[key] ?: KeyCodes.KEY_UNKNOWN

        val keyAction = when (action) {
            GLFW_PRESS -> KeyChangeTypes.PRESS
            GLFW_RELEASE -> KeyChangeTypes.RELEASE
            GLFW_REPEAT -> KeyChangeTypes.REPEAT
            else -> {
                Log.log(LogMessageType.RENDERING_GENERAL, LogLevels.WARN) { "Unknown glfw action $action" }
                return
            }
        }

        eventMaster.fireEvent(RawKeyInputEvent(keyCode = keyCode, keyChangeType = keyAction))
    }

    private fun charInput(windowId: Long, char: Int) {
        if (windowId != window) {
            return
        }
        eventMaster.fireEvent(RawCharInputEvent(char = char))
    }

    private fun mouseMove(windowId: Long, x: Double, y: Double) {
        if (windowId != window) {
            return
        }
        eventMaster.fireEvent(MouseMoveEvent(position = Vec2d(x, y)))
    }

    private fun onScroll(window: Long, xOffset: Double, yOffset: Double) {
        if (window != this.window) {
            return
        }

        eventMaster.fireEvent(MouseScrollEvent(offset = Vec2d(xOffset, yOffset)))
    }

    override fun setIcon(size: Vec2i, buffer: ByteBuffer) {
        val images = GLFWImage.malloc(1)
        val image = GLFWImage.malloc()
        image.set(size.x, size.y, buffer)
        images.put(0, image)
        glfwSetWindowIcon(window, images)
    }

    companion object {
        val CursorModes.glfw: Int
            get() {
                return when (this) {
                    CursorModes.NORMAL -> GLFW_CURSOR_NORMAL
                    CursorModes.HIDDEN -> GLFW_CURSOR_HIDDEN
                    CursorModes.DISABLED -> GLFW_CURSOR_DISABLED
                }
            }

        val Boolean.glfw: Int
            get() {
                return when (this) {
                    true -> GLFW_TRUE
                    false -> GLFW_FALSE
                }
            }
    }
}
