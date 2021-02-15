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
import com.google.gson.JsonObject
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.EntityClassMappings
import de.bixilon.minosoft.data.Mappings
import de.bixilon.minosoft.data.entities.EntityInformation
import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.mappings.*
import de.bixilon.minosoft.data.mappings.blocks.Block
import de.bixilon.minosoft.data.mappings.blocks.BlockProperties
import de.bixilon.minosoft.data.mappings.blocks.BlockRotations
import de.bixilon.minosoft.data.mappings.particle.Particle
import de.bixilon.minosoft.data.mappings.statistics.Statistic
import de.bixilon.minosoft.gui.rendering.chunk.models.BlockModel
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.logging.Log
import javafx.util.Pair
import java.util.*


class VersionMapping(var version: Version?) {
    private val availableFeatures = HashSet<Mappings>()

    private val motiveIdMap = HashBiMap.create<Int, Motive>(30)
    private val motiveIdentifierMap = HashBiMap.create<ModIdentifier, Motive>(30)
    private val itemIdMap = HashBiMap.create<Int, Item>(1200)
    private val itemIdentifierMap = HashBiMap.create<ModIdentifier, Item>(1200)
    private val enchantmentIdMap = HashBiMap.create<Int, Enchantment>(50)
    private val enchantmentIdentifierMap = HashBiMap.create<ModIdentifier, Enchantment>(50)
    private val particleIdMap = HashBiMap.create<Int, Particle>(70)
    private val particleIdentifierMap = HashBiMap.create<ModIdentifier, Particle>(70)
    private val mobEffectIdMap = HashBiMap.create<Int, MobEffect>(40)
    private val mobEffectIdentifierMap = HashBiMap.create<ModIdentifier, MobEffect>(40)
    private val blockIdIdMap = HashBiMap.create<Int, BlockId>(800)
    private val blockIdIdentifierMap = HashBiMap.create<ModIdentifier, BlockId>(800)
    private val statisticIdMap = HashBiMap.create<Int, Statistic>(80)
    private val statisticIdentifierMap = HashBiMap.create<ModIdentifier, Statistic>(80)
    private val dimensionIdMap = HashBiMap.create<Int, Dimension>()
    var dimensionIdentifierMap: HashBiMap<ModIdentifier, Dimension> = HashBiMap.create()

    val blockIdMap = HashBiMap.create<Int, Block>(20000)

    private val entityInformationMap = HashBiMap.create<Class<out Entity>, EntityInformation>(120)
    private val entityMetaIndexMap = HashMap<EntityMetaDataFields, Int>(180)
    private val entityMetaIndexOffsetParentMapping: MutableMap<ModIdentifier, Pair<ModIdentifier, Int>> = mutableMapOf() // identifier, <Parent, Offset>
    private val entityIdClassMap = HashBiMap.create<Int, Class<out Entity?>>(120)

    private val blockModels = HashBiMap.create<ModIdentifier, BlockModel>(500)

    var parentMapping: VersionMapping? = null


    fun getItem(itemId: Int, metaData: Int): Item? {
        var versionItemId = itemId shl 16
        if (metaData > 0 && metaData < Short.MAX_VALUE) {
            versionItemId = versionItemId or metaData
        }
        return getItemByIdIgnoreFlattened(versionItemId) ?: getItemByIdIgnoreFlattened(itemId shl 16) // ignore meta data ?
    }

    fun getItem(identifier: ModIdentifier): Item? {
        return parentMapping?.getItem(identifier) ?: itemIdentifierMap[identifier]
    }

    fun getItem(itemId: Int): Item? {
        return if (version!!.isFlattened()) {
            getItem(itemId ushr 16, itemId and 0xFFFF)
        } else {
            getItemByIdIgnoreFlattened(itemId)
        }
    }

    fun getItemId(item: Item): Int? {
        return parentMapping?.getItemId(item) ?: itemIdMap.inverse()[item]
    }

    private fun getItemByIdIgnoreFlattened(itemId: Int): Item? {
        return parentMapping?.getItem(itemId) ?: itemIdMap[itemId]
    }

    fun getMotive(identifier: ModIdentifier): Motive? {
        return parentMapping?.getMotive(identifier) ?: motiveIdentifierMap[identifier]
    }

