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

package de.bixilon.minosoft.data.registries.items

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.item.items.DurableItem
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.pixlyzer.PixLyzerItem
import de.bixilon.minosoft.data.registries.registries.PixLyzerUtil
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log

object VerifyIntegratedItemRegistry {

    private fun compareDurability(pixlyzer: PixLyzerItem, integrated: Item, errors: StringBuilder) {
        if (pixlyzer.maxDurability > 0 != integrated is DurableItem) {
            errors.append("Durability ability: ${pixlyzer.identifier}")
            return
        }
        if (pixlyzer.maxDurability == 0) return
        integrated as DurableItem
        if (pixlyzer.maxDurability != integrated.maxDurability) {
            errors.append("Durability mismatch: ${pixlyzer.identifier}. e=${pixlyzer.maxDurability}, a=${integrated.maxDurability}")
        }
    }


    private fun compare(pixlyzer: PixLyzerItem, integrated: Item, errors: StringBuilder) {
        compareDurability(pixlyzer, integrated, errors)
    }

    fun verify(registries: Registries, version: Version) {
        val error = StringBuilder()
        val data = PixLyzerUtil.load(ResourcesProfile(), version)["items"]!!.unsafeCast<Map<String, JsonObject>>()

        for ((id, value) in data) {
            if (value["class"] == "AirBlock") {
                continue
            }
            val identifier = id.toResourceLocation()
            val integrated = registries.item[identifier] ?: Broken("Item $id does not exist in the registry?")
            if (integrated is PixLyzerItem) {
                // useless to compare
                continue
            }
            val parsed = PixLyzerItem.deserialize(registries, identifier, value).unsafeCast<PixLyzerItem>()

            parsed.postInit(registries)
            parsed.inject(registries)


            compare(parsed, integrated, error)
        }


        if (error.isEmpty()) {
            return
        }
        error.removePrefix("\n")
        Log.ERROR_PRINT_STREAM.println(error)
        throw AssertionError("Does not match, see above!")
    }
}
