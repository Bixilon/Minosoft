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

package de.bixilon.minosoft.gui.rendering.gui.abstractions.update

import de.bixilon.minosoft.gui.rendering.gui.abstractions.CachedElement
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.ChildedElement
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.ParentedElement

interface UpdatableElement : CachedElement {
    var update: Boolean

    fun invalidate() {
        if (update) return
        update = true
        cache.invalidate()
        invalidateParent()
    }

    private fun invalidateParent() {
        if (this !is ParentedElement) return
        val parent = this.parent
        if (parent !is UpdatableElement) return
        parent.invalidate()
    }

    fun update() {
        if (this !is ChildedElement) return
        for (child in children) {
            child.update()
        }
        update = false
    }
}
