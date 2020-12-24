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

package de.bixilon.minosoft.data.mappings.versions;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.config.StaticConfiguration;
import de.bixilon.minosoft.data.EntityClassMappings;
import de.bixilon.minosoft.data.Mappings;
import de.bixilon.minosoft.data.entities.EntityInformation;
import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.mappings.*;
import de.bixilon.minosoft.data.mappings.blocks.Block;
import de.bixilon.minosoft.data.mappings.blocks.BlockProperties;
import de.bixilon.minosoft.data.mappings.blocks.BlockRotations;
import de.bixilon.minosoft.data.mappings.particle.Particle;
import de.bixilon.minosoft.data.mappings.statistics.Statistic;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import javafx.util.Pair;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;

public class VersionMapping {
    private final HashSet<Mappings> loaded = new HashSet<>();
    private final HashBiMap<Class<? extends Entity>, EntityInformation> entityInformationMap = HashBiMap.create(100);
    private final HashMap<EntityMetaDataFields, Integer> entityMetaIndexMap = new HashMap<>(100);
    private final HashMap<String, Pair<String, Integer>> entityMetaIndexOffsetParentMapping = new HashMap<>(100);
    private final HashBiMap<Integer, Class<? extends Entity>> entityIdClassMap = HashBiMap.create(100);
    private Version version;
    private VersionMapping parentMapping;
    private HashBiMap<String, Motive> motiveIdentifierMap = HashBiMap.create();
    private HashBiMap<String, Particle> particleIdentifierMap = HashBiMap.create();
    private HashBiMap<String, Statistic> statisticIdentifierMap = HashBiMap.create();
    private HashMap<String, HashBiMap<String, Dimension>> dimensionIdentifierMap = new HashMap<>();
    private HashBiMap<Integer, Item> itemMap = HashBiMap.create();
    private HashBiMap<Integer, Motive> motiveIdMap = HashBiMap.create();
    private HashBiMap<Integer, MobEffect> mobEffectMap = HashBiMap.create();
    private HashBiMap<Integer, Dimension> dimensionMap = HashBiMap.create();
    private HashBiMap<Integer, Block> blockMap = HashBiMap.create();
    private HashBiMap<Integer, BlockId> blockIdMap = HashBiMap.create();
    private HashBiMap<Integer, Enchantment> enchantmentMap = HashBiMap.create();
    private HashBiMap<Integer, Particle> particleIdMap = HashBiMap.create();
    private HashBiMap<Integer, Statistic> statisticIdMap = HashBiMap.create();

    public VersionMapping(Version version) {
        this.version = version;
        this.parentMapping = null;
    }

    public VersionMapping(Version version, VersionMapping parentMapping) {
        this.version = version;
        this.parentMapping = parentMapping;
    }

    private static int getBlockId(JsonObject json, boolean metaData) {
        int blockId = json.get("id").getAsInt();
        if (metaData) {
            blockId <<= 4;
            if (json.has("meta")) {
                // old format (with metadata)
                blockId |= json.get("meta").getAsByte();
            }
        }
        return blockId;
    }

    private static void checkAndCrashIfBlockIsIn(int blockId, String identifierName, HashBiMap<Integer, Block> versionMapping) {
        if (versionMapping.containsKey(blockId)) {
            throw new RuntimeException(String.format("Block Id %s is already present for %s! (identifier=%s)", blockId, versionMapping.get(blockId), identifierName));
        }
    }

    public Motive getMotiveByIdentifier(String identifier) {
        if (this.parentMapping != null) {
            Motive motive = this.parentMapping.getMotiveByIdentifier(identifier);
            if (motive != null) {
                return motive;
            }
        }
        return this.motiveIdentifierMap.get(identifier);
    }

    public Statistic getStatisticByIdentifier(String identifier) {
        if (this.parentMapping != null) {
            Statistic statistic = this.parentMapping.getStatisticByIdentifier(identifier);
            if (statistic != null) {
                return statistic;
            }
        }
        return this.statisticIdentifierMap.get(identifier);
    }

