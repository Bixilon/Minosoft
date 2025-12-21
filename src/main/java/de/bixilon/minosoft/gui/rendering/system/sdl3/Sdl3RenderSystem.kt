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

package de.bixilon.minosoft.gui.rendering.system.sdl3

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.*
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.depth.DepthModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.stencil.StencilModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.texture.TextureModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.FloatUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.VertexBuffer
import de.bixilon.minosoft.gui.rendering.system.base.query.QueryTypes
import de.bixilon.minosoft.gui.rendering.system.base.query.RenderQuery
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.vendor.OtherOpenGlVendor
import de.bixilon.minosoft.gui.rendering.system.sdl3.shader.Sdl3ShaderManagement
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Sdl3RenderSystem(
    val context: RenderContext,
) : RenderSystem {
    override val shader = Sdl3ShaderManagement(this)
    override val vendor get() = OtherOpenGlVendor // TODO
    override var framebuffer: Framebuffer? = null
    override val primitives = PrimitiveTypes.set(PrimitiveTypes.POINT, PrimitiveTypes.LINE, PrimitiveTypes.TRIANGLE)
    override val active = true

    override fun init() {
    }

    override fun destroy() {
    }

    override fun enable(capability: RenderingCapabilities) {
    }

    override fun disable(capability: RenderingCapabilities) {
    }

    override fun set(capability: RenderingCapabilities, status: Boolean) {
    }

    override fun get(capability: RenderingCapabilities): Boolean {
        return true
    }

    override fun set(source: BlendingFunctions, destination: BlendingFunctions) {
    }

    override fun setBlendFunction(sourceRGB: BlendingFunctions, destinationRGB: BlendingFunctions, sourceAlpha: BlendingFunctions, destinationAlpha: BlendingFunctions) {
    }

    override var depth = DepthFunctions.LESS
    override var depthMask = true
    override var polygonMode = PolygonModes.FILL
    override val version = ""
    override val vendorString = ""
    override val gpuType = ""
    override var clearColor = ChatColors.BLACK
    override var viewport = Vec2i.EMPTY

    override fun readPixels(start: Vec2i, size: Vec2i): TextureBuffer {
        TODO("Not yet implemented")
    }

    override fun createVertexBuffer(struct: MeshStruct, data: FloatBuffer, primitive: PrimitiveTypes, index: IntBuffer?, reused: Boolean): VertexBuffer {
        TODO("Not yet implemented")
    }

    override fun createFloatUniformBuffer(data: FloatBuffer): FloatUniformBuffer {
        TODO("Not yet implemented")
    }

    override fun createFramebuffer(size: Vec2i, scale: Float, texture: TextureModes?, depth: DepthModes?, stencil: StencilModes?): Framebuffer {
        TODO("Not yet implemented")
    }

    override fun createQuery(type: QueryTypes): RenderQuery {
        TODO("Not yet implemented")
    }

    override fun createTextureManager(): TextureManager {
        TODO("Not yet implemented")
    }

    override fun clear(vararg buffers: IntegratedBufferTypes) {
    }

    override fun getErrors() = emptyList<RenderSystemError>()

    override fun polygonOffset(factor: Float, unit: Float) {
    }


    override fun toString() = "SDL3GPU"
}
