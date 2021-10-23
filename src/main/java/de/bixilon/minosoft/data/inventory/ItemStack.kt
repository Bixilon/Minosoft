/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.inventory

import de.bixilon.minosoft.data.Rarities
import de.bixilon.minosoft.data.inventory.ItemNBTValues.DISPLAY_LORE_TAG
import de.bixilon.minosoft.data.inventory.ItemNBTValues.DISPLAY_MAME_TAG
import de.bixilon.minosoft.data.inventory.ItemNBTValues.DISPLAY_TAG
import de.bixilon.minosoft.data.inventory.ItemNBTValues.ENCHANTMENTS_TAG
import de.bixilon.minosoft.data.inventory.ItemNBTValues.ENCHANTMENT_FLATTENING_TAG
import de.bixilon.minosoft.data.inventory.ItemNBTValues.ENCHANTMENT_ID_TAG
import de.bixilon.minosoft.data.inventory.ItemNBTValues.ENCHANTMENT_LEVEL_TAG
import de.bixilon.minosoft.data.inventory.ItemNBTValues.ENCHANTMENT_PRE_FLATTENING_TAG
import de.bixilon.minosoft.data.inventory.ItemNBTValues.HIDE_FLAGS_TAG
import de.bixilon.minosoft.data.inventory.ItemNBTValues.REPAIR_COST_TAG
import de.bixilon.minosoft.data.inventory.ItemNBTValues.UNBREAKABLE_TAG
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.enchantment.Enchantment
import de.bixilon.minosoft.data.registries.items.Item
import de.bixilon.minosoft.data.registries.other.containers.Container
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextFormattable
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.BitByte.isBit
import de.bixilon.minosoft.util.KUtil.nullCast
import de.bixilon.minosoft.util.KUtil.synchronizedDeepCopy
import de.bixilon.minosoft.util.KUtil.synchronizedListOf
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.KUtil.toSynchronizedList
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.compoundCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.getAndRemove
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast
import java.util.*

