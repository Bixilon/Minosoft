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

package de.bixilon.minosoft.data.entities.event

import de.bixilon.kutil.cast.CollectionCast.asAnyMap
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJsonObject
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.event.events.damage.*
import de.bixilon.minosoft.data.registries.entities.DefaultEntityFactories
import de.bixilon.minosoft.data.registries.factory.DefaultFactory
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.json.ResourceLocationJsonMap.toResourceLocationMap
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import kotlin.reflect.full.companionObject

object EntityEvents : DefaultFactory<EntityEvent<*>>(
    GenericDamageEvent,
    ThornsDamageEvent,
    FireDamageEvent,
    DrowningDamageEvent,
    SweetBerryBushDamageEvent,
    FreezeDamageEvent,
) {
    private val FILE = minosoft("mapping/entity_events.json")
    private val EVENTS: MutableMap<Class<*>, Int2ObjectOpenHashMap<EntityEvent<*>>> = mutableMapOf()

    fun get(version: Version, entity: Entity, id: Int): EntityEvent<*>? {
        entity::class.companionObject?.java?.let { c -> EVENTS[c]?.get(id)?.let { return it } }

        var clazz: Class<*>? = null
        while (true) {
            clazz = if (clazz == null) entity::class.java else clazz.superclass
            if (clazz == Object::class.java) break

            val map = EVENTS[clazz] ?: continue
            return map[id] ?: continue
        }
        return null
    }

    fun load() {
        val json = Minosoft.MINOSOFT_ASSETS_MANAGER[FILE].readJsonObject().toResourceLocationMap()
        for ((name, data) in json) {
            val clazz = DefaultEntityFactories.ABSTRACT_ENTITY_DATA_CLASSES[name]?.java ?: DefaultEntityFactories[name]?.javaClass // TODO: This is the companion class
            if (clazz == null) {
                Log.log(LogMessageType.LOADING, LogLevels.WARN) { "Entity Events: Entity for $name not found!" }
                continue
            }
            val map = Int2ObjectOpenHashMap<EntityEvent<*>>()
            val events = data.asJsonObject()["events"].asAnyMap()

            for ((key, value) in events) {
                map[key.toInt()] = this[value.toResourceLocation()] ?: continue
            }

            if (map.isEmpty()) continue
            EVENTS[clazz] = map
        }
    }
}
