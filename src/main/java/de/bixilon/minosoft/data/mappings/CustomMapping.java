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

package de.bixilon.minosoft.data.mappings;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.data.mappings.blocks.Block;
import de.bixilon.minosoft.data.mappings.particle.Particle;
import de.bixilon.minosoft.data.mappings.statistics.Statistic;
import de.bixilon.minosoft.data.mappings.versions.Version;

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
        int versionId = itemId << 16;
        if (metaData > 0 && metaData < Short.MAX_VALUE) {
            versionId |= metaData;
        }
        Item item = getItemById(versionId);
        if (item == null) {
            // ignore meta data ?
            return getItemById(itemId << 16);
        }
        return item;
    }

    public Item getItemById(int versionId) {
        if (itemMap.containsKey(versionId)) {
            return itemMap.get(versionId);
        }
        return version.getMapping().getItemById(versionId);
    }

    public String getEntityIdentifierById(int versionId) {
        if (itemMap.containsKey(versionId)) {
            return entityMap.get(versionId);
        }
        return version.getMapping().getEntityIdentifierById(versionId);
    }

    public Motive getMotiveById(int versionId) {
        if (motiveIdMap.containsKey(versionId)) {
            return motiveIdMap.get(versionId);
        }
        return version.getMapping().getMotiveById(versionId);
    }

    public MobEffect getMobEffectById(int versionId) {
        if (mobEffectMap.containsKey(versionId)) {
            return mobEffectMap.get(versionId);
        }
        return version.getMapping().getMobEffectById(versionId);
    }

    public Dimension getDimensionById(int versionId) {
        if (dimensionMap.containsKey(versionId)) {
            return dimensionMap.get(versionId);
        }
        return version.getMapping().getDimensionById(versionId);
    }

    public Dimension getDimensionByIdentifier(String identifier) {
        String[] split = identifier.split(":", 2);
        if (dimensionIdentifierMap.containsKey(split[0]) && statisticIdentifierMap.get(split[0]).containsKey(split[1])) {
            return dimensionIdentifierMap.get(split[0]).get(split[1]);
        }
        return null;
    }

    public Block getBlockByIdAndMetaData(int versionId, int metaData) {
        return getBlockById((versionId << 4) | metaData);
    }

    public Block getBlockById(int versionId) {
        if (blockMap.containsKey(versionId)) {
            return blockMap.get(versionId);
        }
        return version.getMapping().getBlockById(versionId);
    }

    public BlockId getBlockIdById(int versionId) {
        if (blockIdMap.containsKey(versionId)) {
            return blockIdMap.get(versionId);
        }
        return version.getMapping().getBlockIdById(versionId);
    }

    public Enchantment getEnchantmentById(int versionId) {
        if (enchantmentMap.containsKey(versionId)) {
            return enchantmentMap.get(versionId);
        }
        return version.getMapping().getEnchantmentById(versionId);
    }

    public Particle getParticleById(int versionId) {
        if (particleIdMap.containsKey(versionId)) {
            return particleIdMap.get(versionId);
        }
        return version.getMapping().getParticleById(versionId);
    }

    public Statistic getStatisticById(int versionId) {
        if (statisticIdMap.containsKey(versionId)) {
            return statisticIdMap.get(versionId);
        }
        return version.getMapping().getStatisticById(versionId);
    }

    public int getIdByEnchantment(Enchantment enchantment) {
        if (enchantmentMap.containsValue(enchantment)) {
            return enchantmentMap.inverse().get(enchantment);
        }
        return version.getMapping().getIdByEnchantment(enchantment);
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
