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

package de.bixilon.minosoft.data.locale.minecraft;

import de.bixilon.minosoft.data.assets.AssetsManager;
import de.bixilon.minosoft.logging.Log;

import java.io.IOException;

public class MinecraftLocaleManager {
    private static MinecraftLanguage language;


    public static MinecraftLanguage getLanguage() {
        return language;
    }

    public static String translate(String key, Object... data) {
        return language.translate(key, data);
    }

    private static MinecraftLanguage loadLanguage(String language) throws IOException {
        return new MinecraftLanguage(language, AssetsManager.readJsonAsset(String.format("minecraft/lang/%s.json", language.toLowerCase())).getAsJsonObject());
    }

    public static void load(String language) {
        long startTime = System.currentTimeMillis();
        Log.verbose(String.format("Loading minecraft language file (%s)", language));
        try {
            MinecraftLocaleManager.language = loadLanguage(language);
        } catch (Exception e) {
            e.printStackTrace();
            Log.warn(String.format("Could not load minecraft language file: %s", language));
        }
        Log.verbose(String.format("Loaded minecraft language files successfully in %dms", (System.currentTimeMillis() - startTime)));
    }
}