class ItemStack(
    val item: Item,
    private val connection: PlayConnection? = null,
    count: Int = 1,
    val enchantments: MutableMap<Enchantment, Int> = synchronizedMapOf(),
    val lore: MutableList<ChatComponent> = synchronizedListOf(),
    repairCost: Int = 0,
    customDisplayName: ChatComponent? = null,
    unbreakable: Boolean = false,
    durability: Int = 0,
    val nbt: MutableMap<String, Any> = synchronizedMapOf(),
    container: Container? = null,
    hideFlags: Int = 0,
) : TextFormattable {
    var count = count
        set(value) {
            if (field == value) {
                return
            }
            field = value
            apply()
        }
    var repairCost = repairCost
        set(value) {
            if (field == value) {
                return
            }
            field = value
            apply()
        }
    var customDisplayName = customDisplayName
        set(value) {
            if (field == value) {
                return
            }
            field = value
            apply()
        }
    var unbreakable = unbreakable
        set(value) {
            if (field == value) {
                return
            }
            field = value
            apply()
        }
    var durability = durability
        set(value) {
            if (field == value) {
                return
            }
            field = value
            apply()
        }
    var hideFlags = hideFlags
        set(value) {
            if (field == value) {
                return
            }
            field = value
            apply()
        }
    var container = container
        set(value) {
            if (field != null && value != null) {
                throw IllegalStateException("Item already in a different container!")
            }
            if (field === value) {
                return
            }
            field = value
            apply()
        }

    // ToDo: Apply if enchantments, lore or nbt changes

    init {
        parseNBT(nbt)
    }

    fun copy(
        item: Item = this.item,
        connection: PlayConnection? = this.connection,
        count: Int = this.count,
        enchantments: MutableMap<Enchantment, Int> = this.enchantments.toSynchronizedMap(),
        lore: MutableList<ChatComponent> = this.lore.toSynchronizedList(),
        repairCost: Int = this.repairCost,
        customDisplayName: ChatComponent? = this.customDisplayName,
        unbreakable: Boolean = this.unbreakable,
        durability: Int = this.durability,
        nbt: MutableMap<String, Any> = this.nbt.synchronizedDeepCopy()!!,
        container: Container? = this.container,
        hideFlags: Int = this.hideFlags,
    ): ItemStack {
        return ItemStack(
            item = item,
            connection = connection,
            count = count,
            enchantments = enchantments,
            lore = lore,
            repairCost = repairCost,
            customDisplayName = customDisplayName,
            unbreakable = unbreakable,
            durability = durability,
            nbt = nbt,
            container = container,
            hideFlags = hideFlags,
        )
    }

    fun apply() {
        val container = container ?: return
        val previousRevision = container.revision
        container.validate()
        if (previousRevision <= container.revision) {
            container.revision++
        }
    }

    override fun toText(): ChatComponent {
        return ChatComponent.of("$item {name=${displayName}, count=$count. nbt=$nbt}")
    }

    override fun toString(): String {
        return toText().legacyText
    }

    private fun parseNBT(nbt: MutableMap<String, Any>?) {
        nbt ?: return

        nbt.getAndRemove(REPAIR_COST_TAG).nullCast<Number>()?.let { repairCost = it.toInt() }

        nbt.getAndRemove(DISPLAY_TAG)?.compoundCast()?.let {
            it.getAndRemove(DISPLAY_MAME_TAG).nullCast<String>()?.let { nameTag ->
                customDisplayName = ChatComponent.of(nameTag, translator = connection?.version?.language)
            }

            it.getAndRemove(DISPLAY_LORE_TAG)?.listCast<String>()?.let { loreTag ->
                for (lore in loreTag) {
                    this.lore.add(ChatComponent.of(lore, translator = connection?.version?.language))
                }
            }
        }

        nbt.getAndRemove(UNBREAKABLE_TAG).nullCast<Number>()?.let { unbreakable = it.toInt() == 0x01 }

        nbt.getAndRemove(HIDE_FLAGS_TAG).nullCast<Number>()?.let { hideFlags = it.toInt() }

        nbt.getAndRemove(*ENCHANTMENTS_TAG)?.listCast<Map<String, Any>>()?.let {
            val enchantmentRegistry = connection!!.registries.enchantmentRegistry
            for (enchantmentTag in it) {
                val enchantment = enchantmentTag[ENCHANTMENT_ID_TAG]?.let { enchantmentId ->
                    when (enchantmentId) {
                        is Number -> enchantmentRegistry[enchantmentId.toInt()]
                        is String -> enchantmentRegistry[ResourceLocation.getPathResourceLocation(enchantmentId)]
                        else -> TODO()
                    }
                }!!
                enchantments[enchantment] = enchantmentTag[ENCHANTMENT_LEVEL_TAG]!!.toInt()
            }
        }
    }

    val nbtOut: MutableMap<String, Any>
        get() {
            val nbt = nbt.toMutableMap()
            if (repairCost != 0) {
                nbt[REPAIR_COST_TAG] = repairCost
            }
            mutableMapOf<String, Any>().let {
                customDisplayName?.let { displayName ->
                    it[DISPLAY_MAME_TAG] = displayName.legacyText
                }
                mutableListOf<String>().let { loreTag ->
                    for (lore in this.lore) {
                        loreTag.add(lore.legacyText)
                    }

                    if (loreTag.isNotEmpty()) {
                        it[DISPLAY_LORE_TAG] = loreTag
                    }
                }

                if (it.isNotEmpty()) {
                    nbt[DISPLAY_TAG] = it
                }
            }

            if (unbreakable) {
                nbt[UNBREAKABLE_TAG] = true
            }

            if (hideFlags != 0) {
                nbt[HIDE_FLAGS_TAG] = hideFlags
            }
            if (enchantments.isNotEmpty()) {
                val enchantmentList: MutableList<Map<String, Any>> = mutableListOf()
                for ((enchantment, level) in enchantments) {
                    val enchantmentTag: MutableMap<String, Any> = mutableMapOf()
                    enchantmentTag[ENCHANTMENT_ID_TAG] = if (connection!!.version.isFlattened()) {
                        enchantment.resourceLocation.full
                    } else {
                        connection.registries.enchantmentRegistry.getId(enchantment)
                    }

                    enchantmentTag[ENCHANTMENT_LEVEL_TAG] = if (connection.version.isFlattened()) {
                        level
                    } else {
                        level.toShort()
                    }
                }
                if (connection!!.version.isFlattened()) {
                    nbt[ENCHANTMENT_FLATTENING_TAG] = enchantmentList
                } else {
                    nbt[ENCHANTMENT_PRE_FLATTENING_TAG] = enchantmentList
                }
            }
            return nbt
        }

    val displayName: ChatComponent
        get() {
            customDisplayName?.let { return it }
            item.translationKey?.let {
                connection?.version?.language?.translate(it)?.let { translatedName ->
                    translatedName.applyDefaultColor(rarity.color)
                    return translatedName
                }
            }
            return ChatComponent.of(item.toString())
        }

    var shouldHideEnchantments: Boolean
        get() = hideFlags.isBit(HIDE_ENCHANTMENT_BIT)
        set(value) = setHideFlag(HIDE_ENCHANTMENT_BIT, value)

    var shouldHideModifiers: Boolean
        get() = hideFlags.isBit(HIDE_MODIFIERS_BIT)
        set(value) = setHideFlag(HIDE_MODIFIERS_BIT, value)

    var shouldHideUnbreakable: Boolean
        get() = hideFlags.isBit(HIDE_UNBREAKABLE_BIT)
        set(value) = setHideFlag(HIDE_UNBREAKABLE_BIT, value)

    var shouldHideCanDestroy: Boolean
        get() = hideFlags.isBit(HIDE_CAN_DESTROY_BIT)
        set(value) = setHideFlag(HIDE_CAN_DESTROY_BIT, value)

    var shouldHideCanPlaceOn: Boolean
        get() = hideFlags.isBit(HIDE_CAN_PLACE_BIT)
        set(value) = setHideFlag(HIDE_CAN_PLACE_BIT, value)

    /**
     * @return hides other information, including potion effects, shield pattern info, "StoredEnchantments", written book "generation" and "author", "Explosion", "Fireworks", and map tooltips
     */
    var shouldHideOtherInformation: Boolean
        get() = hideFlags.isBit(HIDE_OTHER_INFORMATION_BIT)
        set(value) = setHideFlag(HIDE_OTHER_INFORMATION_BIT, value)


    var shouldHideLeatherDyeColor: Boolean
        get() = hideFlags.isBit(HIDE_LEATHER_DYE_COLOR_BIT)
        set(value) = setHideFlag(HIDE_LEATHER_DYE_COLOR_BIT, value)

    private fun setHideFlag(bit: Int, setOrRemove: Boolean) {
        val mask = (1 shl bit)
        hideFlags = if (setOrRemove) {
            hideFlags or mask
        } else {
            hideFlags and mask.inv()
        }
    }

    val rarity: Rarities
        get() {
            if (enchantments.isEmpty()) {
                return item.rarity
            }
            return when (item.rarity) {
                Rarities.COMMON, Rarities.UNCOMMON -> Rarities.RARE
                Rarities.RARE, Rarities.EPIC -> Rarities.EPIC
            }
        }

    val damageable: Boolean
        get() = item.maxDamage > 0 || !unbreakable


    override fun hashCode(): Int {
        return Objects.hash(item, count, durability, nbt)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is ItemStack) {
            return false
        }
        if (hashCode() != other.hashCode()) {
            return false
        }
        return item == other.item
                && count == other.count
                && durability == other.durability
                && enchantments == other.enchantments
                && nbt == other.nbt
                && lore == other.lore
                && customDisplayName == other.customDisplayName
                && repairCost == other.repairCost
                && unbreakable == other.unbreakable
                && hideFlags == other.hideFlags
    }

    companion object {
        private const val HIDE_ENCHANTMENT_BIT = 0
        private const val HIDE_MODIFIERS_BIT = 1
        private const val HIDE_UNBREAKABLE_BIT = 2
        private const val HIDE_CAN_DESTROY_BIT = 3
        private const val HIDE_CAN_PLACE_BIT = 4
        private const val HIDE_OTHER_INFORMATION_BIT = 5
        private const val HIDE_LEATHER_DYE_COLOR_BIT = 6
    }
}
