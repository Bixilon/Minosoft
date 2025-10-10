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
package de.bixilon.minosoft.data.container.stack

import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.minosoft.data.Rarities
import de.bixilon.minosoft.data.container.stack.properties.*
import de.bixilon.minosoft.data.language.translate.Translator
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.text.ChatComponent

data class ItemStack(
    val item: Item,
    val count: Int = 1,
    val display: DisplayProperty? = null,
    val durability: DurabilityProperty? = null,
    val enchanting: EnchantingProperty? = null,
    val hide: HideProperty? = null,
    val nbt: NbtProperty? = null,
) {

    val valid: Boolean
        get() {
            if (count <= 0) {
                return false
            }
            return durability?._valid != false
        }

    val rarity: Rarities
        get() {
            val itemRarity = item.rarity
            if (enchanting?.enchantments?.isEmpty() != false) {
                return itemRarity
            }

            return when (itemRarity) {
                Rarities.COMMON, Rarities.UNCOMMON -> Rarities.RARE
                Rarities.RARE, Rarities.EPIC -> Rarities.EPIC
            }
        }

    fun getDisplayName(language: Translator?): ChatComponent {
        display?.customDisplayName?.let { return it }
        if (language != null) {
            val translated = language.forceTranslate(item.translationKey)
            rarity.color.let { color -> translated.setFallbackColor(color) }
            return translated
        }
        return ChatComponent.of(toString())
    }

    fun toNbt(registries: Registries): JsonObject {
        val nbt: MutableJsonObject = mutableMapOf()

        // TODO: merge, not overwrite
        this.display?.toNbt(registries)?.let { nbt.putAll(it) }
        this.durability?.toNbt(registries)?.let { nbt.putAll(it) }
        this.enchanting?.toNbt(registries)?.let { nbt.putAll(it) }
        this.hide?.toNbt(registries)?.let { nbt.putAll(it) }
        this.nbt?.toNbt(registries)?.let { nbt.putAll(it) }

        return nbt
    }

    fun matches(other: ItemStack?): Boolean {
        if (other == null) return !valid
        return item == other.item && display == other.display && durability == other.durability && enchanting == other.enchanting && hide == other.hide && nbt == other.nbt
    }

    @Deprecated("final", level = DeprecationLevel.ERROR)
    fun copy(): Unit = Broken()

    override fun toString(): String {
        // this should not get synchronized, otherwise your debugger won't work that good:)
        return "Item{type=${item}, count=${count}}"
    }
}
