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

package de.bixilon.minosoft.gui.rendering.system.opengl.texture.dynamic

import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureState
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class OpenGLDynamicTexture(
    override val uuid: UUID,
    shaderId: Int,
) : DynamicTexture {
    var data: Array<ByteBuffer>? = null
    override var onStateChange: (() -> Unit)? = null
    override val usages = AtomicInteger()
    override var state: DynamicTextureState = DynamicTextureState.WAITING
        set(value) {
            field = value
            onStateChange?.invoke()
        }

    override var shaderId: Int = shaderId
        get() {
            if (usages.get() == 0 || state == DynamicTextureState.UNLOADED) {
                throw IllegalStateException("Texture was eventually garbage collected")
            }
            return field
        }

    override fun toString(): String {
        return uuid.toString()
    }
}
