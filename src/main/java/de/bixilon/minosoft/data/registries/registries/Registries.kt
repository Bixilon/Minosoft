/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.concurrent.worker.task.TaskWorker
import de.bixilon.kutil.concurrent.worker.task.WorkerTask
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.latch.ParentLatch
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.entities.EntityAnimations
import de.bixilon.minosoft.data.entities.EntityObjectType
import de.bixilon.minosoft.data.entities.block.BlockDataDataType
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.data.types.EntityDataTypes
import de.bixilon.minosoft.data.registries.Motif
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.BlockRegistry
import de.bixilon.minosoft.data.registries.blocks.entites.BlockEntityTypeRegistry
import de.bixilon.minosoft.data.registries.chat.ChatMessageType
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.data.registries.dimension.Dimension
import de.bixilon.minosoft.data.registries.effects.IntegratedStatusEffects
import de.bixilon.minosoft.data.registries.effects.PixlyzerStatusEffectType
import de.bixilon.minosoft.data.registries.effects.StatusEffectType
import de.bixilon.minosoft.data.registries.enchantment.Enchantment
import de.bixilon.minosoft.data.registries.enchantment.IntegratedEnchantments
import de.bixilon.minosoft.data.registries.enchantment.PixLyzerEnchantment
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.entities.damage.DamageType
import de.bixilon.minosoft.data.registries.entities.variants.CatVariant
import de.bixilon.minosoft.data.registries.entities.variants.FrogVariant
import de.bixilon.minosoft.data.registries.entities.villagers.VillagerProfession
import de.bixilon.minosoft.data.registries.fluid.FluidRegistry
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.ItemRegistry
import de.bixilon.minosoft.data.registries.materials.Material
import de.bixilon.minosoft.data.registries.misc.MiscData
import de.bixilon.minosoft.data.registries.particle.ParticleType
import de.bixilon.minosoft.data.registries.registries.registry.*
import de.bixilon.minosoft.data.registries.shapes.ShapeRegistry
import de.bixilon.minosoft.data.registries.sound.SoundGroup
import de.bixilon.minosoft.data.registries.statistics.Statistic
import de.bixilon.minosoft.datafixer.enumeration.EntityDataTypesFixer
import de.bixilon.minosoft.datafixer.rls.EntityTypeFixer
import de.bixilon.minosoft.datafixer.rls.RegistryFixer.fixRegistry
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.EntityActionC2SP
import de.bixilon.minosoft.protocol.packets.s2c.play.title.TitleS2CF
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.recipes.RecipeRegistry
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.RegistriesUtil
import de.bixilon.minosoft.util.RegistriesUtil.postInit
import de.bixilon.minosoft.util.RegistriesUtil.setParent
import de.bixilon.minosoft.util.Stopwatch
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.get
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.reflect.KProperty0


