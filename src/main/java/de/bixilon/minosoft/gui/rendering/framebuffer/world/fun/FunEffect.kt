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

package de.bixilon.minosoft.gui.rendering.framebuffer.world.`fun`

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.ResourceLocationAble
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.util.KUtil.toResourceLocation

interface FunEffect : ResourceLocationAble {
    val renderWindow: RenderWindow
    val shader: Shader

    fun update() {}
    fun preDraw() {}


    fun createShader(vertex: ResourceLocation = "minosoft:framebuffer/world.vsh".toResourceLocation(), fragment: ResourceLocation = "minosoft:framebuffer/world.fsh".toResourceLocation()): Shader {
        val shader = renderWindow.renderSystem.createShader(vertex = vertex, fragment = fragment)
        shader.load()
        shader.use()

        shader.setInt("uColor", 0)
        return shader
    }
}
