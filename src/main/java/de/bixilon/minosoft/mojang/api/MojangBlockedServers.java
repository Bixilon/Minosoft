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

package de.bixilon.minosoft.mojang.api;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.util.HTTP;
import de.bixilon.minosoft.util.Util;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MojangBlockedServers {


    public static ArrayList<String> getBlockedServers() {
        HttpResponse<String> response = HTTP.get(MojangURLs.BLOCKED_SERVERS.getUrl());
        if (response == null) {
            Log.mojang("Failed to fetch blocked servers");
            return null;
        }
        if (response.statusCode() != 200) {
            Log.mojang(String.format("Failed to fetch blocked server error code %d", response.statusCode()));
            return null;
        }
        // now it is hopefully okay
        return new ArrayList<>(Arrays.asList(response.body().split("\n")));
    }

    public boolean isServerBlocked(List<String> list, String hostname) {
        for (String hash : list) {
            if (hash.equals(Util.sha1(hostname))) {
                return true;
            }
            // ToDo: check subdomains and ip addresses
        }
        return false;
    }
}
