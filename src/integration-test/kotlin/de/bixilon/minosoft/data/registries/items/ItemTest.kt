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

package de.bixilon.minosoft.data.registries.items

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.item.factory.ItemFactory
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.test.IT.reference
import org.testng.Assert
import org.testng.Assert.assertEquals

abstract class ItemTest<T : Item> {
    var item: T = unsafeNull()

    init {
        reference()
    }

    fun retrieveItem(name: ResourceLocation) {
        val item = IT.VERSION.registries!!.itemRegistry[name]
        Assert.assertNotNull(item)
        item!!
        assertEquals(item.identifier, name)
        this.item = item.unsafeCast()
    }

    fun retrieveItem(factory: ItemFactory<T>) {
        val item = IT.VERSION.registries!!.itemRegistry[factory]
        Assert.assertNotNull(item)
        item!!
        assertEquals(item.identifier, factory.identifier)
        this.item = item
    }
}
