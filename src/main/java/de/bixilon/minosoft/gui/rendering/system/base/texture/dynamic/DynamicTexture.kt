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

package de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic

import de.bixilon.minosoft.gui.rendering.system.base.texture.ShaderTexture
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

interface DynamicTexture : ShaderTexture {
    val uuid: UUID
    val usages: AtomicInteger

    val state: DynamicTextureState


    fun addListener(callback: DynamicStateChangeCallback)
    operator fun plusAssign(callback: DynamicStateChangeCallback) = addListener(callback)

    operator fun minusAssign(callback: DynamicStateChangeCallback) = removeListener(callback)
    fun removeListener(callback: DynamicStateChangeCallback)
}
