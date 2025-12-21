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

package de.bixilon.minosoft.gui.rendering.system.window.sdl3.api

import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.opengl.GL
import org.lwjgl.sdl.SDLVideo
import org.lwjgl.sdl.SDLVideo.*
import java.nio.ByteBuffer

class GlSdlApi(val window: Long) : SdlWindowRenderApi {
    var context = -1L

    override var swapInterval = 1
        set(value) {
            if (field == value) return
            field = value
            SDL_GL_SetSwapInterval(value)
        }

    override fun init() {
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_FLAGS, 0)
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 3)
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 3)
        SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER, 1)
        SDL_GL_SetAttribute(SDL_GL_DEPTH_SIZE, 0)
        SDL_GL_SetAttribute(SDL_GL_ALPHA_SIZE, 0)
        SDL_GL_SetAttribute(SDL_GL_STENCIL_SIZE, 0)

        this.context = SDL_GL_CreateContext(window)

        SDL_GL_SetSwapInterval(swapInterval)
    }

    override fun destroy() {
        SDL_GL_DestroyContext(context)
        this.context = -1
    }

    override fun begin() = Unit

    override fun end() {
        SDL_GL_SwapWindow(window)
    }

    companion object {

        init {
            OpenGlRenderSystem.init = {
                Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Loading SDL opengl library" }
                SDL_GL_LoadLibrary(null as ByteBuffer?)

                GL.create(SDLVideo::SDL_GL_GetProcAddress)
            }
        }
    }
}
