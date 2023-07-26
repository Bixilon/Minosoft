/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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
import de.bixilon.kutil.collections.primitive.floats.AbstractFloatList
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.FloatUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.IntUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.FloatVertexBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.settings.RenderSettings
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.collections.floats.DirectArrayFloatList
import java.nio.ByteBuffer
import java.nio.FloatBuffer

interface RenderSystem {
    val nativeShaders: MutableSet<NativeShader>
    val shaders: MutableSet<Shader>
    val vendor: GPUVendor
    var shader: NativeShader?
    var framebuffer: Framebuffer?

    val active: Boolean

    fun init()
    fun destroy()

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
        clearColor: RGBColor = RenderSettings.DEFAULT.clearColor,
        polygonOffsetFactor: Float = RenderSettings.DEFAULT.polygonOffsetFactor,
        polygonOffsetUnit: Float = RenderSettings.DEFAULT.polygonOffsetUnit,
    ) {
        val settings = RenderSettings(depthTest, blending, faceCulling, polygonOffset, depthMask, sourceRGB, destinationRGB, sourceAlpha, destinationAlpha, depth, clearColor, polygonOffsetFactor, polygonOffsetUnit)
        this.set(settings)
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
        shader = null
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

    var clearColor: RGBColor

    var preferredPrimitiveType: PrimitiveTypes
    var primitiveMeshOrder: Array<Pair<Int, Int>>

    fun readPixels(start: Vec2i, end: Vec2i, type: PixelTypes): ByteBuffer


    fun createNativeShader(resourceLocation: ResourceLocation): NativeShader {
        return createNativeShader(
            vertex = "$resourceLocation.vsh".toResourceLocation(),
            geometry = "$resourceLocation.gsh".toResourceLocation(),
            fragment = "$resourceLocation.fsh".toResourceLocation(),
        )
    }

    fun <T : Shader> createShader(resourceLocation: ResourceLocation, creator: (native: NativeShader) -> T): T {
        return creator(createNativeShader(resourceLocation))
    }

    fun createNativeShader(vertex: ResourceLocation, geometry: ResourceLocation? = null, fragment: ResourceLocation): NativeShader

    fun createVertexBuffer(struct: MeshStruct, data: FloatBuffer, primitiveType: PrimitiveTypes = preferredPrimitiveType): FloatVertexBuffer
    fun createVertexBuffer(struct: MeshStruct, data: AbstractFloatList, primitiveType: PrimitiveTypes = preferredPrimitiveType): FloatVertexBuffer {
        if (data is DirectArrayFloatList) {
            return createVertexBuffer(struct, data.toBuffer(), primitiveType)
        }
        return createVertexBuffer(struct, FloatBuffer.wrap(data.toArray()), primitiveType)
    }

    fun createIntUniformBuffer(data: IntArray = IntArray(0)): IntUniformBuffer
    fun createFloatUniformBuffer(data: FloatBuffer): FloatUniformBuffer
    fun createFramebuffer(): Framebuffer

    fun createTextureManager(): TextureManager

    fun clear(vararg buffers: IntegratedBufferTypes)

    fun getErrors(): List<RenderSystemError>


    fun polygonOffset(factor: Float, unit: Float)


    fun resetBlending() {
        disable(RenderingCapabilities.BLENDING)
        setBlendFunction(BlendingFunctions.ONE, BlendingFunctions.ONE_MINUS_SOURCE_ALPHA, BlendingFunctions.ONE, BlendingFunctions.ZERO)
    }

    fun reloadShaders() {
        val copy = shaders.toMutableSet()
        for (shader in copy) {
            shader.reload()
        }
    }
}
