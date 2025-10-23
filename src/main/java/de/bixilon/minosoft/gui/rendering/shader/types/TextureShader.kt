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

package de.bixilon.minosoft.gui.rendering.shader.types

import de.bixilon.minosoft.gui.rendering.shader.AbstractShader
import de.bixilon.minosoft.gui.rendering.shader.uniform.AnyShaderUniform
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager

interface TextureShader : AbstractShader {
    var textures: TextureManager

    fun textureManager(name: String = "uTextures", textureManager: TextureManager = native.context.textures, animated: Boolean = this is AnimatedShader): AnyShaderUniform<TextureManager> {
        return uniform(name, textureManager) { _, name, value: TextureManager ->
            value.use(this, name)
            if (animated) {
                textureManager.static.animator.use(this)
            }
        }
    }
}