class Registries(
    val cleanup: Boolean = true,
) : Parentable<Registries> {
    val registries: MutableMap<ResourceLocation, AbstractRegistry<*>> = mutableMapOf()

    val shape = ShapeRegistry()

    val motif: Registry<Motif> = register("motif", Registry(codec = Motif))
    val block = register("block", BlockRegistry())
    val item: ItemRegistry = register("item", ItemRegistry())
    val enchantment: Registry<Enchantment> = register("enchantment", Registry(codec = PixLyzerEnchantment, integrated = IntegratedEnchantments))
    val particleType: Registry<ParticleType> = register("particle_type", Registry(codec = ParticleType))
    val statusEffect: Registry<StatusEffectType> = register("mob_effect", Registry(codec = PixlyzerStatusEffectType, integrated = IntegratedStatusEffects))
    val statistic: Registry<Statistic> = register("custom_stat", Registry(codec = Statistic))
    val biome: Registry<Biome> = register("biome", Registry(codec = Biome))
    val dimension: Registry<Dimension> = register("dimension_type", Registry(codec = Dimension))
    val material: Registry<Material> = register("material", Registry(codec = Material))
    val fluid: FluidRegistry = register("fluid", FluidRegistry())
    val soundEvent: ResourceLocationRegistry = register("sound_event", ResourceLocationRegistry())
    val recipes = RecipeRegistry()

    val villagerProfession: Registry<VillagerProfession> = register("villager_profession", Registry(codec = VillagerProfession))
    val villagerType: ResourceLocationRegistry = register("villager_type", ResourceLocationRegistry())

    val catVariants: Registry<CatVariant> = register("cat_variant", Registry(codec = CatVariant))
    val frogVariants: Registry<FrogVariant> = register("frog_variant", Registry(codec = FrogVariant))

    val equipmentSlot: EnumRegistry<EquipmentSlots> = EnumRegistry(values = EquipmentSlots)

    val entityDataTypes: EnumRegistry<EntityDataTypes> = EnumRegistry(values = EntityDataTypes, fixer = EntityDataTypesFixer)

    val titleActions: EnumRegistry<TitleS2CF.TitleActions> = EnumRegistry(values = TitleS2CF.TitleActions)

    val entityAnimation: EnumRegistry<EntityAnimations> = EnumRegistry(values = EntityAnimations)
    val entityActions: EnumRegistry<EntityActionC2SP.EntityActions> = EnumRegistry(values = EntityActionC2SP.EntityActions)

    val soundGroup: FakeEnumRegistry<SoundGroup> = FakeEnumRegistry(codec = SoundGroup)

    val blockState = BlockStateRegistry(false)

    val entityDataIndexMap: MutableMap<EntityDataField, Int> = mutableMapOf()
    val entityType: Registry<EntityType> = register("entity_type", Registry(codec = EntityType, fixer = EntityTypeFixer))
    val entityObjectType: Registry<EntityObjectType> = register("entity_object_type", Registry(codec = EntityObjectType))
    val damageType: Registry<DamageType> = register("damage_type", Registry(codec = DamageType))

    val blockEntityType = BlockEntityTypeRegistry()
    val blockDataType: Registry<BlockDataDataType> = Registry(codec = BlockDataDataType)

    val containerType: Registry<ContainerType> = Registry(codec = ContainerType)
    val gameEvent: ResourceLocationRegistry = ResourceLocationRegistry()
    val worldEvent: ResourceLocationRegistry = ResourceLocationRegistry()
    val vibrationSource: ResourceLocationRegistry = ResourceLocationRegistry()

    val argumentType: ResourceLocationRegistry = ResourceLocationRegistry()
    val messageType: Registry<ChatMessageType> = register("chat_type", Registry(codec = ChatMessageType))

    val misc = MiscData()

    var isFullyLoaded = false
        private set


    override var parent: Registries? = null
        set(value) {
            field = value
            this.setParent(value)
            fluid.updateWaterLava()
        }

    fun getEntityDataIndex(field: EntityDataField): Int? {
        return entityDataIndexMap[field] ?: parent?.getEntityDataIndex(field)
    }

    fun updateFlattened(flattened: Boolean) {
        block.flattened = flattened
        blockState.flattened = flattened
        item.flattened = flattened
    }

    fun load(version: Version, pixlyzerData: Map<String, Any>, latch: AbstractLatch) {
        updateFlattened(version.flattened)


        var error: Throwable? = null
        val worker = TaskWorker(errorHandler = { _, it -> if (error == null) error = it })
        val stopwatch = Stopwatch()
        // enums
        worker += WorkerTask(this::shape.i) { this.shape.load(pixlyzerData["shapes"]?.toJsonObject()) }

        worker += WorkerTask(this::equipmentSlot.i) { equipmentSlot.initialize(pixlyzerData["equipment_slots"]) }

        worker += WorkerTask(this::entityDataTypes.i) { entityDataTypes.initialize(pixlyzerData["entity_data_data_types"]) }

        worker += WorkerTask(this::titleActions.i) { titleActions.initialize(pixlyzerData["title_actions"]) }
        worker += WorkerTask(this::entityAnimation.i) { entityAnimation.initialize(pixlyzerData["entity_animations"]) }
        worker += WorkerTask(this::entityActions.i) { entityActions.initialize(pixlyzerData["entity_actions"]) }

        // id stuff
        // id resource location stuff
        worker += WorkerTask(this::containerType.i) { containerType.update(pixlyzerData["container_types", "container_type"]?.toJsonObject(), version, this) }
        worker += WorkerTask(this::gameEvent.i) { gameEvent.update(pixlyzerData["game_events"]?.toJsonObject(), version, this) }
        worker += WorkerTask(this::worldEvent.i) { worldEvent.update(pixlyzerData["world_events"]?.toJsonObject(), version, this) }
        worker += WorkerTask(this::vibrationSource.i) { vibrationSource.update(pixlyzerData["vibration_source"]?.toJsonObject(), version, this) }
        worker += WorkerTask(this::argumentType.i) { argumentType.update(pixlyzerData["argument_type"]?.toJsonObject(), version, this) }
        worker += WorkerTask(this::messageType.i) { messageType.update(pixlyzerData["message_types"]?.toJsonObject(), version, this) }


        worker += WorkerTask(this::entityType.i) { entityType.update(pixlyzerData["entities"]?.toJsonObject(), version, this) }

        worker += WorkerTask(this::motif.i) { motif.update(pixlyzerData["motives", "motif"]?.toJsonObject(), version, this) }
        worker += WorkerTask(this::soundEvent.i) { soundEvent.update(pixlyzerData["sound_events"]?.toJsonObject(), version, null) }
        worker += WorkerTask(this::soundGroup.i, dependencies = arrayOf(this::soundEvent.i)) { soundGroup.update(pixlyzerData["sound_groups"]?.unsafeCast(), this) }
        worker += WorkerTask(this::particleType.i) { particleType.update(pixlyzerData["particles", "particle_type"]?.toJsonObject(), version, this) }
        worker += WorkerTask(this::material.i) { material.update(pixlyzerData["materials"]?.toJsonObject(), version, this) }
        worker += WorkerTask(this::enchantment.i) { enchantment.update(pixlyzerData["enchantments"]?.toJsonObject(), version, this) }
        worker += WorkerTask(this::statusEffect.i) { statusEffect.update(pixlyzerData["status_effects"]?.toJsonObject(), version, this) }
        worker += WorkerTask(this::biome.i) { biome.update(pixlyzerData["biomes"]?.toJsonObject(), version, this) }
        worker += WorkerTask(this::dimension.i) { dimension.update(pixlyzerData["dimensions"]?.toJsonObject(), version, this) }
        worker += WorkerTask(this::fluid.i) { fluid.update(pixlyzerData["fluids"]?.toJsonObject(), version, this) }
        worker += WorkerTask(this::block.i, dependencies = arrayOf(this::material.i, this::fluid.i, this::shape.i, this::soundGroup.i, this::particleType.i)) { block.update(pixlyzerData["blocks"]?.toJsonObject(), version, this) }
        worker += WorkerTask(this::item.i, dependencies = arrayOf(this::material.i, this::block.i, this::entityType.i, this::fluid.i, this::statusEffect.i, this::soundEvent.i)) { item.update(pixlyzerData["items"]?.toJsonObject(), version, this) }

        worker += WorkerTask(this::blockEntityType.i, dependencies = arrayOf(this::block.i)) { blockEntityType.update(pixlyzerData["block_entities"]?.toJsonObject(), version, this) }

        worker += WorkerTask(this::villagerProfession.i) { villagerProfession.update(pixlyzerData["villager_professions"]?.toJsonObject(), version, this) }
        worker += WorkerTask(this::villagerType.i) { villagerType.update(pixlyzerData["villager_types"]?.toJsonObject(), version, null) }


        worker += WorkerTask(this::blockDataType.i) { blockDataType.update(pixlyzerData["block_data_data_types"]?.toJsonObject(), version, this) }

        worker += WorkerTask(this::catVariants.i) { catVariants.update(pixlyzerData["variant/cat"]?.toJsonObject(), version, this) }
        worker += WorkerTask(this::frogVariants.i) { frogVariants.update(pixlyzerData["variant/frog"]?.toJsonObject(), version, this) }

        worker += WorkerTask(this::statistic.i) { statistic.update(pixlyzerData["statistics"]?.toJsonObject(), version, this) }
        worker += WorkerTask(this::misc.i, dependencies = arrayOf(this::item.i)) { misc.rawUpdate(pixlyzerData["misc"]?.toJsonObject(), this) }

        val inner = ParentLatch(1, latch)
        worker.work(inner)
        inner.dec()
        while (inner.count > 0) {
            val error2 = error
            if (error2 != null) {
                throw error2
            }
            inner.waitForChange(100L)
        }
        error?.let { throw it }

        // post init
        inner.inc()
        postInit(inner)
        inner.dec()
        inner.await()
        isFullyLoaded = true
        if (cleanup) {
            shape.cleanup()
        }
        fluid.updateWaterLava()
        Log.log(LogMessageType.LOADING, LogLevels.INFO) { "Registries for $version loaded in ${stopwatch.totalTime()}" }
    }

    operator fun <T : RegistryItem> get(type: Class<T>): Registry<T>? {
        return RegistriesUtil.getRegistry(this, type).unsafeCast()
    }

    private fun <T, R : AbstractRegistry<T>> register(name: String, registry: R): R {
        registries[name.toResourceLocation()] = registry

        return registry
    }

    operator fun get(name: ResourceLocation): AbstractRegistry<*>? {
        return registries[name]
    }

    fun update(version: Version, registries: JsonObject) {
        // TODO: Clear them first?
        for ((key, value) in registries) {
            val fixedKey = key.toResourceLocation().fixRegistry()
            val registry = this[fixedKey]
            if (registry == null) {
                Log.log(LogMessageType.LOADING, LogLevels.WARN) { "Can not find registry: $fixedKey" }
                continue
            }
            val values: List<JsonObject> = if (value is List<*>) {
                value.unsafeCast()
            } else {
                value.asJsonObject()["value"].listCast()!!
            }

            try {
                registry.update(values, version, this)
            } catch (error: Throwable) {
                error.printStackTrace()
                Log.log(LogMessageType.NETWORK_IN, LogLevels.WARN) { "Can not update $fixedKey registry: $error" }
            }
        }
    }

    private val KProperty0<*>.i get() = name
}