    public Particle getParticleByIdentifier(String identifier) {
        if (this.parentMapping != null) {
            Particle particle = this.parentMapping.getParticleByIdentifier(identifier);
            if (particle != null) {
                return particle;
            }
        }
        return this.particleIdentifierMap.get(identifier);
    }

    public Item getItemById(int versionId) {
        if (!this.version.isFlattened()) {
            return getItemByLegacy(versionId >>> 16, versionId & 0xFFFF);
        }
        return getItemByIdIgnoreFlattened(versionId);
    }

    private Item getItemByIdIgnoreFlattened(int versionId) {
        if (this.parentMapping != null) {
            Item item = this.parentMapping.getItemById(versionId);
            if (item != null) {
                return item;
            }
        }
        return this.itemMap.get(versionId);
    }

    public Integer getItemId(Item item) {
        if (this.parentMapping != null) {
            Integer itemId = this.parentMapping.getItemId(item);
            if (item != null) {
                return itemId;
            }
        }
        return this.itemMap.inverse().get(item);
    }

    public Motive getMotiveById(int versionId) {
        if (this.parentMapping != null) {
            Motive motive = this.parentMapping.getMotiveById(versionId);
            if (motive != null) {
                return motive;
            }
        }
        return this.motiveIdMap.get(versionId);
    }

    public MobEffect getMobEffectById(int versionId) {
        if (this.parentMapping != null) {
            MobEffect mobEffect = this.parentMapping.getMobEffectById(versionId);
            if (mobEffect != null) {
                return mobEffect;
            }
        }
        return this.mobEffectMap.get(versionId);
    }

    public Dimension getDimensionById(int versionId) {
        if (this.parentMapping != null) {
            Dimension dimension = this.parentMapping.getDimensionById(versionId);
            if (dimension != null) {
                return dimension;
            }
        }
        return this.dimensionMap.get(versionId);
    }

    @Nullable
    public Block getBlockById(int versionId) {
        if (versionId == ProtocolDefinition.NULL_BLOCK_ID) {
            return null;
        }
        if (this.parentMapping != null) {
            Block block = this.parentMapping.getBlockById(versionId);
            if (block != null) {
                return block;
            }
        }
        return this.blockMap.get(versionId);
    }

    public BlockId getBlockIdById(int versionId) {
        if (this.parentMapping != null) {
            BlockId blockId = this.parentMapping.getBlockIdById(versionId);
            if (blockId != null) {
                return blockId;
            }
        }
        return this.blockIdMap.get(versionId);
    }

    public Enchantment getEnchantmentById(int versionId) {
        if (this.parentMapping != null) {
            Enchantment enchantment = this.parentMapping.getEnchantmentById(versionId);
            if (enchantment != null) {
                return enchantment;
            }
        }
        return this.enchantmentMap.get(versionId);
    }

    public Particle getParticleById(int versionId) {
        if (this.parentMapping != null) {
            Particle particle = this.parentMapping.getParticleById(versionId);
            if (particle != null) {
                return particle;
            }
        }
        return this.particleIdMap.get(versionId);
    }

    public Statistic getStatisticById(int versionId) {
        if (this.parentMapping != null) {
            Statistic statistic = this.parentMapping.getStatisticById(versionId);
            if (statistic != null) {
                return statistic;
            }
        }
        return this.statisticIdMap.get(versionId);
    }

    public Integer getIdByEnchantment(Enchantment enchantment) {
        if (this.parentMapping != null) {
            Integer enchantmentId = this.parentMapping.getIdByEnchantment(enchantment);
            if (enchantmentId != null) {
                return enchantmentId;
            }
        }
        return this.enchantmentMap.inverse().get(enchantment);
    }

