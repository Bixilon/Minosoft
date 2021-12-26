/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.base.phases

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.gui.rendering.Renderer
import kotlin.reflect.KClass

class RenderPhases<T : Renderer>(
    val type: KClass<T>,
    val skip: (T) -> Boolean,
    val setup: (T) -> Unit,
    val draw: (T) -> Unit,
) {

    fun invokeSkip(renderer: Renderer): Boolean {
        return skip.invoke(renderer.unsafeCast())
    }

    fun invokeSetup(renderer: Renderer) {
        setup.invoke(renderer.unsafeCast())
    }

    fun invokeDraw(renderer: Renderer) {
        draw.invoke(renderer.unsafeCast())
    }

    companion object {
        val OTHER = RenderPhases(OtherDrawable::class, { it.skipOther }, { it.setupOther() }, { it.drawOther() })
        val CUSTOM = RenderPhases(CustomDrawable::class, { it.skipCustom }, { }, { it.drawCustom() })
        val OPAQUE = RenderPhases(OpaqueDrawable::class, { it.skipOpaque }, { it.setupOpaque() }, { it.drawOpaque() })
        val TRANSPARENT = RenderPhases(TransparentDrawable::class, { it.skipTransparent }, { it.setupTransparent() }, { it.drawTransparent() })
        val TRANSLUCENT = RenderPhases(TranslucentDrawable::class, { it.skipTranslucent }, { it.setupTranslucent() }, { it.drawTranslucent() })


        val VALUES = arrayOf(TRANSPARENT, OTHER, CUSTOM, OPAQUE, TRANSLUCENT)
    }
}
