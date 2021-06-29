/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.registries.versions

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.block.BlockEntityMetaType
import de.bixilon.minosoft.data.entities.meta.EntityMetaData
import de.bixilon.minosoft.data.inventory.InventorySlots
import de.bixilon.minosoft.data.registries.DefaultRegistries
import de.bixilon.minosoft.data.registries.Dimension
import de.bixilon.minosoft.data.registries.Motive
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.biomes.BiomeCategory
import de.bixilon.minosoft.data.registries.biomes.BiomePrecipitation
import de.bixilon.minosoft.data.registries.blocks.entites.BlockEntityType
import de.bixilon.minosoft.data.registries.blocks.entites.BlockEntityTypeRegistry
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.effects.StatusEffect
import de.bixilon.minosoft.data.registries.enchantment.Enchantment
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.entities.villagers.VillagerProfession
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.inventory.CreativeModeTab
import de.bixilon.minosoft.data.registries.items.Item
import de.bixilon.minosoft.data.registries.items.ItemRegistry
import de.bixilon.minosoft.data.registries.materials.Material
import de.bixilon.minosoft.data.registries.other.containers.ContainerType
import de.bixilon.minosoft.data.registries.other.game.event.GameEvent
import de.bixilon.minosoft.data.registries.particle.ParticleType
import de.bixilon.minosoft.data.registries.registry.*
import de.bixilon.minosoft.data.registries.sounds.SoundEvent
import de.bixilon.minosoft.data.registries.statistics.Statistic
import de.bixilon.minosoft.gui.rendering.chunk.VoxelShape
import de.bixilon.minosoft.gui.rendering.chunk.models.AABB
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModel
import de.bixilon.minosoft.protocol.packets.c2s.play.EntityActionC2SP
import de.bixilon.minosoft.protocol.packets.s2c.play.EntityAnimationS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.title.TitleS2CF
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.collections.Clearable
import de.bixilon.minosoft.util.json.ResourceLocationJsonMap.toResourceLocationMap
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType


class Registries {
    var shapes: MutableList<VoxelShape> = mutableListOf()
    val motiveRegistry: Registry<Motive> = Registry()
    val blockRegistry: Registry<Block> = Registry()
    val itemRegistry: ItemRegistry = ItemRegistry()
    val enchantmentRegistry: Registry<Enchantment> = Registry()
    val particleTypeRegistry: Registry<ParticleType> = Registry()
    val statusEffectRegistry: Registry<StatusEffect> = Registry()
    val statisticRegistry: Registry<Statistic> = Registry()
    val biomeRegistry: Registry<Biome> = Registry()
    val dimensionRegistry: Registry<Dimension> = Registry()
    val materialRegistry: Registry<Material> = Registry()
    val fluidRegistry: Registry<Fluid> = Registry()
    val soundEventRegistry: Registry<SoundEvent> = Registry()

    val villagerProfessionRegistry: Registry<VillagerProfession> = Registry()

    val equipmentSlotRegistry: EnumRegistry<InventorySlots.EquipmentSlots> = EnumRegistry(values = InventorySlots.EquipmentSlots)
    val handEquipmentSlotRegistry: EnumRegistry<InventorySlots.EquipmentSlots> = EnumRegistry(values = InventorySlots.EquipmentSlots)
    val armorEquipmentSlotRegistry: EnumRegistry<InventorySlots.EquipmentSlots> = EnumRegistry(values = InventorySlots.EquipmentSlots)
    val armorStandEquipmentSlotRegistry: EnumRegistry<InventorySlots.EquipmentSlots> = EnumRegistry(values = InventorySlots.EquipmentSlots)

    val entityMetaDataDataDataTypesRegistry: EnumRegistry<EntityMetaData.EntityMetaDataDataTypes> = EnumRegistry(values = EntityMetaData.EntityMetaDataDataTypes)

    val titleActionsRegistry: EnumRegistry<TitleS2CF.TitleActions> = EnumRegistry(values = TitleS2CF.TitleActions)

    val entityAnimationRegistry: EnumRegistry<EntityAnimationS2CP.EntityAnimations> = EnumRegistry(values = EntityAnimationS2CP.EntityAnimations)
    val entityActionsRegistry: EnumRegistry<EntityActionC2SP.EntityActions> = EnumRegistry(values = EntityActionC2SP.EntityActions)

    val creativeModeTabRegistry: FakeEnumRegistry<CreativeModeTab> = FakeEnumRegistry()