    fun getMotive(motiveId: Int): Motive? {
        return parentMapping?.getMotive(motiveId) ?: motiveIdMap[motiveId]
    }

    fun getMotiveId(motive: Motive): Int {
        return parentMapping?.getMotiveId(motive) ?: motiveIdMap.inverse()[motive]!!
    }

    fun getEnchantment(identifier: ModIdentifier): Enchantment? {
        return parentMapping?.getEnchantment(identifier) ?: enchantmentIdentifierMap[identifier]
    }

    fun getEnchantment(enchantmentId: Int): Enchantment? {
        return parentMapping?.getEnchantment(enchantmentId) ?: enchantmentIdMap[enchantmentId]
    }

    fun getEnchantmentId(enchantment: Enchantment): Int {
        return parentMapping?.getEnchantmentId(enchantment) ?: enchantmentIdMap.inverse()[enchantment]!!
    }

    fun getParticle(identifier: ModIdentifier): Particle? {
        return parentMapping?.getParticle(identifier) ?: particleIdentifierMap[identifier]
    }

    fun getParticle(particleId: Int): Particle? {
        return parentMapping?.getParticle(particleId) ?: particleIdMap[particleId]
    }

    fun getParticleId(particle: Particle): Int {
        return parentMapping?.getParticleId(particle) ?: particleIdMap.inverse()[particle]!!
    }

    fun getMobEffect(identifier: ModIdentifier): MobEffect? {
        return parentMapping?.getMobEffect(identifier) ?: mobEffectIdentifierMap[identifier]
    }

    fun getMobEffect(mobEffectId: Int): MobEffect? {
        return parentMapping?.getMobEffect(mobEffectId) ?: mobEffectIdMap[mobEffectId]
    }

    fun getMobEffectID(mobEffect: MobEffect): Int {
        return parentMapping?.getMobEffectID(mobEffect) ?: mobEffectIdMap.inverse()[mobEffect]!!
    }

    fun getBlockId(identifier: ModIdentifier): BlockId? {
        return parentMapping?.getBlockId(identifier) ?: blockIdIdentifierMap[identifier]
    }

    fun getBlockId(blockId: Int): BlockId? {
        return parentMapping?.getBlockId(blockId) ?: blockIdIdMap[blockId]
    }

    fun getBlockIdId(blockId: BlockId): Int {
        return parentMapping?.getBlockIdId(blockId) ?: blockIdIdMap.inverse()[blockId]!!
    }

    fun getStatistic(identifier: ModIdentifier): Statistic? {
        return parentMapping?.getStatistic(identifier) ?: statisticIdentifierMap[identifier]
    }

    fun getStatistic(statisticId: Int): Statistic? {
        return parentMapping?.getStatistic(statisticId) ?: statisticIdMap[statisticId]
    }

    fun getStatisticId(statistic: Statistic): Int {
        return parentMapping?.getStatisticId(statistic) ?: statisticIdMap.inverse()[statistic]!!
    }

    fun getDimension(identifier: ModIdentifier): Dimension? {
        return parentMapping?.getDimension(identifier) ?: dimensionIdentifierMap[identifier]
    }

    fun getDimension(dimensionId: Int): Dimension? {
        return parentMapping?.getDimension(dimensionId) ?: dimensionIdMap[dimensionId]
    }

    fun getDimensionId(dimension: Dimension): Int {
        return parentMapping?.getDimensionId(dimension) ?: dimensionIdMap.inverse()[dimension]!!
    }

    fun getBlock(blockId: Int): Block? {
        if (blockId == ProtocolDefinition.NULL_BLOCK_ID) {
            return null
        }
        return parentMapping?.getBlock(blockId) ?: blockIdMap[blockId]
    }

    fun getBlockId(block: Block): Int {
        if (block.identifier == ProtocolDefinition.AIR_IDENTIFIER) {
            return ProtocolDefinition.NULL_BLOCK_ID
        }
        return parentMapping?.getBlockId(block) ?: blockIdMap.inverse()[block]!!
    }


    fun getEntityInformation(clazz: Class<out Entity?>): EntityInformation? {
        return parentMapping?.getEntityInformation(clazz) ?: entityInformationMap[clazz]
    }

    fun getEntityMetaDataIndex(field: EntityMetaDataFields): Int? {
        return parentMapping?.getEntityMetaDataIndex(field) ?: entityMetaIndexMap[field]
    }

