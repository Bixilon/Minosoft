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

import com.google.gson.JsonObject;
import de.bixilon.minosoft.Config;
import de.bixilon.minosoft.game.datatypes.Mappings;
import de.bixilon.minosoft.game.datatypes.objectLoader.blockIds.BlockIds;
import de.bixilon.minosoft.game.datatypes.objectLoader.blocks.Blocks;
import de.bixilon.minosoft.game.datatypes.objectLoader.dimensions.Dimensions;
import de.bixilon.minosoft.game.datatypes.objectLoader.effects.MobEffects;
import de.bixilon.minosoft.game.datatypes.objectLoader.enchantments.Enchantments;
import de.bixilon.minosoft.game.datatypes.objectLoader.entities.Entities;
import de.bixilon.minosoft.game.datatypes.objectLoader.items.Items;
import de.bixilon.minosoft.game.datatypes.objectLoader.motives.Motives;
import de.bixilon.minosoft.game.datatypes.objectLoader.particle.Particles;
import de.bixilon.minosoft.game.datatypes.objectLoader.statistics.Statistics;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.util.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ObjectLoader {
    public static void loadMappings() {
        HashMap<String, Mappings> mappingsHashMap = new HashMap<>();
        mappingsHashMap.put("registries", Mappings.REGISTRIES);
        mappingsHashMap.put("blocks", Mappings.BLOCKS);
        try {
            for (ProtocolVersion version : ProtocolVersion.versionMappingArray) {
                if (version.getVersionNumber() < ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
                    // skip them, use mapping of 1.12
                    continue;
                }
                long startTime = System.currentTimeMillis();
                for (Map.Entry<String, Mappings> mappingSet : mappingsHashMap.entrySet()) {
                    JsonObject data = Util.readJsonFromFile(Config.homeDir + String.format("assets/mapping/%s/%s.json", version.getVersionString(), mappingSet.getKey()));
                    for (String mod : data.keySet()) {
                        JsonObject modJSON = data.getAsJsonObject(mod);
                        switch (mappingSet.getValue()) {
                            case REGISTRIES:
                                Items.load(mod, modJSON.getAsJsonObject("item").getAsJsonObject("entries"), version);
                                Entities.load(mod, modJSON.getAsJsonObject("entity_type").getAsJsonObject("entries"), version);
                                Enchantments.load(mod, modJSON.getAsJsonObject("enchantment").getAsJsonObject("entries"), version);
                                Statistics.load(mod, modJSON.getAsJsonObject("custom_stat").getAsJsonObject("entries"), version);
                                BlockIds.load(mod, modJSON.getAsJsonObject("block").getAsJsonObject("entries"), version);
                                Motives.load(mod, modJSON.getAsJsonObject("motive").getAsJsonObject("entries"), version);
                                Particles.load(mod, modJSON.getAsJsonObject("particle_type").getAsJsonObject("entries"), version);
                                MobEffects.load(mod, modJSON.getAsJsonObject("mob_effect").getAsJsonObject("entries"), version);
                                if (modJSON.has("dimension_type")) {
                                    Dimensions.load(mod, modJSON.getAsJsonObject("dimension_type").getAsJsonObject("entries"), version);
                                }
                                break;
                            case BLOCKS:
                                Blocks.load(mod, modJSON, false);
                                break;
                        }
                    }
                }
                Log.verbose(String.format("Loaded mappings for version %s in %dms (%s)", version, (System.currentTimeMillis() - startTime), version.getReleaseName()));
            }
        } catch (IOException e) {
            Log.fatal("Error occurred while loading version mapping: " + e.getLocalizedMessage());
            System.exit(1);
        }
    }
}
