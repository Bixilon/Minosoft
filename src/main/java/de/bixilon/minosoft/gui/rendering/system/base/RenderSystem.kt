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

package de.bixilon.minosoft.gui.rendering.system.base

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.depth.DepthModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.stencil.StencilModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.texture.TextureModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.FloatUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.IntUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.VertexBuffer
import de.bixilon.minosoft.gui.rendering.system.base.settings.RenderSettings
import de.bixilon.minosoft.gui.rendering.system.base.shader.ShaderManagement
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct
import java.nio.FloatBuffer
import java.nio.IntBuffer

interface RenderSystem {
    val shader: ShaderManagement
    val vendor: GPUVendor
    var framebuffer: Framebuffer?

    val active: Boolean

    fun init()
    fun destroy()

    fun reset() = set(RenderSettings.DEFAULT)

    fun reset(
        depthTest: Boolean = RenderSettings.DEFAULT.depthTest,
        blending: Boolean = RenderSettings.DEFAULT.blending,
        faceCulling: Boolean = RenderSettings.DEFAULT.faceCulling,
        polygonOffset: Boolean = RenderSettings.DEFAULT.polygonOffset,
        depthMask: Boolean = RenderSettings.DEFAULT.depthMask,
        sourceRGB: BlendingFunctions = RenderSettings.DEFAULT.sourceRGB,
        destinationRGB: BlendingFunctions = RenderSettings.DEFAULT.destinationRGB,
        sourceAlpha: BlendingFunctions = RenderSettings.DEFAULT.sourceAlpha,
        destinationAlpha: BlendingFunctions = RenderSettings.DEFAULT.destinationAlpha,
        depth: DepthFunctions = RenderSettings.DEFAULT.depth,
        clearColor: RGBAColor = RenderSettings.DEFAULT.clearColor,
        polygonOffsetFactor: Float = RenderSettings.DEFAULT.polygonOffsetFactor,
        polygonOffsetUnit: Float = RenderSettings.DEFAULT.polygonOffsetUnit,
    ) {
        setBlendFunction(sourceRGB, destinationRGB, sourceAlpha, destinationAlpha)
        this[RenderingCapabilities.DEPTH_TEST] = depthTest
        this[RenderingCapabilities.BLENDING] = blending
        this[RenderingCapabilities.FACE_CULLING] = faceCulling
        this[RenderingCapabilities.POLYGON_OFFSET] = polygonOffset
        this.depth = depth
        this.depthMask = depthMask
        this.clearColor = clearColor
        // shader = null
        polygonOffset(polygonOffsetFactor, polygonOffsetUnit)
    }

    fun set(settings: RenderSettings) {
        setBlendFunction(settings.sourceRGB, settings.destinationRGB, settings.sourceAlpha, settings.destinationAlpha)
        this[RenderingCapabilities.DEPTH_TEST] = settings.depthTest
        this[RenderingCapabilities.BLENDING] = settings.blending
        this[RenderingCapabilities.FACE_CULLING] = settings.faceCulling
        this[RenderingCapabilities.POLYGON_OFFSET] = settings.polygonOffset
        this.depth = settings.depth
        this.depthMask = settings.depthMask
        this.clearColor = settings.clearColor
        // shader = null
        polygonOffset(settings.polygonOffsetFactor, settings.polygonOffsetUnit)
    }

    fun enable(capability: RenderingCapabilities)
    fun disable(capability: RenderingCapabilities)
    operator fun set(capability: RenderingCapabilities, status: Boolean)
    operator fun get(capability: RenderingCapabilities): Boolean

    operator fun set(source: BlendingFunctions, destination: BlendingFunctions)

    fun setBlendFunction(sourceRGB: BlendingFunctions = BlendingFunctions.ONE, destinationRGB: BlendingFunctions = BlendingFunctions.ONE_MINUS_SOURCE_ALPHA, sourceAlpha: BlendingFunctions = BlendingFunctions.ONE, destinationAlpha: BlendingFunctions = BlendingFunctions.ZERO)

    var depth: DepthFunctions
    var depthMask: Boolean

    var polygonMode: PolygonModes


    val usedVRAM: Long
    val availableVRAM: Long
    val maximumVRAM: Long

    val vendorString: String
    val version: String
    val gpuType: String

    var clearColor: RGBAColor

    var viewport: Vec2i

    fun readPixels(start: Vec2i, size: Vec2i): TextureBuffer


    fun createVertexBuffer(struct: MeshStruct, data: FloatBuffer, primitive: PrimitiveTypes, index: IntBuffer? = null, reused: Boolean = false): VertexBuffer

    fun createIntUniformBuffer(data: IntBuffer): IntUniformBuffer
    fun createFloatUniformBuffer(data: FloatBuffer): FloatUniformBuffer
    fun createFramebuffer(size: Vec2i, scale: Float, texture: TextureModes? = null, depth: DepthModes? = null, stencil: StencilModes? = null): Framebuffer

    fun createTextureManager(): TextureManager

    fun clear(vararg buffers: IntegratedBufferTypes)

    @Deprecated("There should not be any errors, or they should directly crash the render system")
    fun getErrors(): List<RenderSystemError>


    fun polygonOffset(factor: Float, unit: Float)

    fun resetBlending() {
        disable(RenderingCapabilities.BLENDING)
        setBlendFunction(BlendingFunctions.ONE, BlendingFunctions.ONE_MINUS_SOURCE_ALPHA, BlendingFunctions.ONE, BlendingFunctions.ZERO)
    }
}
