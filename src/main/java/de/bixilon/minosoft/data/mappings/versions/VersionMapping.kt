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


class VersionMapping {
    private val availableFeatures = HashSet<Mappings>()
    private val entityInformationMap = HashBiMap.create<Class<out Entity>, EntityInformation>(120)
    private val entityMetaIndexMap = HashMap<EntityMetaDataFields, Int>(180)
    private val entityMetaIndexOffsetParentMapping = HashMap<String, Pair<String, Int>>(150)
    private val entityIdClassMap = HashBiMap.create<Int, Class<out Entity?>>(120)
    private val motiveIdentifierMap = HashBiMap.create<String, Motive>(30)
    private val particleIdentifierMap = HashBiMap.create<String, Particle>(70)
    private val statisticIdentifierMap = HashBiMap.create<String, Statistic>(80)
    private val itemMap = HashBiMap.create<Int, Item?>(1200)
    private val motiveIdMap = HashBiMap.create<Int, Motive>(30)
    private val mobEffectMap = HashBiMap.create<Int, MobEffect>(40)
    val blockMap = HashBiMap.create<Int, Block>(20000)
    private val blockIdMap = HashBiMap.create<Int, BlockId>(800)
    private val enchantmentMap = HashBiMap.create<Int, Enchantment>(50)
    private val particleIdMap = HashBiMap.create<Int, Particle>(80)
    private val statisticIdMap = HashBiMap.create<Int, Statistic>(80)
    private val blockModels = HashBiMap.create<ModIdentifier, BlockModel>(500)
    private var dimensionIdentifierMap: HashBiMap<ModIdentifier, Dimension> = HashBiMap.create()
    private var dimensionMap = HashBiMap.create<Int, Dimension>()

    var version: Version
    var parentMapping: VersionMapping? = null

    constructor(version: Version) {
        this.version = version
    }

    constructor(version: Version, parentMapping: VersionMapping?) {
        this.version = version
        this.parentMapping = parentMapping
    }

    fun getMotiveByIdentifier(identifier: String): Motive? {
        return parentMapping?.getMotiveByIdentifier(identifier) ?: motiveIdentifierMap[identifier]
    }

    fun getStatisticByIdentifier(identifier: String): Statistic? {
        return parentMapping?.getStatisticByIdentifier(identifier) ?: statisticIdentifierMap[identifier]
    }

    fun getParticleByIdentifier(identifier: String): Particle? {
        return parentMapping?.getParticleByIdentifier(identifier) ?: particleIdentifierMap[identifier]
    }

    fun getItemById(itemId: Int): Item? {
        return if (!version.isFlattened) {
            getItemByLegacy(itemId ushr 16, itemId and 0xFFFF)
        } else {
            getItemByIdIgnoreFlattened(itemId)
        }
    }

    private fun getItemByIdIgnoreFlattened(itemId: Int): Item? {
        return parentMapping?.getItemById(itemId) ?: itemMap[itemId]
    }

    fun getItemId(item: Item?): Int? {
        return parentMapping?.getItemId(item) ?: itemMap.inverse()[item]
    }

    fun getMotiveById(motiveId: Int): Motive? {
        return parentMapping?.getMotiveById(motiveId) ?: motiveIdMap[motiveId]
    }

    fun getMobEffectById(mobEffectId: Int): MobEffect? {
        return parentMapping?.getMobEffectById(mobEffectId) ?: mobEffectMap[mobEffectId]
    }

    fun getDimensionById(dimensionId: Int): Dimension? {
        return parentMapping?.getDimensionById(dimensionId) ?: dimensionMap[dimensionId]
    }

    fun getBlockById(blockId: Int): Block? {
        if (blockId == ProtocolDefinition.NULL_BLOCK_ID) {
            return null
        }
        return parentMapping?.getBlockById(blockId) ?: blockMap[blockId]
    }

    fun getBlockIdById(blockIdId: Int): BlockId? {
        return parentMapping?.getBlockIdById(blockIdId) ?: blockIdMap[blockIdId]
    }

    fun getEnchantmentById(enchantmentId: Int): Enchantment? {
        return parentMapping?.getEnchantmentById(enchantmentId) ?: enchantmentMap[enchantmentId]
    }

    fun getParticleById(particleId: Int): Particle? {
        return parentMapping?.getParticleById(particleId) ?: particleIdMap[particleId]
    }

    fun getStatisticById(statisticId: Int): Statistic? {
        return parentMapping?.getStatisticById(statisticId) ?: statisticIdMap[statisticId]
    }

