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

package de.bixilon.minosoft.gui.rendering.system.sdl3.shader

import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.shader.ShaderManagement
import de.bixilon.minosoft.gui.rendering.system.sdl3.Sdl3RenderSystem

class Sdl3ShaderManagement(val system: Sdl3RenderSystem) : ShaderManagement {
    override var shader: Shader? = null

    override fun create(vertex: ResourceLocation, geometry: ResourceLocation?, fragment: ResourceLocation): NativeShader {
        TODO("Not yet implemented")
    }

    override fun plusAssign(shader: Shader) {
        TODO("Not yet implemented")
    }

    override fun minusAssign(shader: Shader) {
        TODO("Not yet implemented")
    }

    override fun iterator(): Iterator<Shader> {
        TODO("Not yet implemented")
    }
}
