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

package de.bixilon.minosoft.data.registries.fallback.tags

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.cast.CollectionCast.asAnyCollection
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJsonObject
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.tool.ToolLevels
import de.bixilon.minosoft.data.registries.item.items.tool.axe.AxeItem
import de.bixilon.minosoft.data.registries.item.items.tool.hoe.HoeItem
import de.bixilon.minosoft.data.registries.item.items.tool.pickaxe.PickaxeItem
import de.bixilon.minosoft.data.registries.item.items.tool.shovel.ShovelItem
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.Registry
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.tags.MinecraftTagTypes.BLOCK
import de.bixilon.minosoft.tags.MinecraftTagTypes.ENTITY_TYPE
import de.bixilon.minosoft.tags.MinecraftTagTypes.FLUID
import de.bixilon.minosoft.tags.MinecraftTagTypes.GAME_EVENT
import de.bixilon.minosoft.tags.MinecraftTagTypes.ITEM
import de.bixilon.minosoft.tags.Tag
import de.bixilon.minosoft.tags.TagList
import de.bixilon.minosoft.tags.TagManager

object FallbackTags {
    private val tags: MutableMap<ResourceLocation, MutableMap<ResourceLocation, Set<ResourceLocation>>> = mutableMapOf()

    private fun read(type: ResourceLocation, name: ResourceLocation): Set<ResourceLocation> {
        val content = Minosoft.MINOSOFT_ASSETS_MANAGER[ResourceLocation(name.namespace, "tags/${type.path}/${name.path}.json")].readJsonObject()["values"].asAnyCollection()

        val set: MutableSet<ResourceLocation> = mutableSetOf()
        for (entry in content) {
            set += ResourceLocation.of(entry.toString())
        }
        return set
    }

    fun register(type: ResourceLocation, name: ResourceLocation) {
        val content = read(type, name)
        this.tags.getOrPut(type) { mutableMapOf() }[name] = content
    }

    fun load() {
        register(BLOCK, ToolLevels.STONE.tag!!)
        register(BLOCK, ToolLevels.IRON.tag!!)
        register(BLOCK, ToolLevels.DIAMOND.tag!!)

        register(BLOCK, AxeItem.TAG)
        register(BLOCK, HoeItem.TAG)
        register(BLOCK, PickaxeItem.TAG)
        register(BLOCK, ShovelItem.TAG)
    }

    private fun <T : RegistryItem> map(registry: Registry<T>, entries: MutableMap<ResourceLocation, Set<ResourceLocation>>): TagList<T> {
        val map: MutableMap<ResourceLocation, Tag<T>> = mutableMapOf()

        for ((name, entries) in entries) {
            val set: MutableSet<T> = mutableSetOf()
            for (item in entries) {
                set += registry[item] ?: continue
            }
            map[name] = Tag(set)
        }

        return TagList(map)
    }

    fun map(registries: Registries): TagManager {
        val map: MutableMap<ResourceLocation, TagList<*>> = mutableMapOf()

        this.tags[BLOCK]?.let { map[BLOCK] = map(registries.block, it) }
        this.tags[ITEM]?.let { map[ITEM] = map(registries.block, it) }
        this.tags[FLUID]?.let { map[FLUID] = map(registries.block, it) }
        this.tags[ENTITY_TYPE]?.let { map[ENTITY_TYPE] = map(registries.block, it) }
        this.tags[GAME_EVENT]?.let { map[GAME_EVENT] = map(registries.block, it) }

        return TagManager(map.unsafeCast())
    }
}
