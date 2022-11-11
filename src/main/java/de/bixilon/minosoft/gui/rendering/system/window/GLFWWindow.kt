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

package de.bixilon.minosoft.gui.rendering.system.window

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2d
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.os.OSTypes
import de.bixilon.kutil.os.PlatformInfo
import de.bixilon.kutil.watcher.DataWatcher.Companion.watched
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfile
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Rendering
import de.bixilon.minosoft.gui.rendering.events.RenderEvent
import de.bixilon.minosoft.gui.rendering.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.events.WindowCloseEvent
import de.bixilon.minosoft.gui.rendering.events.input.MouseMoveEvent
import de.bixilon.minosoft.gui.rendering.events.input.MouseScrollEvent
import de.bixilon.minosoft.gui.rendering.events.input.RawCharInputEvent
import de.bixilon.minosoft.gui.rendering.events.input.RawKeyInputEvent
import de.bixilon.minosoft.gui.rendering.system.window.BaseWindow.Companion.DEFAULT_MAXIMUM_WINDOW_SIZE
import de.bixilon.minosoft.gui.rendering.system.window.BaseWindow.Companion.DEFAULT_MINIMUM_WINDOW_SIZE
import de.bixilon.minosoft.gui.rendering.system.window.BaseWindow.Companion.DEFAULT_WINDOW_SIZE
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2dUtil.EMPTY
import de.bixilon.minosoft.modding.event.master.AbstractEventMaster
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.system.Configuration
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer


