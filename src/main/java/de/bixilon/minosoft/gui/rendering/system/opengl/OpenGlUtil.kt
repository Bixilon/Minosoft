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

import de.bixilon.minosoft.gui.rendering.system.base.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL14.*

object OpenGlUtil {

    val RenderingCapabilities.gl: Int
        get() = when (this) {
            RenderingCapabilities.BLENDING -> GL_BLEND
            RenderingCapabilities.DEPTH_TEST -> GL_DEPTH_TEST
            RenderingCapabilities.FACE_CULLING -> GL_CULL_FACE
            RenderingCapabilities.POLYGON_OFFSET -> GL_POLYGON_OFFSET_FILL
            else -> throw IllegalArgumentException("OpenGL does not support capability: $this")
        }

    val BlendingFunctions.gl: Int
        get() = when (this) {
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

    val DepthFunctions.gl: Int
        get() = when (this) {
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

    val PolygonModes.gl: Int
        get() = when (this) {
            PolygonModes.FILL -> GL_FILL
            PolygonModes.LINE -> GL_LINE
            PolygonModes.POINT -> GL_POINT
            else -> throw IllegalArgumentException("OpenGL does not support polygon mode: $this")
        }

    val FaceTypes.gl: Int
        get() = when (this) {
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

    val IntegratedBufferTypes.gl: Int
        get() = when (this) {
            IntegratedBufferTypes.DEPTH_BUFFER -> GL_DEPTH_BUFFER_BIT
            IntegratedBufferTypes.ACCUM_BUFFER -> GL_ACCUM_BUFFER_BIT
            IntegratedBufferTypes.STENCIL_BUFFER -> GL_STENCIL_BUFFER_BIT
            IntegratedBufferTypes.COLOR_BUFFER -> GL_COLOR_BUFFER_BIT
            else -> throw IllegalArgumentException("OpenGL does not support integrated buffer type: $this")
        }
}
