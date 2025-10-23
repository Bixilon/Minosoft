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

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.shader.types.TextureShader
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.StaticTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.TextureArrayProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture

class DummyStaticTextureArray(context: RenderContext) : StaticTextureArray(context, false, 0) {

    override fun upload(textures: Collection<Texture>) {
        for (texture in textures) {
            texture.renderData = DummyTextureRenderData
            texture.array = TextureArrayProperties(null, 1024, 1.0f / 1024)
            if (texture !is DummyTexture) continue
            texture.state = TextureStates.LOADED
        }
    }

    override fun upload(latch: AbstractLatch?) {
        super.upload(latch)
        animator.init()
    }

    override fun create(name: ResourceLocation, mipmaps: Boolean, factory: (mipmaps: Int) -> Texture): Texture {
        return super.create(name, mipmaps) { DummyTexture() }
    }

    override fun findResolution(size: Vec2i) = size

    override fun activate() = Unit
    override fun use(shader: TextureShader, name: String) = Unit
}
