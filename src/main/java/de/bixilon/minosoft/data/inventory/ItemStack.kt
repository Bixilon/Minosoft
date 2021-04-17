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
import de.bixilon.minosoft.data.mappings.Enchantment
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.items.Item
import de.bixilon.minosoft.data.mappings.versions.Version
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.util.BitByte
import de.bixilon.minosoft.util.KUtil.nullCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.compoundCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.getAndRemove
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast

class ItemStack(
    val item: Item,
    private val version: Version? = null,
) {
    val enchantments: MutableMap<Enchantment, Int> = mutableMapOf()
    val lore: MutableList<ChatComponent> = mutableListOf()
    var itemCount = 0
    var itemMetadata = 0
    var repairCost = 0
    var durability = 0
    var customDisplayName: ChatComponent? = null
    var isUnbreakable = false
    var hideFlags = 0
    private var additionalNBT: MutableMap<String, Any>? = null

    constructor(version: Version, item: Item, itemCount: Int, nbt: MutableMap<String, Any>?) : this(item, version) {
        this.itemCount = itemCount
        setNBT(nbt)
    }

    constructor(version: Version, item: Item, itemCount: Byte, itemMetadata: Int, nbt: MutableMap<String, Any>?) : this(item, version) {
        this.itemMetadata = itemMetadata
        this.itemCount = itemCount.toInt()
        setNBT(nbt)
    }

    constructor(version: Version, item: Item, itemCount: Int) : this(item, version) {
        this.itemCount = itemCount
    }

    private fun setNBT(nbt: MutableMap<String, Any>?) {
        if (nbt == null) {
            return
        }

        nbt.getAndRemove(REPAIR_COST_TAG)?.let {
            check(it is Number) { "Invalid $REPAIR_COST_TAG NBT data" }
            repairCost = it.toInt()
        }

        nbt.getAndRemove(DISPLAY_TAG)?.compoundCast()?.let {
            it.getAndRemove(DISPLAY_MAME_TAG)?.nullCast<String>()?.let { nameTag ->
                customDisplayName = ChatComponent.valueOf(translator = version?.localeManager, raw = nameTag)
            }

            it.getAndRemove(DISPLAY_LORE_TAG)?.listCast<String>()?.let { loreTag ->
                for (lore in loreTag) {
                    this.lore.add(ChatComponent.valueOf(translator = version?.localeManager, raw = lore))
                }
            }
        }

        nbt.getAndRemove(UNBREAKABLE_TAG)?.nullCast<Number>()?.let {
            isUnbreakable = it.toInt() == 0x01
        }

        nbt.getAndRemove(UNBREAKABLE_TAG)?.nullCast<Number>()?.let {
            isUnbreakable = it.toInt() == 0x01
        }

        nbt.getAndRemove(HIDE_FLAGS_TAG)?.nullCast<Number>()?.let {
            hideFlags = it.toInt()
        }

        nbt.getAndRemove(*ENCHANTMENTS_TAG)?.listCast<Map<String, Any>>()?.let {
            for (enchantmentTag in it) {
                val enchantment = enchantmentTag[ENCHANTMENT_ID_TAG]?.let { enchantmentId ->
                    when (enchantmentId) {
                        is Number -> {
                            version!!.mapping.enchantmentRegistry.get(enchantmentId.toInt())
                        }
                        is String -> {
                            version!!.mapping.enchantmentRegistry.get(ResourceLocation.getPathResourceLocation(enchantmentId))
                        }
                        else -> TODO()
                    }
                }!!
                enchantments[enchantment] = enchantmentTag[ENCHANTMENT_LEVEL_TAG]?.nullCast<Number>()?.toInt()!!
            }
        }
        additionalNBT = nbt
    }

    fun getNBT(): MutableMap<String, Any> {
        val nbt = additionalNBT?.toMutableMap() ?: mutableMapOf()
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

        if (isUnbreakable) {
            nbt[UNBREAKABLE_TAG] = true
        }

        if (hideFlags != 0) {
            nbt[HIDE_FLAGS_TAG] = hideFlags
        }
        if (enchantments.isNotEmpty()) {
            val enchantmentList: MutableList<Map<String, Any>> = mutableListOf()
            for ((enchantment, level) in enchantments) {
                val enchantmentTag: MutableMap<String, Any> = mutableMapOf()
                if (version!!.isFlattened()) {
                    enchantmentTag[ENCHANTMENT_ID_TAG] = enchantment.resourceLocation.full
                } else {
                    enchantmentTag[ENCHANTMENT_ID_TAG] = version.mapping.enchantmentRegistry.getId(enchantment)
                }

                enchantmentTag[ENCHANTMENT_LEVEL_TAG] = if (version.isFlattened()) {
                    level
                } else {
                    level.toShort()
                }
            }
            if (version!!.isFlattened()) {
                nbt[ENCHANTMENT_FLATTENING_TAG] = enchantmentList
            } else {
                nbt[ENCHANTMENT_PRE_FLATTENING_TAG] = enchantmentList
            }
        }
        return nbt
    }

    override fun equals(other: Any?): Boolean {
        if (super.equals(other)) {
            return true
        }
        if (other !is ItemStack?) {
            return false
        }
        if (other == null) {
            return false
        }

        // ToDo: check nbt
        return other.item == item && other.itemCount == itemCount && other.itemMetadata == itemMetadata
    }

    override fun toString(): String {
        return fullDisplayName
    }

    val displayName: ChatComponent
        get() {
            customDisplayName?.let { return it }
            item.translationKey?.let { version?.localeManager?.translate(it)?.let { translatedName -> return translatedName } }
            return ChatComponent.valueOf(raw = item.toString())
        }

    // ToDo all properties
    val fullDisplayName: String
        get() {
            val builder = StringBuilder()
            builder.append(displayName)
            builder.append('{')
            if (itemCount != 1) {
                builder.append("count: ")
                builder.append(itemCount)
                builder.append(", ")
            }
            if (enchantments.isNotEmpty()) {
                builder.append("enchantments: ")
                builder.append(enchantments.toString())
                builder.append(", ")
            }
            // ToDo all properties
            var endString = builder.toString()
            if (endString.endsWith(", ")) {
                endString = endString.substring(0, endString.length - 2)
            }
            endString += "}"
            if (endString.endsWith("{}")) {
                endString = endString.substring(0, endString.length - 2)
            }
            return endString
        }

    fun shouldHideEnchantments(): Boolean {
        return BitByte.isBitSet(hideFlags, HIDE_ENCHANTMENT_BIT)
    }

    fun shouldHideModifiers(): Boolean {
        return BitByte.isBitSet(hideFlags, HIDE_MODIFIERS_BIT)
    }

    fun shouldHideUnbreakable(): Boolean {
        return BitByte.isBitSet(hideFlags, HIDE_UNBREAKABLE_BIT)
    }

    fun shouldHideCanDestroy(): Boolean {
        return BitByte.isBitSet(hideFlags, HIDE_CAN_DESTROY_BIT)
    }

    fun shouldHideCanPlaceOn(): Boolean {
        return BitByte.isBitSet(hideFlags, HIDE_CAN_PLACE_BIT)
    }

    /**
     * @return hides other information, including potion effects, shield pattern info, "StoredEnchantments", written book "generation" and "author", "Explosion", "Fireworks", and map tooltips
     */
    fun shouldHideOtherInformation(): Boolean {
        return BitByte.isBitSet(hideFlags, HIDE_OTHER_INFORMATION_BIT)
    }

    fun shouldHideLeatherDyeColor(): Boolean {
        return BitByte.isBitSet(hideFlags, HIDE_LEATHER_DYE_COLOR_BIT)
    }

    private fun setHideFlag(bit: Int, setOrRemove: Boolean) {
        val mask = (1 shl bit)
        hideFlags = if (setOrRemove) {
            hideFlags or mask
        } else {
            hideFlags and mask.inv()
        }
    }

    fun setShouldHideEnchantments(hideEnchantments: Boolean) {
        setHideFlag(HIDE_ENCHANTMENT_BIT, hideEnchantments)
    }

    fun setShouldHideModifiers(hideModifiers: Boolean) {
        setHideFlag(HIDE_MODIFIERS_BIT, hideModifiers)
    }

    fun setShouldHideUnbreakable(hideUnbreakable: Boolean) {
        setHideFlag(HIDE_UNBREAKABLE_BIT, hideUnbreakable)
    }

    fun setShouldHideCanDestroy(hideCanDestroy: Boolean) {
        setHideFlag(HIDE_CAN_DESTROY_BIT, hideCanDestroy)
    }

    fun setShouldHideCanPlaceOn(hideCanPlaceOn: Boolean) {
        setHideFlag(HIDE_CAN_PLACE_BIT, hideCanPlaceOn)
    }

    /**
     * @ hides other information, including potion effects, shield pattern info, "StoredEnchantments", written book "generation" and "author", "Explosion", "Fireworks", and map tooltips
     */
    fun setShouldHideOtherInformation(hideOtherInformation: Boolean) {
        setHideFlag(HIDE_OTHER_INFORMATION_BIT, hideOtherInformation)
    }

    fun setShouldHideLeatherDyeColor(hideLeatherDyeColor: Boolean) {
        setHideFlag(HIDE_LEATHER_DYE_COLOR_BIT, hideLeatherDyeColor)
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
