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
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.EntityClassMappings
import de.bixilon.minosoft.data.entities.EntityInformation
import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.inventory.InventorySlots
import de.bixilon.minosoft.data.mappings.*
import de.bixilon.minosoft.data.mappings.biomes.Biome
import de.bixilon.minosoft.data.mappings.biomes.BiomeCategory
import de.bixilon.minosoft.data.mappings.biomes.BiomePrecipitation
import de.bixilon.minosoft.data.mappings.blocks.Block
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.inventory.CreativeModeTab
import de.bixilon.minosoft.data.mappings.items.Item
import de.bixilon.minosoft.data.mappings.items.ItemRegistry
import de.bixilon.minosoft.data.mappings.materials.Material
import de.bixilon.minosoft.data.mappings.particle.Particle
import de.bixilon.minosoft.data.mappings.registry.EnumRegistry
import de.bixilon.minosoft.data.mappings.registry.FakeEnumRegistry
import de.bixilon.minosoft.data.mappings.registry.Registry
import de.bixilon.minosoft.data.mappings.statistics.Statistic
import de.bixilon.minosoft.gui.rendering.chunk.VoxelShape
import de.bixilon.minosoft.gui.rendering.chunk.models.AABB
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModel
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.collections.Clearable
import de.bixilon.minosoft.util.json.ResourceLocationJsonMap.toResourceLocationMap
import java.util.*


class VersionMapping(var version: Version?) {
    var shapes: MutableList<VoxelShape> = mutableListOf()
    val motiveRegistry: Registry<Motive> = Registry()
    val blockRegistry: Registry<Block> = Registry()
    val itemRegistry: ItemRegistry = ItemRegistry()
    val enchantmentRegistry: Registry<Enchantment> = Registry()
    val particleRegistry: Registry<Particle> = Registry()
    val statusEffectRegistry: Registry<StatusEffect> = Registry()
    val statisticRegistry: Registry<Statistic> = Registry()
    val biomeRegistry: Registry<Biome> = Registry()
    val dimensionRegistry: Registry<Dimension> = Registry()
    val materialRegistry: Registry<Material> = Registry()

    val equipmentSlotRegistry: EnumRegistry<InventorySlots.EquipmentSlots> = EnumRegistry(values = InventorySlots.EquipmentSlots)
    val handEquipmentSlotRegistry: EnumRegistry<InventorySlots.EquipmentSlots> = EnumRegistry(values = InventorySlots.EquipmentSlots)
    val armorEquipmentSlotRegistry: EnumRegistry<InventorySlots.EquipmentSlots> = EnumRegistry(values = InventorySlots.EquipmentSlots)
    val armorStandEquipmentSlotRegistry: EnumRegistry<InventorySlots.EquipmentSlots> = EnumRegistry(values = InventorySlots.EquipmentSlots)

    val creativeModeTabRegistry: FakeEnumRegistry<CreativeModeTab> = FakeEnumRegistry()

    val biomePrecipitationRegistry: FakeEnumRegistry<BiomePrecipitation> = FakeEnumRegistry()
    val biomeCategoryRegistry: FakeEnumRegistry<BiomeCategory> = FakeEnumRegistry()


    internal val blockStateIdMap: MutableMap<Int, BlockState> = mutableMapOf()

    private val entityInformationMap = HashBiMap.create<Class<out Entity>, EntityInformation>(120)
    private val entityMetaIndexMap = HashMap<EntityMetaDataFields, Int>(180)
    private val entityIdClassMap = HashBiMap.create<Int, Class<out Entity?>>(120)

