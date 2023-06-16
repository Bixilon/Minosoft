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

package de.bixilon.minosoft.gui.rendering.system.dummy.texture

import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.TextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureArray
import java.util.*

class DummyDynamicTextureArray : DynamicTextureArray {
    override val size: Int = 0

    override fun pushBuffer(identifier: UUID, force: Boolean, data: () -> TextureData): DynamicTexture {
        return DummyDynamicTexture
    }

    override fun load(latch: AbstractLatch?) {
    }

    override fun activate() {
    }

    override fun use(shader: NativeShader, name: String) {
    }
}