    fun getEnchantmentId(enchantment: Enchantment): Int? {
        return parentMapping?.getEnchantmentId(enchantment) ?: enchantmentMap.inverse()[enchantment]
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

    fun getDimensionByIdentifier(identifier: ModIdentifier): Dimension? {
        return parentMapping?.getDimensionByIdentifier(identifier) ?: dimensionIdentifierMap[identifier]
    }

    fun setDimensions(dimensions: HashBiMap<ModIdentifier, Dimension>) {
        dimensionIdentifierMap = dimensions
    }

    fun getItemByLegacy(itemId: Int, metaData: Int): Item? {
        var versionItemId = itemId shl 16
        if (metaData > 0 && metaData < Short.MAX_VALUE) {
            versionItemId = versionItemId or metaData
        }
        return getItemByIdIgnoreFlattened(versionItemId) ?: getItemByIdIgnoreFlattened(itemId shl 16) // ignore meta data ?
    }

    fun load(type: Mappings, mod: String, data: JsonObject?, version: Version) {
        if (data == null) {
            availableFeatures.add(type)
            return
        }
        when (type) {
            Mappings.REGISTRIES -> {
                val itemJson = data.getAsJsonObject("item").getAsJsonObject("entries")
                for (identifier in itemJson.keySet()) {
                    val item = Item(mod, identifier)
                    val identifierJSON = itemJson.getAsJsonObject(identifier)
                    var itemId = identifierJSON["id"].asInt
                    if (version.versionId < ProtocolDefinition.FLATTING_VERSION_ID) {
                        itemId = itemId shl 16
                        if (identifierJSON.has("meta")) {
                            // old format (with metadata)
                            itemId = itemId or identifierJSON["meta"].asInt
                        }
                    }
                    itemMap[itemId] = item
                }
                val enchantmentJson = data.getAsJsonObject("enchantment").getAsJsonObject("entries")
                for (identifier in enchantmentJson.keySet()) {
                    val enchantment = Enchantment(mod, identifier)
                    enchantmentMap[enchantmentJson.getAsJsonObject(identifier)["id"].asInt] = enchantment
                }
                val statisticJson = data.getAsJsonObject("custom_stat").getAsJsonObject("entries")
                for (identifier in statisticJson.keySet()) {
                    val statistic = Statistic(mod, identifier)
                    if (statisticJson.getAsJsonObject(identifier).has("id")) {
                        statisticIdMap[statisticJson.getAsJsonObject(identifier)["id"].asInt] = statistic
                    }
                    statisticIdentifierMap[identifier] = statistic
                }
                val blockIdJson = data.getAsJsonObject("block").getAsJsonObject("entries")
                for (identifier in blockIdJson.keySet()) {
                    val blockId = BlockId(mod, identifier)
                    blockIdMap[blockIdJson.getAsJsonObject(identifier)["id"].asInt] = blockId
                }
                val motiveJson = data.getAsJsonObject("motive").getAsJsonObject("entries")
                for (identifier in motiveJson.keySet()) {
                    val motive = Motive(mod, identifier)
                    if (motiveJson.getAsJsonObject(identifier).has("id")) {
                        motiveIdMap[motiveJson.getAsJsonObject(identifier)["id"].asInt] = motive
                    }
                    motiveIdentifierMap[identifier] = motive
                }
                val particleJson = data.getAsJsonObject("particle_type").getAsJsonObject("entries")
                for (identifier in particleJson.keySet()) {
                    val particle = Particle(mod, identifier)
                    if (particleJson.getAsJsonObject(identifier).has("id")) {
                        particleIdMap[particleJson.getAsJsonObject(identifier)["id"].asInt] = particle
                    }
                    particleIdentifierMap[identifier] = particle
                }
                val mobEffectJson = data.getAsJsonObject("mob_effect").getAsJsonObject("entries")
                for (identifier in mobEffectJson.keySet()) {
                    val mobEffect = MobEffect(mod, identifier)
                    mobEffectMap[mobEffectJson.getAsJsonObject(identifier)["id"].asInt] = mobEffect
                }
                if (data.has("dimension_type")) {
                    dimensionMap = HashBiMap.create()
                    val dimensionJson = data.getAsJsonObject("dimension_type").getAsJsonObject("entries")
                    for (identifier in dimensionJson.keySet()) {
                        val dimension = Dimension(ModIdentifier(mod, identifier).fullIdentifier, hasSkyLight = dimensionJson.getAsJsonObject(identifier)["has_skylight"].asBoolean)
                        dimensionMap[dimensionJson.getAsJsonObject(identifier)["id"].asInt] = dimension
                    }
                }
            }
            Mappings.BLOCKS -> {
                for (identifierName in data.keySet()) {
                    val identifierJSON = data.getAsJsonObject(identifierName)
                    val statesArray = identifierJSON.getAsJsonArray("states")
                    var i = 0
                    while (i < statesArray.size()) {
                        val statesJSON = statesArray[i].asJsonObject
                        val block = loadBlockState(mod, identifierName!!, statesJSON)
                        if (this.version.isFlattened) {
                            // map block id
                            blockIdMap[blockIdMap.inverse()[BlockId(block)]]!!.blocks.add(block)
                        }
                        val blockNumericId = getBlockId(statesJSON, !version.isFlattened)
                        if (StaticConfiguration.DEBUG_MODE) {
                            checkIfBlockIsIsPresent(blockNumericId, identifierName, blockMap)
                        }
                        if (!version.isFlattened) {
                            // map block id
                            var blockId = blockIdMap[blockIdMap.inverse()[BlockId(block)]]
                            if (blockId == null) {
                                blockId = BlockId(block)
                                blockIdMap[blockNumericId] = blockId
                            }
                            blockId.blocks.add(block)
                        }
                        blockMap[blockNumericId] = block
                        i++
                    }
                }
            }
            Mappings.ENTITIES -> {
                for (identifier in data.keySet()) {
                    if (entityMetaIndexOffsetParentMapping.containsKey(identifier)) {
                        continue  // ToDo: Check this in the function (because of parent checking, etc)
                    }
                    loadEntityMapping(mod, identifier!!, data)
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

        val blockStates = blockIdMap[blockIdMap.inverse()[identifier]!!]!!.blocks

        for (value in states) {
            check(value is JsonObject) { "Invalid model json" }

            val state = loadBlockState(mod, identifierString, value)
            var ckecked = false
            for (blockState in blockStates) {
                if (blockState.bareEquals(state)) {
                    for (type in value.getAsJsonArray("types")) {
                        blockState.blockModels.add(BlockModel(blockModels[ModIdentifier(type.asJsonObject["model"].asString.replace("block/", ""))], value))
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


    private fun loadBlockState(mod: String, identifier: String, blockStateJson: JsonObject): Block {
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
            return Block(mod, identifier, properties, rotation)
        }
        return Block(mod, identifier)
    }

    private fun loadEntityMapping(mod: String, identifier: String, fullModData: JsonObject) {
        val data = fullModData.getAsJsonObject(identifier)
        val clazz = EntityClassMappings.getByIdentifier(mod, identifier)
        val information: EntityInformation? = EntityInformation.deserialize(mod, identifier, data)
        information?.let {
            // not abstract, has id and attributes
            entityInformationMap[clazz] = information
            data["id"]?.asInt.let {
                entityIdClassMap[it] = clazz
            }

        }
        var parent: String? = null
        var metaDataIndexOffset = 0
        data["extends"]?.asString?.let {
            parent = it
            // check if parent has been loaded
            val metaParent = entityMetaIndexOffsetParentMapping[parent]
            if (metaParent == null) {
                loadEntityMapping(mod, it, fullModData)
            }
            metaDataIndexOffset += entityMetaIndexOffsetParentMapping[parent]!!.value
        }
        data["data"]?.let {
            when {
                it is JsonArray -> {
                    for (jsonElement in it) {
                        this.entityMetaIndexMap[EntityMetaDataFields.valueOf(jsonElement.asString)] = metaDataIndexOffset++
                    }
                }
                it is JsonObject -> {
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
        motiveIdentifierMap.clear()
        particleIdentifierMap.clear()
        statisticIdentifierMap.clear()
        itemMap.clear()
        motiveIdMap.clear()
        mobEffectMap.clear()
        dimensionMap.clear()
        blockMap.clear()
        blockIdMap.clear()
        enchantmentMap.clear()
        particleIdMap.clear()
        statisticIdMap.clear()
        entityInformationMap.clear()
        entityMetaIndexMap.clear()
        entityMetaIndexOffsetParentMapping.clear()
        entityIdClassMap.clear()
        blockModels.clear()
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

    fun doesItemExist(identifier: ModIdentifier): Boolean {
        return parentMapping?.doesItemExist(identifier) ?: itemMap[identifier] != null
    }

    fun doesBlockExist(identifier: ModIdentifier): Boolean {
        return parentMapping?.doesBlockExist(identifier) ?: blockIdMap[identifier] != null
    }

    fun doesEnchantmentExist(identifier: ModIdentifier): Boolean {
        return parentMapping?.doesEnchantmentExist(identifier) ?: enchantmentMap[identifier] != null
    }

    fun doesMobEffectExist(identifier: ModIdentifier): Boolean {
        return parentMapping?.doesMobEffectExist(identifier) ?: mobEffectMap[identifier] != null
    }

    fun doesDimensionExist(identifier: ModIdentifier): Boolean {
        return parentMapping?.doesDimensionExist(identifier) ?: dimensionMap[identifier] != null
    }

    fun doesParticleExist(identifier: ModIdentifier): Boolean {
        return parentMapping?.doesParticleExist(identifier) ?: particleIdMap[identifier] != null
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

        private fun checkIfBlockIsIsPresent(blockId: Int, identifierName: String, versionMapping: HashBiMap<Int, Block>) {
            if (versionMapping.containsKey(blockId)) {
                throw RuntimeException(String.format("Block Id %s is already present for %s! (identifier=%s)", blockId, versionMapping[blockId], identifierName))
            }
        }
    }
}