    fun getEntityClassById(entityTypeId: Int): Class<out Entity?>? {
        return parentMapping?.getEntityClassById(entityTypeId) ?: entityIdClassMap[entityTypeId]
    }


    fun load(type: Mappings, mod: String, data: JsonObject?, version: Version) {
        if (data == null) {
            availableFeatures.add(type)
            return
        }
        when (type) {
            Mappings.REGISTRIES -> {
                data["item"]?.asJsonObject?.getAsJsonObject("entries")?.let {
                    for ((key, value) in it.entrySet()) {
                        check(value is JsonObject) { "Invalid motive json" }
                        val identifier = ModIdentifier(mod, key)
                        val item = Item(identifier)
                        value["id"]?.asInt?.let { id ->
                            var itemId = id
                            if (version.versionId < ProtocolDefinition.FLATTING_VERSION_ID) {
                                itemId = itemId shl 16
                                value["meta"]?.asInt?.let { meta ->
                                    itemId = itemId or meta
                                }
                            }
                            itemIdMap[id] = item
                        }
                        itemIdentifierMap[identifier] = item
                    }
                }

                data["enchantment"]?.asJsonObject?.getAsJsonObject("entries")?.let {
                    for ((key, value) in it.entrySet()) {
                        check(value is JsonObject) { "Invalid enchantment json" }
                        val identifier = ModIdentifier(mod, key)
                        val enchantment = Enchantment(identifier)
                        value["id"]?.asInt?.let { id ->
                            enchantmentIdMap[id] = enchantment
                        }
                        enchantmentIdentifierMap[identifier] = enchantment
                    }
                }

                data["custom_stat"]?.asJsonObject?.getAsJsonObject("entries")?.let {
                    for ((key, value) in it.entrySet()) {
                        check(value is JsonObject) { "Invalid statistics json" }
                        val identifier = ModIdentifier(mod, key)
                        val statistic = Statistic(identifier)
                        value["id"]?.asInt?.let { id ->
                            statisticIdMap[id] = statistic
                        }
                        statisticIdentifierMap[identifier] = statistic
                    }
                }

                data["block"]?.asJsonObject?.getAsJsonObject("entries")?.let {
                    for ((key, value) in it.entrySet()) {
                        check(value is JsonObject) { "Invalid block id json" }
                        val identifier = ModIdentifier(mod, key)
                        val blockId = BlockId(identifier)
                        value["id"]?.asInt?.let { id ->
                            blockIdIdMap[id] = blockId
                        }
                        blockIdIdentifierMap[identifier] = blockId
                    }
                }

                data["motive"]?.asJsonObject?.getAsJsonObject("entries")?.let {
                    for ((key, value) in it.entrySet()) {
                        check(value is JsonObject) { "Invalid motive json" }
                        val identifier = ModIdentifier(mod, key)
                        val motive = Motive(identifier)
                        value["id"]?.asInt?.let { id ->
                            motiveIdMap[id] = motive
                        }
                        motiveIdentifierMap[identifier] = motive
                    }
                }

                data["particle"]?.asJsonObject?.getAsJsonObject("entries")?.let {
                    for ((key, value) in it.entrySet()) {
                        check(value is JsonObject) { "Invalid particle json" }
                        val identifier = ModIdentifier(mod, key)
                        val particle = Particle(identifier)
                        value["id"]?.asInt?.let { id ->
                            particleIdMap[id] = particle
                        }
                        particleIdentifierMap[identifier] = particle
                    }
                }

                data["mob_effect"]?.asJsonObject?.getAsJsonObject("entries")?.let {
                    for ((key, value) in it.entrySet()) {
                        check(value is JsonObject) { "Invalid mob effect json" }
                        val identifier = ModIdentifier(mod, key)
                        val mobEffect = MobEffect(identifier)
                        value["id"]?.asInt?.let { id ->
                            mobEffectIdMap[id] = mobEffect
                        }
                        mobEffectIdentifierMap[identifier] = mobEffect
                    }
                }

                data["dimension_type"]?.asJsonObject?.getAsJsonObject("entries")?.let {
                    for ((key, value) in it.entrySet()) {
                        check(value is JsonObject) { "Invalid dimension json" }
                        val identifier = ModIdentifier(mod, key)
                        val dimension = Dimension.deserialize(identifier, value)
                        value["id"]?.asInt?.let { id ->
                            dimensionIdMap[id] = dimension
                        }
                        dimensionIdentifierMap[identifier] = dimension
                    }
                }
            }
            Mappings.BLOCKS -> {
                for ((identifierName, json) in data.entrySet()) {
                    check(json is JsonObject) { "Invalid block json" }

                    for (statesJson in json.getAsJsonArray("states")) {
                        check(statesJson is JsonObject) { "Invalid block state json" }
                        val block = loadBlockState(ModIdentifier(mod, identifierName!!), statesJson)
                        var blockId = getBlockId(block.identifier)
                        val blockNumericId = getBlockId(statesJson, !version.isFlattened())
                        if (StaticConfiguration.DEBUG_MODE) {
                            checkIfBlockIsIsPresent(blockNumericId, block.identifier, blockIdMap)
                        }
                        if (!version.isFlattened()) {
                            // map block id
                            if (blockId == null) {
                                blockId = BlockId(block.identifier)
                                blockIdIdMap[blockNumericId] = blockId
                                blockIdIdentifierMap[block.identifier] = blockId
                            }
                        }
                        blockId!!.blocks.add(block)
                        blockIdMap[blockNumericId] = block
                    }
                }
            }
            Mappings.ENTITIES -> {
                for ((identifierName, json) in data.entrySet()) {
                    check(json is JsonObject) { "Invalid entity meta data json" }
                    val identifier = ModIdentifier(mod, identifierName)
                    loadEntityMapping(identifier, json, data)
                }
            }
            Mappings.MODELS -> {
                val block = data.getAsJsonObject("block")
                val blockModels = block.getAsJsonObject("models")
                for (identifier in blockModels.keySet()) {
                    if (this.blockModels.containsKey(ModIdentifier(mod, identifier))) {
                        continue
                    }
                    loadBlockModel(mod, identifier!!, blockModels)
                }
                val blockStates = block.getAsJsonObject("states")
                for (identifier in blockStates.keySet()) {
                    if (identifier == "dispenser") {
                        Log.debug("")
                    }

                    loadBlockModelState(mod, identifier!!, blockStates)
                }
            }
        }
        availableFeatures.add(type)
    }


