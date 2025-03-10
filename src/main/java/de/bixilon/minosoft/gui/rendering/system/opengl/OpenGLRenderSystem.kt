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

package de.bixilon.minosoft.gui.rendering.system.opengl

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.system.base.*
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.driver.DriverHacks
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader.Companion.shader
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.RGB8Buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame.OpenGLFramebuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.uniform.FloatOpenGLUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.uniform.IntOpenGLUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.vertex.FloatOpenGLVertexBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureManager
import de.bixilon.minosoft.gui.rendering.system.opengl.vendor.OpenGLVendor
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshOrder
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL43.GL_DEBUG_OUTPUT
import org.lwjgl.opengl.GL43.glDebugMessageCallback
import java.nio.FloatBuffer

class OpenGLRenderSystem(
    private val context: RenderContext,
    val log: Boolean = RunConfiguration.VERBOSE_LOGGING,
) : RenderSystem {
    private var thread: Thread? = null
    override val shaders: MutableSet<Shader> = mutableSetOf()
    private val capabilities: MutableSet<RenderingCapabilities> = RenderingCapabilities.set()
    override lateinit var vendor: OpenGLVendor
        private set
    override var active: Boolean = false
        private set

    var blendingSource = BlendingFunctions.ONE
        private set
    var blendingDestination = BlendingFunctions.ZERO
        private set

    override var quadType: PrimitiveTypes = if (context.preferQuads) PrimitiveTypes.QUAD else PrimitiveTypes.TRIANGLE
    override var quadOrder: RenderOrder = if (quadType == PrimitiveTypes.QUAD) MeshOrder.QUAD else MeshOrder.TRIANGLE
    override var legacyQuadOrder: RenderOrder = if (quadType == PrimitiveTypes.QUAD) MeshOrder.LEGACY_QUAD else MeshOrder.LEGACY_TRIANGLE
    var boundVao = -1
    var boundBuffer = -1
    var uniformBufferBindingIndex = 0
    var textureBindingIndex = 0

    override var shader: NativeShader? = null
        set(value) {
            if (value === field) {
                return
            }
            if (value == null) {
                glUseProgram(0)
                field = null
                return
            }

            check(value is OpenGLNativeShader) { "Can not use non OpenGL shader in OpenGL render system!" }
            check(value.loaded) { "Shader not loaded!" }
            check(this === value.system) { "Shader not part of this context!" }

            value.unsafeUse()

            field = value
        }
    override var framebuffer: Framebuffer? = null
        set(value) {
            if (value == field) {
                return
            }
            if (value == null) {
                glBindFramebuffer(GL_FRAMEBUFFER, 0)
                viewport = context.window.size
            } else {
                check(value is OpenGLFramebuffer) { "Can not use non OpenGL framebuffer!" }
                value.bind()
            }
            field = value
        }


    @Synchronized
    override fun init() {
        if (thread != null) {
            throw IllegalStateException("Context is thread bound!")
        }
        thread = Thread.currentThread()
        GL.createCapabilities()

        this.vendorString = glGetString(GL_VENDOR) ?: "UNKNOWN"
        val vendorString = vendorString.lowercase()

        vendor = OpenGLVendor.of(vendorString.lowercase())
        if (context.preferQuads && DriverHacks.USE_QUADS_OVER_TRIANGLE !in vendor.hacks) {
            throw IllegalStateException("Your GPU driver does not support the `prefer_quads` config option!")
        }

        this.version = glGetString(GL_VERSION) ?: "UNKNOWN"
        this.gpuType = glGetString(GL_RENDERER) ?: "UNKNOWN"

        if (DEBUG_MODE) {
            glEnable(GL_DEBUG_OUTPUT)
            glDebugMessageCallback({ source, type, id, severity, length, message, userParameter ->
                Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "OpenGL error: source=$source, type=$type, id=$id, severity=$severity, length=$length, message=$message, userParameter=$userParameter" }
            }, 0)
        }
        active = true
    }

    override fun destroy() {
        active = false
    }

    override fun enable(capability: RenderingCapabilities) {
        this[capability] = true
    }

    override fun disable(capability: RenderingCapabilities) {
        this[capability] = false
    }

    override fun set(capability: RenderingCapabilities, status: Boolean) {
        val enabled = capabilities.contains(capability)
        if (enabled == status) {
            return
        }

        val glCapability = capability.gl

        if (status) {
            glEnable(glCapability)
            capabilities += capability
        } else {
            glDisable(glCapability)
            capabilities -= capability
        }
    }

    override fun get(capability: RenderingCapabilities): Boolean {
        return capabilities.contains(capability)
    }

    override fun set(source: BlendingFunctions, destination: BlendingFunctions) {
        if (blendingDestination == destination && blendingSource == source) {
            return
        }
        blendingSource = source
        blendingDestination = destination
        glBlendFunc(source.gl, destination.gl)
    }

    private var sourceRGB: BlendingFunctions = BlendingFunctions.ONE
    private var destinationRGB: BlendingFunctions = BlendingFunctions.ONE
    private var sourceAlpha: BlendingFunctions = BlendingFunctions.ONE
    private var destinationAlpha: BlendingFunctions = BlendingFunctions.ONE

    override fun setBlendFunction(sourceRGB: BlendingFunctions, destinationRGB: BlendingFunctions, sourceAlpha: BlendingFunctions, destinationAlpha: BlendingFunctions) {
        if (this.sourceRGB == sourceRGB && this.destinationRGB == destinationRGB && this.sourceAlpha == sourceAlpha && this.destinationAlpha == destinationAlpha) {
            return
        }
        glBlendFuncSeparate(sourceRGB.gl, destinationRGB.gl, sourceAlpha.gl, destinationAlpha.gl)
        this.sourceRGB = sourceRGB
        this.destinationRGB = destinationRGB
        this.sourceAlpha = sourceAlpha
        this.destinationAlpha = destinationAlpha
    }

    override var depth: DepthFunctions = DepthFunctions.LESS
        set(value) {
            if (field == value) {
                return
            }
            glDepthFunc(value.gl)
            field = value
        }

    override var depthMask: Boolean = true
        set(value) {
            if (field == value) {
                return
            }
            glDepthMask(value)
            field = value
        }

    override var polygonMode: PolygonModes = PolygonModes.FILL
        set(value) {
            if (field == value) {
                return
            }
            glPolygonMode(FaceTypes.FRONT_AND_BACK.gl, value.gl)
            field = value
        }

    override val usedVRAM: Long
        get() = vendor.usedVRAM

    override val availableVRAM: Long
        get() = vendor.availableVRAM

    override val maximumVRAM: Long
        get() = vendor.maximumVRAM

    override lateinit var vendorString: String
        private set
    override lateinit var version: String
        private set
    override lateinit var gpuType: String
        private set

    override fun readPixels(start: Vec2i, end: Vec2i): TextureBuffer {
        val size = Vec2i(end.x - start.x, end.y - start.y)
        val buffer = RGB8Buffer(size)
        glReadPixels(start.x, start.y, end.x, end.y, GL_RGB, GL_UNSIGNED_BYTE, buffer.data)
        return buffer
    }

    override fun createNativeShader(vertex: ResourceLocation, geometry: ResourceLocation?, fragment: ResourceLocation): OpenGLNativeShader {
        return OpenGLNativeShader(context, vertex.shader(), geometry?.shader(), fragment.shader())
    }

    override fun createVertexBuffer(struct: MeshStruct, data: FloatBuffer, primitiveType: PrimitiveTypes): FloatOpenGLVertexBuffer {
        return FloatOpenGLVertexBuffer(this, struct, data, primitiveType)
    }

    override fun createFloatUniformBuffer(data: FloatBuffer): FloatOpenGLUniformBuffer {
        return FloatOpenGLUniformBuffer(this, uniformBufferBindingIndex++, data)
    }

    override fun createIntUniformBuffer(data: IntArray): IntOpenGLUniformBuffer {
        return IntOpenGLUniformBuffer(this, uniformBufferBindingIndex++, data)
    }

    override fun createFramebuffer(color: Boolean, depth: Boolean, scale: Float): OpenGLFramebuffer {
        return OpenGLFramebuffer(this, context.window.size, scale, color, depth)
    }

    override fun createTextureManager(): OpenGLTextureManager {
        return OpenGLTextureManager(context)
    }

    override var clearColor: RGBColor = Colors.TRUE_BLACK
        set(value) {
            if (value == field) {
                return
            }
            glClearColor(value.floatRed, value.floatGreen, value.floatBlue, value.floatAlpha)

            field = value
        }

    override fun clear(vararg buffers: IntegratedBufferTypes) {
        var bits = 0
        for (buffer in buffers) {
            bits = bits or buffer.gl
        }
        glClear(bits)
    }

    override fun getErrors(): List<OpenGLError> {
        val error = glGetError()

        if (error == GL_NO_ERROR) return emptyList()
        // opengl only supports single error
        return listOf(OpenGLError(error))
    }

    private var polygonOffsetFactor: Float = 0.0f
    private var polygonOffsetUnit: Float = 0.0f
    override fun polygonOffset(factor: Float, unit: Float) {
        if (this.polygonOffsetFactor != factor || this.polygonOffsetUnit != unit) {
            glPolygonOffset(factor, unit)
            this.polygonOffsetFactor = factor
            this.polygonOffsetUnit = unit
        }
    }

    fun assertThread() {
        val thread = thread ?: throw IllegalStateException("Not yet initialized!")
        val current = Thread.currentThread()
        if (thread !== current) {
            throw Exception("Thread mismatch: thread=$thread, current=$current")
        }
    }

    inline fun log(builder: () -> Any?) {
        if (!log) return
        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "[OpenGL] ${builder.invoke()}" }
    }

    override var viewport: Vec2i = Vec2i.EMPTY
        set(value) {
            if (field == value) return
            field = Vec2i(value)
            glViewport(0, 0, viewport.x, viewport.y)
        }

    companion object : RenderSystemFactory {
        const val DEBUG_MODE = false

        override fun create(context: RenderContext) = OpenGLRenderSystem(context)

        private val RenderingCapabilities.gl: Int
            get() {
                return when (this) {
                    RenderingCapabilities.BLENDING -> GL_BLEND
                    RenderingCapabilities.DEPTH_TEST -> GL_DEPTH_TEST
                    RenderingCapabilities.FACE_CULLING -> GL_CULL_FACE
                    RenderingCapabilities.POLYGON_OFFSET -> GL_POLYGON_OFFSET_FILL
                    else -> throw IllegalArgumentException("OpenGL does not support capability: $this")
                }
            }

        private val BlendingFunctions.gl: Int
            get() {
                return when (this) {
                    BlendingFunctions.ZERO -> GL_ZERO
                    BlendingFunctions.ONE -> GL_ONE
                    BlendingFunctions.SOURCE_COLOR -> GL_SRC_COLOR
                    BlendingFunctions.ONE_MINUS_SOURCE_COLOR -> GL_ONE_MINUS_SRC_COLOR
                    BlendingFunctions.DESTINATION_COLOR -> GL_DST_COLOR
                    BlendingFunctions.ONE_MINUS_DESTINATION_COLOR -> GL_ONE_MINUS_DST_COLOR
                    BlendingFunctions.SOURCE_ALPHA -> GL_SRC_ALPHA
                    BlendingFunctions.ONE_MINUS_SOURCE_ALPHA -> GL_ONE_MINUS_SRC_ALPHA
                    BlendingFunctions.DESTINATION_ALPHA -> GL_DST_ALPHA
                    BlendingFunctions.ONE_MINUS_DESTINATION_ALPHA -> GL_ONE_MINUS_DST_ALPHA
                    BlendingFunctions.CONSTANT_COLOR -> GL_CONSTANT_COLOR
                    BlendingFunctions.ONE_MINUS_CONSTANT_COLOR -> GL_ONE_MINUS_CONSTANT_COLOR
                    BlendingFunctions.CONSTANT_ALPHA -> GL_CONSTANT_ALPHA
                    BlendingFunctions.ONE_MINUS_CONSTANT_ALPHA -> GL_ONE_MINUS_CONSTANT_ALPHA
                    else -> throw IllegalArgumentException("OpenGL does not support blending function: $this")
                }
            }

        private val DepthFunctions.gl: Int
            get() {
                return when (this) {
                    DepthFunctions.NEVER -> GL_NEVER
                    DepthFunctions.LESS -> GL_LESS
                    DepthFunctions.EQUAL -> GL_EQUAL
                    DepthFunctions.LESS_OR_EQUAL -> GL_LEQUAL
                    DepthFunctions.GREATER -> GL_GREATER
                    DepthFunctions.NOT_EQUAL -> GL_NOTEQUAL
                    DepthFunctions.GREATER_OR_EQUAL -> GL_GEQUAL
                    DepthFunctions.ALWAYS -> GL_ALWAYS
                    else -> throw IllegalArgumentException("OpenGL does not support depth function: $this")
                }
            }

        private val PolygonModes.gl: Int
            get() {
                return when (this) {
                    PolygonModes.FILL -> GL_FILL
                    PolygonModes.LINE -> GL_LINE
                    PolygonModes.POINT -> GL_POINT
                    else -> throw IllegalArgumentException("OpenGL does not support polygon mode: $this")
                }
            }

        private val FaceTypes.gl: Int
            get() {
                return when (this) {
                    FaceTypes.FRONT_LEFT -> GL_FRONT_LEFT
                    FaceTypes.FRONT_RIGHT -> GL_FRONT_RIGHT
                    FaceTypes.BACK_LEFT -> GL_BACK_LEFT
                    FaceTypes.BACK_RIGHT -> GL_BACK_RIGHT
                    FaceTypes.FRONT -> GL_FRONT
                    FaceTypes.BACK -> GL_BACK
                    FaceTypes.LEFT -> GL_LEFT
                    FaceTypes.RIGHT -> GL_RIGHT
                    FaceTypes.FRONT_AND_BACK -> GL_FRONT_AND_BACK
                    else -> throw IllegalArgumentException("OpenGL does not support face type: $this")
                }
            }

        private val IntegratedBufferTypes.gl: Int
            get() {
                return when (this) {
                    IntegratedBufferTypes.DEPTH_BUFFER -> GL_DEPTH_BUFFER_BIT
                    IntegratedBufferTypes.ACCUM_BUFFER -> GL_ACCUM_BUFFER_BIT
                    IntegratedBufferTypes.STENCIL_BUFFER -> GL_STENCIL_BUFFER_BIT
                    IntegratedBufferTypes.COLOR_BUFFER -> GL_COLOR_BUFFER_BIT
                    else -> throw IllegalArgumentException("OpenGL does not support integrated buffer type: $this")
                }
            }
    }
}
