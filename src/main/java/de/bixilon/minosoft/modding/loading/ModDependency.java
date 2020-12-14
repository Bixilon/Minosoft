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

package de.bixilon.minosoft.modding.loading;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.util.Util;

import java.util.HashSet;
import java.util.UUID;

public class ModDependency {

    private final UUID uuid;
    private Integer versionMinimum;
    private Integer versionMaximum;

    public ModDependency(UUID uuid, Integer versionMinimum, Integer versionMaximum) {
        this.uuid = uuid;
        this.versionMinimum = versionMinimum;
        this.versionMaximum = versionMaximum;
    }

    public ModDependency(UUID uuid) {
        this.uuid = uuid;
    }

    public static ModDependency serialize(JsonObject json) {
        UUID uuid = Util.getUUIDFromString(json.get("uuid").getAsString());
        Integer versionMinimum = null;
        Integer versionMaximum = null;

        if (json.has("version")) {
            JsonObject version = json.getAsJsonObject("version");
            if (version.has("minimum")) {
                versionMinimum = version.get("minimum").getAsInt();
            }
            if (version.has("maximum")) {
                versionMaximum = version.get("maximum").getAsInt();
            }
        }
        return new ModDependency(uuid, versionMinimum, versionMaximum);
    }

    public static HashSet<ModDependency> serializeDependencyArray(JsonArray json) {
        HashSet<ModDependency> result = new HashSet<>();
        json.forEach((jsonElement -> result.add(serialize(jsonElement.getAsJsonObject()))));
        return result;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public Integer getVersionMinimum() {
        return this.versionMinimum;
    }

    public Integer getVersionMaximum() {
        return this.versionMaximum;
    }

    @Override
    public int hashCode() {
        int result = this.uuid.hashCode();
        if (this.versionMinimum != null && this.versionMinimum > 0) {
            result *= this.versionMinimum;
        }

        if (this.versionMaximum != null && this.versionMaximum > 0) {
            result *= this.versionMaximum;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (hashCode() != obj.hashCode()) {
            return false;
        }
        ModDependency their = (ModDependency) obj;
        return getUUID().equals(their.getUUID()) && getVersionMaximum().equals(their.getVersionMaximum()) && getVersionMinimum().equals(their.getVersionMinimum());
    }

    @Override
    public String toString() {
        String result = this.uuid.toString();
        if (this.versionMinimum != null) {
            result += " >" + this.versionMinimum;
        }
        if (this.versionMaximum != null) {
            result += " <" + this.versionMaximum;
        }
        return result;
    }
}
