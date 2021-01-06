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

package de.bixilon.minosoft.data.locale.minecraft;

import de.bixilon.minosoft.data.mappings.versions.Version;
import de.bixilon.minosoft.util.logging.Log;

import java.io.IOException;

public class MinecraftLocaleManager {
    private final Version version;
    private MinecraftLanguage language;

    public MinecraftLocaleManager(Version version) {
        this.version = version;
    }

    public MinecraftLanguage getLanguage() {
        return this.language;
    }

    public String translate(String key, Object... data) {
        return this.language.translate(key, data);
    }

    private MinecraftLanguage loadLanguage(String language) throws IOException {
        return new MinecraftLanguage(language, this.version.getAssetsManager().readJsonAsset(String.format("minecraft/lang/%s.json", language.toLowerCase())).getAsJsonObject());
    }

    public void load(String language) {
        long startTime = System.currentTimeMillis();
        Log.verbose(String.format("Loading minecraft language file (%s) for %s", language, this.version));
        try {
            this.language = loadLanguage(language);
        } catch (Exception e) {
            e.printStackTrace();
            Log.warn("Could not load minecraft language file: %s for %s", language, this.version);
        }
        Log.verbose("Loaded minecraft language files for %s successfully in %dms", this.version, (System.currentTimeMillis() - startTime));
    }

    public boolean canTranslate(String key) {
        return getLanguage().canTranslate(key);
    }
}