    public EntityInformation getEntityInformation(Class<? extends Entity> clazz) {
        if (this.parentMapping != null) {
            EntityInformation information = this.parentMapping.getEntityInformation(clazz);
            if (information != null) {
                return information;
            }
        }
        return this.entityInformationMap.get(clazz);
    }

    public Integer getEntityMetaDataIndex(EntityMetaDataFields field) {
        if (this.parentMapping != null) {
            Integer metaDataIndex = this.parentMapping.getEntityMetaDataIndex(field);
            if (metaDataIndex != null) {
                return metaDataIndex;
            }
        }
        return this.entityMetaIndexMap.get(field);
    }

    public Class<? extends Entity> getEntityClassById(int id) {
        if (this.parentMapping != null) {
            Class<? extends Entity> clazz = this.parentMapping.getEntityClassById(id);
            if (clazz != null) {
                return clazz;
            }
        }
        return this.entityIdClassMap.get(id);
    }

    public Dimension getDimensionByIdentifier(String identifier) {
        if (this.parentMapping != null) {
            Dimension dimension = this.parentMapping.getDimensionByIdentifier(identifier);
            if (dimension != null) {
                return dimension;
            }
        }
        String[] split = identifier.split(":", 2);
        if (this.dimensionIdentifierMap.containsKey(split[0]) && this.dimensionIdentifierMap.get(split[0]).containsKey(split[1])) {
            return this.dimensionIdentifierMap.get(split[0]).get(split[1]);
        }
        return null;
    }

    public void setDimensions(HashMap<String, HashBiMap<String, Dimension>> dimensions) {
        this.dimensionIdentifierMap = dimensions;
    }

    public Item getItemByLegacy(int itemId, int metaData) {
        int versionItemId = itemId << 16;
        if (metaData > 0 && metaData < Short.MAX_VALUE) {
            versionItemId |= metaData;
        }
        Item item = getItemByIdIgnoreFlattened(versionItemId);
        if (item == null) {
            // ignore meta data ?
            return getItemByIdIgnoreFlattened(itemId << 16);
        }
        return item;
    }

