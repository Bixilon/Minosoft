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

package de.bixilon.minosoft.gui.rendering.block

import de.bixilon.minosoft.data.text.RGBColor.Companion.asGray
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.system.opengl.FloatUniformBuffer
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition


class LightMap(private val connection: PlayConnection) {
    private val uniformBuffer = FloatUniformBuffer(1, FloatArray(16 * 16 * 4) { 1.0f })
    private var lastUpdate = -1L


    fun init() {
        uniformBuffer.init()

    }

    fun use(shader: Shader, bufferName: String = "uLightMapBuffer") {
        uniformBuffer.use(shader, bufferName)
    }

    fun update() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdate < ProtocolDefinition.TICK_TIME * 10) {
            return
        }
        lastUpdate = currentTime

        // ToDo
        for (skyLight in 0 until 16) {
            for (blockLight in 0 until 16) {
                val index = ((skyLight shl 4) or blockLight) * 4

                val color = ((blockLight + skyLight) / 30.0f).asGray()

                uniformBuffer.data[index + 0] = color.floatRed
                uniformBuffer.data[index + 1] = color.floatRed
                uniformBuffer.data[index + 2] = color.floatGreen
            }
        }
        uniformBuffer.upload()
    }
}
