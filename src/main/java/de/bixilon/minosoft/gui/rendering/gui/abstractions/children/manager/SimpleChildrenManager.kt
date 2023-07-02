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

package de.bixilon.minosoft.gui.rendering.gui.abstractions.children.manager

import de.bixilon.minosoft.gui.rendering.gui.abstractions.CachedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.Element

class SimpleChildrenManager(val element: CachedElement) : ChildrenManager {
    private val children: MutableSet<Element> = mutableSetOf()

    override fun iterator() = children.iterator()

    override fun add(element: Element) {
        if (!children.add(element)) {
            throw IllegalStateException("$element was already a child!")
        }
        element.cache.onChildrenChange()
    }

    override fun remove(element: Element) {
        if (!children.remove(element)) {
            throw IllegalStateException("$element was not a child!")
        }
        element.cache.onChildrenChange()
    }

    override fun contains(element: Element) = element in children
}
