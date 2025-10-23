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

package de.bixilon.minosoft.gui.rendering.system.dummy.texture

import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.shader.types.TextureShader
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureState

class DummyDynamicTextureArray(context: RenderContext) : DynamicTextureArray(context, 1, 0) {
    override fun createTexture(identifier: Any, index: Int) = DummyDynamicTexture

    override fun unload() = Unit
    override fun unsafeUse(shader: TextureShader, name: String) = Unit
    override fun activate() = Unit
    override fun upload() = Unit
    override fun upload(index: Int, texture: DynamicTexture) {

        texture.state = DynamicTextureState.LOADED
    }
}
