/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.mappings.versions

import com.google.common.collect.HashBiMap
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.EntityClassMappings
import de.bixilon.minosoft.data.entities.EntityInformation
import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.mappings.*
import de.bixilon.minosoft.data.mappings.biomes.Biome
import de.bixilon.minosoft.data.mappings.biomes.BiomeCategory
import de.bixilon.minosoft.data.mappings.biomes.BiomePrecipation
import de.bixilon.minosoft.data.mappings.blocks.Block
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.items.ItemRegistry
import de.bixilon.minosoft.data.mappings.particle.Particle
import de.bixilon.minosoft.data.mappings.statistics.Statistic
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModel
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.json.ResourceLocationJsonMap
import java.util.*


class VersionMapping(var version: Version?) {

    val motiveRegistry: Registry<Motive> = Registry(initialSize = 30)
    val blockRegistry: Registry<Block> = Registry(initialSize = 1000)
    val itemRegistry: ItemRegistry = ItemRegistry(initialSize = 1200)
    val enchantmentRegistry: Registry<Enchantment> = Registry(initialSize = 50)
    val particleRegistry: Registry<Particle> = Registry(initialSize = 70)
    val mobEffectRegistry: Registry<MobEffect> = Registry(initialSize = 40)
    val statisticRegistry: Registry<Statistic> = Registry(initialSize = 80)
    val biomeRegistry: Registry<Biome> = Registry(initialSize = 30)
    val dimensionRegistry: Registry<Dimension> = Registry(initialSize = 5)

    val biomePrecipationRegistry: EnumRegistry<BiomePrecipation> = EnumRegistry(initialSize = 5)
    val biomeCategoryRegistry: EnumRegistry<BiomeCategory> = EnumRegistry(initialSize = 5)


    internal val blockStateIdMap: HashBiMap<Int, BlockState> = HashBiMap.create(20000)

    private val entityInformationMap = HashBiMap.create<Class<out Entity>, EntityInformation>(120)
    private val entityMetaIndexMap = HashMap<EntityMetaDataFields, Int>(180)
    private val entityIdClassMap = HashBiMap.create<Int, Class<out Entity?>>(120)

    internal val models = HashBiMap.create<ResourceLocation, BlockModel>(500)


    var isFullyLoaded = false
        private set

    private var _parentMapping: VersionMapping? = null

    var parentMapping: VersionMapping?
        get() = _parentMapping
        set(value) {
            _parentMapping = value
            motiveRegistry.setParent(value?.motiveRegistry)
            itemRegistry.setParent(value?.itemRegistry)
            enchantmentRegistry.setParent(value?.enchantmentRegistry)
            particleRegistry.setParent(value?.particleRegistry)
            mobEffectRegistry.setParent(value?.mobEffectRegistry)
            blockRegistry.setParent(value?.blockRegistry)
            statisticRegistry.setParent(value?.statisticRegistry)
            biomeRegistry.setParent(value?.biomeRegistry)
            dimensionRegistry.setParent(value?.dimensionRegistry)
            biomePrecipationRegistry.setParent(value?.biomePrecipationRegistry)
            biomeCategoryRegistry.setParent(value?.biomeCategoryRegistry)
        }


    fun getBlockState(blockState: Int): BlockState? {
        if (blockState == ProtocolDefinition.NULL_BLOCK_ID) {
            return null
        }
        return blockStateIdMap[blockState] ?: _parentMapping?.getBlockState(blockState)
    }

    fun getBlockId(blockState: BlockState): Int {
        if (blockState.owner.resourceLocation == ProtocolDefinition.AIR_RESOURCE_LOCATION) {
            return ProtocolDefinition.NULL_BLOCK_ID
        }
        return blockStateIdMap.inverse()[blockState] ?: _parentMapping?.getBlockId(blockState)!!
    }


    fun getEntityInformation(clazz: Class<out Entity?>): EntityInformation? {
        return entityInformationMap[clazz] ?: _parentMapping?.getEntityInformation(clazz)
    }