    internal val models: MutableMap<ResourceLocation, BlockModel> = mutableMapOf()


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
            statusEffectRegistry.setParent(value?.statusEffectRegistry)
            blockRegistry.setParent(value?.blockRegistry)
            statisticRegistry.setParent(value?.statisticRegistry)
            biomeRegistry.setParent(value?.biomeRegistry)
            dimensionRegistry.setParent(value?.dimensionRegistry)
            biomePrecipitationRegistry.setParent(value?.biomePrecipitationRegistry)
            biomeCategoryRegistry.setParent(value?.biomeCategoryRegistry)
            materialRegistry.setParent(value?.materialRegistry)
        }

    fun getBlockState(blockState: Int): BlockState? {
        if (blockState == ProtocolDefinition.NULL_BLOCK_ID) {
            return null
        }
        return blockStateIdMap[blockState] ?: _parentMapping?.getBlockState(blockState)
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

    private fun <T : Enum<*>> loadEnumRegistry(data: JsonElement?, registry: EnumRegistry<T>, alternative: PerVersionRegistry<T>) {
        data?.let {
            registry.initialize(it)
        } ?: let {
            registry.setParent(alternative.forVersion(version!!))
        }
    }

    fun load(pixlyzerData: JsonObject) {
        val version = version!!
        // pre init stuff
        loadShapes(pixlyzerData["shapes"]?.asJsonObject)

        loadBlockModels(pixlyzerData["models"].asJsonObject.toResourceLocationMap())

        // enums
        loadEnumRegistry(pixlyzerData["equipment_slots"], equipmentSlotRegistry, DefaultRegistries.EQUIPMENT_SLOTS_REGISTRY)
        loadEnumRegistry(pixlyzerData["hand_equipment_slots"], handEquipmentSlotRegistry, DefaultRegistries.HAND_EQUIPMENT_SLOTS_REGISTRY)
        loadEnumRegistry(pixlyzerData["armor_equipment_slots"], armorEquipmentSlotRegistry, DefaultRegistries.ARMOR_EQUIPMENT_SLOTS_REGISTRY)
        loadEnumRegistry(pixlyzerData["armor_stand_equipment_slots"], armorStandEquipmentSlotRegistry, DefaultRegistries.ARMOR_STAND_EQUIPMENT_SLOTS_REGISTRY)

        // id stuff
        biomeCategoryRegistry.initialize(pixlyzerData["biome_categories"]?.asJsonObject, this, BiomeCategory.Companion)
        biomePrecipitationRegistry.initialize(pixlyzerData["biome_precipations"]?.asJsonObject, this, BiomePrecipitation.Companion)
        creativeModeTabRegistry.initialize(pixlyzerData["creative_inventory_tab"]?.asJsonObject, this, CreativeModeTab.Companion)

        // id resource location stuff
        materialRegistry.initialize(pixlyzerData["materials"]?.asJsonObject, this, Material.Companion)
        motiveRegistry.initialize(pixlyzerData["motives"]?.asJsonObject, this, Motive.Companion, version.isFlattened())
        blockRegistry.initialize(pixlyzerData["blocks"]?.asJsonObject, this, Block.Companion, version.isFlattened(), Registry.MetaTypes.BITS_4)
        itemRegistry.initialize(pixlyzerData["items"]?.asJsonObject, this, Item.Companion, version.isFlattened(), Registry.MetaTypes.BITS_16)
        enchantmentRegistry.initialize(pixlyzerData["enchantments"]?.asJsonObject, this, Enchantment.Companion)
        particleRegistry.initialize(pixlyzerData["particles"]?.asJsonObject, this, Particle.Companion)
        statusEffectRegistry.initialize(pixlyzerData["mob_effect"]?.asJsonObject, this, StatusEffect.Companion)
        biomeRegistry.initialize(pixlyzerData["biomes"]?.asJsonObject, this, Biome.Companion)
        dimensionRegistry.initialize(pixlyzerData["dimensions"]?.asJsonObject, this, Dimension.Companion)

        loadEntities(pixlyzerData["entities"]?.asJsonObject)
        // post init
        biomeRegistry.postInit(this)
        isFullyLoaded = true
    }

    private fun loadShapes(pixlyzerData: JsonObject?) {
        pixlyzerData ?: return
        val aabbs = loadAABBs(pixlyzerData["aabbs"]?.asJsonArray!!)
        loadVoxelShapes(pixlyzerData["shapes"].asJsonArray!!, aabbs)
    }

    private fun loadVoxelShapes(pixlyzerData: JsonArray, aabbs: List<AABB>) {
        for (shape in pixlyzerData) {
            shapes.add(VoxelShape(shape.asJsonObject, aabbs))
        }
    }

    private fun loadAABBs(pixlyzerData: JsonArray): List<AABB> {
        val aabbs = mutableListOf<AABB>()
        for (data in pixlyzerData) {
            aabbs.add(AABB(data.asJsonObject))
        }
        return aabbs
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
            if (parentResourceLocation.path.startsWith("builtin/")) {
                // ToDo
                return@let
            }

            parent = loadBlockModel(parentResourceLocation, fullModelData[parentResourceLocation]!!, fullModelData)
        }
        model = BlockModel(parent, modelData)

        models[resourceLocation] = model
        return model
    }

    fun clear() {
        for (field in this::class.java.fields) {
            if (!field.type.isAssignableFrom(Clearable::class.java)) {
                continue
            }
            field.javaClass.getMethod("clear").invoke(this)
        }
    }
}
