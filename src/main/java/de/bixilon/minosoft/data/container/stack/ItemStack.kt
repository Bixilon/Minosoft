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
import de.bixilon.minosoft.data.registries.enchantment.Enchantment
import de.bixilon.minosoft.data.registries.item.items.DurableItem
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.versions.Version

data class ItemStack(
    val item: Item,
    val count: Int = 1,
    val display: DisplayProperty = DisplayProperty.DEFAULT,
    val durability: DurabilityProperty? = null,
    val enchanting: EnchantingProperty = EnchantingProperty.DEFAULT,
    val hide: HideProperty = HideProperty.DEFAULT,
    val nbt: NbtProperty = NbtProperty.DEFAULT,
) {

    init {
        assert(count > 0) { "Must habe positive stack count: $count" }
        if (durability != null) {
            assert(item is DurableItem) { "Can not have durability set when item is not durable" }
        }
    }

    val rarity: Rarities
        get() {
            val rarity = item.rarity
            if (enchanting.enchantments.isEmpty()) {
                return rarity
            }

            return when (rarity) {
                Rarities.COMMON, Rarities.UNCOMMON -> Rarities.RARE
                Rarities.RARE, Rarities.EPIC -> Rarities.EPIC
            }
        }

    fun getDisplayName(language: Translator?): ChatComponent {
        display.displayName?.let { return it }
        if (language != null) {
            val translated = language.forceTranslate(item.translationKey)
            rarity.color.let { color -> translated.setFallbackColor(color) }
            return translated
        }
        return ChatComponent.of(toString())
    }

    fun toNbt(version: Version, registries: Registries): JsonObject {
        val nbt: MutableJsonObject = mutableMapOf()

        // TODO: merge, not overwrite
        this.display.writeNbt(item, version, registries, nbt)
        this.durability?.writeNbt(item, version, registries, nbt)
        this.enchanting.writeNbt(item, version, registries, nbt)
        this.hide.writeNbt(item, version, registries, nbt)
        this.nbt.writeNbt(item, version, registries, nbt)

        return nbt
    }

    fun matches(other: ItemStack?): Boolean {
        if (other == null) return false
        return item == other.item && display == other.display && durability == other.durability && enchanting == other.enchanting && hide == other.hide && nbt == other.nbt
    }

    @Deprecated("final", level = DeprecationLevel.ERROR)
    fun copy(): Unit = Broken()

    @Deprecated("final", level = DeprecationLevel.ERROR)
    fun with(): Unit = Broken()

    fun with(count: Int = this.count, durability: Int? = this.durability?.durability): ItemStack? {
        if (count <= 0) return null

        var durabilityProperty = this.durability

        if (durability != null) {
            durabilityProperty = durabilityProperty?.copy(durability = durability) ?: DurabilityProperty(durability = durability)
        }

        return copy(count = count, durability = durabilityProperty)
    }

    fun with(enchantment: Enchantment, level: Int): ItemStack {
        return copy(enchanting = enchanting.with(enchantment, level))
    }

    override fun toString() = "Item(type=${item}, count=${count})"
}
