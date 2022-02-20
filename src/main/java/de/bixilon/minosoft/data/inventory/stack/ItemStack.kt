/*
 * Minosoft
 * Copyright (C) 2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.inventory.stack

import de.bixilon.kutil.concurrent.lock.ParentLock
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.minosoft.data.inventory.stack.property.*
import de.bixilon.minosoft.data.registries.items.Item
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

class ItemStack {
    val lock = ParentLock()
    val item: ItemProperty
    var holder: HolderProperty? = null

    var _display: DisplayProperty? = null
        private set
    val display: DisplayProperty by lazy { _display?.let { return@lazy it }; return@lazy DisplayProperty(this).apply { _display = this } }
    var _durability: DurabilityProperty? = null
        private set
    val durability: DurabilityProperty by lazy { _durability?.let { return@lazy it }; return@lazy DurabilityProperty(this).apply { this@ItemStack._durability = this } }
    var _enchanting: EnchantingProperty? = null
    val enchanting: EnchantingProperty by lazy { _enchanting?.let { return@lazy it }; return@lazy EnchantingProperty(this).apply { _enchanting = this } }
    var _hide: HideProperty? = null
        private set
    val hide: HideProperty by lazy { _hide?.let { return@lazy it }; return@lazy HideProperty(this).apply { _hide = this } }
    var _nbt: NbtProperty? = null
        private set
    val nbt: NbtProperty by lazy { _nbt?.let { return@lazy it }; return@lazy NbtProperty(this).apply { _nbt = this } }

    init {
        val container = holder?.container
        if (container != null) {
            lock += container.lock
        }
    }


    constructor(item: Item, count: Int = 1) {
        this.item = ItemProperty(this, item, count)
    }

    constructor(
        item: ItemProperty,
        holder: HolderProperty? = null,
        display: DisplayProperty? = null,
        durability: DurabilityProperty? = null,
        enchanting: EnchantingProperty? = null,
        hide: HideProperty? = null,
        nbt: NbtProperty? = null,
    ) {
        this.item = item
        this.holder = holder
        this._display = display
        this._durability = durability
        this._enchanting = enchanting
        this._hide = hide
        this._nbt = nbt
    }

    val _valid: Boolean
        get() {
            if (item._count <= 0) {
                return false
            }
            if (!durability._valid) {
                return false
            }
            return true
        }

    override fun hashCode(): Int {
        return Objects.hash(item, _display, _durability, _enchanting, _hide, _nbt)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ItemStack) {
            return false
        }
        if (other.hashCode() != this.hashCode()) {
            return false
        }
        return item == other.item && _display == other._display && _durability == other._durability && _enchanting == other._enchanting && _hide == other._hide && _nbt == other._nbt
    }

    override fun toString(): String {
        // this should not get synchronized, otherwise your debugger won't work that good:)
        return "Item{type=${item.item}, count=${item._count}}"
    }

    fun copy(
        item: Item = this.item.item,
        count: Int = this.item.count,

        connection: PlayConnection? = holder?.connection,

        durability: Int? = _durability?.durability,

        nbt: MutableJsonObject? = _nbt?.nbt,
    ): ItemStack {
        val stack = ItemStack(item, count)
        if (connection != null) {
            stack.holder = HolderProperty(connection = connection)
        }
        stack._display = _display?.copy(stack)
        stack._durability = _durability?.copy(stack)
        if (durability != null) {
            stack.durability._durability = durability
        }
        stack._enchanting = _enchanting?.copy(stack)
        stack._hide = _hide?.copy(stack)
        stack._nbt = _nbt?.copy(stack)
        if (nbt != null) {
            stack._nbt?.nbt?.putAll(nbt)
        }

        return stack
    }

    fun updateNbt(nbt: MutableJsonObject?) {
        if (nbt == null || nbt.isEmpty()) {
            return
        }
        // ToDo: This force creates an instance of every property
        display.updateNbt(nbt)
        durability.updateNbt(nbt)
        enchanting.updateNbt(nbt)
        hide.updateNbt(nbt)
        this.nbt.updateNbt(nbt)
    }

    fun getNBT(): JsonObject {
        val nbt: MutableJsonObject = mutableMapOf()
        // ToDo: This overwrites the previous nbt
        _display?.getNBT()?.let { nbt.putAll(it) }
        _durability?.getNBT()?.let { nbt.putAll(it) }
        _enchanting?.getNBT()?.let { nbt.putAll(it) }
        _hide?.getNBT()?.let { nbt.putAll(it) }
        _nbt?.getNBT()?.let { nbt.putAll(it) }

        return nbt
    }

    fun lock() {
        lock.lock()
    }

    fun commit() {
        if (!_valid) {
            holder?.container?._validate()
        }
        lock.unlock()
        holder?.container?.apply { revision++ } // increase revision after unlock to prevent deadlock
    }
}
