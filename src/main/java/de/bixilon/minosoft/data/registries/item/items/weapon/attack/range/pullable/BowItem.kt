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

package de.bixilon.minosoft.data.registries.item.items.weapon.attack.range.pullable

import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.enchantment.tool.weapon.WeaponEnchantment
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.factory.ItemFactory
import de.bixilon.minosoft.data.registries.item.handler.item.LongItemUseHandler
import de.bixilon.minosoft.data.registries.item.handler.item.LongUseResults
import de.bixilon.minosoft.data.registries.item.items.weapon.attack.range.RangeWeapon
import de.bixilon.minosoft.data.registries.registries.Registries

open class BowItem(identifier: ResourceLocation = this.identifier) : RangeWeapon(identifier), LongItemUseHandler {

    override fun startUse(player: LocalPlayerEntity, hand: Hands, stack: ItemStack): LongUseResults {
        if (player.gamemode == Gamemodes.CREATIVE) {
            // infinite arrows
            return LongUseResults.START
        }
        if (stack._enchanting?.enchantments?.get(WeaponEnchantment.Infinity) != null) {
            return LongUseResults.START
        }
        // TODO: check if player has arrows

        return LongUseResults.START
    }

    override fun finishUse(player: LocalPlayerEntity, hand: Hands, stack: ItemStack, ticks: Int) {
        // ToDO: check time, consume arrow, ...
    }

    companion object : ItemFactory<BowItem> {
        override val identifier = minecraft("bow")

        override fun build(registries: Registries, data: JsonObject) = BowItem()
    }
}
