/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.language.deprecated;

import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.ShutdownReasons;
import de.bixilon.minosoft.data.registries.ResourceLocation;
import de.bixilon.minosoft.data.registries.versions.Versions;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.logging.Log;

@Deprecated
public class DLocaleManager {
    private static DLanguage fallbackLanguage; // en_US
    private static DLanguage language;

    public static DLanguage getLanguage() {
        return language;
    }

    public static DLanguage getFallbackLanguage() {
        return fallbackLanguage;
    }

    /**
     * Translates a string with placeholders
     *
     * @param key  String id
     * @param data Placeholders in the following format: {0} is awesome.
     * @return The formatted string
     */
    public static String translate(Strings key, Object... data) {
        if (language.canTranslate(key)) {
            return language.translate(key, data);
        }
        return fallbackLanguage.translate(key, data);
    }

    private static DLanguage loadLanguage(String language) {
        return new DLanguage(language, Minosoft.MINOSOFT_ASSETS_MANAGER.readLegacyJsonAsset(new ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, String.format("locale/%s.json", language))));
    }

    public static void load(String language) {
        long startTime = System.currentTimeMillis();
        Log.verbose(String.format("Loading language file (%s)", language));
        if (fallbackLanguage == null) {
            try {
                fallbackLanguage = loadLanguage("en_US");
            } catch (Exception e) {
                e.printStackTrace();
                Minosoft.shutdown("Could not load fallback language file (en_US). Exiting...", ShutdownReasons.CRITICAL_EXCEPTION);
            }
        }
        try {
            DLocaleManager.language = loadLanguage(language);
        } catch (Exception e) {
            e.printStackTrace();
            Log.warn(String.format("Could not load language file: %s", language));
        }
        Versions.AUTOMATIC_VERSION.setVersionName(translate(Strings.VERSION_AUTOMATIC));
        Log.verbose(String.format("Loaded language files successfully in %dms", (System.currentTimeMillis() - startTime)));
    }
}