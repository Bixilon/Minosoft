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

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.meta.EntityMetaData
import de.bixilon.minosoft.data.inventory.InventorySlots
import de.bixilon.minosoft.data.mappings.*
import de.bixilon.minosoft.data.mappings.biomes.Biome
import de.bixilon.minosoft.data.mappings.biomes.BiomeCategory
import de.bixilon.minosoft.data.mappings.biomes.BiomePrecipitation
import de.bixilon.minosoft.data.mappings.blocks.Block
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.entities.EntityType
import de.bixilon.minosoft.data.mappings.entities.villagers.VillagerProfession
import de.bixilon.minosoft.data.mappings.fluid.Fluid
import de.bixilon.minosoft.data.mappings.inventory.CreativeModeTab
import de.bixilon.minosoft.data.mappings.items.Item
import de.bixilon.minosoft.data.mappings.items.ItemRegistry
import de.bixilon.minosoft.data.mappings.materials.Material
import de.bixilon.minosoft.data.mappings.particle.Particle
import de.bixilon.minosoft.data.mappings.registry.*
import de.bixilon.minosoft.data.mappings.statistics.Statistic
import de.bixilon.minosoft.gui.rendering.chunk.VoxelShape
import de.bixilon.minosoft.gui.rendering.chunk.models.AABB
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModel
import de.bixilon.minosoft.protocol.packets.clientbound.play.title.TitleClientboundPacketFactory
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.collections.Clearable
import de.bixilon.minosoft.util.json.ResourceLocationJsonMap.toResourceLocationMap
import java.lang.reflect.Field
import java.util.*


class VersionMapping {
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
    val fluidRegistry: Registry<Fluid> = Registry()

    val villagerProfessionRegistry: Registry<VillagerProfession> = Registry()

    val equipmentSlotRegistry: EnumRegistry<InventorySlots.EquipmentSlots> = EnumRegistry(values = InventorySlots.EquipmentSlots)
    val handEquipmentSlotRegistry: EnumRegistry<InventorySlots.EquipmentSlots> = EnumRegistry(values = InventorySlots.EquipmentSlots)
    val armorEquipmentSlotRegistry: EnumRegistry<InventorySlots.EquipmentSlots> = EnumRegistry(values = InventorySlots.EquipmentSlots)
    val armorStandEquipmentSlotRegistry: EnumRegistry<InventorySlots.EquipmentSlots> = EnumRegistry(values = InventorySlots.EquipmentSlots)

    val entityMetaDataDataDataTypesRegistry: EnumRegistry<EntityMetaData.EntityMetaDataDataTypes> = EnumRegistry(values = EntityMetaData.EntityMetaDataDataTypes)

    val titleActionsRegistry: EnumRegistry<TitleClientboundPacketFactory.TitleActions> = EnumRegistry(values = TitleClientboundPacketFactory.TitleActions)

    val creativeModeTabRegistry: FakeEnumRegistry<CreativeModeTab> = FakeEnumRegistry()

    val biomePrecipitationRegistry: FakeEnumRegistry<BiomePrecipitation> = FakeEnumRegistry()
    val biomeCategoryRegistry: FakeEnumRegistry<BiomeCategory> = FakeEnumRegistry()


    val blockStateIdMap: MutableMap<Int, BlockState> = mutableMapOf()

    val entityMetaIndexMap = HashMap<EntityMetaDataFields, Int>(180)
    val entityRegistry: Registry<EntityType> = Registry()

    internal val models: MutableMap<ResourceLocation, BlockModel> = mutableMapOf()


    val blockStateCount: Int
        get() = blockStateIdMap.size + (parentMapping?.blockStateCount ?: 0)


    var isFullyLoaded = false
        private set

    private var _parentMapping: VersionMapping? = null

    var parentMapping: VersionMapping?
        get() = _parentMapping
        set(value) {
            _parentMapping = value

            for (field in PARENTABLE_FIELDS) {
                PARENTABLE_SET_PARENT_METHOD.invoke(field.get(this), value?.let { field.get(it) })
            }
        }

    fun getBlockState(blockState: Int): BlockState? {
        if (blockState == ProtocolDefinition.NULL_BLOCK_ID) {
            return null
        }
        return blockStateIdMap[blockState] ?: _parentMapping?.getBlockState(blockState)
    }