    val biomePrecipitationRegistry: FakeEnumRegistry<BiomePrecipitation> = FakeEnumRegistry()
    val biomeCategoryRegistry: FakeEnumRegistry<BiomeCategory> = FakeEnumRegistry()

    val blockStateRegistry = BlockStateRegistry(false)

    val entityMetaIndexMap: MutableMap<EntityMetaDataFields, Int> = mutableMapOf()
    val entityTypeRegistry: Registry<EntityType> = Registry()

    val blockEntityTypeRegistry = BlockEntityTypeRegistry()
    val blockEntityMetaDataTypeRegistry: Registry<BlockEntityMetaType> = Registry()

    val containerTypeRegistry: Registry<ContainerType> = Registry()
    val gameEventRegistry: Registry<GameEvent> = Registry()

    internal val models: MutableMap<ResourceLocation, BlockModel> = mutableMapOf()


    var isFullyLoaded = false
        private set

    private var isFlattened = false


    var parentRegistries: Registries? = null
        set(value) {
            field = value

            for (parentableField in PARENTABLE_FIELDS) {
                PARENTABLE_SET_PARENT_METHOD(parentableField.get(this), value?.let { parentableField.get(it) })
            }
        }

    fun getEntityMetaDataIndex(field: EntityMetaDataFields): Int? {
        return entityMetaIndexMap[field] ?: parentRegistries?.getEntityMetaDataIndex(field)
    }

    private fun <T : Enum<*>> loadEnumRegistry(version: Version, data: JsonElement?, registry: EnumRegistry<T>, alternative: PerVersionEnumRegistry<T>) {
        data?.let {
            registry.initialize(it)
        } ?: let {
            registry.parent = alternative.forVersion(version)
        }
    }

    fun load(version: Version, pixlyzerData: JsonObject) {
        isFlattened = version.isFlattened()
        blockStateRegistry.flattened = isFlattened
        // pre init stuff
        loadShapes(pixlyzerData["shapes"]?.asJsonObject)

        loadBlockModels(pixlyzerData["models"]?.asJsonObject?.toResourceLocationMap() ?: mutableMapOf())

        // enums
        loadEnumRegistry(version, pixlyzerData["equipment_slots"], equipmentSlotRegistry, DefaultRegistries.EQUIPMENT_SLOTS_REGISTRY)
        loadEnumRegistry(version, pixlyzerData["hand_equipment_slots"], handEquipmentSlotRegistry, DefaultRegistries.HAND_EQUIPMENT_SLOTS_REGISTRY)
        loadEnumRegistry(version, pixlyzerData["armor_equipment_slots"], armorEquipmentSlotRegistry, DefaultRegistries.ARMOR_EQUIPMENT_SLOTS_REGISTRY)
        loadEnumRegistry(version, pixlyzerData["armor_stand_equipment_slots"], armorStandEquipmentSlotRegistry, DefaultRegistries.ARMOR_STAND_EQUIPMENT_SLOTS_REGISTRY)

        loadEnumRegistry(version, pixlyzerData["entity_meta_data_data_types"], entityMetaDataDataDataTypesRegistry, DefaultRegistries.ENTITY_META_DATA_DATA_TYPES_REGISTRY)

        loadEnumRegistry(version, pixlyzerData["title_actions"], titleActionsRegistry, DefaultRegistries.TITLE_ACTIONS_REGISTRY)
        loadEnumRegistry(version, pixlyzerData["entity_animations"], entityAnimationRegistry, DefaultRegistries.ENTITY_ANIMATION_REGISTRY)
        loadEnumRegistry(version, pixlyzerData["entity_actions"], entityActionsRegistry, DefaultRegistries.ENTITY_ACTIONS_REGISTRY)

        // id stuff
        biomeCategoryRegistry.initialize(pixlyzerData["biome_categories"]?.asJsonObject, this, BiomeCategory)
        biomePrecipitationRegistry.initialize(pixlyzerData["biome_precipitations"]?.asJsonObject, this, BiomePrecipitation)
        creativeModeTabRegistry.initialize(pixlyzerData["creative_inventory_tab"]?.asJsonObject, this, CreativeModeTab)

        // id resource location stuff
        containerTypeRegistry.initialize(pixlyzerData["container_types"]?.asJsonObject, this, ContainerType, alternative = DefaultRegistries.CONTAINER_TYPE_REGISTRY.forVersion(version))
        gameEventRegistry.initialize(pixlyzerData["game_events"]?.asJsonObject, this, GameEvent, alternative = DefaultRegistries.GAME_EVENT_REGISTRY.forVersion(version))


        entityTypeRegistry.initialize(pixlyzerData["entities"]?.asJsonObject, this, EntityType)

        motiveRegistry.initialize(pixlyzerData["motives"]?.asJsonObject, this, Motive, version.isFlattened())
        soundEventRegistry.initialize(pixlyzerData["sound_events"]?.asJsonObject, this, SoundEvent)
        particleTypeRegistry.initialize(pixlyzerData["particles"]?.asJsonObject, this, ParticleType)
        materialRegistry.initialize(pixlyzerData["materials"]?.asJsonObject, this, Material)
        enchantmentRegistry.initialize(pixlyzerData["enchantments"]?.asJsonObject, this, Enchantment)
        statusEffectRegistry.initialize(pixlyzerData["status_effects"]?.asJsonObject, this, StatusEffect)
        biomeRegistry.initialize(pixlyzerData["biomes"]?.asJsonObject, this, Biome)
        dimensionRegistry.initialize(pixlyzerData["dimensions"]?.asJsonObject, this, Dimension)
        fluidRegistry.initialize(pixlyzerData["fluids"]?.asJsonObject, this, Fluid)
        blockRegistry.initialize(pixlyzerData["blocks"]?.asJsonObject, this, Block, version.isFlattened(), Registry.MetaTypes.BITS_4)
        itemRegistry.initialize(pixlyzerData["items"]?.asJsonObject, this, Item, version.isFlattened(), Registry.MetaTypes.BITS_16)

        blockEntityTypeRegistry.initialize(pixlyzerData["block_entities"]?.asJsonObject, this, BlockEntityType)

        villagerProfessionRegistry.initialize(pixlyzerData["villager_professions"]?.asJsonObject, this, VillagerProfession)


        blockEntityMetaDataTypeRegistry.initialize(pixlyzerData["block_entity_meta_data_types"]?.asJsonObject, this, BlockEntityMetaType, alternative = DefaultRegistries.BLOCK_ENTITY_META_TYPE_REGISTRY.forVersion(version))


        // post init
        for (field in TYPE_MAP.values) {
            val registry = field.get(this) as Registry<*>
            registry.postInit(this)
        }
        isFullyLoaded = true
    }

