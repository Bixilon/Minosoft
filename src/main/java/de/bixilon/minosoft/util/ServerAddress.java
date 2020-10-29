/*
 * Minosoft
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

public class ServerAddress {
    final String hostname;
    final int port;

    public ServerAddress(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    @Override
    public int hashCode() {
        return hostname.hashCode() * port;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        ServerAddress their = (ServerAddress) obj;
        return hostname.equals(their.getHostname()) && port == their.getPort();
    }

    @Override
    public String toString() {
        return getHostname() + ":" + getPort();
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }
}
