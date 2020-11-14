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
import com.google.gson.JsonObject;
import de.bixilon.minosoft.data.EntityClassMappings;
import de.bixilon.minosoft.data.Mappings;
import de.bixilon.minosoft.data.entities.EntityInformation;
import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.mappings.*;
import de.bixilon.minosoft.data.mappings.blocks.Block;
import de.bixilon.minosoft.data.mappings.blocks.Blocks;
import de.bixilon.minosoft.data.mappings.particle.Particle;
import de.bixilon.minosoft.data.mappings.statistics.Statistic;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;

import java.util.HashMap;
import java.util.HashSet;

public class VersionMapping {
    final Version version;
    final HashSet<Mappings> loaded = new HashSet<>();
    HashBiMap<String, Motive> motiveIdentifierMap;
    HashBiMap<String, Particle> particleIdentifierMap;
    HashBiMap<String, Statistic> statisticIdentifierMap;
    HashBiMap<Integer, Item> itemMap;
    HashBiMap<Integer, Motive> motiveIdMap;
    HashBiMap<Integer, MobEffect> mobEffectMap;
    HashBiMap<Integer, Dimension> dimensionMap;
    HashBiMap<Integer, Block> blockMap;
    HashBiMap<Integer, BlockId> blockIdMap;
    HashBiMap<Integer, Enchantment> enchantmentMap;
    HashBiMap<Integer, Particle> particleIdMap;
    HashBiMap<Integer, Statistic> statisticIdMap;
    EntityMappings entityMappings;

    public VersionMapping(Version version) {
        this.version = version;
    }

    public Motive getMotiveByIdentifier(String identifier) {
        return motiveIdentifierMap.get(identifier);
    }

    public Statistic getStatisticByIdentifier(String identifier) {
        return statisticIdentifierMap.get(identifier);
    }

    public Particle getParticleByIdentifier(String identifier) {
        return particleIdentifierMap.get(identifier);
    }

    public Item getItemById(int versionId) {
        return itemMap.get(versionId);
    }

    public int getItemId(Item item) {
        return itemMap.inverse().get(item);
    }

    public Motive getMotiveById(int versionId) {
        return motiveIdMap.get(versionId);
    }

    public MobEffect getMobEffectById(int versionId) {
        return mobEffectMap.get(versionId);
    }

    public Dimension getDimensionById(int versionId) {
        return dimensionMap.get(versionId);
    }

    public Block getBlockByIdAndMetaData(int versionId, int metaData) {
        return getBlockById((versionId << 4) | metaData);
    }

    public Block getBlockById(int versionId) {
        return blockMap.get(versionId);
    }

    public BlockId getBlockIdById(int versionId) {
        return blockIdMap.get(versionId);
    }

    public Enchantment getEnchantmentById(int versionId) {
        return enchantmentMap.get(versionId);
    }

    public Particle getParticleById(int versionId) {
        return particleIdMap.get(versionId);
    }

    public Statistic getStatisticById(int versionId) {
        return statisticIdMap.get(versionId);
    }

    public int getIdByEnchantment(Enchantment enchantment) {
        return enchantmentMap.inverse().get(enchantment);
    }

