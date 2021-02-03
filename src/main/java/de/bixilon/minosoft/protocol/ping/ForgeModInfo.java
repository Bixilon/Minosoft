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

package de.bixilon.minosoft.protocol.ping;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class ForgeModInfo implements ServerModInfo {
    private final JsonObject modInfo;
    private final String info;
    private final ArrayList<ServerModItem> modList = new ArrayList<>();

    public ForgeModInfo(JsonObject modInfo) {
        this.modInfo = modInfo;
        JsonArray mods = modInfo.getAsJsonArray("modList");
        this.info = String.format("Modded server, %d mods present", mods.size());
        for (JsonElement mod : mods) {
            JsonObject mod2 = (JsonObject) mod;
            this.modList.add(new ServerModItem(mod2.get("modid").getAsString(), mod2.get("version").getAsString()));
        }
    }

    @Override
    public String getBrand() {
        return "Forge";
    }

    @Override
    public String getInfo() {
        return this.info;
    }

    @Override
    public ServerModTypes getType() {
        return ServerModTypes.FORGE;
    }

    public ArrayList<ServerModItem> getModList() {
        return this.modList;
    }
}
