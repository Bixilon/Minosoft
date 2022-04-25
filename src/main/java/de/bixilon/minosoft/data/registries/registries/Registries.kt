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
package de.bixilon.minosoft.data.registries.registries

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.minosoft.data.container.InventorySlots
import de.bixilon.minosoft.data.entities.block.BlockDataDataType
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.data.types.EntityDataDataTypes
import de.bixilon.minosoft.data.registries.*
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.biomes.BiomeCategory
import de.bixilon.minosoft.data.registries.biomes.BiomePrecipitation
import de.bixilon.minosoft.data.registries.blocks.entites.BlockEntityType
import de.bixilon.minosoft.data.registries.blocks.entites.BlockEntityTypeRegistry
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.dimension.Dimension
import de.bixilon.minosoft.data.registries.effects.StatusEffect
import de.bixilon.minosoft.data.registries.enchantment.Enchantment
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.entities.variants.CatVariant
import de.bixilon.minosoft.data.registries.entities.variants.FrogVariant
import de.bixilon.minosoft.data.registries.entities.villagers.VillagerProfession
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.inventory.CreativeModeTab
import de.bixilon.minosoft.data.registries.items.Item
import de.bixilon.minosoft.data.registries.items.ItemRegistry
import de.bixilon.minosoft.data.registries.materials.Material
import de.bixilon.minosoft.data.registries.other.containers.ContainerType
import de.bixilon.minosoft.data.registries.particle.ParticleType
import de.bixilon.minosoft.data.registries.registries.registry.*
import de.bixilon.minosoft.data.registries.statistics.Statistic
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.EntityActionC2SP
import de.bixilon.minosoft.protocol.packets.s2c.play.entity.EntityAnimationS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.title.TitleS2CF
import de.bixilon.minosoft.recipes.RecipeRegistry
import de.bixilon.minosoft.util.collections.Clearable
import java.lang.reflect.Field


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
    val soundEventRegistry: ResourceLocationRegistry = ResourceLocationRegistry()
    val recipes = RecipeRegistry()

    val villagerProfessionRegistry: Registry<VillagerProfession> = Registry()
    val villagerTypeRegistry = ResourceLocationRegistry()

    val catVariants: Registry<CatVariant> = Registry()
    val frogVariants: Registry<FrogVariant> = Registry()

    val equipmentSlotRegistry: EnumRegistry<InventorySlots.EquipmentSlots> = EnumRegistry(values = InventorySlots.EquipmentSlots)
    val handEquipmentSlotRegistry: EnumRegistry<InventorySlots.EquipmentSlots> = EnumRegistry(values = InventorySlots.EquipmentSlots)
    val armorEquipmentSlotRegistry: EnumRegistry<InventorySlots.EquipmentSlots> = EnumRegistry(values = InventorySlots.EquipmentSlots)
    val armorStandEquipmentSlotRegistry: EnumRegistry<InventorySlots.EquipmentSlots> = EnumRegistry(values = InventorySlots.EquipmentSlots)

    val entityDataDataDataTypesRegistry: EnumRegistry<EntityDataDataTypes> = EnumRegistry(values = EntityDataDataTypes)

    val titleActionsRegistry: EnumRegistry<TitleS2CF.TitleActions> = EnumRegistry(values = TitleS2CF.TitleActions)

    val entityAnimationRegistry: EnumRegistry<EntityAnimationS2CP.EntityAnimations> = EnumRegistry(values = EntityAnimationS2CP.EntityAnimations)
    val entityActionsRegistry: EnumRegistry<EntityActionC2SP.EntityActions> = EnumRegistry(values = EntityActionC2SP.EntityActions)

    val creativeModeTabRegistry: FakeEnumRegistry<CreativeModeTab> = FakeEnumRegistry()

    val biomePrecipitationRegistry: FakeEnumRegistry<BiomePrecipitation> = FakeEnumRegistry()
    val biomeCategoryRegistry: FakeEnumRegistry<BiomeCategory> = FakeEnumRegistry()

    val blockStateRegistry = BlockStateRegistry(false)

    val entityDataIndexMap: MutableMap<EntityDataField, Int> = mutableMapOf()
    val entityTypeRegistry: Registry<EntityType> = Registry()

    val blockEntityTypeRegistry = BlockEntityTypeRegistry()
    val blockDataDataDataTypeRegistry: Registry<BlockDataDataType> = Registry()

    val containerTypeRegistry: Registry<ContainerType> = Registry()
    val gameEventRegistry: ResourceLocationRegistry = ResourceLocationRegistry()
    val worldEventRegistry: ResourceLocationRegistry = ResourceLocationRegistry()

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

    fun getEntityDataIndex(field: EntityDataField): Int? {
        return entityDataIndexMap[field] ?: parentRegistries?.getEntityDataIndex(field)
    }

    private fun <T : Enum<*>> loadEnumRegistry(version: Version, data: Any?, registry: EnumRegistry<T>, alternative: PerVersionEnumRegistry<T>) {
        data?.let {
            registry.initialize(it)
        } ?: let {
            registry.parent = alternative.forVersion(version)
        }
    }

    fun load(version: Version, pixlyzerData: Map<String, Any>) {
        isFlattened = version.flattened
        blockStateRegistry.flattened = isFlattened
        // pre init stuff
        loadShapes(pixlyzerData["shapes"]?.toJsonObject())

        // enums
        loadEnumRegistry(version, pixlyzerData["equipment_slots"], equipmentSlotRegistry, DefaultRegistries.EQUIPMENT_SLOTS_REGISTRY)
        loadEnumRegistry(version, pixlyzerData["hand_equipment_slots"], handEquipmentSlotRegistry, DefaultRegistries.HAND_EQUIPMENT_SLOTS_REGISTRY)
        loadEnumRegistry(version, pixlyzerData["armor_equipment_slots"], armorEquipmentSlotRegistry, DefaultRegistries.ARMOR_EQUIPMENT_SLOTS_REGISTRY)
        loadEnumRegistry(version, pixlyzerData["armor_stand_equipment_slots"], armorStandEquipmentSlotRegistry, DefaultRegistries.ARMOR_STAND_EQUIPMENT_SLOTS_REGISTRY)

        loadEnumRegistry(version, pixlyzerData["entity_data_data_types"], entityDataDataDataTypesRegistry, DefaultRegistries.ENTITY_DATA_DATA_TYPES_REGISTRY)

        loadEnumRegistry(version, pixlyzerData["title_actions"], titleActionsRegistry, DefaultRegistries.TITLE_ACTIONS_REGISTRY)
        loadEnumRegistry(version, pixlyzerData["entity_animations"], entityAnimationRegistry, DefaultRegistries.ENTITY_ANIMATION_REGISTRY)
        loadEnumRegistry(version, pixlyzerData["entity_actions"], entityActionsRegistry, DefaultRegistries.ENTITY_ACTIONS_REGISTRY)

        // id stuff
        biomeCategoryRegistry.initialize(pixlyzerData["biome_categories"]?.unsafeCast(), this, BiomeCategory)
        biomePrecipitationRegistry.initialize(pixlyzerData["biome_precipitations"]?.unsafeCast(), this, BiomePrecipitation)
        creativeModeTabRegistry.initialize(pixlyzerData["creative_inventory_tab"]?.unsafeCast(), this, CreativeModeTab)

        // id resource location stuff
        containerTypeRegistry.rawInitialize(pixlyzerData["container_types"]?.toJsonObject(), this, ContainerType, alternative = DefaultRegistries.CONTAINER_TYPE_REGISTRY.forVersion(version))
        gameEventRegistry.rawInitialize(pixlyzerData["game_events"]?.toJsonObject(), this, null, alternative = DefaultRegistries.GAME_EVENT_REGISTRY.forVersion(version))
        worldEventRegistry.rawInitialize(pixlyzerData["world_events"]?.toJsonObject(), this, null, alternative = DefaultRegistries.WORLD_EVENT_REGISTRY.forVersion(version))


        entityTypeRegistry.rawInitialize(pixlyzerData["entities"]?.toJsonObject(), this, EntityType)

        motiveRegistry.rawInitialize(pixlyzerData["motives"]?.toJsonObject(), this, Motive, version.flattened)
        soundEventRegistry.rawInitialize(pixlyzerData["sound_events"]?.toJsonObject())
        particleTypeRegistry.rawInitialize(pixlyzerData["particles"]?.toJsonObject(), this, ParticleType)
        materialRegistry.rawInitialize(pixlyzerData["materials"]?.toJsonObject(), this, Material)
        enchantmentRegistry.rawInitialize(pixlyzerData["enchantments"]?.toJsonObject(), this, Enchantment)
        statusEffectRegistry.rawInitialize(pixlyzerData["status_effects"]?.toJsonObject(), this, StatusEffect)
        biomeRegistry.rawInitialize(pixlyzerData["biomes"]?.toJsonObject(), this, Biome)
        dimensionRegistry.rawInitialize(pixlyzerData["dimensions"]?.toJsonObject(), this, Dimension)
        fluidRegistry.rawInitialize(pixlyzerData["fluids"]?.toJsonObject(), this, Fluid)
        blockRegistry.rawInitialize(pixlyzerData["blocks"]?.toJsonObject(), this, Block, version.flattened, Registry.MetaTypes.BITS_4)
        itemRegistry.rawInitialize(pixlyzerData["items"]?.toJsonObject(), this, Item, version.flattened, Registry.MetaTypes.BITS_16)

        blockEntityTypeRegistry.rawInitialize(pixlyzerData["block_entities"]?.toJsonObject(), this, BlockEntityType)

        villagerProfessionRegistry.rawInitialize(pixlyzerData["villager_professions"]?.toJsonObject(), this, VillagerProfession)
        villagerTypeRegistry.rawInitialize(pixlyzerData["villager_types"]?.toJsonObject())


        blockDataDataDataTypeRegistry.rawInitialize(pixlyzerData["block_data_data_types"]?.toJsonObject(), this, BlockDataDataType, alternative = DefaultRegistries.BLOCK_DATA_TYPE_REGISTRY.forVersion(version))

        catVariants.rawInitialize(pixlyzerData["variant/cat"]?.toJsonObject(), this, CatVariant)
        frogVariants.rawInitialize(pixlyzerData["variant/frog"]?.toJsonObject(), this, FrogVariant)


        // post init
        for (field in TYPE_MAP.values) {
            field.get(this).unsafeCast<Registry<*>>().postInit(this)
        }
        isFullyLoaded = true
        shapes.clear()
    }

    private fun loadShapes(pixlyzerData: Map<String, Any>?) {
        pixlyzerData ?: return
        val aabbs = loadAABBs(pixlyzerData["aabbs"].nullCast()!!)
        loadVoxelShapes(pixlyzerData["shapes"].unsafeCast(), aabbs)
    }

    private fun loadVoxelShapes(pixlyzerData: Collection<Any>, aabbs: List<AABB>) {
        for (shape in pixlyzerData) {
            shapes.add(VoxelShape(shape, aabbs))
        }
    }

    private fun loadAABBs(pixlyzerData: Collection<Map<String, Any>>): List<AABB> {
        val aabbs = mutableListOf<AABB>()
        for (data in pixlyzerData) {
            aabbs.add(AABB(data))
        }
        return aabbs
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


                types[RegistryUtil.getClassOfFactory(generic)] = field
            }

            types[Item::class.java] = Registries::class.java.getDeclaredField("itemRegistry")

            TYPE_MAP = types.toMap()
        }
    }
}
