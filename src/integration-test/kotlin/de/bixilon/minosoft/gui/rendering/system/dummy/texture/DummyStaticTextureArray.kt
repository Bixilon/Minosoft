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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.StaticTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import java.util.concurrent.atomic.AtomicInteger

class DummyStaticTextureArray(context: RenderContext) : StaticTextureArray(context, false, 0) {

    override fun load(animationIndex: AtomicInteger, textures: Collection<Texture>) {
        for (texture in textures) {
            if (texture !is DummyTexture) continue
            texture.state = TextureStates.LOADED
        }
    }

    override fun upload(latch: AbstractLatch?) {
        animator.init()
    }

    override fun create(resourceLocation: ResourceLocation, mipmaps: Boolean, properties: Boolean, factory: (mipmaps: Int) -> Texture): Texture {
        return super.create(resourceLocation, mipmaps, properties) { DummyTexture() }
    }

    override fun findResolution(size: Vec2i) = size

    override fun activate() = Unit
    override fun use(shader: NativeShader, name: String) = Unit
}