    public void load(Mappings type, JsonObject data) {
        switch (type) {
            case REGISTRIES -> {
                JsonObject itemJson = data.getAsJsonObject("item").getAsJsonObject("entries");
                itemMap = HashBiMap.create();
                for (String identifier : itemJson.keySet()) {
                    Item item = new Item("minecraft", identifier);
                    JsonObject identifierJSON = itemJson.getAsJsonObject(identifier);
                    int itemId = identifierJSON.get("id").getAsInt();
                    if (version.getVersionId() < ProtocolDefinition.FLATTING_VERSION_ID) {
                        itemId <<= 16;
                        if (identifierJSON.has("meta")) {
                            // old format (with metadata)
                            itemId |= identifierJSON.get("meta").getAsInt();
                        }
                    }
                    itemMap.put(itemId, item);
                }
                enchantmentMap = HashBiMap.create();
                JsonObject enchantmentJson = data.getAsJsonObject("enchantment").getAsJsonObject("entries");
                for (String identifier : enchantmentJson.keySet()) {
                    Enchantment enchantment = new Enchantment("minecraft", identifier);
                    enchantmentMap.put(enchantmentJson.getAsJsonObject(identifier).get("id").getAsInt(), enchantment);
                }
                statisticIdMap = HashBiMap.create();
                statisticIdentifierMap = HashBiMap.create();
                JsonObject statisticJson = data.getAsJsonObject("custom_stat").getAsJsonObject("entries");
                for (String identifier : statisticJson.keySet()) {
                    Statistic statistic = new Statistic("minecraft", identifier);
                    if (statisticJson.getAsJsonObject(identifier).has("id")) {
                        statisticIdMap.put(statisticJson.getAsJsonObject(identifier).get("id").getAsInt(), statistic);
                    }
                    statisticIdentifierMap.put(identifier, statistic);
                }
                blockIdMap = HashBiMap.create();
                JsonObject blockIdJson = data.getAsJsonObject("block").getAsJsonObject("entries");
                for (String identifier : blockIdJson.keySet()) {
                    BlockId blockId = new BlockId("minecraft", identifier);
                    blockIdMap.put(blockIdJson.getAsJsonObject(identifier).get("id").getAsInt(), blockId);
                }
                motiveIdMap = HashBiMap.create();
                motiveIdentifierMap = HashBiMap.create();
                JsonObject motiveJson = data.getAsJsonObject("motive").getAsJsonObject("entries");
                for (String identifier : motiveJson.keySet()) {
                    Motive motive = new Motive("minecraft", identifier);
                    if (motiveJson.getAsJsonObject(identifier).has("id")) {
                        motiveIdMap.put(motiveJson.getAsJsonObject(identifier).get("id").getAsInt(), motive);
                    }
                    motiveIdentifierMap.put(identifier, motive);
                }
                particleIdMap = HashBiMap.create();
                particleIdentifierMap = HashBiMap.create();
                JsonObject particleJson = data.getAsJsonObject("particle_type").getAsJsonObject("entries");
                for (String identifier : particleJson.keySet()) {
                    Particle particle = new Particle("minecraft", identifier);
                    if (particleJson.getAsJsonObject(identifier).has("id")) {
                        particleIdMap.put(particleJson.getAsJsonObject(identifier).get("id").getAsInt(), particle);
                    }
                    particleIdentifierMap.put(identifier, particle);
                }
                mobEffectMap = HashBiMap.create();
                JsonObject mobEffectJson = data.getAsJsonObject("mob_effect").getAsJsonObject("entries");
                for (String identifier : mobEffectJson.keySet()) {
                    MobEffect mobEffect = new MobEffect("minecraft", identifier);
                    mobEffectMap.put(mobEffectJson.getAsJsonObject(identifier).get("id").getAsInt(), mobEffect);
                }
                if (data.has("dimension_type")) {
                    dimensionMap = HashBiMap.create();
                    JsonObject dimensionJson = data.getAsJsonObject("dimension_type").getAsJsonObject("entries");
                    for (String identifier : dimensionJson.keySet()) {
                        Dimension dimension = new Dimension("minecraft", identifier, dimensionJson.getAsJsonObject(identifier).get("has_skylight").getAsBoolean());
                        dimensionMap.put(dimensionJson.getAsJsonObject(identifier).get("id").getAsInt(), dimension);
                    }
                }
            }
            case BLOCKS -> blockMap = Blocks.load("minecraft", data, version.getVersionId() < ProtocolDefinition.FLATTING_VERSION_ID);
            case ENTITIES -> {
                HashBiMap<Class<? extends Entity>, EntityInformation> entityInformationMap = HashBiMap.create();
                HashMap<EntityMetaDataFields, Integer> indexMapping = new HashMap<>();
                HashBiMap<Integer, Class<? extends Entity>> entityIdMap = HashBiMap.create();

                for (String mod : data.keySet()) {
                    JsonObject modJson = data.getAsJsonObject(mod);
                    for (String identifier : modJson.keySet()) {
                        JsonObject identifierJson = modJson.getAsJsonObject(identifier);
                        if (!identifier.startsWith("~abstract")) {
                            // not abstract, has attributes
                            Class<? extends Entity> clazz = EntityClassMappings.getByIdentifier(mod, identifier);
                            entityInformationMap.put(clazz, new EntityInformation(mod, identifier, identifierJson.get("maxHealth").getAsInt(), identifierJson.get("length").getAsInt(), identifierJson.get("width").getAsInt(), identifierJson.get("height").getAsInt()));

                            entityIdMap.put(identifierJson.get("id").getAsInt(), clazz);
                        }
// meta data index
                        if (identifierJson.has("data")) {
                            JsonObject metaDataJson = identifierJson.getAsJsonObject("data");
                            for (String field : metaDataJson.keySet()) {
                                indexMapping.put(EntityMetaDataFields.valueOf(field), metaDataJson.get(field).getAsInt());
                            }
                        }
                    }
                }

                entityMappings = new EntityMappings(entityInformationMap, indexMapping, entityIdMap);
            }
        }
        loaded.add(type);
    }

    public void unload() {
        motiveIdentifierMap.clear();
        particleIdentifierMap.clear();
        statisticIdentifierMap.clear();
        itemMap.clear();
        motiveIdMap.clear();
        mobEffectMap.clear();
        dimensionMap.clear();
        blockMap.clear();
        blockIdMap.clear();
        enchantmentMap.clear();
        particleIdMap.clear();
        statisticIdMap.clear();
    }

    public boolean isFullyLoaded() {
        for (Mappings mapping : Mappings.values()) {
            if (!loaded.contains(mapping)) {
                return false;
            }
        }
        return true;
    }

    public EntityInformation getEntityInformation(Class<? extends Entity> clazz) {
        return entityMappings.getEntityInformation(clazz);
    }

    public int getEntityMetaDatIndex(EntityMetaDataFields field) {
        return entityMappings.getEntityMetaDatIndex(field);
    }

    public Class<? extends Entity> getEntityClassById(int id) {
        return entityMappings.getEntityClassById(id);
    }
}
