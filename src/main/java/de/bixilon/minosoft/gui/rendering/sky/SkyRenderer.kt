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

package de.bixilon.minosoft.gui.rendering.sky

import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderBuilder
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.shader.ShaderHolder
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import org.lwjgl.opengl.GL11.*

class SkyRenderer(
    private val connection: PlayConnection,
    val renderWindow: RenderWindow,
) : Renderer, ShaderHolder {
    private var lastMatrixUpdate = System.currentTimeMillis()
    override val shader = Shader(
        vertexPath = ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "rendering/shader/sky_vertex.glsl"),
        fragmentPath = ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "rendering/shader/sky_fragment.glsl"),
    )
    private val mesh = SkyMesh()

    init {
        mesh.data!!.addAll(VERTICES)
    }

    override fun init() {
        shader.load()

        mesh.load()
    }

    override fun postInit() {
    }

    private fun setShaderMatrix() {
        if (lastMatrixUpdate == renderWindow.inputHandler.camera.lastMatrixChange) {
            return
        }
        shader.use().setMat4("skyViewProjectionMatrix", renderWindow.inputHandler.camera.projectionMatrix * renderWindow.inputHandler.camera.viewMatrix.toMat3().toMat4())
        lastMatrixUpdate = renderWindow.inputHandler.camera.lastMatrixChange
    }


    override fun draw() {
        glDepthFunc(GL_LEQUAL)
        shader.use()
        setShaderMatrix()
        mesh.draw()
        glDepthFunc(GL_LESS)
    }

    companion object : RenderBuilder {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:sky")


        override fun build(connection: PlayConnection, renderWindow: RenderWindow): SkyRenderer {
            return SkyRenderer(connection, renderWindow)
        }

        private val VERTICES = floatArrayOf(
            -1.0f, +1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            +1.0f, -1.0f, -1.0f,
            +1.0f, -1.0f, -1.0f,
            +1.0f, +1.0f, -1.0f,
            -1.0f, +1.0f, -1.0f,

            -1.0f, -1.0f, +1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, +1.0f, -1.0f,
            -1.0f, +1.0f, -1.0f,
            -1.0f, +1.0f, +1.0f,
            -1.0f, -1.0f, +1.0f,

            +1.0f, -1.0f, -1.0f,
            +1.0f, -1.0f, +1.0f,
            +1.0f, +1.0f, +1.0f,
            +1.0f, +1.0f, +1.0f,
            +1.0f, +1.0f, -1.0f,
            +1.0f, -1.0f, -1.0f,

            -1.0f, -1.0f, +1.0f,
            -1.0f, +1.0f, +1.0f,
            +1.0f, +1.0f, +1.0f,
            +1.0f, +1.0f, +1.0f,
            +1.0f, -1.0f, +1.0f,
            -1.0f, -1.0f, +1.0f,

            -1.0f, +1.0f, -1.0f,
            +1.0f, +1.0f, -1.0f,
            +1.0f, +1.0f, +1.0f,
            +1.0f, +1.0f, +1.0f,
            -1.0f, +1.0f, +1.0f,
            -1.0f, +1.0f, -1.0f,

            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, +1.0f,
            +1.0f, -1.0f, -1.0f,
            +1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, +1.0f,
            +1.0f, -1.0f, +1.0f,
        )
    }
}