    private fun loadBlockModel(mod: String, identifierString: String, fullModData: JsonObject): BlockModel? {
        val identifierString = identifierString.replace("block/", "")
        val identifier = ModIdentifier(mod, identifierString)
        var model = blockModels[identifier]
        if (blockModels.containsKey(identifier)) {
            return model
        }
        val data = fullModData.getAsJsonObject(identifierString)
        var parent: BlockModel? = null
        data["parent"]?.asString?.let {
            parent = loadBlockModel(mod, it, fullModData)
        }

        model = data["conditional"]?.let {
            // ToDo
            return@let BlockModel(parent, data)
        } ?: BlockModel(parent, data)

        blockModels[identifier] = model
        return model
    }


    private fun loadBlockModelState(mod: String, identifierString: String, fullModData: JsonObject) {
        if (identifierString == "item_frame" || identifierString == "glow_item_frame") {
            return  // ToDo
        }
        val blockData = fullModData.getAsJsonObject(identifierString)
        val identifier = ModIdentifier(mod, identifierString)
        val states: JsonArray? = blockData["states"]?.asJsonArray
        if (states == null) {
            Log.warn("Block model state: Not states (%s)", identifier)
            return
        }

        val blockStates = getBlockId(identifier)!!.blocks

        for (value in states) {
            check(value is JsonObject) { "Invalid model json" }

            val state = loadBlockState(identifier, value)
            var ckecked = false
            for (blockState in blockStates) {
                if (blockState.bareEquals(state)) {
                    for (type in value.getAsJsonArray("types")) {
                        check(type is JsonObject) { "Invalid block type json" }
                        blockState.blockModels.add(BlockModel(blockModels[ModIdentifier(type["model"].asString.replace("block/", ""))], type))
                    }
                    ckecked = true
                }
            }
            if (!ckecked) {
                Log.warn("Block model state: Block is null (%s)", state)
                continue
            }
        }
    }


