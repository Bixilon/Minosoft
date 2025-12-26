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

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.kutil.profiler.stack.StackedProfiler.Companion.invoke
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.Rendering
import de.bixilon.minosoft.gui.rendering.system.base.*
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.depth.DepthModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.stencil.StencilModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.texture.TextureModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.driver.DriverHacks
import de.bixilon.minosoft.gui.rendering.system.base.query.QueryTypes
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.RGB8Buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlUtil.gl
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.FloatOpenGlBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame.OpenGlFramebuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.uniform.FloatOpenGlUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.vertex.OpenGlIndexBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.vertex.OpenGlVertexBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.error.OpenGlError
import de.bixilon.minosoft.gui.rendering.system.opengl.error.OpenGlException
import de.bixilon.minosoft.gui.rendering.system.opengl.query.OpenGlQuery
import de.bixilon.minosoft.gui.rendering.system.opengl.shader.OpenGlShaderManagement
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGlTextureManager
import de.bixilon.minosoft.gui.rendering.system.opengl.vendor.OpenGlVendor
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.logging.LogOptions
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL43.GL_DEBUG_OUTPUT
import org.lwjgl.opengl.GL43.glDebugMessageCallback
import org.lwjgl.system.Configuration
import java.nio.FloatBuffer
import java.nio.IntBuffer

