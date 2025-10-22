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

package de.bixilon.minosoft.data.container.transaction

import de.bixilon.minosoft.data.container.ContainerTestUtil.createInventory
import de.bixilon.minosoft.data.container.TestItem1
import de.bixilon.minosoft.data.container.TestItem2
import de.bixilon.minosoft.data.container.stack.ItemStack
import org.testng.Assert.assertEquals
import org.testng.Assert.assertThrows
import org.testng.annotations.Test

@Test(groups = ["container"])
class ContainerTransactionTest {
    val A = ItemStack(TestItem1)
    val B = ItemStack(TestItem2)

    fun `commit empty transaction`() {
        val container = createInventory()
        val transaction = ContainerTransaction(container)
        val (id, changes) = transaction.commit()

        assertEquals(id, 0)
        assertEquals(changes, emptyMap<Int, ItemStack?>())
    }

    fun `drop empty transaction`() {
        val container = createInventory()
        val transaction = ContainerTransaction(container)
        transaction.drop()
    }

    fun `revert committed transaction`() {
        val container = createInventory()
        val transaction = ContainerTransaction(container)
        val (_, _) = transaction.commit()

        transaction.revert()
    }


    fun `non committed does not modify container`() {
        val container = createInventory()
        val transaction = ContainerTransaction(container)
        transaction[5] = A
        transaction.floating = B

        assertEquals(container.items[5], null)
        assertEquals(container.floating, null)
    }

    fun `commit create single item`() {
        val container = createInventory()
        val transaction = ContainerTransaction(container)
        transaction[5] = A

        val (_, changes) = transaction.commit()

        assertEquals(changes, mapOf(5 to A))
        assertEquals(container.items[5], A)
    }

    fun `commit modify single item`() {
        val container = createInventory()
        container.items[5] = A
        val transaction = ContainerTransaction(container)
        transaction[5] = B

        val (_, changes) = transaction.commit()

        assertEquals(changes, mapOf(5 to B))
        assertEquals(container.items[5], B)
    }

    fun `transaction retrieve non transaction item`() {
        val container = createInventory()
        container.items[5] = A
        val transaction = ContainerTransaction(container)

        assertEquals(transaction[5], A)
    }

    fun `commit delete item`() {
        val container = createInventory()
        container.items[5] = A
        val transaction = ContainerTransaction(container)
        transaction[5] = null

        val (_, changes) = transaction.commit()

        assertEquals(changes, mapOf(5 to null))
        assertEquals(container.items[5], null)
    }


    fun `commit deleted item not retrievable`() {
        val container = createInventory()
        container.items[5] = A
        val transaction = ContainerTransaction(container)
        transaction[5] = null

        assertEquals(transaction[5], null)
    }

    fun `commit create floating item`() {
        val container = createInventory()
        val transaction = ContainerTransaction(container)
        transaction.floating = A

        val (_, changes) = transaction.commit()

        assertEquals(container.floating, A)
        assertEquals(changes, emptyMap<Int, ItemStack?>())
    }

    fun `commit modify floating item`() {
        val container = createInventory()
        container.floating = A
        val transaction = ContainerTransaction(container)
        transaction.floating = B

        val (_, changes) = transaction.commit()

        assertEquals(container.floating, B)
        assertEquals(changes, emptyMap<Int, ItemStack?>())
    }

    fun `commit delete floating item`() {
        val container = createInventory()
        container.floating = A
        val transaction = ContainerTransaction(container)
        transaction.floating = null

        val (_, changes) = transaction.commit()

        assertEquals(container.floating, null)
        assertEquals(changes, emptyMap<Int, ItemStack?>())
    }

    fun `commit deleted floating not retrievable`() {
        val container = createInventory()
        container.floating = A
        val transaction = ContainerTransaction(container)
        transaction.floating = null

        assertEquals(transaction.floating, null)
    }

    fun `prevent double commit`() {
        val container = createInventory()
        val transaction = ContainerTransaction(container)
        transaction.commit()
        assertThrows { transaction.commit() }
    }

    fun `prevent revert of uncommitted`() {
        val container = createInventory()
        val transaction = ContainerTransaction(container)
        assertThrows { transaction.revert() }
    }

    fun `prevent drop of committed`() {
        val container = createInventory()
        val transaction = ContainerTransaction(container)
        transaction.commit()
        assertThrows { transaction.drop() }
    }
}
