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

package de.bixilon.minosoft.data.accounts;

import de.bixilon.minosoft.util.Util;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OfflineAccount extends Account {
    public OfflineAccount(String username) {
        super(username, UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8)));
    }

    public OfflineAccount(String username, UUID uuid) {
        super(username, uuid);
    }

    public static OfflineAccount deserialize(Map<String, Object> json) {
        return new OfflineAccount((String) json.get("username"), Util.getUUIDFromString((String) json.get("uuid")));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> json = new HashMap<>();
        json.put("username", getUsername());
        json.put("uuid", getUUID().toString());
        json.put("type", "offline");
        return json;
    }

    @Override
    public void join(String serverId) {
    }

    @Override
    public boolean select() {
        return true;
    }

    @Override
    public void logout() {
    }

    @Override
    public String getId() {
        return getUsername() + ":" + getUUID().toString();
    }
}
