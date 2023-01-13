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
package de.bixilon.minosoft.data.container.stack

import de.bixilon.kutil.concurrent.lock.simple.ParentLock
import de.bixilon.kutil.concurrent.lock.thread.ThreadLock
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.data.Rarities
import de.bixilon.minosoft.data.container.SlotEdit
import de.bixilon.minosoft.data.container.stack.property.*
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

class ItemStack {
    val lock = ParentLock(lock = ThreadLock())
    val item: ItemProperty
    var holder: HolderProperty? = null

    var edit: SlotEdit? = null

    var _display: DisplayProperty? = null
        private set
    val display: DisplayProperty
        get() {
            _display?.let { return it }; return DisplayProperty(this).apply { _display = this }
        }
    var _durability: DurabilityProperty? = null
        private set
    val durability: DurabilityProperty
        get() {
            _durability?.let { return it }; return DurabilityProperty(this).apply { this@ItemStack._durability = this }
        }
    var _enchanting: EnchantingProperty? = null
    val enchanting: EnchantingProperty
        get() {
            _enchanting?.let { return it }; return EnchantingProperty(this).apply { _enchanting = this }
        }
    var _hide: HideProperty? = null
        private set
    val hide: HideProperty
        get() {
            _hide?.let { return it }; return HideProperty(this).apply { _hide = this }
        }
    var _nbt: NbtProperty? = null
        private set
    val nbt: NbtProperty
        get() {
            _nbt?.let { return it }; return NbtProperty(this).apply { _nbt = this }
        }

    var revision by observed(0L)

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
            if (_durability?._valid == false) {
                return false
            }
            return true
        }

    val rarity: Rarities
        get() {
            lock.acquire()
            val itemRarity = item.item.rarity
            try {
                if (_enchanting?.enchantments?.isEmpty() != false) {
                    return itemRarity
                }
            } finally {
                lock.release()
            }

            return when (itemRarity) {
                Rarities.COMMON, Rarities.UNCOMMON -> Rarities.RARE
                Rarities.RARE, Rarities.EPIC -> Rarities.EPIC
            }
        }

    val displayName: ChatComponent
        get() {
            _display?.customDisplayName?.let { return it }
            item.item.translationKey.let {
                val language = holder?.connection?.language ?: return@let
                val translated = language.translate(it)
                rarity.color.let { color -> translated.setFallbackColor(color) }
                return translated
            }
            return ChatComponent.of(toString())
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
        if (!this.display.updateNbt(nbt)) {
            _display = null
        }
        if (!this.durability.updateNbt(nbt)) {
            _durability = null
        }
        if (!this.enchanting.updateNbt(nbt)) {
            _enchanting = null
        }
        if (!this.hide.updateNbt(nbt)) {
            _hide = null
        }
        if (!this.nbt.updateNbt(nbt)) {
            _nbt = null
        }
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
        if (holder?.container?.edit != null) {
            return
        }
        if (edit == null) {
            edit = SlotEdit()
        }
    }

    fun commitChange() {
        val container = holder?.container
        if (!_valid) {
            container?.validate()
        }
        val containerEdit = container?.edit
        containerEdit?.let { it.addChange(); it.slots += this }
        val edit = edit
        if (edit != null) {
            return
        }
        lock.unlock()
        if (containerEdit != null) {
            return
        }
        revision++
        container?.apply { revision++ }
    }

    fun commit() {
        if (!_valid) {
            holder?.container?.validate()
        }

        val edit = edit ?: throw IllegalStateException("Not in edit mode!")
        this.edit = null
        lock.unlock()
        if (edit.changes > 0) {
            revision++
            holder?.container?.apply { revision++ }
        }
    }

    override fun hashCode(): Int {
        if (!_valid) {
            return 0
        }
        return Objects.hash(item, _display, _durability, _enchanting, _hide, _nbt)
    }

    private fun _equals(other: ItemStack): Boolean {
        return _display == other._display && _durability == other._durability && _enchanting == other._enchanting && _hide == other._hide && _nbt == other._nbt
    }

    override fun equals(other: Any?): Boolean {
        if (other == null && !this._valid) {
            return true
        }
        if (other !is ItemStack) {
            return false
        }
        if (!other._valid && !this._valid) {
            return true
        }
        if (other.hashCode() != this.hashCode()) {
            return false
        }
        return item == other.item && _equals(other)
    }

    fun matches(other: ItemStack?): Boolean {
        if (other == null) {
            return false
        }
        return item.item == other.item.item && _equals(other)
    }

    override fun toString(): String {
        // this should not get synchronized, otherwise your debugger won't work that good:)
        return "Item{type=${item.item}, count=${item._count}}"
    }
}
