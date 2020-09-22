/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.game.datatypes.objectLoader.versions;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.game.datatypes.Mappings;
import de.bixilon.minosoft.game.datatypes.objectLoader.blockIds.BlockId;
import de.bixilon.minosoft.game.datatypes.objectLoader.blocks.Block;
import de.bixilon.minosoft.game.datatypes.objectLoader.blocks.Blocks;
import de.bixilon.minosoft.game.datatypes.objectLoader.dimensions.Dimension;
import de.bixilon.minosoft.game.datatypes.objectLoader.effects.MobEffect;
import de.bixilon.minosoft.game.datatypes.objectLoader.enchantments.Enchantment;
import de.bixilon.minosoft.game.datatypes.objectLoader.items.Item;
import de.bixilon.minosoft.game.datatypes.objectLoader.motives.Motive;
import de.bixilon.minosoft.game.datatypes.objectLoader.particle.Particle;
import de.bixilon.minosoft.game.datatypes.objectLoader.statistics.Statistic;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;

import java.util.HashSet;

public class VersionMapping {
    final Version version;
    final HashSet<Mappings> loaded = new HashSet<>();
    HashBiMap<String, Motive> motiveIdentifierMap;
    HashBiMap<String, Particle> particleIdentifierMap;
    HashBiMap<String, Statistic> statisticIdentifierMap;
    HashBiMap<Integer, Item> itemMap;
    HashBiMap<Integer, String> entityMap;
    HashBiMap<Integer, Motive> motiveIdMap;
    HashBiMap<Integer, MobEffect> mobEffectMap;
    HashBiMap<Integer, Dimension> dimensionMap;
    HashBiMap<Integer, Block> blockMap;
    HashBiMap<Integer, BlockId> blockIdMap;
    HashBiMap<Integer, Enchantment> enchantmentMap;
    HashBiMap<Integer, Particle> particleIdMap;
    HashBiMap<Integer, Statistic> statisticIdMap;

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

    public Item getItemById(int protocolId) {
        return itemMap.get(protocolId);
    }

    public int getItemId(Item item) {
        return itemMap.inverse().get(item);
    }

    public String getEntityIdentifierById(int protocolId) {
        return "minecraft:" + entityMap.get(protocolId);
    }

    public Motive getMotiveById(int protocolId) {
        return motiveIdMap.get(protocolId);
    }

    public MobEffect getMobEffectById(int protocolId) {
        return mobEffectMap.get(protocolId);
    }

    public Dimension getDimensionById(int protocolId) {
        return dimensionMap.get(protocolId);
    }

    public Block getBlockById(int protocolId) {
        return blockMap.get(protocolId);
    }

    public Block getBlockByIdAndMetaData(int protocolId, int metaData) {
        return getBlockById((protocolId << 4) | metaData);
    }

    public BlockId getBlockIdById(int protocolId) {
        return blockIdMap.get(protocolId);
    }

    public Enchantment getEnchantmentById(int protocolId) {
        return enchantmentMap.get(protocolId);
    }

    public Particle getParticleById(int protocolId) {
        return particleIdMap.get(protocolId);
    }

    public Statistic getStatisticById(int protocolId) {
        return statisticIdMap.get(protocolId);
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
                    if (version.getProtocolVersion() < ProtocolDefinition.FLATTING_VERSION_ID) {
                        itemId <<= 16;
                        if (identifierJSON.has("meta")) {
                            // old format (with metadata)
                            itemId |= identifierJSON.get("meta").getAsInt();
                        }
                    }
                    itemMap.put(itemId, item);
                }
                entityMap = HashBiMap.create();
                JsonObject entityJson = data.getAsJsonObject("entity_type").getAsJsonObject("entries");
                for (String identifier : entityJson.keySet()) {
                    entityMap.put(entityJson.getAsJsonObject(identifier).get("id").getAsInt(), identifier);
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
            case BLOCKS -> blockMap = Blocks.load("minecraft", data, version.getProtocolVersion() < ProtocolDefinition.FLATTING_VERSION_ID);
        }
        loaded.add(type);
    }

    public void unload() {
        motiveIdentifierMap.clear();
        particleIdentifierMap.clear();
        statisticIdentifierMap.clear();
        itemMap.clear();
        entityMap.clear();
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
}
