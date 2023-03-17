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
package de.bixilon.minosoft.data.registries.item.items

import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.Rarities
import de.bixilon.minosoft.data.language.LanguageUtil.translation
import de.bixilon.minosoft.data.language.translate.Translatable
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.gui.rendering.models.baked.item.BakedItemModel
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

abstract class Item(
    override val identifier: ResourceLocation,
) : RegistryItem(), Translatable {
    @Deprecated("interface")
    open val rarity: Rarities get() = Rarities.COMMON

    override val translationKey: ResourceLocation = identifier.translation("item")

    open var model: BakedItemModel? = null
    var tintProvider: TintProvider? = null

    override fun toString(): String {
        return identifier.toString()
    }

    override fun hashCode(): Int {
        return identifier.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Item) return false
        if (other.identifier != identifier) return false
        if (StaticConfiguration.REGISTRY_ITEM_COMPARE_CLASS && other::class.java != this::class.java) {
            Log.log(LogMessageType.OTHER, LogLevels.INFO) { "Mismatching class: ${other::class.java} vs ${this::class.java}, but same identifier: $identifier" }
        }
        return true
    }
}