    private fun loadShapes(pixlyzerData: JsonObject?) {
        pixlyzerData ?: return
        val aabbs = loadAABBs(pixlyzerData["aabbs"]?.asJsonArray!!)
        loadVoxelShapes(pixlyzerData["shapes"].asJsonArray, aabbs)
    }

    private fun loadVoxelShapes(pixlyzerData: JsonArray, aabbs: List<AABB>) {
        for (shape in pixlyzerData) {
            shapes.add(VoxelShape(shape, aabbs))
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
            field.javaClass.getMethod("clear")(this)
        }
    }

    operator fun <T : RegistryItem> get(type: Class<T>): Registry<T>? {
        var currentField: Field?
        var currentClass: Class<*> = type
        do {
            currentField = TYPE_MAP[currentClass]
            currentClass = currentClass.superclass
        } while (currentField == null && currentClass != Object::class.java)
        return currentField?.get(this) as Registry<T>?
    }


    companion object {
        private val PARENTABLE_FIELDS: List<Field>
        private val PARENTABLE_SET_PARENT_METHOD = Parentable::class.java.getDeclaredMethod("setParent", Any::class.java)
        private val TYPE_MAP: Map<Class<*>, Field>

        init {
            val fields: MutableList<Field> = mutableListOf()

            for (field in Registries::class.java.declaredFields) {
                if (!Parentable::class.java.isAssignableFrom(field.type)) {
                    continue
                }
                fields.add(field)
            }

            PARENTABLE_FIELDS = fields.toList()
        }

        init {
            val types: MutableMap<Class<*>, Field> = mutableMapOf()


            for (field in Registries::class.java.declaredFields) {
                if (!Registry::class.java.isAssignableFrom(field.type)) {
                    continue
                }
                field.isAccessible = true

                var generic = field.genericType

                if (field.type != Registry::class.java) {
                    var type = field.type
                    while (type != Object::class.java) {
                        if (type.superclass == Registry::class.java) {
                            generic = type.genericSuperclass
                            break
                        }
                        type = type.superclass
                    }
                }


                types[generic.unsafeCast<ParameterizedType>().actualTypeArguments.first() as Class<*>] = field
            }

            types[Item::class.java] = Registries::class.java.getDeclaredField("itemRegistry")

            TYPE_MAP = types.toMap()
        }
    }
}
