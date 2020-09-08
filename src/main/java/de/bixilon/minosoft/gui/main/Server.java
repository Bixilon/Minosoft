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

package de.bixilon.minosoft.gui.main;

import de.bixilon.minosoft.Config;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.protocol.ConnectionReasons;
import javafx.scene.image.Image;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class Server {
    static int highestServerId;
    final int id;
    String name;
    String address;
    int desiredVersion;
    String favicon;
    Connection lastPing;
    ArrayList<Connection> connections = new ArrayList<>();

    public Server(int id, String name, String address, int desiredVersion) {
        this.id = id;
        if (id > highestServerId) {
            highestServerId = id;
        }
        this.name = name;
        this.address = address;
        this.desiredVersion = desiredVersion;
    }

    public Server(int id, String name, String address, int desiredVersion, String favicon) {
        this(id, name, address, desiredVersion);
        this.favicon = favicon;
    }

    public static int getNextServerId() {
        return ++highestServerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getDesiredVersion() {
        return desiredVersion;
    }

    public void setDesiredVersion(int desiredVersion) {
        this.desiredVersion = desiredVersion;
    }

    @Nullable
    public String getBase64Favicon() {
        return favicon;
    }

    public void setBase64Favicon(String favicon) {
        this.favicon = favicon;
    }

    @Nullable
    public Image getFavicon() {
        return GUITools.getImageFromBase64(getBase64Favicon());
    }

    public int getId() {
        return id;
    }

    public void saveToConfig() {
        Minosoft.getConfig().putServer(this);
        Minosoft.getConfig().saveToFile(Config.configFileName);
    }

    public void delete() {
        Minosoft.getConfig().removeServer(this);
        Minosoft.getConfig().saveToFile(Config.configFileName);
    }

    public Connection getLastPing() {
        return lastPing;
    }

    @Override
    public String toString() {
        return getName() + " (" + getAddress() + ")";
    }

    @Override
    public int hashCode() {
        return id;
    }

    public void ping() {
        if (lastPing == null) {
            lastPing = new Connection(Connection.lastConnectionId++, getAddress(), null);
        }
        lastPing.resolve(ConnectionReasons.PING, getDesiredVersion()); // resolve dns address and ping
    }

    public ArrayList<Connection> getConnections() {
        return connections;
    }

    public void addConnection(Connection connection) {
        connections.add(connection);
    }

    public boolean isConnected() {
        for (Connection connection : connections) {
            if (connection.isConnected()) {
                return true;
            }
        }
        return false;
    }
}
