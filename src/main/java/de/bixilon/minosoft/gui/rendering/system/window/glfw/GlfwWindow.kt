/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.kutil.os.OSTypes
import de.bixilon.kutil.os.PlatformInfo
import de.bixilon.kutil.primitive.IntUtil.toHex
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfile
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.Rendering
import de.bixilon.minosoft.gui.rendering.events.RenderEvent
import de.bixilon.minosoft.gui.rendering.events.WindowCloseEvent
import de.bixilon.minosoft.gui.rendering.events.input.CharInputEvent
import de.bixilon.minosoft.gui.rendering.events.input.KeyInputEvent
import de.bixilon.minosoft.gui.rendering.events.input.MouseMoveEvent
import de.bixilon.minosoft.gui.rendering.events.input.MouseScrollEvent
import de.bixilon.minosoft.gui.rendering.system.window.CursorModes
import de.bixilon.minosoft.gui.rendering.system.window.CursorShapes
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.system.window.Window
import de.bixilon.minosoft.gui.rendering.system.window.Window.Companion.DEFAULT_MAXIMUM_WINDOW_SIZE
import de.bixilon.minosoft.gui.rendering.system.window.Window.Companion.DEFAULT_MINIMUM_WINDOW_SIZE
import de.bixilon.minosoft.gui.rendering.system.window.Window.Companion.DEFAULT_WINDOW_SIZE
import de.bixilon.minosoft.gui.rendering.system.window.glfw.GlfwUtil.KEY_CODE_MAPPING
import de.bixilon.minosoft.gui.rendering.system.window.glfw.GlfwUtil.glfw
import de.bixilon.minosoft.gui.rendering.system.window.glfw.GlfwUtil.scalePosition
import de.bixilon.minosoft.gui.rendering.system.window.glfw.GlfwUtil.unscalePosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2dUtil.EMPTY
import de.bixilon.minosoft.modding.event.master.AbstractEventMaster
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.delegate.RenderingDelegate.observeRendering
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec2.Vec2
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.system.Configuration
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer


class GlfwWindow(
    private val context: RenderContext,
    private val eventMaster: AbstractEventMaster = context.session.events,
) : Window {
    private var mousePosition = Vec2d.EMPTY
    private var skipNextMouseEvent = true
    private var window = -1L


    // TODO: on poll events get correct window and fire event there

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

    override var size by observed(DEFAULT_WINDOW_SIZE)

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
                glfwSetWindowMonitor(window, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate())
            } else {
                val size = scalePosition(Vec2i(DEFAULT_WINDOW_SIZE.x, DEFAULT_WINDOW_SIZE.y))
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
        if (context.preferQuads) {
            // yes, this is dirty. for using a geometry shader we need 3.3+. The thing is 3.3+ does not allow us to use GL_QUAD.
            // we can still bind it to a lower version and use features that need a more recent version of opengl.
            // most drivers allow us to do this, if not it'll crash
            setOpenGLVersion(3, 0, false)
        } else {
            setOpenGLVersion(3, 3, true)
        }
        glfwWindowHint(GLFW_VISIBLE, false.glfw)
        if (PlatformInfo.OS != OSTypes.MAC) {
            // Somehow apple does not like the value 0. Setting it to GLFW_DONT_CARE does not help. See https://github.com/Bixilon/Minosoft/issues/40
            glfwWindowHint(GLFW_ALPHA_BITS, 0)
            glfwWindowHint(GLFW_DEPTH_BITS, 0)
            glfwWindowHint(GLFW_STENCIL_BITS, 0)
        }


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


        window = glfwCreateWindow(size.x, size.y, RunConfiguration.APPLICATION_NAME, MemoryUtil.NULL, MemoryUtil.NULL)
        if (window == MemoryUtil.NULL) {
            try {
                destroy()
            } catch (ignored: Throwable) {
            }
            throw IllegalStateException("Failed to create the GLFW window. Check the console for details. BEFORE opening any issue check that your GPU supports OpenGL 3.3+ and the most recent drivers are installed!")
        }

        log { "Created window (window=$window)" }

        glfwMakeContextCurrent(window)

        super.init(profile)
        this::size.observeRendering(this) { glfwSetWindowSize(window, it.x, it.y) }
        val size = scalePosition(Vec2i(DEFAULT_WINDOW_SIZE.x, DEFAULT_WINDOW_SIZE.y))
        this.size = size

        val primaryMonitor = glfwGetPrimaryMonitor()
        if (primaryMonitor != MemoryUtil.NULL) {
            glfwGetVideoMode(primaryMonitor)?.let {
                glfwSetWindowPos(window, (it.width() - size.x) / 2, (it.height() - size.y) / 2)
            }
        }

        initCallbacks()

        onWindowScale(window, getWindowScale())
    }

    private fun initCallbacks() {
        glfwSetKeyCallback(window, this::onKeyInput)
        glfwSetMouseButtonCallback(window, this::onMouseKeyInput)

        glfwSetCharCallback(window, this::onCharInput)
        glfwSetCursorPosCallback(window, this::onMouseMove)

        glfwSetWindowSizeCallback(window, this::onResize)

        glfwSetWindowCloseCallback(window, this::onClose)
        glfwSetWindowFocusCallback(window, this::onFocusChange)
        glfwSetWindowIconifyCallback(window, this::onIconify)
        glfwSetScrollCallback(window, this::onScroll)
        glfwSetWindowContentScaleCallback(window, this::onWindowScale)
    }

    override fun destroy() {
        if (window != MemoryUtil.NULL) {
            glfwFreeCallbacks(window)
            glfwDestroyWindow(window)
        }

        glfwSetErrorCallback(null)?.free()
    }

    override fun close() {
        if (fireGLFWEvent(WindowCloseEvent(context, window = this))) {
            return
        }
        forceClose()
    }

    override fun forceClose() {
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


    override var systemScale by observed(Vec2(1.0f))


    private fun getWindowScale(): Vec2 {
        val x = FloatArray(1)
        val y = FloatArray(1)
        glfwGetWindowContentScale(window, x, y)

        return Vec2(x[0], y[0])
    }

    private fun onWindowScale(window: Long, x: Float, y: Float) = onWindowScale(window, Vec2(x, y))

    private fun onWindowScale(window: Long, scale: Vec2) {
        log { "Window scale (window=$window, scale=$scale)" }

        if (window != this.window) return
        if (this.systemScale == scale) return

        this.systemScale = scale

        apply {
            val x = IntArray(1)
            val y = IntArray(1)
            glfwGetWindowSize(window, x, y)
            onResize(window, x[0], y[0])
        }
        apply {
            val x = DoubleArray(1)
            val y = DoubleArray(1)
            glfwGetCursorPos(window, x, y)
            onMouseMove(window, x[0], y[0])
        }
    }

    override var focused by observed(false)

    private fun onFocusChange(window: Long, focused: Boolean) {
        log { "Focus (window=$window, focused=$focused)" }
        if (window != this.window) return
        this.focused = focused
    }

    override var iconified by observed(false)

    private fun onIconify(window: Long, iconified: Boolean) {
        log { "Iconify (window=$window, iconified=$iconified)" }
        if (window != this.window) return
        this.iconified = iconified
    }

    private fun onClose(window: Long) {
        log { "Close (window=$window)" }
        if (window != this.window) return
        val cancelled = fireGLFWEvent(WindowCloseEvent(context, window = this))

        if (cancelled) {
            glfwSetWindowShouldClose(window, false)
        }
    }

    private fun onResize(window: Long, width: Int, height: Int) {
        log { "Resize (window=$window, width=$width, height=$height)" }
        if (window != this.window) return

        val nextSize = unscalePosition(Vec2i(width, height))
        if (nextSize.x <= 0 || nextSize.y <= 0) return  // windows returns size (0,0) if minimized
        if (this.size == nextSize) return
        this.size = nextSize
        this.skipNextMouseEvent = true
    }

    private fun onMouseKeyInput(windowId: Long, button: Int, action: Int, modifierKey: Int) {
        onKeyInput(windowId, button, 0, action, modifierKey)
    }

    private fun onKeyInput(window: Long, key: Int, char: Int, action: Int, modifierKey: Int) {
        log { "Key input (window=$window, key=$key, char=$char, action=$action)" }
        if (window != this.window) return

        val keyCode = KEY_CODE_MAPPING[key] ?: KeyCodes.KEY_UNKNOWN

        val keyAction = when (action) {
            GLFW_PRESS -> KeyChangeTypes.PRESS
            GLFW_RELEASE -> KeyChangeTypes.RELEASE
            GLFW_REPEAT -> KeyChangeTypes.REPEAT
            else -> {
                Log.log(LogMessageType.RENDERING, LogLevels.WARN) { "Unknown glfw action $action" }
                return
            }
        }

        fireGLFWEvent(KeyInputEvent(context, code = keyCode, change = keyAction))
    }

    private fun onCharInput(windowId: Long, char: Int) {
        log { "Char input (window=$window, char=${char.toHex()})" }
        if (windowId != window) return
        fireGLFWEvent(CharInputEvent(context, char = char))
    }

    private fun onMouseMove(windowId: Long, x: Double, y: Double) {
        log { "Mouse move (window=$window, x=$x, y=$y)" }
        if (windowId != window) return

        val position = unscalePosition(Vec2d(x, y))
        val previous = this.mousePosition
        val delta = position - previous
        this.mousePosition = position
        if (!skipNextMouseEvent) {
            fireGLFWEvent(MouseMoveEvent(context, position = position, previous = previous, delta = delta))
        } else {
            skipNextMouseEvent = false
        }
    }

    private fun onScroll(window: Long, xOffset: Double, yOffset: Double) {
        log { "Scroll (window=$window, x=$xOffset, y=$yOffset)" }
        if (window != this.window) return
        fireGLFWEvent(MouseScrollEvent(context, offset = Vec2d(xOffset, yOffset)))
    }

    override fun setIcon(size: Vec2i, buffer: ByteBuffer) {
        if (PlatformInfo.OS == OSTypes.MAC) {
            return // the window icon can just be set with the TaskBar api. See SystemUtil for more information
        }
        val images = GLFWImage.malloc(1)
        val image = GLFWImage.malloc()
        image.set(size.x, size.y, buffer)
        images.put(0, image)
        glfwSetWindowIcon(window, images)
    }

    private fun fireGLFWEvent(event: RenderEvent): Boolean {
        // ToDo: It looks like glfwPollEvents is mixing threads. This should not happen.
        if (Rendering.currentContext != event.context) {
            event.context.queue += { eventMaster.fire(event) }
            return false
        }
        return eventMaster.fire(event)
    }

    companion object {
        private val initLatch = SimpleLatch(1)

        init {
            if (PlatformInfo.OS == OSTypes.MAC) {
                Configuration.GLFW_LIBRARY_NAME.set("glfw_async")
            }
            DefaultThreadPool += {
                log { "Initializing library..." }
                GLFWErrorCallback.createPrint(Log.FATAL_PRINT_STREAM).set()
                if (PlatformInfo.OS == OSTypes.LINUX) {
                    glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_X11)
                }
                check(glfwInit()) { "Unable to initialize GLFW" }
                log { "Initialized library!" }
                initLatch.dec()
            }
        }


        inline fun log(builder: () -> Any?) {
            if (!RunConfiguration.VERBOSE_LOGGING) return
            Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "[GLFW] ${builder.invoke()}" }
        }
    }
}