    public void load(Mappings type, String mod, @Nullable JsonObject data, Version version) {
        switch (type) {
            case REGISTRIES -> {
                if (!version.isFlattened() && version.getVersionId() != ProtocolDefinition.PRE_FLATTENING_VERSION_ID) {
                    // clone all values
                    this.itemMap = Versions.PRE_FLATTENING_MAPPING.itemMap;
                    this.enchantmentMap = Versions.PRE_FLATTENING_MAPPING.enchantmentMap;
                    this.statisticIdMap = Versions.PRE_FLATTENING_MAPPING.statisticIdMap;
                    this.statisticIdentifierMap = Versions.PRE_FLATTENING_MAPPING.statisticIdentifierMap;
                    this.blockIdMap = Versions.PRE_FLATTENING_MAPPING.blockIdMap;
                    this.motiveIdMap = Versions.PRE_FLATTENING_MAPPING.motiveIdMap;
                    this.motiveIdentifierMap = Versions.PRE_FLATTENING_MAPPING.motiveIdentifierMap;
                    this.particleIdMap = Versions.PRE_FLATTENING_MAPPING.particleIdMap;
                    this.particleIdentifierMap = Versions.PRE_FLATTENING_MAPPING.particleIdentifierMap;
                    this.mobEffectMap = Versions.PRE_FLATTENING_MAPPING.mobEffectMap;
                    this.dimensionMap = Versions.PRE_FLATTENING_MAPPING.dimensionMap;
                    break;
                }

                if (data == null) {
                    break;
                }

                JsonObject itemJson = data.getAsJsonObject("item").getAsJsonObject("entries");
                for (String identifier : itemJson.keySet()) {
                    Item item = new Item(mod, identifier);
                    JsonObject identifierJSON = itemJson.getAsJsonObject(identifier);
                    int itemId = identifierJSON.get("id").getAsInt();
                    if (version.getVersionId() < ProtocolDefinition.FLATTING_VERSION_ID) {
                        itemId <<= 16;
                        if (identifierJSON.has("meta")) {
                            // old format (with metadata)
                            itemId |= identifierJSON.get("meta").getAsInt();
                        }
                    }
                    this.itemMap.put(itemId, item);
                }
                JsonObject enchantmentJson = data.getAsJsonObject("enchantment").getAsJsonObject("entries");
                for (String identifier : enchantmentJson.keySet()) {
                    Enchantment enchantment = new Enchantment(mod, identifier);
                    this.enchantmentMap.put(enchantmentJson.getAsJsonObject(identifier).get("id").getAsInt(), enchantment);
                }
                JsonObject statisticJson = data.getAsJsonObject("custom_stat").getAsJsonObject("entries");
                for (String identifier : statisticJson.keySet()) {
                    Statistic statistic = new Statistic(mod, identifier);
                    if (statisticJson.getAsJsonObject(identifier).has("id")) {
                        this.statisticIdMap.put(statisticJson.getAsJsonObject(identifier).get("id").getAsInt(), statistic);
                    }
                    this.statisticIdentifierMap.put(identifier, statistic);
                }
                JsonObject blockIdJson = data.getAsJsonObject("block").getAsJsonObject("entries");
                for (String identifier : blockIdJson.keySet()) {
                    BlockId blockId = new BlockId(mod, identifier);
                    this.blockIdMap.put(blockIdJson.getAsJsonObject(identifier).get("id").getAsInt(), blockId);
                }
                JsonObject motiveJson = data.getAsJsonObject("motive").getAsJsonObject("entries");
                for (String identifier : motiveJson.keySet()) {
                    Motive motive = new Motive(mod, identifier);
                    if (motiveJson.getAsJsonObject(identifier).has("id")) {
                        this.motiveIdMap.put(motiveJson.getAsJsonObject(identifier).get("id").getAsInt(), motive);
                    }
                    this.motiveIdentifierMap.put(identifier, motive);
                }
                JsonObject particleJson = data.getAsJsonObject("particle_type").getAsJsonObject("entries");
                for (String identifier : particleJson.keySet()) {
                    Particle particle = new Particle(mod, identifier);
                    if (particleJson.getAsJsonObject(identifier).has("id")) {
                        this.particleIdMap.put(particleJson.getAsJsonObject(identifier).get("id").getAsInt(), particle);
                    }
                    this.particleIdentifierMap.put(identifier, particle);
                }
                JsonObject mobEffectJson = data.getAsJsonObject("mob_effect").getAsJsonObject("entries");
                for (String identifier : mobEffectJson.keySet()) {
                    MobEffect mobEffect = new MobEffect(mod, identifier);
                    this.mobEffectMap.put(mobEffectJson.getAsJsonObject(identifier).get("id").getAsInt(), mobEffect);
                }
                if (data.has("dimension_type")) {
                    this.dimensionMap = HashBiMap.create();
                    JsonObject dimensionJson = data.getAsJsonObject("dimension_type").getAsJsonObject("entries");
                    for (String identifier : dimensionJson.keySet()) {
                        Dimension dimension = new Dimension(mod, identifier, dimensionJson.getAsJsonObject(identifier).get("has_skylight").getAsBoolean());
                        this.dimensionMap.put(dimensionJson.getAsJsonObject(identifier).get("id").getAsInt(), dimension);
                    }
                }
            }
            case BLOCKS -> {
                if (!version.isFlattened() && version.getVersionId() != ProtocolDefinition.PRE_FLATTENING_VERSION_ID) {
                    // clone all values
                    this.blockMap = Versions.PRE_FLATTENING_MAPPING.blockMap;
                    break;
                }

                if (data == null) {
                    break;
                }

                for (String identifierName : data.keySet()) {
                    JsonObject identifierJSON = data.getAsJsonObject(identifierName);
                    JsonArray statesArray = identifierJSON.getAsJsonArray("states");
                    for (int i = 0; i < statesArray.size(); i++) {
                        JsonObject statesJSON = statesArray.get(i).getAsJsonObject();
                        Block block;
                        if (statesJSON.has("properties")) {
                            // properties are optional
                            JsonObject propertiesJSON = statesJSON.getAsJsonObject("properties");
                            BlockRotations rotation = BlockRotations.NONE;
                            if (propertiesJSON.has("facing")) {
                                rotation = BlockRotations.ROTATION_MAPPING.get(propertiesJSON.get("facing").getAsString());
                                propertiesJSON.remove("facing");
                            } else if (propertiesJSON.has("rotation")) {
                                rotation = BlockRotations.ROTATION_MAPPING.get(propertiesJSON.get("rotation").getAsString());
                                propertiesJSON.remove("rotation");
                            } else if (propertiesJSON.has("orientation")) {
                                rotation = BlockRotations.ROTATION_MAPPING.get(propertiesJSON.get("orientation").getAsString());
                                propertiesJSON.remove("orientation");
                            } else if (propertiesJSON.has("axis")) {
                                rotation = BlockRotations.ROTATION_MAPPING.get(propertiesJSON.get("axis").getAsString());
                                propertiesJSON.remove("axis");
                            }

                            HashSet<BlockProperties> properties = new HashSet<>();
                            for (String propertyName : propertiesJSON.keySet()) {
                                if (StaticConfiguration.DEBUG_MODE) {
                                    if (BlockProperties.PROPERTIES_MAPPING.get(propertyName) == null) {
                                        throw new RuntimeException(String.format("Unknown block property: %s (identifier=%s)", propertyName, identifierName));
                                    }
                                    if (BlockProperties.PROPERTIES_MAPPING.get(propertyName).get(propertiesJSON.get(propertyName).getAsString()) == null) {
                                        throw new RuntimeException(String.format("Unknown block property: %s -> %s (identifier=%s)", propertyName, propertiesJSON.get(propertyName).getAsString(), identifierName));
                                    }
                                }
                                properties.add(BlockProperties.PROPERTIES_MAPPING.get(propertyName).get(propertiesJSON.get(propertyName).getAsString()));
                            }

                            block = new Block(mod, identifierName, properties, rotation);

                            if (version.isFlattened()) {
                                // map block id
                                this.blockIdMap.get(this.blockIdMap.inverse().get(new BlockId(block))).getBlocks().add(block);
                            }
                        } else {
                            // no properties, directly add block
                            block = new Block(mod, identifierName);
                        }
                        int blockNumericId = getBlockId(statesJSON, !version.isFlattened());
                        if (StaticConfiguration.DEBUG_MODE) {
                            checkAndCrashIfBlockIsIn(blockNumericId, identifierName, this.blockMap);
                        }

                        if (!version.isFlattened()) {
                            // map block id
                            BlockId blockId = this.blockIdMap.get(this.blockIdMap.inverse().get(new BlockId(block)));
                            if (blockId == null) {
                                blockId = new BlockId(block);
                                this.blockIdMap.put(blockNumericId, blockId);
                            }
                            blockId.getBlocks().add(block);
                        }
                        this.blockMap.put(blockNumericId, block);
                    }
                }
            }
            case ENTITIES -> {
                if (data == null) {
                    break;
                }
                for (String identifier : data.keySet()) {
                    if (this.entityMetaIndexOffsetParentMapping.containsKey(identifier)) {
                        continue;
                    }
                    loadEntityMapping(mod, identifier, data);
                }
            }
        }
        this.loaded.add(type);
    }