    fun getEntityMetaDataIndex(field: EntityMetaDataFields): Int? {
        return entityMetaIndexMap[field] ?: _parentMapping?.getEntityMetaDataIndex(field)
    }

    private fun <T : Enum<*>> loadEnumRegistry(version: Version, data: JsonElement?, registry: EnumRegistry<T>, alternative: PerEnumVersionRegistry<T>) {
        data?.let {
            registry.initialize(it)
        } ?: let {
            registry.setParent(alternative.forVersion(version))
        }
    }

    fun load(version: Version, pixlyzerData: JsonObject) {
        // pre init stuff
        loadShapes(pixlyzerData["shapes"]?.asJsonObject)

        loadBlockModels(pixlyzerData["models"].asJsonObject.toResourceLocationMap())

        // enums
        loadEnumRegistry(version, pixlyzerData["equipment_slots"], equipmentSlotRegistry, DefaultRegistries.EQUIPMENT_SLOTS_REGISTRY)
        loadEnumRegistry(version, pixlyzerData["hand_equipment_slots"], handEquipmentSlotRegistry, DefaultRegistries.HAND_EQUIPMENT_SLOTS_REGISTRY)
        loadEnumRegistry(version, pixlyzerData["armor_equipment_slots"], armorEquipmentSlotRegistry, DefaultRegistries.ARMOR_EQUIPMENT_SLOTS_REGISTRY)
        loadEnumRegistry(version, pixlyzerData["armor_stand_equipment_slots"], armorStandEquipmentSlotRegistry, DefaultRegistries.ARMOR_STAND_EQUIPMENT_SLOTS_REGISTRY)

        loadEnumRegistry(version, pixlyzerData["entity_meta_data_data_types"], entityMetaDataDataDataTypesRegistry, DefaultRegistries.ENTITY_META_DATA_DATA_TYPES_REGISTRY)

        loadEnumRegistry(version, pixlyzerData["title_actions"], titleActionsRegistry, DefaultRegistries.TITLE_ACTIONS_REGISTRY)

        // id stuff
        biomeCategoryRegistry.initialize(pixlyzerData["biome_categories"]?.asJsonObject, this, BiomeCategory)
        biomePrecipitationRegistry.initialize(pixlyzerData["biome_precipations"]?.asJsonObject, this, BiomePrecipitation)
        creativeModeTabRegistry.initialize(pixlyzerData["creative_inventory_tab"]?.asJsonObject, this, CreativeModeTab)

        // id resource location stuff
        materialRegistry.initialize(pixlyzerData["materials"]?.asJsonObject, this, Material)
        motiveRegistry.initialize(pixlyzerData["motives"]?.asJsonObject, this, Motive, version.isFlattened())
        blockRegistry.initialize(pixlyzerData["blocks"]?.asJsonObject, this, Block, version.isFlattened(), Registry.MetaTypes.BITS_4)
        itemRegistry.initialize(pixlyzerData["items"]?.asJsonObject, this, Item, version.isFlattened(), Registry.MetaTypes.BITS_16)
        enchantmentRegistry.initialize(pixlyzerData["enchantments"]?.asJsonObject, this, Enchantment)
        particleRegistry.initialize(pixlyzerData["particles"]?.asJsonObject, this, Particle)
        statusEffectRegistry.initialize(pixlyzerData["mob_effect"]?.asJsonObject, this, StatusEffect)
        biomeRegistry.initialize(pixlyzerData["biomes"]?.asJsonObject, this, Biome)
        dimensionRegistry.initialize(pixlyzerData["dimensions"]?.asJsonObject, this, Dimension)

        villagerProfessionRegistry.initialize(pixlyzerData["villager_professions"]?.asJsonObject, this, VillagerProfession)
        fluidRegistry.initialize(pixlyzerData["fluids"]?.asJsonObject, this, Fluid)

        entityRegistry.initialize(pixlyzerData["entities"]?.asJsonObject, this, EntityType)

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

    companion object {
        private val PARENTABLE_FIELDS: List<Field>
        private val PARENTABLE_SET_PARENT_METHOD = Parentable::class.java.getDeclaredMethod("setParent", Any::class.java)

        init {
            val fields: MutableList<Field> = mutableListOf()

            for (field in VersionMapping::class.java.declaredFields) {
                if (!Parentable::class.java.isAssignableFrom(field.type)) {
                    continue
                }
                fields.add(field)
            }

            PARENTABLE_FIELDS = fields.toList()
        }
    }
}
