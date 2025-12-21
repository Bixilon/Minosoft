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
import de.bixilon.kutil.observer.DataObserver.Companion.observed
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
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.window.CursorModes
import de.bixilon.minosoft.gui.rendering.system.window.CursorShapes
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.system.window.Window
import de.bixilon.minosoft.gui.rendering.system.window.sdl.SdlUtil.MOUSE_CODE_MAPPING
import de.bixilon.minosoft.terminal.RunConfiguration
import org.lwjgl.opengl.GL
import org.lwjgl.sdl.*
import org.lwjgl.sdl.SDLEvents.*
import org.lwjgl.sdl.SDLInit.*
import org.lwjgl.sdl.SDLProperties.*
import org.lwjgl.sdl.SDLVideo.*
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer

class SdlWindow(
    val context: RenderContext,
) : Window {
    private var mouse = Vec2f.EMPTY
    private var skipMouse = false

    override val systemScale by observed(Vec2f(1.0f))
    override var size by observed(Window.DEFAULT_WINDOW_SIZE)
    override var minSize = Window.DEFAULT_MINIMUM_WINDOW_SIZE
    override var maxSize = Window.DEFAULT_MAXIMUM_WINDOW_SIZE
    override var visible = false
    override var resizable = true
    override var fullscreen = false
    override var swapInterval = 1
    override var cursorMode = CursorModes.NORMAL
    override var cursorShape = CursorShapes.ARROW
    override var title = RunConfiguration.APPLICATION_NAME
    override val iconified by observed(false)
    override var focused by observed(true)

    private var window = -1L


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

        SDL_GL_SetAttribute(SDL_GL_CONTEXT_FLAGS, SDL_GL_CONTEXT_DEBUG_FLAG)
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 3)
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 3)
        SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER, 1)
        SDL_GL_SetAttribute(SDL_GL_DEPTH_SIZE, 0)
        SDL_GL_SetAttribute(SDL_GL_ALPHA_SIZE, 0)
        SDL_GL_SetAttribute(SDL_GL_STENCIL_SIZE, 0)


        val window = SDL_CreateWindowWithProperties(properties)
        SDL_DestroyProperties(properties)
        if (window == MemoryUtil.NULL) throw Exception("null")
        this.window = window

        // val device = SDL_CreateGPUDevice(SDL_GPU_SHADERFORMAT_SPIRV, false, null as ByteBuffer?)
        // if (device == MemoryUtil.NULL) throw Exception("null")
        // assert(SDL_ClaimWindowForGPUDevice(device, window))


        val context = SDL_GL_CreateContext(window)
        SDL_GL_LoadLibrary(null as ByteBuffer?)

        GL.create(SDLVideo::SDL_GL_GetProcAddress)



        super.init(profile)
    }

    override fun destroy() {
    }

    override fun close() {
    }

    override fun forceClose() {
    }

    override fun begin() {
        SDL_GL_SetSwapInterval(1)
        SDL_ShowWindow(window)
    }

    override fun end() {
        SDL_GL_SwapWindow(window)
    }

    private fun onClose(event: SDL_CommonEvent) {
        fire(WindowCloseEvent(context, window = this))
    }

    private fun onResize(event: SDL_WindowEvent) {
        this.size = Vec2i(event.data1(), event.data2())
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
        if (action == KeyChangeTypes.PRESS || action == KeyChangeTypes.REPEAT) {
            fire(CharInputEvent(context, char = event.key()))
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

    private fun handleEvent(event: SDL_Event) = when (event.type()) {
        SDL_EVENT_QUIT -> onClose(event.common())
        SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED, SDL_EVENT_WINDOW_METAL_VIEW_RESIZED -> onResize(event.window())
        SDL_EVENT_MOUSE_MOTION -> onMouseMove(event.motion())
        SDL_EVENT_KEY_DOWN, SDL_EVENT_KEY_UP -> onKeyInput(event.key())
        SDL_EVENT_MOUSE_BUTTON_DOWN, SDL_EVENT_MOUSE_BUTTON_UP -> onMouseKeyInput(event.button())
        SDL_EVENT_MOUSE_WHEEL -> onScroll(event.wheel())
        SDL_EVENT_WINDOW_FOCUS_LOST, SDL_EVENT_WINDOW_FOCUS_GAINED -> onFocusChange(event.window(), event.type() == SDL_EVENT_WINDOW_FOCUS_GAINED)
        else -> Unit
    }

    override fun pollEvents() = stackPush().use {
        val event = SDL_Event.calloc(it)
        while (SDL_PollEvent(event)) {
            handleEvent(event)
        }
    }

    override fun setIcon(buffer: TextureBuffer) {
    }


    private fun fire(event: RenderEvent): Boolean {
        if (Rendering.currentContext != event.context) {
            event.context.queue += { event.context.session.events.fire(event) }
            return false
        }
        return context.session.events.fire(event)
    }

    companion object {

        init {
            assert(SDL_SetAppMetadata("Minosoft", RunConfiguration.APPLICATION_NAME, "de.bixilon.minosoft"))
            assert(SDL_Init(SDL_INIT_VIDEO))
        }
    }
}