    private void loadEntityMapping(String mod, String identifier, JsonObject fullModData) {
        JsonObject data = fullModData.getAsJsonObject(identifier);
        Class<? extends Entity> clazz = EntityClassMappings.INSTANCE.getByIdentifier(mod, identifier);
        EntityInformation information = EntityInformation.deserialize(mod, identifier, data);
        if (information != null) {
            // not abstract, has id and attributes
            this.entityInformationMap.put(clazz, information);

            if (data.has("id")) {
                this.entityIdClassMap.put(data.get("id").getAsInt(), clazz);
            }
        }
        String parent = null;
        int metaDataIndexOffset = 0;
        if (data.has("extends")) {
            parent = data.get("extends").getAsString();

            // check if parent has been loaded
            Pair<String, Integer> metaParent = this.entityMetaIndexOffsetParentMapping.get(parent);
            if (metaParent == null) {
                loadEntityMapping(mod, parent, fullModData);
            }

            metaDataIndexOffset += this.entityMetaIndexOffsetParentMapping.get(parent).getValue();
        }
        // meta data index
        if (data.has("data")) {
            JsonElement metaDataJson = data.get("data");
            if (metaDataJson instanceof JsonArray metaDataJsonArray) {
                for (JsonElement jsonElement : metaDataJsonArray) {
                    String field = jsonElement.getAsString();
                    this.entityMetaIndexMap.put(EntityMetaDataFields.valueOf(field), metaDataIndexOffset++);
                }
            } else if (metaDataJson instanceof JsonObject metaDataJsonObject) {
                for (String key : metaDataJsonObject.keySet()) {
                    this.entityMetaIndexMap.put(EntityMetaDataFields.valueOf(key), metaDataJsonObject.get(key).getAsInt());
                    metaDataIndexOffset++;
                }
            } else {
                throw new RuntimeException("entities.json is invalid");
            }
        }
        this.entityMetaIndexOffsetParentMapping.put(identifier, new Pair<>(parent, metaDataIndexOffset));
    }

