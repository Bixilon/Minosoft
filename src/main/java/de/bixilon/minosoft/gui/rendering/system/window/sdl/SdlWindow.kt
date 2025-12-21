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

package de.bixilon.minosoft.gui.rendering.system.window.sdl

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.Minosoft
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
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.RGB8Buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.RGBA8Buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.gui.rendering.system.window.CursorModes
import de.bixilon.minosoft.gui.rendering.system.window.CursorShapes
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.system.window.Window
import de.bixilon.minosoft.gui.rendering.system.window.Window.Companion.DEFAULT_WINDOW_SIZE
import de.bixilon.minosoft.gui.rendering.system.window.sdl.SdlUtil.MOUSE_CODE_MAPPING
import de.bixilon.minosoft.gui.rendering.system.window.sdl.SdlUtil.sdl3
import de.bixilon.minosoft.gui.rendering.system.window.sdl.api.GlSdlApi
import de.bixilon.minosoft.gui.rendering.system.window.sdl.api.SdlWindowRenderApi
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.delegate.RenderingDelegate.observeRendering
import org.lwjgl.sdl.*
import org.lwjgl.sdl.SDLEvents.*
import org.lwjgl.sdl.SDLInit.*
import org.lwjgl.sdl.SDLKeyboard.SDL_StartTextInput
import org.lwjgl.sdl.SDLMouse.*
import org.lwjgl.sdl.SDLPixels.SDL_PIXELFORMAT_RGB24
import org.lwjgl.sdl.SDLPixels.SDL_PIXELFORMAT_RGBA8888
import org.lwjgl.sdl.SDLProperties.*
import org.lwjgl.sdl.SDLSurface.SDL_SURFACE_PREALLOCATED
import org.lwjgl.sdl.SDLVideo.*
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil

class SdlWindow(
    val context: RenderContext,
) : Window {
    private var mouse = Vec2f.EMPTY
    private var skipMouse = false

    private var _size = DEFAULT_WINDOW_SIZE
    override var size by observed(DEFAULT_WINDOW_SIZE)
    override var minSize = Window.DEFAULT_MINIMUM_WINDOW_SIZE
        set(value) {
            if (field == value) return
            field = value
            SDL_SetWindowMinimumSize(window, value.x, value.y)
        }
    override var maxSize = Window.DEFAULT_MAXIMUM_WINDOW_SIZE
        set(value) {
            if (field == value) return
            field = value
            SDL_SetWindowMaximumSize(window, value.x, value.y)
        }
    override var visible = false
        set(value) {
            if (field == value) return
            field = value
            if (value) {
                SDL_ShowWindow(window)
            } else {
                SDL_HideWindow(window)
            }
        }
    override var resizable = true
        set(value) {
            if (field == value) return
            field = value
            SDL_SetWindowResizable(window, value)
        }
    override var fullscreen = false
        set(value) {
            if (field == value) return
            field = value
            SDL_SetWindowFullscreen(window, value)
        }
    override var swapInterval
        get() = api.swapInterval
        set(value) {
            api.swapInterval = value
        }
    override var cursorMode: CursorModes = CursorModes.NORMAL
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value == CursorModes.NORMAL) {
                SDL_ShowCursor()
            } else {
                SDL_HideCursor()
            }
            SDL_CaptureMouse(value != CursorModes.NORMAL)
            skipMouse = true
        }
    override var cursorShape: CursorShapes = CursorShapes.ARROW
        set(value) {
            if (field == value) {
                return
            }
            field = value
            val cursor = SDL_CreateSystemCursor(value.sdl3)
            SDL_SetCursor(cursor)
        }
    override var title = RunConfiguration.APPLICATION_NAME
        set(value) {
            if (field == value) {
                return
            }
            field = value
            SDL_SetWindowTitle(window, value)
        }
    override var focused by observed(true)

    private var window = -1L
    private lateinit var api: SdlWindowRenderApi


    @Deprecated("unsupported")
    override val systemScale by observed(Vec2f(1.0f))

    @Deprecated("unsupported")
    override val iconified by observed(false)


    override fun init(profile: RenderingProfile) {
        val properties = SDL_CreateProperties()

        SDL_SetNumberProperty(properties, SDL_PROP_WINDOW_CREATE_X_NUMBER, SDL_WINDOWPOS_CENTERED.toLong())
        SDL_SetNumberProperty(properties, SDL_PROP_WINDOW_CREATE_Y_NUMBER, SDL_WINDOWPOS_CENTERED.toLong())
        SDL_SetNumberProperty(properties, SDL_PROP_WINDOW_CREATE_WIDTH_NUMBER, size.x.toLong())
        SDL_SetNumberProperty(properties, SDL_PROP_WINDOW_CREATE_HEIGHT_NUMBER, size.y.toLong())
        SDL_SetStringProperty(properties, SDL_PROP_WINDOW_CREATE_TITLE_STRING, title)

        SDL_SetBooleanProperty(properties, SDL_PROP_WINDOW_CREATE_OPENGL_BOOLEAN, true)
        SDL_SetBooleanProperty(properties, SDL_PROP_WINDOW_CREATE_HIDDEN_BOOLEAN, true)
        SDL_SetBooleanProperty(properties, SDL_PROP_WINDOW_CREATE_RESIZABLE_BOOLEAN, true)



        val window = SDL_CreateWindowWithProperties(properties)
        SDL_DestroyProperties(properties)
        if (window == MemoryUtil.NULL) throw Exception("null")
        this.window = window

        this.api = when (context.system) {
            is OpenGlRenderSystem -> GlSdlApi(window)
            else -> throw UnsupportedOperationException("SDL3 does not support using ${context.system} render system!")
        }
        api.init()


        SDL_StartTextInput(window)

        this::size.observeRendering(this) {
            if (_size == size) return@observeRendering
            SDL_SetWindowSize(window, it.x, it.y)
        }



        super.init(profile)
    }

    override fun destroy() {
        SDL_DestroyWindow(window)
        this.window = -1
        api.destroy()
    }

    override fun close() {
        if (fire(WindowCloseEvent(context, window = this))) {
            return
        }
        forceClose()
    }

    override fun forceClose() {
        SDL_PushEvent(SDL_Event.calloc().apply { this.type(SDL_EVENT_QUIT) })
    }

    override fun begin() {
        api.begin()
    }

    override fun end() {
        api.end()
    }

    private fun onClose(event: SDL_CommonEvent) {
        fire(WindowCloseEvent(context, window = this))
    }

    private fun onResize(event: SDL_WindowEvent) {
        val size = Vec2i(event.data1(), event.data2())
        this._size = size
        this.size = size
        this.skipMouse = true
    }

    private fun onFocusChange(event: SDL_WindowEvent, focused: Boolean) {
        this.focused = focused
    }

    private fun onMouseKeyInput(event: SDL_MouseButtonEvent) {
        val button = MOUSE_CODE_MAPPING[event.button().toInt()] ?: KeyCodes.UNKNOWN

        val action = when {
            event.down() -> KeyChangeTypes.PRESS
            else -> KeyChangeTypes.RELEASE
        }

        fire(KeyInputEvent(context, code = button, change = action))
    }

    private fun onKeyInput(event: SDL_KeyboardEvent) {
        val code = SdlUtil.KEY_CODE_MAPPING[event.scancode()] ?: KeyCodes.UNKNOWN

        val action = when {
            event.repeat() -> KeyChangeTypes.REPEAT
            event.down() -> KeyChangeTypes.PRESS
            else -> KeyChangeTypes.RELEASE
        }

        fire(KeyInputEvent(context, code = code, change = action))
    }

    private fun onCharInput(event: SDL_TextInputEvent) {
        val string = event.textString() ?: return
        val iterator = string.codePoints().iterator()
        while (iterator.hasNext()) {
            fire(CharInputEvent(context, char = iterator.nextInt()))
        }
    }

    private fun onMouseMove(event: SDL_MouseMotionEvent) {
        val position = Vec2f(event.x(), event.y())
        val delta = if (skipMouse) Vec2f.EMPTY else position - this.mouse
        this.mouse = position
        fire(MouseMoveEvent(context, position, delta))
        this.skipMouse = false
    }

    private fun onScroll(event: SDL_MouseWheelEvent) {
        val offset = Vec2f(event.x(), event.y())
        fire(MouseScrollEvent(context, offset = offset))
    }

    private fun onFullscreenChange(event: SDL_WindowEvent, fullscreen: Boolean) {
        this.fullscreen = fullscreen // TODO: skip setter
    }

    private fun onWindowVisibilityChange(event: SDL_WindowEvent, visible: Boolean) {
        this.visible = visible // TODO: skip setter
    }

    private fun handleEvent(event: SDL_Event) = when (event.type()) {
        SDL_EVENT_QUIT -> onClose(event.common())
        SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED, SDL_EVENT_WINDOW_METAL_VIEW_RESIZED -> onResize(event.window())
        SDL_EVENT_MOUSE_MOTION -> onMouseMove(event.motion())
        SDL_EVENT_KEY_DOWN, SDL_EVENT_KEY_UP -> onKeyInput(event.key())
        SDL_EVENT_MOUSE_BUTTON_DOWN, SDL_EVENT_MOUSE_BUTTON_UP -> onMouseKeyInput(event.button())
        SDL_EVENT_MOUSE_WHEEL -> onScroll(event.wheel())
        SDL_EVENT_WINDOW_FOCUS_LOST, SDL_EVENT_WINDOW_FOCUS_GAINED -> onFocusChange(event.window(), event.type() == SDL_EVENT_WINDOW_FOCUS_GAINED)
        SDL_EVENT_WINDOW_ENTER_FULLSCREEN, SDL_EVENT_WINDOW_LEAVE_FULLSCREEN -> onFullscreenChange(event.window(), event.type() == SDL_EVENT_WINDOW_ENTER_FULLSCREEN)
        SDL_EVENT_WINDOW_SHOWN, SDL_EVENT_WINDOW_HIDDEN -> onWindowVisibilityChange(event.window(), event.type() == SDL_EVENT_WINDOW_SHOWN)
        SDL_EVENT_TEXT_INPUT -> onCharInput(event.text())
        else -> Unit
    }

    override fun pollEvents() = stackPush().use {
        val event = SDL_Event.calloc(it)
        while (SDL_PollEvent(event)) {
            handleEvent(event)
        }
    }

    override fun setIcon(buffer: TextureBuffer) {
        val surface = SDL_Surface.calloc()
        surface.flags(SDL_SURFACE_PREALLOCATED)
        surface.w(buffer.size.x)
        surface.h(buffer.size.y)
        surface.pitch(buffer.components * buffer.size.x)
        surface.pixels(buffer.data)
        surface.format(when (buffer) {
            is RGB8Buffer -> SDL_PIXELFORMAT_RGB24
            is RGBA8Buffer -> SDL_PIXELFORMAT_RGBA8888
            else -> Broken("")
        })
        SDL_SetWindowIcon(window, surface)
    }


    private fun fire(event: RenderEvent): Boolean {
        if (Rendering.currentContext != event.context) {
            event.context.queue += { event.context.session.events.fire(event) }
            return false
        }
        return event.context.session.events.fire(event)
    }

    override fun toString() = "SdlWindow"


    companion object {

        init {
            assert(SDL_SetAppMetadata("Minosoft", RunConfiguration.APPLICATION_NAME, Minosoft.GROUP_ID))
            assert(SDL_Init(SDL_INIT_VIDEO))
        }
    }
}
