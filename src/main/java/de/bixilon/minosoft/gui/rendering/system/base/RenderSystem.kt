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

package de.bixilon.minosoft.gui.rendering.system.base

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.FloatUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.IntUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.FloatVertexBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.nio.ByteBuffer
import java.nio.FloatBuffer

interface RenderSystem {
    val shaders: MutableSet<Shader>
    val vendor: GPUVendor
    var shader: Shader?
    var framebuffer: Framebuffer?

    val active: Boolean

    fun init()
    fun destroy()

    fun reset(
        depthTest: Boolean = true,
        blending: Boolean = false,
        faceCulling: Boolean = true,
        polygonOffset: Boolean = false,
        depthMask: Boolean = true,
        sourceRGB: BlendingFunctions = BlendingFunctions.ONE,
        destinationRGB: BlendingFunctions = BlendingFunctions.ONE_MINUS_SOURCE_ALPHA,
        sourceAlpha: BlendingFunctions = BlendingFunctions.ONE,
        destinationAlpha: BlendingFunctions = BlendingFunctions.ZERO,
        depth: DepthFunctions = DepthFunctions.LESS,
        clearColor: RGBColor = Colors.TRANSPARENT,
        polygonOffsetFactor: Float = 0.0f,
        polygonOffsetUnit: Float = 0.0f,
    ) {
        setBlendFunction(sourceRGB, destinationRGB, sourceAlpha, destinationAlpha)
        this[RenderingCapabilities.DEPTH_TEST] = depthTest
        this[RenderingCapabilities.BLENDING] = blending
        this[RenderingCapabilities.FACE_CULLING] = faceCulling
        this[RenderingCapabilities.POLYGON_OFFSET] = polygonOffset
        this.depth = depth
        this.depthMask = depthMask
        this.clearColor = clearColor
        shader = null
        polygonOffset(polygonOffsetFactor, polygonOffsetUnit)
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

    var clearColor: RGBColor

    var preferredPrimitiveType: PrimitiveTypes
    var primitiveMeshOrder: Array<Pair<Int, Int>>

    fun readPixels(start: Vec2i, end: Vec2i, type: PixelTypes): ByteBuffer


    fun createShader(resourceLocation: ResourceLocation): Shader {
        return createShader(
            vertex = "$resourceLocation.vsh".toResourceLocation(),
            geometry = "$resourceLocation.gsh".toResourceLocation(),
            fragment = "$resourceLocation.fsh".toResourceLocation(),
        )
    }

    fun createShader(vertex: ResourceLocation, geometry: ResourceLocation? = null, fragment: ResourceLocation): Shader

    fun createVertexBuffer(structure: MeshStruct, data: FloatBuffer, primitiveType: PrimitiveTypes = preferredPrimitiveType): FloatVertexBuffer
    fun createIntUniformBuffer(data: IntArray = IntArray(0)): IntUniformBuffer
    fun createFloatUniformBuffer(data: FloatBuffer): FloatUniformBuffer
    fun createFramebuffer(): Framebuffer

    fun createTextureManager(): TextureManager

    fun clear(vararg buffers: IntegratedBufferTypes)

    fun getErrors(): List<RenderSystemError>


    fun polygonOffset(factor: Float, unit: Float)


    companion object {

        fun createRenderSystem(renderWindow: RenderWindow): RenderSystem {
            return OpenGLRenderSystem(renderWindow)
        }
    }
}
