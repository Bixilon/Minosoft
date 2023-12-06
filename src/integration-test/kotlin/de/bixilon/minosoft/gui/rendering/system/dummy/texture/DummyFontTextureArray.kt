/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.TextureArrayProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.font.FontCompressions
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.font.FontTextureArray

class DummyFontTextureArray(context: RenderContext) : FontTextureArray(context, 1024, FontCompressions.NONE) {

    override fun load(latch: AbstractLatch) {
        for (texture in textures) {
            texture.renderData = DummyTextureRenderData
            texture.array = TextureArrayProperties(null, 1024, 1.0f / 1024)
            if (texture !is DummyTexture) continue
            texture.state = TextureStates.LOADED
        }
    }

    override fun upload(latch: AbstractLatch?) {
    }

    override fun activate() {
    }

    override fun use(shader: NativeShader, name: String) {
    }
}
