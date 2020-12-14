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

package de.bixilon.minosoft.data.locale;

import com.google.gson.JsonObject;

import java.text.MessageFormat;
import java.util.HashMap;

public class Language {
    private final String language;
    private final HashMap<Strings, String> data = new HashMap<>();

    protected Language(String language, JsonObject json) {
        this.language = language;
        json.keySet().forEach((key) -> this.data.put(Strings.valueOf(key.toUpperCase()), json.get(key).getAsString()));
    }

    public String getLanguage() {
        return this.language;
    }

    public boolean canTranslate(Strings key) {
        return this.data.containsKey(key);
    }

    public String translate(Strings key, Object... data) {
        return MessageFormat.format(this.data.get(key), data);
    }

    @Override
    public String toString() {
        return this.language;
    }
}
