/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.modding.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.RenderingCapabilities
import de.bixilon.minosoft.modding.event.CallbackEventInvoker
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL20.*

class OpenGLRenderSystem(
    private val renderWindow: RenderWindow,
) : RenderSystem {
    val shaders: MutableMap<Shader, Int> = synchronizedMapOf() // ToDo
    private val capabilities: MutableSet<RenderingCapabilities> = synchronizedSetOf()
    var blendingSource = BlendingFunctions.ONE
        private set
    var blendingDestination = BlendingFunctions.ZERO
        private set

    override var shader: Shader? = null
        set(value) {
            if (value === field) {
                return
            }
            val programId = shaders[value] ?: error("Shader not loaded: $value")
            glUseProgram(programId)
            field = value
        }


    override fun init() {
        GL.createCapabilities()

        renderWindow.connection.registerEvent(CallbackEventInvoker.of<ResizeWindowEvent> {
            renderWindow.queue += {
                glViewport(0, 0, it.size.x, it.size.y)
            }
        })
    }

    override fun enable(capability: RenderingCapabilities) {
        this[capability] = true
    }

    override fun disable(capability: RenderingCapabilities) {
        this[capability] = false
    }

    override fun set(capability: RenderingCapabilities, status: Boolean) {
        val enabled = capabilities.contains(capability)
        if ((enabled && status) || (!status && !enabled)) {
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


    companion object {
        private val RenderingCapabilities.gl: Int
            get() {
                return when (this) {
                    RenderingCapabilities.BLENDING -> GL_BLEND
                    RenderingCapabilities.DEPTH_TEST -> GL_DEPTH_TEST
                    RenderingCapabilities.FACE_CULLING -> GL_CULL_FACE
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
    }
}