class GLFWWindow(
    private val renderWindow: RenderWindow,
    private val eventMaster: AbstractEventMaster = renderWindow.connection,
) : BaseWindow {
    private var mousePosition = Vec2d.EMPTY
    private var skipNextMouseEvent = true
    private var window = -1L

    override var cursorMode: CursorModes = CursorModes.NORMAL
        set(value) {
            if (field == value) {
                return
            }
            glfwSetInputMode(window, GLFW_CURSOR, value.glfw)
            field = value
            skipNextMouseEvent = true
        }
    override var cursorShape: CursorShapes = CursorShapes.ARROW
        set(value) {
            if (field == value) {
                return
            }
            glfwSetCursor(window, glfwCreateStandardCursor(value.glfw))
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

    override var fullscreen: Boolean = false
        set(value) {
            if (field == value) {
                return
            }
            val monitor = glfwGetPrimaryMonitor()
            val mode = glfwGetVideoMode(monitor) ?: return
            if (value) {
                glfwSetWindowMonitor(window, monitor, 15, 15, mode.width(), mode.height(), mode.refreshRate())
            } else {
                val scale = getWindowScale()
                val size = Vec2i(DEFAULT_WINDOW_SIZE.x / scale.x, DEFAULT_WINDOW_SIZE.y / scale.y)
                glfwSetWindowMonitor(window, 0, (mode.width() - size.x) / 2, (mode.height() - size.y) / 2, size.x, size.y, GLFW_DONT_CARE)
            }

            field = value
        }

    override var swapInterval: Int = -1
        set(value) {
            if (field == value) {
                return
            }
            //  ******************* WAIT *******************
            // So you found that option...
            // Good search, maybe I should hide it a bit more
            // about v*s*y*n*c: Why the hell would you want to disable it?
            // it makes no sense, you are always 50ms behind, because of position interpolation
            // same applied to minecraft
            // want more fps? flexing? enable exper*imental f|p|s.
            // want to benchmark, yes. understandable. search in the config and set it to 0
            // if you want to compare this with minecraft? not needed.
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

    override fun init(profile: RenderingProfile) {
        initLatch.await() // wait for async glfw init
        glfwDefaultWindowHints()
        if (renderWindow.preferQuads) {
            // yes, this is dirty. for using a geometry shader we need 3.3+. The thing is 3.3+ does not allow us to use GL_QUAD.
            // we can still bind it to a lower version and use features that need a more recent version of opengl.
            // most drivers allow us to do this, if not it'll crash
            setOpenGLVersion(3, 0, false)
        } else {
            setOpenGLVersion(3, 3, true)
        }
        glfwWindowHint(GLFW_VISIBLE, false.glfw)


        window = glfwCreateWindow(size.x, size.y, RunConfiguration.APPLICATION_NAME, MemoryUtil.NULL, MemoryUtil.NULL)
        if (window == MemoryUtil.NULL) {
            try {
                destroy()
            } catch (ignored: Throwable) {
            }
            throw IllegalStateException("Failed to create the GLFW window. Check the console for details. BEFORE opening any issue check that your GPU supports OpenGL 3.3+ and the most recent drivers are installed!")
        }

        glfwMakeContextCurrent(window)

        super.init(profile)
        val scale = getWindowScale()
        val size = Vec2i(DEFAULT_WINDOW_SIZE.x / scale.x, DEFAULT_WINDOW_SIZE.y / scale.y)
        glfwSetWindowSize(window, size.x, size.y)

        val primaryMonitor = glfwGetPrimaryMonitor()
        if (primaryMonitor != MemoryUtil.NULL) {
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

        when (PlatformInfo.OS) {
            OSTypes.MAC -> {
                glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, true.glfw)
                glfwWindowHintString(GLFW_COCOA_FRAME_NAME, "Minosoft")
            }

            OSTypes.LINUX -> {
                glfwWindowHintString(GLFW_X11_CLASS_NAME, "de.bixilon.minosoft")
                glfwWindowHintString(GLFW_X11_INSTANCE_NAME, "Minosoft")
            }

            else -> {}
        }
    }

    override fun destroy() {
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        glfwSetErrorCallback(null)?.free()
    }

    override fun close() {
        if (fireGLFWEvent(WindowCloseEvent(renderWindow, window = this))) {
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
        if (PlatformInfo.OS == OSTypes.MAC) {
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, true.glfw)
        }
        glfwWindowHint(GLFW_OPENGL_PROFILE, if (coreProfile) GLFW_OPENGL_CORE_PROFILE else GLFW_OPENGL_ANY_PROFILE)
    }

    override var focused by watched(false)

    private fun onFocusChange(window: Long, focused: Boolean) {
        if (window != this.window) {
            return
        }
        this.focused = focused
    }

    override var iconified by watched(false)

    private fun onIconify(window: Long, iconified: Boolean) {
        if (window != this.window) {
            return
        }
        this.iconified = iconified
    }

    private fun onClose(window: Long) {
        if (window != this.window) {
            return
        }
        val cancelled = fireGLFWEvent(WindowCloseEvent(renderWindow, window = this))

        if (cancelled) {
            glfwSetWindowShouldClose(window, false)
        }
    }

    private fun onResize(window: Long, width: Int, height: Int) {
        if (window != this.window) {
            return
        }
        val previousSize = Vec2i(_size)
        val scale = getWindowScale()
        val nextSize = Vec2i(width * scale.x, height * scale.y)
        _size = nextSize
        fireGLFWEvent(ResizeWindowEvent(renderWindow, previousSize = previousSize, size = _size))
        this.skipNextMouseEvent = true
    }

    private fun mouseKeyInput(windowId: Long, button: Int, action: Int, modifierKey: Int) {
        keyInput(windowId, button, 0, action, modifierKey)
    }

    private fun keyInput(window: Long, key: Int, char: Int, action: Int, modifierKey: Int) {
        if (window != this.window) {
            return
        }
        val keyCode = KEY_CODE_MAPPING[key] ?: KeyCodes.KEY_UNKNOWN

        val keyAction = when (action) {
            GLFW_PRESS -> KeyChangeTypes.PRESS
            GLFW_RELEASE -> KeyChangeTypes.RELEASE
            GLFW_REPEAT -> KeyChangeTypes.REPEAT
            else -> {
                Log.log(LogMessageType.RENDERING_GENERAL, LogLevels.WARN) { "Unknown glfw action $action" }
                return
            }
        }

        fireGLFWEvent(RawKeyInputEvent(renderWindow, keyCode = keyCode, keyChangeType = keyAction))
    }

    private fun charInput(windowId: Long, char: Int) {
        if (windowId != window) {
            return
        }
        fireGLFWEvent(RawCharInputEvent(renderWindow, char = char))
    }

    private fun mouseMove(windowId: Long, x: Double, y: Double) {
        if (windowId != window) {
            return
        }


        val position = Vec2d(x, y) * getWindowScale()
        val previous = this.mousePosition
        val delta = position - previous
        this.mousePosition = position
        if (!skipNextMouseEvent) {
            fireGLFWEvent(MouseMoveEvent(renderWindow, position = position, previous = previous, delta = delta))
        } else {
            skipNextMouseEvent = false
        }
    }

    private fun onScroll(window: Long, xOffset: Double, yOffset: Double) {
        if (window != this.window) {
            return
        }

        fireGLFWEvent(MouseScrollEvent(renderWindow, offset = Vec2d(xOffset, yOffset)))
    }

    override fun setIcon(size: Vec2i, buffer: ByteBuffer) {
        if (PlatformInfo.OS == OSTypes.MAC) {
            Log.log(LogMessageType.RENDERING_GENERAL, LogLevels.WARN) { "Can not set window icon on mac os!" } // ToDo
            return
        }
        val images = GLFWImage.malloc(1)
        val image = GLFWImage.malloc()
        image.set(size.x, size.y, buffer)
        images.put(0, image)
        glfwSetWindowIcon(window, images)
    }

    private fun getWindowScale(): Vec2 {
        val x = FloatArray(1)
        val y = FloatArray(1)
        glfwGetWindowContentScale(window, x, y)
        return Vec2(x[0], y[0])
    }

    private fun fireGLFWEvent(event: RenderEvent): Boolean {
        // ToDo: It looks like glfwPollEvents is mixing threads. This should not happen.
        if (Rendering.currentContext != event.renderWindow) {
            event.renderWindow.queue += { eventMaster.fire(event) }
            return false
        }
        return eventMaster.fire(event)
    }

    companion object {
        private val initLatch = CountUpAndDownLatch(1)

        init {
            if (PlatformInfo.OS == OSTypes.MAC) {
                Configuration.GLFW_LIBRARY_NAME.set("glfw_async")
            }
            DefaultThreadPool += {
                GLFWErrorCallback.createPrint(Log.FATAL_PRINT_STREAM).set()
                check(glfwInit()) { "Unable to initialize GLFW" }
                initLatch.dec()
            }
        }

        val KEY_CODE_MAPPING = mapOf(
            GLFW_KEY_UNKNOWN to KeyCodes.KEY_UNKNOWN,
            GLFW_KEY_SPACE to KeyCodes.KEY_SPACE,
            GLFW_KEY_APOSTROPHE to KeyCodes.KEY_APOSTROPHE,
            GLFW_KEY_COMMA to KeyCodes.KEY_COMMA,
            GLFW_KEY_MINUS to KeyCodes.KEY_MINUS,
            GLFW_KEY_PERIOD to KeyCodes.KEY_PERIOD,
            GLFW_KEY_SLASH to KeyCodes.KEY_SLASH,
            GLFW_KEY_0 to KeyCodes.KEY_0,
            GLFW_KEY_1 to KeyCodes.KEY_1,
            GLFW_KEY_2 to KeyCodes.KEY_2,
            GLFW_KEY_3 to KeyCodes.KEY_3,
            GLFW_KEY_4 to KeyCodes.KEY_4,
            GLFW_KEY_5 to KeyCodes.KEY_5,
            GLFW_KEY_6 to KeyCodes.KEY_6,
            GLFW_KEY_7 to KeyCodes.KEY_7,
            GLFW_KEY_8 to KeyCodes.KEY_8,
            GLFW_KEY_9 to KeyCodes.KEY_9,
            GLFW_KEY_SEMICOLON to KeyCodes.KEY_SEMICOLON,
            GLFW_KEY_EQUAL to KeyCodes.KEY_EQUAL,
            GLFW_KEY_A to KeyCodes.KEY_A,
            GLFW_KEY_B to KeyCodes.KEY_B,
            GLFW_KEY_C to KeyCodes.KEY_C,
            GLFW_KEY_D to KeyCodes.KEY_D,
            GLFW_KEY_E to KeyCodes.KEY_E,
            GLFW_KEY_F to KeyCodes.KEY_F,
            GLFW_KEY_G to KeyCodes.KEY_G,
            GLFW_KEY_H to KeyCodes.KEY_H,
            GLFW_KEY_I to KeyCodes.KEY_I,
            GLFW_KEY_J to KeyCodes.KEY_J,
            GLFW_KEY_K to KeyCodes.KEY_K,
            GLFW_KEY_L to KeyCodes.KEY_L,
            GLFW_KEY_M to KeyCodes.KEY_M,
            GLFW_KEY_N to KeyCodes.KEY_N,
            GLFW_KEY_O to KeyCodes.KEY_O,
            GLFW_KEY_P to KeyCodes.KEY_P,
            GLFW_KEY_Q to KeyCodes.KEY_Q,
            GLFW_KEY_R to KeyCodes.KEY_R,
            GLFW_KEY_S to KeyCodes.KEY_S,
            GLFW_KEY_T to KeyCodes.KEY_T,
            GLFW_KEY_U to KeyCodes.KEY_U,
            GLFW_KEY_V to KeyCodes.KEY_V,
            GLFW_KEY_W to KeyCodes.KEY_W,
            GLFW_KEY_X to KeyCodes.KEY_X,
            GLFW_KEY_Y to KeyCodes.KEY_Y,
            GLFW_KEY_Z to KeyCodes.KEY_Z,
            GLFW_KEY_LEFT_BRACKET to KeyCodes.KEY_LEFT_BRACKET,
            GLFW_KEY_BACKSLASH to KeyCodes.KEY_BACKSLASH,
            GLFW_KEY_RIGHT_BRACKET to KeyCodes.KEY_RIGHT_BRACKET,
            GLFW_KEY_GRAVE_ACCENT to KeyCodes.KEY_GRAVE_ACCENT,
            GLFW_KEY_WORLD_1 to KeyCodes.KEY_WORLD_1,
            GLFW_KEY_WORLD_2 to KeyCodes.KEY_WORLD_2,

            GLFW_KEY_ESCAPE to KeyCodes.KEY_ESCAPE,
            GLFW_KEY_ENTER to KeyCodes.KEY_ENTER,
            GLFW_KEY_TAB to KeyCodes.KEY_TAB,
            GLFW_KEY_BACKSPACE to KeyCodes.KEY_BACKSPACE,
            GLFW_KEY_INSERT to KeyCodes.KEY_INSERT,
            GLFW_KEY_DELETE to KeyCodes.KEY_DELETE,
            GLFW_KEY_RIGHT to KeyCodes.KEY_RIGHT,
            GLFW_KEY_LEFT to KeyCodes.KEY_LEFT,
            GLFW_KEY_DOWN to KeyCodes.KEY_DOWN,
            GLFW_KEY_UP to KeyCodes.KEY_UP,
            GLFW_KEY_PAGE_UP to KeyCodes.KEY_PAGE_UP,
            GLFW_KEY_PAGE_DOWN to KeyCodes.KEY_PAGE_DOWN,
            GLFW_KEY_HOME to KeyCodes.KEY_HOME,
            GLFW_KEY_END to KeyCodes.KEY_END,
            GLFW_KEY_CAPS_LOCK to KeyCodes.KEY_CAPS_LOCK,
            GLFW_KEY_SCROLL_LOCK to KeyCodes.KEY_SCROLL_LOCK,
            GLFW_KEY_NUM_LOCK to KeyCodes.KEY_NUM_LOCK,
            GLFW_KEY_PRINT_SCREEN to KeyCodes.KEY_PRINT_SCREEN,
            GLFW_KEY_PAUSE to KeyCodes.KEY_PAUSE,
            GLFW_KEY_F1 to KeyCodes.KEY_F1,
            GLFW_KEY_F2 to KeyCodes.KEY_F2,
            GLFW_KEY_F3 to KeyCodes.KEY_F3,
            GLFW_KEY_F4 to KeyCodes.KEY_F4,
            GLFW_KEY_F5 to KeyCodes.KEY_F5,
            GLFW_KEY_F6 to KeyCodes.KEY_F6,
            GLFW_KEY_F7 to KeyCodes.KEY_F7,
            GLFW_KEY_F8 to KeyCodes.KEY_F8,
            GLFW_KEY_F9 to KeyCodes.KEY_F9,
            GLFW_KEY_F10 to KeyCodes.KEY_F10,
            GLFW_KEY_F11 to KeyCodes.KEY_F11,
            GLFW_KEY_F12 to KeyCodes.KEY_F12,
            GLFW_KEY_F13 to KeyCodes.KEY_F13,
            GLFW_KEY_F14 to KeyCodes.KEY_F14,
            GLFW_KEY_F15 to KeyCodes.KEY_F15,
            GLFW_KEY_F16 to KeyCodes.KEY_F16,
            GLFW_KEY_F17 to KeyCodes.KEY_F17,
            GLFW_KEY_F18 to KeyCodes.KEY_F18,
            GLFW_KEY_F19 to KeyCodes.KEY_F19,
            GLFW_KEY_F20 to KeyCodes.KEY_F20,
            GLFW_KEY_F21 to KeyCodes.KEY_F21,
            GLFW_KEY_F22 to KeyCodes.KEY_F22,
            GLFW_KEY_F23 to KeyCodes.KEY_F23,
            GLFW_KEY_F24 to KeyCodes.KEY_F24,
            GLFW_KEY_F25 to KeyCodes.KEY_F25,
            GLFW_KEY_KP_0 to KeyCodes.KEY_KP_0,
            GLFW_KEY_KP_1 to KeyCodes.KEY_KP_1,
            GLFW_KEY_KP_2 to KeyCodes.KEY_KP_2,
            GLFW_KEY_KP_3 to KeyCodes.KEY_KP_3,
            GLFW_KEY_KP_4 to KeyCodes.KEY_KP_4,
            GLFW_KEY_KP_5 to KeyCodes.KEY_KP_5,
            GLFW_KEY_KP_6 to KeyCodes.KEY_KP_6,
            GLFW_KEY_KP_7 to KeyCodes.KEY_KP_7,
            GLFW_KEY_KP_8 to KeyCodes.KEY_KP_8,
            GLFW_KEY_KP_9 to KeyCodes.KEY_KP_9,
            GLFW_KEY_KP_DECIMAL to KeyCodes.KEY_KP_DECIMAL,
            GLFW_KEY_KP_DIVIDE to KeyCodes.KEY_KP_DIVIDE,
            GLFW_KEY_KP_MULTIPLY to KeyCodes.KEY_KP_MULTIPLY,
            GLFW_KEY_KP_SUBTRACT to KeyCodes.KEY_KP_SUBTRACT,
            GLFW_KEY_KP_ADD to KeyCodes.KEY_KP_ADD,
            GLFW_KEY_KP_ENTER to KeyCodes.KEY_KP_ENTER,
            GLFW_KEY_KP_EQUAL to KeyCodes.KEY_KP_EQUAL,
            GLFW_KEY_LEFT_SHIFT to KeyCodes.KEY_LEFT_SHIFT,
            GLFW_KEY_LEFT_CONTROL to KeyCodes.KEY_LEFT_CONTROL,
            GLFW_KEY_LEFT_ALT to KeyCodes.KEY_LEFT_ALT,
            GLFW_KEY_LEFT_SUPER to KeyCodes.KEY_LEFT_SUPER,
            GLFW_KEY_RIGHT_SHIFT to KeyCodes.KEY_RIGHT_SHIFT,
            GLFW_KEY_RIGHT_CONTROL to KeyCodes.KEY_RIGHT_CONTROL,
            GLFW_KEY_RIGHT_ALT to KeyCodes.KEY_RIGHT_ALT,
            GLFW_KEY_RIGHT_SUPER to KeyCodes.KEY_RIGHT_SUPER,
            GLFW_KEY_MENU to KeyCodes.KEY_MENU,
            GLFW_KEY_LAST to KeyCodes.KEY_LAST,


            GLFW_MOUSE_BUTTON_1 to KeyCodes.MOUSE_BUTTON_1,
            GLFW_MOUSE_BUTTON_2 to KeyCodes.MOUSE_BUTTON_2,
            GLFW_MOUSE_BUTTON_3 to KeyCodes.MOUSE_BUTTON_3,
            GLFW_MOUSE_BUTTON_4 to KeyCodes.MOUSE_BUTTON_4,
            GLFW_MOUSE_BUTTON_5 to KeyCodes.MOUSE_BUTTON_5,
            GLFW_MOUSE_BUTTON_6 to KeyCodes.MOUSE_BUTTON_6,
            GLFW_MOUSE_BUTTON_7 to KeyCodes.MOUSE_BUTTON_7,
            GLFW_MOUSE_BUTTON_8 to KeyCodes.MOUSE_BUTTON_8,
            GLFW_MOUSE_BUTTON_LAST to KeyCodes.MOUSE_BUTTON_LAST,
            GLFW_MOUSE_BUTTON_LEFT to KeyCodes.MOUSE_BUTTON_LEFT,
            GLFW_MOUSE_BUTTON_RIGHT to KeyCodes.MOUSE_BUTTON_RIGHT,
            GLFW_MOUSE_BUTTON_MIDDLE to KeyCodes.MOUSE_BUTTON_MIDDLE,
        )

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
        val CursorShapes.glfw: Int
            get() {
                return when (this) {
                    CursorShapes.ARROW -> GLFW_ARROW_CURSOR
                    CursorShapes.IBEAM -> GLFW_IBEAM_CURSOR
                    CursorShapes.CROSSHAIR -> GLFW_CROSSHAIR_CURSOR
                    CursorShapes.HAND -> GLFW_HAND_CURSOR
                    CursorShapes.HORIZONTAL_RESIZE -> GLFW_HRESIZE_CURSOR
                    CursorShapes.VERTICAL_RESIZE -> GLFW_VRESIZE_CURSOR
                }
            }
    }
}
