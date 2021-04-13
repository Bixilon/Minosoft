/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.modding.loading;

import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.config.StaticConfiguration;
import de.bixilon.minosoft.modding.MinosoftMod;
import de.bixilon.minosoft.util.CountUpAndDownLatch;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.logging.Log;
import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.JclObjectFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.zip.ZipFile;

public class ModLoader {
    public static final int CURRENT_MODDING_API_VERSION = 1;
    public static final ConcurrentHashMap<UUID, MinosoftMod> MOD_MAP = new ConcurrentHashMap<>();

    public static void loadMods(CountUpAndDownLatch progress) throws Exception {
        final long startTime = System.currentTimeMillis();
        Log.info("Start loading mods...");

        // load all jars, parse the mod.json
        // sort the list and prioritize
        // load all lists and dependencies async
        File[] files = new File(StaticConfiguration.HOME_DIRECTORY + "mods").listFiles();
        if (files == null) {
            // no mods to load
            return;
        }
        CountDownLatch latch = new CountDownLatch(files.length);
        for (File modFile : files) {
            if (modFile.isDirectory()) {
                continue;
            }
            Minosoft.THREAD_POOL.execute(() -> {
                MinosoftMod mod = loadMod(progress, modFile);
                if (mod != null) {
                    MOD_MAP.put(mod.getInfo().getModVersionIdentifier().getUUID(), mod);
                }
                latch.countDown();
            });
        }
        latch.await();

        if (MOD_MAP.isEmpty()) {
            Log.info("No mods to load.");
            return;
        }

        progress.addCount(MOD_MAP.size() * ModPhases.values().length); // count * mod phases

        // check if all dependencies are available
        modLoop:
        for (Map.Entry<UUID, MinosoftMod> modEntry : MOD_MAP.entrySet()) {
            ModInfo currentModInfo = modEntry.getValue().getInfo();

            for (ModDependency dependency : currentModInfo.getHardDependencies()) {
                ModInfo info = getModInfoByUUID(dependency.getUUID());
                if (info == null) {
                    Log.warn("Could not satisfy mod dependency for mod %s (Requires %s)", modEntry.getValue().getInfo(), dependency.getUUID());
                    MOD_MAP.remove(modEntry.getKey());
                    continue modLoop;
                }
                if (dependency.getVersionMinimum() < info.getModVersionIdentifier().getVersionId()) {
                    Log.warn("Could not satisfy mod dependency for mod %s (Requires %s version > %d)", modEntry.getValue().getInfo(), dependency.getUUID(), dependency.getVersionMinimum());
                    MOD_MAP.remove(modEntry.getKey());
                    continue modLoop;
                }
                if (dependency.getVersionMaximum() > info.getModVersionIdentifier().getVersionId()) {
                    Log.warn("Could not satisfy mod dependency for mod %s (Requires %s version < %d)", modEntry.getValue().getInfo(), dependency.getUUID(), dependency.getVersionMaximum());
                    MOD_MAP.remove(modEntry.getKey());
                    continue modLoop;
                }
            }
            for (ModDependency dependency : currentModInfo.getSoftDependencies()) {
                ModInfo info = getModInfoByUUID(dependency.getUUID());
                if (info == null) {
                    Log.warn("Could not satisfy mod soft dependency for mod %s (Requires %s)", modEntry.getValue().getInfo(), dependency.getUUID());
                    continue;
                }
                if (dependency.getVersionMinimum() < info.getModVersionIdentifier().getVersionId()) {
                    Log.warn("Could not satisfy mod dependency for mod %s (Requires %s version > %d)", modEntry.getValue().getInfo(), dependency.getUUID(), dependency.getVersionMinimum());
                    continue;
                }
                if (dependency.getVersionMaximum() > info.getModVersionIdentifier().getVersionId()) {
                    Log.warn("Could not satisfy mod soft dependency for mod %s (Requires %s version < %d)", modEntry.getValue().getInfo(), dependency.getUUID(), dependency.getVersionMaximum());
                }
            }

        }

        final TreeMap<UUID, MinosoftMod> sortedModMap = new TreeMap<>((mod1UUID, mod2UUID) -> {
            // ToDo: Load dependencies first
            if (mod1UUID == null || mod2UUID == null) {
                return 0;
            }
            return -(getLoadingPriorityOrDefault(MOD_MAP.get(mod2UUID).getInfo()).ordinal() - getLoadingPriorityOrDefault(MOD_MAP.get(mod1UUID).getInfo()).ordinal());
        });

        sortedModMap.putAll(MOD_MAP);

        for (ModPhases phase : ModPhases.values()) {
            Log.verbose(String.format("Mod loading phase changed: %s", phase));
            CountDownLatch modLatch = new CountDownLatch(sortedModMap.size());
            for (Map.Entry<UUID, MinosoftMod> entry : sortedModMap.entrySet()) {
                Minosoft.THREAD_POOL.execute(() -> {
                    if (!entry.getValue().isEnabled()) {
                        modLatch.countDown();
                        progress.countDown();
                        return;
                    }
                    try {
                        if (!entry.getValue().start(phase)) {
                            throw new ModLoadingException(String.format("Could not load mod %s", entry.getValue().getInfo()));
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        Log.warn(String.format("An error occurred while loading %s", entry.getValue().getInfo()));
                        entry.getValue().setEnabled(false);
                    }
                    modLatch.countDown();
                    progress.countDown();
                });
            }
            modLatch.await();
        }

        for (Map.Entry<UUID, MinosoftMod> entry : sortedModMap.entrySet()) {
            if (entry.getValue().isEnabled()) {
                Minosoft.EVENT_MANAGERS.add(entry.getValue().getEventManager());
            } else {
                MOD_MAP.remove(entry.getKey());
            }
        }
        Log.info("Loading of %d mods finished in %dms!", sortedModMap.size(), (System.currentTimeMillis() - startTime));
    }

    public static MinosoftMod loadMod(CountUpAndDownLatch progress, File file) {
        MinosoftMod instance;
        try {
            Log.verbose(String.format("[MOD] Loading file %s", file.getAbsolutePath()));
            progress.countUp();
            ZipFile zipFile = new ZipFile(file);
            ModInfo modInfo = new ModInfo(Util.readJsonFromZip("mod.json", zipFile));
            if (isModLoaded(modInfo)) {
                Log.warn(String.format("Mod %s:%d (uuid=%s) is loaded multiple times! Skipping", modInfo.getName(), modInfo.getModVersionIdentifier().getVersionId(), modInfo.getModVersionIdentifier().getUUID()));
                return null;
            }
            JarClassLoader jcl = new JarClassLoader();
            jcl.add(file.getAbsolutePath());
            JclObjectFactory factory = JclObjectFactory.getInstance();

            instance = (MinosoftMod) factory.create(jcl, modInfo.getMainClass());
            instance.setInfo(modInfo);
            Log.verbose(String.format("[MOD] Mod file loaded and added to classpath (%s)", modInfo));
            zipFile.close();
        } catch (Throwable e) {
            instance = null;
            e.printStackTrace();
            Log.warn(String.format("Could not load mod: %s", file.getAbsolutePath()));
        }
        progress.countDown(); // failed
        return instance;
    }

    private static Priorities getLoadingPriorityOrDefault(ModInfo info) {
        if (info.getLoadingInfo() != null && info.getLoadingInfo().getLoadingPriority() != null) {
            return info.getLoadingInfo().getLoadingPriority();
        }
        return Priorities.NORMAL;
    }

    public static boolean isModLoaded(ModInfo info) {
        return MOD_MAP.containsKey(info.getModVersionIdentifier().getUUID());
    }

    @Nullable
    public static MinosoftMod getModByUUID(UUID uuid) {
        return MOD_MAP.get(uuid);
    }

    @Nullable
    public static ModInfo getModInfoByUUID(UUID uuid) {
        MinosoftMod mod = getModByUUID(uuid);
        if (mod == null) {
            return null;
        }
        return mod.getInfo();
    }
}