class OpenGlRenderSystem(
    val context: RenderContext,
    val log: Boolean = LogOptions.verbose,
) : RenderSystem {
    override val shader = OpenGlShaderManagement(this)
    private var thread: Thread? = null
    private val capabilities: MutableSet<RenderingCapabilities> = RenderingCapabilities.set()
    override lateinit var vendor: OpenGlVendor
        private set
    override var active: Boolean = false
        private set
    override val primitives = PrimitiveTypes.set(PrimitiveTypes.POINT, PrimitiveTypes.LINE, PrimitiveTypes.TRIANGLE)

    var blendingSource = BlendingFunctions.ONE
        private set
    var blendingDestination = BlendingFunctions.ZERO
        private set


    var boundVao = -1
    var boundBuffer = Int2IntOpenHashMap(3).apply { defaultReturnValue(-1) }

    var nextUniformBufferIndex = 0
    var nextTextureIndex = 0
    val framebufferTextureIndex = nextTextureIndex++

    override var framebuffer: Framebuffer? = null
        set(value) {
            if (value == field) {
                return
            }
            if (value == null) {
                gl { glBindFramebuffer(GL_FRAMEBUFFER, 0) }
                viewport = context.window.size
            } else {
                check(value is OpenGlFramebuffer) { "Can not use non OpenGL framebuffer!" }
                value.bind()
            }
            field = value
        }

    init {
        if (latch.count == 2) {
            latch.dec()
            DefaultThreadPool += { init.invoke(); latch.dec() }
        }
    }


    override fun init() {
        if (thread != null) throw IllegalStateException("Already initialized!")
        thread = Thread.currentThread()
        latch.await()
        GL.createCapabilities()

        this.vendorString = gl { glGetString(GL_VENDOR) } ?: "UNKNOWN"

        vendor = OpenGlVendor.of(vendorString.lowercase())
        if (context.profile.advanced.preferQuads) {
            if (DriverHacks.USE_QUADS_OVER_TRIANGLE in vendor.hacks) {
                primitives += PrimitiveTypes.QUAD
            } else {
                Log.log(LogMessageType.RENDERING, LogLevels.WARN) { "You requested to prefer GL_QUADS over GL_TRIANGLES, but your driver does not support it. Disabling..." }
            }
        }

        this.version = gl { glGetString(GL_VERSION) } ?: "UNKNOWN"
        this.gpuType = gl { glGetString(GL_RENDERER) } ?: "UNKNOWN"

        if (OpenGlOptions.DEBUG_OUTPUT) {
            gl { glEnable(GL_DEBUG_OUTPUT) }
            gl {
                glDebugMessageCallback({ source, type, id, severity, length, message, userParameter ->
                    Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "OpenGL error: source=$source, type=$type, id=$id, severity=$severity, length=$length, message=$message, userParameter=$userParameter" }
                }, 0)
            }
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
        val enabled = capability in capabilities
        if (enabled == status) {
            return
        }

        val glCapability = capability.gl

        if (status) {
            gl { glEnable(glCapability) }
            capabilities += capability
        } else {
            gl { glDisable(glCapability) }
            capabilities -= capability
        }
    }

    override fun get(capability: RenderingCapabilities): Boolean {
        return capability in capabilities
    }

    override fun set(source: BlendingFunctions, destination: BlendingFunctions) {
        if (blendingDestination == destination && blendingSource == source) {
            return
        }
        blendingSource = source
        blendingDestination = destination
        gl { glBlendFunc(source.gl, destination.gl) }
    }

    private var sourceRGB: BlendingFunctions = BlendingFunctions.ONE
    private var destinationRGB: BlendingFunctions = BlendingFunctions.ONE
    private var sourceAlpha: BlendingFunctions = BlendingFunctions.ONE
    private var destinationAlpha: BlendingFunctions = BlendingFunctions.ONE

    override fun setBlendFunction(sourceRGB: BlendingFunctions, destinationRGB: BlendingFunctions, sourceAlpha: BlendingFunctions, destinationAlpha: BlendingFunctions) {
        if (this.sourceRGB == sourceRGB && this.destinationRGB == destinationRGB && this.sourceAlpha == sourceAlpha && this.destinationAlpha == destinationAlpha) {
            return
        }
        gl { glBlendFuncSeparate(sourceRGB.gl, destinationRGB.gl, sourceAlpha.gl, destinationAlpha.gl) }
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
            gl { glDepthFunc(value.gl) }
            field = value
        }

    override var depthMask: Boolean = true
        set(value) {
            if (field == value) {
                return
            }
            gl { glDepthMask(value) }
            field = value
        }

    override var polygonMode: PolygonModes = PolygonModes.FILL
        set(value) {
            if (field == value) {
                return
            }
            gl { glPolygonMode(FaceTypes.FRONT_AND_BACK.gl, value.gl) }
            field = value
        }

    override lateinit var vendorString: String
        private set
    override lateinit var version: String
        private set
    override lateinit var gpuType: String
        private set

    override fun readPixels(start: Vec2i, size: Vec2i): TextureBuffer {
        val buffer = RGB8Buffer(size)
        gl { glReadPixels(start.x, start.y, size.x, size.y, GL_RGB, GL_UNSIGNED_BYTE, buffer.data) }
        return buffer
    }

    override fun createVertexBuffer(struct: MeshStruct, data: FloatBuffer, primitive: PrimitiveTypes, index: IntBuffer?, reused: Boolean): OpenGlVertexBuffer {
        val buffer = FloatOpenGlBuffer(this, data, !reused)
        val index = index?.let { OpenGlIndexBuffer(this, it, !reused) }
        return OpenGlVertexBuffer(this, primitive, struct, buffer, index)
    }

    override fun createFloatUniformBuffer(data: FloatBuffer): FloatOpenGlUniformBuffer {
        return FloatOpenGlUniformBuffer(this, nextUniformBufferIndex++, data)
    }

    override fun createFramebuffer(size: Vec2i, scale: Float, texture: TextureModes?, depth: DepthModes?, stencil: StencilModes?): OpenGlFramebuffer {
        return OpenGlFramebuffer(this, size, scale, texture, depth, stencil)
    }

    override fun createQuery(type: QueryTypes) = OpenGlQuery(type)

    override fun createTextureManager() = OpenGlTextureManager(this)

    override var clearColor: RGBAColor = Colors.TRUE_BLACK.rgba()
        set(value) {
            if (value == field) {
                return
            }
            gl { glClearColor(value.redf, value.greenf, value.bluef, value.alphaf) }

            field = value
        }

    override fun clear(vararg buffers: IntegratedBufferTypes) {
        var bits = 0
        for (buffer in buffers) {
            bits = bits or buffer.gl
        }
        gl { glClear(bits) }
    }

    override fun getErrors(): List<OpenGlError> {
        val error = glGetError()

        if (error == GL_NO_ERROR) return emptyList()
        // opengl only supports single error
        return listOf(OpenGlError(error))
    }

    private var polygonOffsetFactor: Float = 0.0f
    private var polygonOffsetUnit: Float = 0.0f

    override fun polygonOffset(factor: Float, unit: Float) {
        if (this.polygonOffsetFactor != factor || this.polygonOffsetUnit != unit) {
            gl { glPolygonOffset(factor, unit) }
            this.polygonOffsetFactor = factor
            this.polygonOffsetUnit = unit
        }
    }

    inline fun log(builder: () -> Any?) {
        if (!log) return
        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "[OpenGL] ${builder.invoke()}" }
    }

    override var viewport: Vec2i = Vec2i.EMPTY
        set(value) {
            if (field == value) return
            field = value
            gl { glViewport(0, 0, viewport.x, viewport.y) }
        }

    override fun toString() = "OpenGlSystem"


    companion object {
        private var latch = SimpleLatch(2)
        var init: () -> Unit = {
            Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Loading default opengl library" }
            GL.create()
        }

        init {
            Configuration.OPENGL_EXPLICIT_INIT.set(true)
        }

        inline fun <T> gl(runnable: () -> T): T {
            val context = Rendering.currentContext
            if (OpenGlOptions.ASSERT_THREAD && context == null) {
                throw IllegalStateException("No open gl context!")
            }
            if (OpenGlOptions.ASSERT_BEFORE) {
                val error = glGetError()
                if (error != GL_NO_ERROR) throw OpenGlException(OpenGlError(error))
            }
            val profiler = context?.profiler
            val result = profiler("gl") { runnable.invoke() }
            if (OpenGlOptions.ASSERT_AFTER) {
                val error = glGetError()
                if (error != GL_NO_ERROR) throw OpenGlException(OpenGlError(error))
            }
            return result
        }
    }
}