    public void unload() {
        this.motiveIdentifierMap.clear();
        this.particleIdentifierMap.clear();
        this.statisticIdentifierMap.clear();
        this.itemMap.clear();
        this.motiveIdMap.clear();
        this.mobEffectMap.clear();
        this.dimensionMap.clear();
        this.blockMap.clear();
        this.blockIdMap.clear();
        this.enchantmentMap.clear();
        this.particleIdMap.clear();
        this.statisticIdMap.clear();
        this.entityInformationMap.clear();
        this.entityMetaIndexMap.clear();
        this.entityMetaIndexOffsetParentMapping.clear();
        this.entityIdClassMap.clear();
    }

    public boolean isFullyLoaded() {
        if (this.loaded.size() == Mappings.values().length) {
            return true;
        }
        for (Mappings mapping : Mappings.values()) {
            if (!this.loaded.contains(mapping)) {
                return false;
            }
        }
        return true;
    }

    public Version getVersion() {
        return this.version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public VersionMapping getParentMapping() {
        return this.parentMapping;
    }

    public void setParentMapping(VersionMapping parentMapping) {
        this.parentMapping = parentMapping;
    }

    public HashSet<Mappings> getAvailableFeatures() {
        return this.loaded;
    }

    public boolean doesItemExist(ModIdentifier identifier) {
        if (this.parentMapping != null) {
            if (this.parentMapping.doesItemExist(identifier)) {
                return true;
            }
        }
        return this.itemMap.containsValue(identifier);
    }

    public boolean doesBlockExist(ModIdentifier identifier) {
        if (this.parentMapping != null) {
            if (this.parentMapping.doesBlockExist(identifier)) {
                return true;
            }
        }
        return this.blockIdMap.containsValue(identifier);
    }

    public boolean doesEnchantmentExist(ModIdentifier identifier) {
        if (this.parentMapping != null) {
            if (this.parentMapping.doesEnchantmentExist(identifier)) {
                return true;
            }
        }
        return this.enchantmentMap.containsValue(identifier);
    }

    public boolean doesMobEffectExist(ModIdentifier identifier) {
        if (this.parentMapping != null) {
            if (this.parentMapping.doesMobEffectExist(identifier)) {
                return true;
            }
        }
        return this.mobEffectMap.containsValue(identifier);
    }
}