    private fun loadBlockState(identifier: ModIdentifier, blockStateJson: JsonObject): Block {
        blockStateJson["properties"]?.asJsonObject?.let {
            var rotation: BlockRotations = BlockRotations.NONE
            for (rotationName in ROTATION_PROPERTIES) {
                it[rotationName]?.asString?.let { rotationValue ->
                    rotation = BlockRotations.ROTATION_MAPPING[rotationValue]!!
                    it.remove(rotationName)
                    // break
                }
                if (rotation != BlockRotations.NONE) {
                    break
                }
            }
            val properties: MutableSet<BlockProperties> = mutableSetOf()

            for (propertyName in it.keySet()) {
                if (StaticConfiguration.DEBUG_MODE) {
                    if (BlockProperties.PROPERTIES_MAPPING[propertyName] == null) {
                        throw RuntimeException(String.format("Unknown block property: %s (identifier=%s)", propertyName, identifier))
                    }
                    if (BlockProperties.PROPERTIES_MAPPING[propertyName]?.get(it[propertyName]?.asString) == null) {
                        throw RuntimeException(String.format("Unknown block property: %s -> %s (identifier=%s)", propertyName, it[propertyName]?.asString, identifier))
                    }
                }
                properties.add(BlockProperties.PROPERTIES_MAPPING[propertyName]!![it[propertyName].asString]!!)
            }
            return Block(identifier, properties, rotation)
        }
        return Block(identifier)
    }

    private fun loadEntityMapping(identifier: ModIdentifier, data: JsonObject, fullModData: JsonObject) {
        val clazz = EntityClassMappings.getByIdentifier(identifier)
        val information: EntityInformation? = EntityInformation.deserialize(identifier, data)
        information?.let {
            // not abstract, has id and attributes
            entityInformationMap[clazz] = information
            data["id"]?.asInt.let {
                entityIdClassMap[it] = clazz
            }

        }
        var parent: ModIdentifier? = null
        var metaDataIndexOffset = 0
        data["extends"]?.asString?.let {
            parent = ModIdentifier(it)
            // check if parent has been loaded
            val metaParent = entityMetaIndexOffsetParentMapping[parent]
            if (metaParent == null) {
                loadEntityMapping(ModIdentifier(identifier.mod, it), fullModData[it].asJsonObject, fullModData)
            }
            metaDataIndexOffset += entityMetaIndexOffsetParentMapping[parent]!!.value
        }
        data["data"]?.let {
            when (it) {
                is JsonArray -> {
                    for (jsonElement in it) {
                        this.entityMetaIndexMap[EntityMetaDataFields.valueOf(jsonElement.asString)] = metaDataIndexOffset++
                    }
                }
                is JsonObject -> {
                    for ((key, value) in it.entrySet()) {
                        this.entityMetaIndexMap[EntityMetaDataFields.valueOf(key)] = value.asInt
                        metaDataIndexOffset++
                    }
                }
                else -> {
                    throw RuntimeException("entities.json is invalid")
                }
            }

        }
        entityMetaIndexOffsetParentMapping[identifier] = Pair(parent, metaDataIndexOffset)
    }

    fun unload() {
        for (field in this::class.java.fields) {
            if (!field.type.isAssignableFrom(Map::class.java)) {
                continue
            }
            field.javaClass.getMethod("clear").invoke(this)
        }
    }

    val isFullyLoaded: Boolean
        get() {
            if (availableFeatures.size == Mappings.values().size) {
                return true
            }
            for (mapping in Mappings.values()) {
                if (!availableFeatures.contains(mapping)) {
                    return false
                }
            }
            return true
        }


    companion object {
        private val ROTATION_PROPERTIES = setOf("facing", "rotation", "orientation", "axis")
        private fun getBlockId(json: JsonObject, metaData: Boolean): Int {
            var blockId = json["id"].asInt
            if (metaData) {
                blockId = blockId shl 4
                if (json.has("meta")) {
                    // old format (with metadata)
                    blockId = blockId or json["meta"].asByte.toInt()
                }
            }
            return blockId
        }

        private fun checkIfBlockIsIsPresent(blockId: Int, identifier: ModIdentifier, blockIdMap: HashBiMap<Int, Block>) {
            blockIdMap[blockId]?.let {
                throw RuntimeException(String.format("Block Id %s is already present for %s! (identifier=%s)", blockId, it, identifier))
            }
        }
    }
}
