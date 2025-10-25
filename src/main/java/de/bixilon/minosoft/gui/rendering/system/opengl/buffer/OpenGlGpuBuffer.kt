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

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer

import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.system.base.buffer.GpuBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.GpuBufferStates
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import de.bixilon.minosoft.gui.rendering.system.opengl.error.MemoryLeakException
import org.lwjgl.opengl.GL15.*

abstract class OpenGlGpuBuffer(
    protected var system: OpenGlRenderSystem,
) : GpuBuffer {
    override var state: GpuBufferStates = GpuBufferStates.PREPARING

    protected var id: Int = -1
        private set

    protected abstract val glType: Int

    override fun init() {
        if (this.state != GpuBufferStates.PREPARING) throw IllegalStateException("Already initialized (buffer=$this, state=$state)")
        system.log { "Init gpu buffer $this" }
        id = gl { glGenBuffers() }
        initialUpload()
    }

    protected abstract fun initialUpload()

    open fun bind() {
        if (system.boundBuffer[glType] == id) {
            return
        }
        gl { glBindBuffer(glType, id) }
        system.boundBuffer[glType] = id
    }

    open fun unbind() {
        if (this.state != GpuBufferStates.INITIALIZED) throw IllegalStateException("Not uploaded (buffer=$this, state=$state)")
        if (RenderConstants.DIRTY_BUFFER_UNBIND) {
            // This is unclean, yes. But it is not required to do at all (we always bind another buffer), so this saves a ton of gl calls
            return
        }
        gl { glBindBuffer(glType, -1) }
        system.boundBuffer -= glType
    }

    override fun unload() {
        if (this.state != GpuBufferStates.INITIALIZED) throw IllegalStateException("Not uploaded (buffer=$this, state=$state)")
        gl { glDeleteBuffers(id) }
        if (system.boundBuffer[glType] == id) {
            system.boundBuffer -= glType
        }
        id = -1
        state = GpuBufferStates.UNLOADED
    }

    protected fun finalize() {
        if (state == GpuBufferStates.INITIALIZED && system.active) {
            throw MemoryLeakException("Buffer has not been unloaded!")
        }
    }
}
