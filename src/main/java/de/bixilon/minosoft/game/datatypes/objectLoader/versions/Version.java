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
import de.bixilon.minosoft.protocol.protocol.Packets;

import static de.bixilon.minosoft.protocol.protocol.ProtocolDefinition.FLATTING_VERSION_ID;

public class Version {
    final String versionName;
    final int protocolVersion;

    final HashBiMap<Packets.Serverbound, Integer> serverboundPacketMapping;
    final HashBiMap<Packets.Clientbound, Integer> clientboundPacketMapping;

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

    public Version(String versionName, int protocolVersion, HashBiMap<Packets.Serverbound, Integer> serverboundPacketMapping, HashBiMap<Packets.Clientbound, Integer> clientboundPacketMapping) {
        this.versionName = versionName;
        this.protocolVersion = protocolVersion;
        this.serverboundPacketMapping = serverboundPacketMapping;
        this.clientboundPacketMapping = clientboundPacketMapping;
    }

    public String getVersionName() {
        return versionName;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public Packets.Clientbound getPacketByCommand(int command) { // state must be play!
        return clientboundPacketMapping.inverse().get(command);
    }

    public int getCommandByPacket(Packets.Serverbound packet) {
        return serverboundPacketMapping.get(packet);
    }

    public int getCommandByPacket(Packets.Clientbound packet) {
        return clientboundPacketMapping.get(packet);
    }


    public HashBiMap<Packets.Clientbound, Integer> getClientboundPacketMapping() {
        return clientboundPacketMapping;
    }

    public HashBiMap<Packets.Serverbound, Integer> getServerboundPacketMapping() {
        return serverboundPacketMapping;
    }

    @Override
    public int hashCode() {
        return getProtocolVersion();
    }

    public Item getItemById(int protocolId) {
        if (getProtocolVersion() < FLATTING_VERSION_ID) {
            // old format
            return Versions.legacyVersion.getItemById(protocolId);
        }
        return itemMap.get(protocolId);
    }

    public int getItemId(Item item) {
        if (getProtocolVersion() < FLATTING_VERSION_ID) {
            // old format
            return Versions.legacyVersion.getItemId(item);
        }
        return itemMap.inverse().get(item);
    }

    public String getEntityIdentifierById(int protocolId) {
        if (getProtocolVersion() < FLATTING_VERSION_ID) {
            // old format
            return Versions.legacyVersion.getEntityIdentifierById(protocolId);
        }
        return entityMap.get(protocolId);
    }

    public Motive getMotiveById(int protocolId) {
        if (getProtocolVersion() < FLATTING_VERSION_ID) {
            // old format
            return Versions.legacyVersion.getMotiveById(protocolId);
        }
        return motiveIdMap.get(protocolId);
    }

    public Motive getMotiveByIdentifier(String identifier) {
        if (identifier.contains(":")) {
            String[] splitted = identifier.split(":", 2);
            if (!splitted[0].equals("minecraft")) {
                return null;
            }
            identifier = splitted[1];
        }
        // 1.7.x
        return Versions.legacyVersion.getMotiveByIdentifier(identifier);
    }

    public MobEffect getMobEffectById(int protocolId) {
        if (getProtocolVersion() < FLATTING_VERSION_ID) {
            // old format
            return Versions.legacyVersion.getMobEffectById(protocolId);
        }
        return mobEffectMap.get(protocolId);
    }

    public Dimension getDimensionById(int protocolId) {
        if (getProtocolVersion() < FLATTING_VERSION_ID) {
            // old format
            return Versions.legacyVersion.getDimensionById(protocolId);
        }
        return dimensionMap.get(protocolId);
    }

    public Block getBlockById(int protocolId) {
        if (getProtocolVersion() < FLATTING_VERSION_ID) {
            // old format
            return Versions.legacyVersion.getBlockById(protocolId);
        }
        return blockMap.get(protocolId);
    }

    public Block getBlockByIdAndMetaData(int protocolId, int metaData) {
        return getBlockById((protocolId << 4) | metaData);
    }

    public BlockId getBlockIdById(int protocolId) {
        if (getProtocolVersion() < FLATTING_VERSION_ID) {
            // old format
            return Versions.legacyVersion.getBlockIdById(protocolId);
        }
        return blockIdMap.get(protocolId);
    }

    public Enchantment getEnchantmentById(int protocolId) {
        if (getProtocolVersion() < FLATTING_VERSION_ID) {
            // old format
            return Versions.legacyVersion.getEnchantmentById(protocolId);
        }
        return enchantmentMap.get(protocolId);
    }

    public Particle getParticleById(int protocolId) {
        if (getProtocolVersion() < FLATTING_VERSION_ID) {
            // old format
            return Versions.legacyVersion.getParticleById(protocolId);
        }
        return particleIdMap.get(protocolId);
    }

    public Particle getParticleByIdentifier(String identifier) {
        if (identifier.contains(":")) {
            String[] splitted = identifier.split(":", 2);
            if (!splitted[0].equals("minecraft")) {
                return null;
            }
            identifier = splitted[1];
        }
        return Versions.legacyVersion.getParticleByIdentifier(identifier);
    }

    public Statistic getStatisticById(int protocolId) {
        if (getProtocolVersion() < FLATTING_VERSION_ID) {
            // old format
            return Versions.legacyVersion.getStatisticById(protocolId);
        }
        return statisticIdMap.get(protocolId);
    }

    public Statistic getStatisticByIdentifier(String identifier) {
        if (identifier.contains(":")) {
            String[] splitted = identifier.split(":", 2);
            if (!splitted[0].equals("minecraft")) {
                return null;
            }
            identifier = splitted[1];
        }
        return Versions.legacyVersion.getStatisticByIdentifier(identifier);
    }

    public void load(Mappings type, JsonObject data) {
        switch (type) {
            case REGISTRIES:
                JsonObject itemJson = data.getAsJsonObject("item").getAsJsonObject("entries");
                for (String identifier : itemJson.keySet()) {
                    Item item = new Item("minecraft", identifier);
                    JsonObject identifierJSON = itemJson.getAsJsonObject(identifier);
                    int itemId = identifierJSON.get("id").getAsInt();
                    if (getProtocolVersion() < FLATTING_VERSION_ID) {
                        // old format (with metadata)
                        itemId <<= 4;
                        if (identifierJSON.has("meta")) {
                            itemId |= identifierJSON.get("meta").getAsInt();
                        }
                    }
                    itemMap.put(itemId, item);
                }

                JsonObject entityJson = data.getAsJsonObject("entity_type").getAsJsonObject("entries");
                for (String identifier : entityJson.keySet()) {
                    entityMap.put(entityJson.getAsJsonObject(identifier).get("id").getAsInt(), identifier);
                }


                JsonObject enchantmentJson = data.getAsJsonObject("enchantment").getAsJsonObject("entries");
                for (String identifier : enchantmentJson.keySet()) {
                    Enchantment enchantment = new Enchantment("minecraft", identifier);
                    enchantmentMap.put(enchantmentJson.getAsJsonObject(identifier).get("id").getAsInt(), enchantment);
                }

                JsonObject statisticJson = data.getAsJsonObject("custom_stat").getAsJsonObject("entries");
                for (String identifier : statisticJson.keySet()) {
                    Statistic statistic = new Statistic("minecraft", identifier);
                    statisticIdMap.put(statisticJson.getAsJsonObject(identifier).get("id").getAsInt(), statistic);
                }

                JsonObject blockIdJson = data.getAsJsonObject("block").getAsJsonObject("entries");
                for (String identifier : blockIdJson.keySet()) {
                    BlockId blockId = new BlockId("minecraft", identifier);
                    blockIdMap.put(blockIdJson.getAsJsonObject(identifier).get("id").getAsInt(), blockId);
                }

                JsonObject motiveJson = data.getAsJsonObject("motive").getAsJsonObject("entries");
                for (String identifier : motiveJson.keySet()) {
                    Motive motive = new Motive("minecraft", identifier);
                    motiveIdMap.put(motiveJson.getAsJsonObject(identifier).get("id").getAsInt(), motive);
                }

                JsonObject particleJson = data.getAsJsonObject("particle_type").getAsJsonObject("entries");
                for (String identifier : particleJson.keySet()) {
                    Particle particle = new Particle("minecraft", identifier);
                    particleIdMap.put(particleJson.getAsJsonObject(identifier).get("id").getAsInt(), particle);
                }

                JsonObject mobEffectJson = data.getAsJsonObject("mob_effect").getAsJsonObject("entries");
                for (String identifier : mobEffectJson.keySet()) {
                    MobEffect mobEffect = new MobEffect("minecraft", identifier);
                    mobEffectMap.put(mobEffectJson.getAsJsonObject(identifier).get("id").getAsInt(), mobEffect);
                }
                if (data.has("dimension_type")) {
                    JsonObject dimensionJson = data.getAsJsonObject("dimension_type").getAsJsonObject("entries");
                    for (String identifier : dimensionJson.keySet()) {
                        Dimension dimension = new Dimension("minecraft", identifier, dimensionJson.getAsJsonObject(identifier).get("has_skylight").getAsBoolean());
                        dimensionMap.put(dimensionJson.getAsJsonObject(identifier).get("id").getAsInt(), dimension);
                    }
                }
                break;
            case BLOCKS:
                blockMap = Blocks.load("minecraft", data, getProtocolVersion() < FLATTING_VERSION_ID);
                break;
        }
    }

    public void unload() {
        serverboundPacketMapping.clear();
        clientboundPacketMapping.clear();

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

}
