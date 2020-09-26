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

package de.bixilon.minosoft.modding.loading;

import de.bixilon.minosoft.Config;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.modding.MinosoftMod;
import de.bixilon.minosoft.util.Util;
import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.JclObjectFactory;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.zip.ZipFile;

public class ModLoader {
    static TreeMap<ModInfo, MinosoftMod> modMap;

    ModPhases currentPhase;

    public static void loadMods() {
        modMap = new TreeMap<>((a, b) -> {
            LoadingPriorities priorityA = getLoadingPriorityOrDefault(a);
            LoadingPriorities priorityB = getLoadingPriorityOrDefault(b);
            return -(priorityA.ordinal() - priorityB.ordinal());
        });
        // load all jars, parse the mod.json
        // sort the list and prioritize
        // load all lists and dependencies async
        File[] files = new File(Config.homeDir + "mods").listFiles();
        if (files == null) {
            // no mods to load
            return;
        }
        for (File modFile : files) {
            if (modFile.isDirectory()) {
                continue;
            }
            try {
                Log.verbose(String.format("[MOD] Loading file %s", modFile.getAbsolutePath()));
                ZipFile zipFile = new ZipFile(modFile);
                ModInfo modInfo = new ModInfo(Util.readJsonFromZip("mod.json", zipFile));
                if (modMap.containsKey(modInfo)) {
                    Log.warn(String.format("Mod %s:%d (uuid=%s) is loaded multiple times! Skipping", modInfo.getName(), modInfo.getVersionId(), modInfo.getUUID()));
                    continue;
                }
                JarClassLoader jcl = new JarClassLoader();
                jcl.add(modFile.getAbsolutePath());
                JclObjectFactory factory = JclObjectFactory.getInstance();

                MinosoftMod instance = (MinosoftMod) factory.create(jcl, modInfo.getMainClass());
                Log.verbose(String.format("[MOD] Mod file loaded (%s)", modInfo));
                modMap.put(modInfo, instance);
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.warn(String.format("Could not load mod: %s", modFile.getAbsolutePath()));
            }
        }

        for (ModPhases phase : ModPhases.values()) {
            Log.verbose(String.format("Map loading phase changed: %s", phase));
            modMap.forEach((modInfo, instance) -> {
                instance.start(phase);
            });
        }
    }

    private static LoadingPriorities getLoadingPriorityOrDefault(ModInfo info) {
        if (info.getLoadingInfo() != null && info.getLoadingInfo().getLoadingPriority() != null) {
            return info.getLoadingInfo().getLoadingPriority();
        }
        return LoadingPriorities.NORMAL;
    }
}
