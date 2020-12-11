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

import com.google.gson.JsonObject;
import de.bixilon.minosoft.util.Util;

import java.util.UUID;

public record ModIdentifier(UUID uuid, int versionId) {

    public static ModIdentifier serialize(JsonObject json) {
        return new ModIdentifier(Util.getUUIDFromString(json.get("uuid").getAsString()), json.get("versionId").getAsInt());
    }

    @Override
    public String toString() {
        return String.format("uuid=%s, versionId=%d", uuid, versionId);
    }
}
