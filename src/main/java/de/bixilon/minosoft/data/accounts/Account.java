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

import com.google.gson.JsonObject;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.util.mojang.api.exceptions.MojangJoinServerErrorException;
import de.bixilon.minosoft.util.mojang.api.exceptions.NoNetworkConnectionException;

import java.util.UUID;

public abstract class Account {
    private final String username;
    private final UUID uuid;

    protected Account(String username, UUID uuid) {
        this.username = username;
        this.uuid = uuid;
    }

    public String getUsername() {
        return this.username;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public abstract JsonObject serialize();

    public abstract void join(String serverId) throws MojangJoinServerErrorException, NoNetworkConnectionException;

    public abstract boolean select();

    public abstract void logout();

    public abstract String getId();

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (hashCode() != obj.hashCode()) {
            return false;
        }
        Account account = (Account) obj;
        return getId().equals(account.getId());
    }

    public void saveToConfig() {
        Minosoft.getConfig().putAccount(this);
        Minosoft.getConfig().saveToFile();
    }

    @Override
    public String toString() {
        return getId();
    }
}
