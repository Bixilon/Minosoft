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

package de.bixilon.minosoft.util;

import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import org.xbill.DNS.Record;
import org.xbill.DNS.*;

import java.util.LinkedList;

public final class DNSUtil {

    public static LinkedList<ServerAddress> getServerAddresses(String hostname) throws TextParseException {
        ServerAddress fallbackAddress = getServerAddress(hostname);
        LinkedList<ServerAddress> ret = new LinkedList<>();
        if (hostname.contains(":")) {
            // port provided, skip srv check
            ret.add(fallbackAddress);
            return ret;
        }
        String query = "_minecraft._tcp." + hostname;
        Record[] records = new Lookup(query, Type.SRV).run();
        if (records == null) {
            ret.add(fallbackAddress);
            return ret;
        }
        for (Record record : records) {
            SRVRecord srvRecord = (SRVRecord) record;
            ret.add(new ServerAddress(srvRecord.getTarget().toString(true), srvRecord.getPort()));
        }
        ret.add(fallbackAddress);
        return ret;
    }

    private static ServerAddress getServerAddress(String hostname) {
        String[] splitHostname = hostname.split(":", 2);
        if (splitHostname.length == 1) {
            return new ServerAddress(splitHostname[0], ProtocolDefinition.DEFAULT_PORT);
        }
        return new ServerAddress(splitHostname[0], Integer.parseInt(splitHostname[1]));

    }

    public static String correctHostName(String hostname) {
        // replaces invalid chars to avoid copy and paste issues (like spaces, ...)
        hostname = hostname.replaceAll("\\s", "");
        return hostname.toLowerCase();
    }

}
