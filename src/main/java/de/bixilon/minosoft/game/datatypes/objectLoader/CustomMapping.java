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

package de.bixilon.minosoft.game.datatypes.objectLoader;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.game.datatypes.objectLoader.blockIds.BlockId;
import de.bixilon.minosoft.game.datatypes.objectLoader.blocks.Block;
import de.bixilon.minosoft.game.datatypes.objectLoader.dimensions.Dimension;
import de.bixilon.minosoft.game.datatypes.objectLoader.effects.MobEffect;
import de.bixilon.minosoft.game.datatypes.objectLoader.enchantments.Enchantment;
import de.bixilon.minosoft.game.datatypes.objectLoader.items.Item;
import de.bixilon.minosoft.game.datatypes.objectLoader.motives.Motive;
import de.bixilon.minosoft.game.datatypes.objectLoader.particle.Particle;
import de.bixilon.minosoft.game.datatypes.objectLoader.statistics.Statistic;
import de.bixilon.minosoft.game.datatypes.objectLoader.versions.Version;

import java.util.HashMap;

public class CustomMapping {
    // used for custom mappings (mod support)
    final HashMap<String, HashBiMap<String, Motive>> motiveIdentifierMap = new HashMap<>();
    final HashMap<String, HashBiMap<String, Particle>> particleIdentifierMap = new HashMap<>();
    final HashMap<String, HashBiMap<String, Statistic>> statisticIdentifierMap = new HashMap<>();
    final HashBiMap<Integer, Item> itemMap = HashBiMap.create();
    final HashBiMap<Integer, String> entityMap = HashBiMap.create();
    final HashBiMap<Integer, Motive> motiveIdMap = HashBiMap.create();
    final HashBiMap<Integer, MobEffect> mobEffectMap = HashBiMap.create();
    final HashBiMap<Integer, Dimension> dimensionMap = HashBiMap.create();
    final HashBiMap<Integer, Block> blockMap = HashBiMap.create();
    final HashBiMap<Integer, BlockId> blockIdMap = HashBiMap.create();
    final HashBiMap<Integer, Enchantment> enchantmentMap = HashBiMap.create();
    final HashBiMap<Integer, Particle> particleIdMap = HashBiMap.create();
    final HashBiMap<Integer, Statistic> statisticIdMap = HashBiMap.create();
    Version version;
    HashMap<String, HashBiMap<String, Dimension>> dimensionIdentifierMap = new HashMap<>();

    public CustomMapping(Version version) {
        this.version = version;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Motive getMotiveByIdentifier(String identifier) {
        String[] split = identifier.split(":", 2);
        if (motiveIdentifierMap.containsKey(split[0]) && motiveIdentifierMap.get(split[0]).containsKey(split[1])) {
            return motiveIdentifierMap.get(split[0]).get(split[1]);
        }
        if (split[0].equals("minecraft")) {
            return version.getMapping().getMotiveByIdentifier(split[1]);
        }
        return null;
    }

    public Statistic getStatisticByIdentifier(String identifier) {
        String[] split = identifier.split(":", 2);
        if (statisticIdentifierMap.containsKey(split[0]) && statisticIdentifierMap.get(split[0]).containsKey(split[1])) {
            return statisticIdentifierMap.get(split[0]).get(split[1]);
        }
        if (split[0].equals("minecraft")) {
            return version.getMapping().getStatisticByIdentifier(split[1]);
        }
        return null;
    }

    public Particle getParticleByIdentifier(String identifier) {
        String[] split = identifier.split(":", 2);
        if (particleIdentifierMap.containsKey(split[0]) && statisticIdentifierMap.get(split[0]).containsKey(split[1])) {
            return particleIdentifierMap.get(split[0]).get(split[1]);
        }
        if (split[0].equals("minecraft")) {
            return version.getMapping().getParticleByIdentifier(split[1]);
        }
        return null;
    }

    public int getItemId(Item item) {
        if (itemMap.inverse().containsKey(item)) {
            return itemMap.inverse().get(item);
        }
        return version.getMapping().getItemId(item);
    }

    public Item getItemByLegacy(int itemId, int metaData) {
        int protocolId = itemId << 16;
        if (metaData > 0 && metaData < Short.MAX_VALUE) {
            protocolId |= metaData;
        }
        Item item = getItemById(protocolId);
        if (item == null) {
            // ignore meta data ?
            return getItemById(itemId << 16);
        }
        return item;
    }

    public Item getItemById(int protocolId) {
        if (itemMap.containsKey(protocolId)) {
            return itemMap.get(protocolId);
        }
        return version.getMapping().getItemById(protocolId);
    }

    public String getEntityIdentifierById(int protocolId) {
        if (itemMap.containsKey(protocolId)) {
            return entityMap.get(protocolId);
        }
        return version.getMapping().getEntityIdentifierById(protocolId);
    }

    public Motive getMotiveById(int protocolId) {
        if (motiveIdMap.containsKey(protocolId)) {
            return motiveIdMap.get(protocolId);
        }
        return version.getMapping().getMotiveById(protocolId);
    }

    public MobEffect getMobEffectById(int protocolId) {
        if (mobEffectMap.containsKey(protocolId)) {
            return mobEffectMap.get(protocolId);
        }
        return version.getMapping().getMobEffectById(protocolId);
    }

    public Dimension getDimensionById(int protocolId) {
        if (dimensionMap.containsKey(protocolId)) {
            return dimensionMap.get(protocolId);
        }
        return version.getMapping().getDimensionById(protocolId);
    }

    public Dimension getDimensionByIdentifier(String identifier) {
        String[] split = identifier.split(":", 2);
        if (dimensionIdentifierMap.containsKey(split[0]) && statisticIdentifierMap.get(split[0]).containsKey(split[1])) {
            return dimensionIdentifierMap.get(split[0]).get(split[1]);
        }
        return null;
    }

    public Block getBlockByIdAndMetaData(int protocolId, int metaData) {
        return getBlockById((protocolId << 4) | metaData);
    }

    public Block getBlockById(int protocolId) {
        if (blockMap.containsKey(protocolId)) {
            return blockMap.get(protocolId);
        }
        return version.getMapping().getBlockById(protocolId);
    }

    public BlockId getBlockIdById(int protocolId) {
        if (blockIdMap.containsKey(protocolId)) {
            return blockIdMap.get(protocolId);
        }
        return version.getMapping().getBlockIdById(protocolId);
    }

    public Enchantment getEnchantmentById(int protocolId) {
        if (enchantmentMap.containsKey(protocolId)) {
            return enchantmentMap.get(protocolId);
        }
        return version.getMapping().getEnchantmentById(protocolId);
    }

    public Particle getParticleById(int protocolId) {
        if (particleIdMap.containsKey(protocolId)) {
            return particleIdMap.get(protocolId);
        }
        return version.getMapping().getParticleById(protocolId);
    }

    public Statistic getStatisticById(int protocolId) {
        if (statisticIdMap.containsKey(protocolId)) {
            return statisticIdMap.get(protocolId);
        }
        return version.getMapping().getStatisticById(protocolId);
    }

    public void unload() {
        motiveIdentifierMap.clear();
        particleIdentifierMap.clear();
        statisticIdentifierMap.clear();
        dimensionIdentifierMap.clear();
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

    public void setDimensions(HashMap<String, HashBiMap<String, Dimension>> dimensions) {
        dimensionIdentifierMap = dimensions;
    }
}
