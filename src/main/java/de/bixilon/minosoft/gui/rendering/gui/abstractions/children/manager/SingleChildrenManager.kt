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

import de.bixilon.kutil.collections.iterator.SingleIterator
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import java.util.Collections.emptyIterator


class SingleChildrenManager(private var element: Element? = null) : ChildrenManager {

    override fun remove(element: Element) = Broken("I must have children!")

    override fun add(element: Element) {
        if (this.element == null) {
            this.element = element
            return
        }
        throw IllegalArgumentException("Not my child!")
    }

    override fun contains(element: Element) = element === this.element

    override fun iterator(): Iterator<Element> {
        val element = this.element ?: return emptyIterator()
        return SingleIterator(element)
    }
}
