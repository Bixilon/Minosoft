/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.recipes

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.ResourceLocationAble
import de.bixilon.minosoft.data.registries.registries.registry.AbstractRegistry
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class RecipeRegistry(
    override var parent: AbstractRegistry<Recipe>? = null,
) : AbstractRegistry<Recipe> {
    private val idValueMap: MutableMap<Int, Recipe> = mutableMapOf()
    private val valueIdMap: MutableMap<Recipe, Int> = mutableMapOf()
    private val resourceLocationRecipeMap: MutableMap<ResourceLocation, Recipe> = mutableMapOf()
    private val recipeResourceLocationMap: MutableMap<Recipe, ResourceLocation> = mutableMapOf()

    override val size: Int
        get() = (parent?.size ?: 0) + maxOf(idValueMap.size, recipeResourceLocationMap.size)

    override fun get(any: Any?): Recipe? {
        return when (any) {
            null -> null
            is Number -> get(any.toInt())
            is ResourceLocation -> resourceLocationRecipeMap[any]
            is String -> get(any.toResourceLocation())
            is ResourceLocationAble -> get(any.resourceLocation)
            else -> TODO()
        } ?: parent?.get(any)
    }

    override fun get(id: Int): Recipe? {
        return idValueMap[id] ?: parent?.get(id)
    }

    override fun getId(value: Recipe): Int {
        return valueIdMap[value] ?: parent?.getId(value) ?: throw IllegalArgumentException("No id available for $value")
    }

    override fun noParentIterator(): Iterator<Recipe> {
        return resourceLocationRecipeMap.values.iterator() // ToDo
    }

    override fun clear() {
        idValueMap.clear()
        valueIdMap.clear()
        resourceLocationRecipeMap.clear()
        recipeResourceLocationMap.clear()
    }

    fun getResourceLocation(recipe: Recipe): ResourceLocation? {
        return recipeResourceLocationMap[recipe]
    }

    fun add(id: Int?, resourceLocation: ResourceLocation?, recipe: Recipe) {
        if (id != null) {
            idValueMap[id] = recipe
            valueIdMap[recipe] = id
        }
        if (resourceLocation != null) {
            resourceLocationRecipeMap[resourceLocation] = recipe
            recipeResourceLocationMap[recipe] = resourceLocation
        }
    }
}