    fun getEntityMetaDataIndex(field: EntityMetaDataFields): Int? {
        return entityMetaIndexMap[field] ?: _parentMapping?.getEntityMetaDataIndex(field)
    }

    fun getEntityClassById(entityTypeId: Int): Class<out Entity?>? {
        return entityIdClassMap[entityTypeId] ?: _parentMapping?.getEntityClassById(entityTypeId)
    }


    fun load(pixlyzerData: JsonObject) {
        // pre init stuff
        loadBlockModels(ResourceLocationJsonMap.create(pixlyzerData["models"].asJsonObject))

        // id stuff
        biomeCategoryRegistry.initialize(pixlyzerData["biome_categories"]?.asJsonObject, this, BiomeCategory.Companion)
        biomePrecipationRegistry.initialize(pixlyzerData["biome_precipations"]?.asJsonObject, this, BiomePrecipation.Companion)

        // id resource location stuff
        motiveRegistry.initialize(pixlyzerData["motives"]?.asJsonObject, this, Motive.Companion, version!!.isFlattened())
        blockRegistry.initialize(pixlyzerData["blocks"]?.asJsonObject, this, Block.Companion, version!!.isFlattened(), Registry.MetaTypes.BITS_4)
        itemRegistry.initialize(pixlyzerData["items"]?.asJsonObject, this, Item.Companion, version!!.isFlattened(), Registry.MetaTypes.BITS_16)
        enchantmentRegistry.initialize(pixlyzerData["enchantments"]?.asJsonObject, this, Enchantment.Companion)
        particleRegistry.initialize(pixlyzerData["particles"]?.asJsonObject, this, Particle.Companion)
        mobEffectRegistry.initialize(pixlyzerData["mob_effect"]?.asJsonObject, this, MobEffect.Companion)
        biomeRegistry.initialize(pixlyzerData["biomes"]?.asJsonObject, this, Biome.Companion)
        dimensionRegistry.initialize(pixlyzerData["dimensions"]?.asJsonObject, this, Dimension.Companion)

        loadEntities(pixlyzerData["entities"]?.asJsonObject)
        // post init
        biomeRegistry.postInit(this)
        isFullyLoaded = true
    }

    private fun loadEntities(data: JsonObject?) {
        if (data == null) {
            return
        }

        for ((resourceLocationName, entity) in data.entrySet()) {
            check(entity is JsonObject)
            val resourceLocation = ResourceLocation(resourceLocationName)
            EntityClassMappings.getByResourceLocation(resourceLocation)?.let {
                // not abstract
                entityInformationMap[it] = EntityInformation.deserialize(resourceLocation, entity)
                entityIdClassMap[entity["id"].asInt] = it
            }
            entity["meta"]?.asJsonObject?.let {
                for ((minosoftFieldName, index) in it.entrySet()) {
                    val minosoftField = EntityMetaDataFields.valueOf(minosoftFieldName)
                    entityMetaIndexMap[minosoftField] = index.asInt
                }
            }

        }
    }


    private fun loadBlockModels(data: Map<ResourceLocation, JsonObject>) {
        for ((resourceLocation, model) in data) {
            if (models.containsKey(resourceLocation)) {
                continue
            }
            loadBlockModel(resourceLocation, model, data)
        }
    }

    private fun loadBlockModel(resourceLocation: ResourceLocation, modelData: JsonObject, fullModelData: Map<ResourceLocation, JsonObject>): BlockModel {
        var model = models[resourceLocation]
        model?.let {
            return it
        }
        var parent: BlockModel? = null
        modelData["parent"]?.asString?.let {
            val parentResourceLocation = ResourceLocation(it)
            parent = loadBlockModel(parentResourceLocation, fullModelData[parentResourceLocation]!!, fullModelData)
        }
        model = BlockModel(parent, modelData)

        models[resourceLocation] = model
        return model
    }

    fun unload() {
        for (field in this::class.java.fields) {
            if (!field.type.isAssignableFrom(Registry::class.java) && field.type.isAssignableFrom(EnumRegistry::class.java)) {
                continue
            }
            field.javaClass.getMethod("clear").invoke(this)
        }
    }

}
