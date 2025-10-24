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

package de.bixilon.minosoft.gui.rendering.system.dummy

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.system.base.*
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.depth.DepthModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.stencil.StencilModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.texture.TextureModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.FloatUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.IntUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.FloatVertexBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.dummy.buffer.DummyFramebuffer
import de.bixilon.minosoft.gui.rendering.system.dummy.buffer.DummyVertexBuffer
import de.bixilon.minosoft.gui.rendering.system.dummy.buffer.uniform.DummyFloatUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.dummy.buffer.uniform.DummyIntUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.dummy.shader.DummyNativeShader
import de.bixilon.minosoft.gui.rendering.system.dummy.shader.DummyShaderManagement
import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTextureManager
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshOrder
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import java.nio.FloatBuffer

class DummyRenderSystem(
    val context: RenderContext,
) : RenderSystem {
    override val shader = DummyShaderManagement(this)
    override lateinit var vendor: GPUVendor
    override var framebuffer: Framebuffer? = null
    override val active: Boolean = true

    override var viewport = Vec2i.EMPTY

    override fun init() {
        vendor = DummyVendor
    }

    override fun destroy() = Unit

    override fun enable(capability: RenderingCapabilities) = Unit

    override fun disable(capability: RenderingCapabilities) = Unit

    override fun set(capability: RenderingCapabilities, status: Boolean) = Unit

    override fun set(source: BlendingFunctions, destination: BlendingFunctions) = Unit

    override fun get(capability: RenderingCapabilities): Boolean {
        return false
    }

    override fun setBlendFunction(sourceRGB: BlendingFunctions, destinationRGB: BlendingFunctions, sourceAlpha: BlendingFunctions, destinationAlpha: BlendingFunctions) = Unit

    override var depth: DepthFunctions = DepthFunctions.NOT_EQUAL
    override var depthMask: Boolean = true
    override var polygonMode: PolygonModes = PolygonModes.FILL
    override val usedVRAM: Long = -1
    override val availableVRAM: Long = -1
    override val maximumVRAM: Long = -1
    override val vendorString: String = "Bixilon's hand made super gpu"
    override val version: String = "dummy"
    override val gpuType: String = "dummy"
    override var clearColor = Colors.TRANSPARENT
    override var quadType: PrimitiveTypes = PrimitiveTypes.QUAD

    override var quadOrder: RenderOrder = if (quadType == PrimitiveTypes.QUAD) MeshOrder.QUAD else MeshOrder.TRIANGLE
    override var legacyQuadOrder: RenderOrder = if (quadType == PrimitiveTypes.QUAD) MeshOrder.LEGACY_QUAD else MeshOrder.LEGACY_TRIANGLE

    override fun readPixels(start: Vec2i, size: Vec2i): TextureBuffer {
        TODO("Not yet implemented")
    }

    override fun createVertexBuffer(struct: MeshStruct, data: FloatBuffer, primitive: PrimitiveTypes, index: IntArray?): FloatVertexBuffer {
        return DummyVertexBuffer(struct, data)
    }

    override fun createIntUniformBuffer(data: IntArray): IntUniformBuffer {
        return DummyIntUniformBuffer(data)
    }

    override fun createFloatUniformBuffer(data: FloatBuffer): FloatUniformBuffer {
        return DummyFloatUniformBuffer(data)
    }

    override fun createFramebuffer(size: Vec2i, scale: Float, texture: TextureModes?, depth: DepthModes?, stencil: StencilModes?): Framebuffer {
        return DummyFramebuffer(size, scale)
    }

    override fun createTextureManager(): TextureManager {
        return DummyTextureManager(context)
    }

    override fun clear(vararg buffers: IntegratedBufferTypes) = Unit

    override fun getErrors(): List<RenderSystemError> = emptyList()

    override fun polygonOffset(factor: Float, unit: Float) = Unit

    companion object : RenderSystemFactory {

        override fun create(context: RenderContext) = DummyRenderSystem(context)
    }
}
