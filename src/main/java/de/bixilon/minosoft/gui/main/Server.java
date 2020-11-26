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

package de.bixilon.minosoft.gui.main;

import com.google.gson.JsonObject;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.data.text.BaseComponent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.protocol.ConnectionReasons;
import de.bixilon.minosoft.protocol.protocol.LANServerListener;
import de.bixilon.minosoft.util.ServerAddress;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Base64;

public class Server {
    private static int highestServerId;
    private final int id;
    private final ArrayList<Connection> connections = new ArrayList<>();
    private BaseComponent name;
    private BaseComponent addressName;
    private String address;
    private int desiredVersion;
    private byte[] favicon;
    private Connection lastPing;
    private boolean readOnly = false;

    public Server(int id, BaseComponent name, String address, int desiredVersion, byte[] favicon) {
        this(id, name, address, desiredVersion);
        this.favicon = favicon;
    }

    public Server(int id, BaseComponent name, String address, int desiredVersion) {
        this.id = id;
        if (id > highestServerId) {
            highestServerId = id;
        }
        this.name = name;
        this.address = address;
        this.addressName = new BaseComponent(address);
        this.desiredVersion = desiredVersion;
    }

    public Server(ServerAddress address) {
        this.id = getNextServerId();
        this.name = new BaseComponent(String.format("LAN Server #%d", LANServerListener.getServers().size()));
        this.address = address.toString();
        this.desiredVersion = -1; // Automatic
        this.readOnly = true;
    }

    public static int getNextServerId() {
        return ++highestServerId;
    }

    public static Server deserialize(JsonObject json) {
        Server server = new Server(json.get("id").getAsInt(), new BaseComponent(json.get("name").getAsString()), json.get("address").getAsString(), json.get("version").getAsInt());
        if (json.has("favicon")) {
            server.setFavicon(Base64.getDecoder().decode(json.get("favicon").getAsString()));
        }
        return server;
    }

    @Nullable
    public byte[] getFavicon() {
        return favicon;
    }

    public void setFavicon(byte[] favicon) {
        this.favicon = favicon;
    }

    public int getId() {
        return id;
    }

    public void saveToConfig() {
        if (isReadOnly()) {
            return;
        }
        Minosoft.getConfig().putServer(this);
        Minosoft.getConfig().saveToFile();
    }

    public void delete() {
        if (isReadOnly()) {
            return;
        }
        Minosoft.getConfig().removeServer(this);
        Minosoft.getConfig().saveToFile();
    }

    public Connection getLastPing() {
        return lastPing;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getName(), getAddress());
    }

    public BaseComponent getName() {
        if (name.isEmpty()) {
            return addressName;
        }
        return name;
    }


    public void setName(BaseComponent name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        this.addressName = new BaseComponent(address);
    }

    public void ping() {
        if (lastPing == null) {
            lastPing = new Connection(Connection.lastConnectionId++, getAddress(), null);
        }
        lastPing.resolve(ConnectionReasons.PING, getDesiredVersionId()); // resolve dns address and ping
    }

    public int getDesiredVersionId() {
        return desiredVersion;
    }

    public void setDesiredVersionId(int versionId) {
        this.desiredVersion = versionId;
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

    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", name.getLegacyText());
        json.addProperty("address", address);
        json.addProperty("version", desiredVersion);
        if (favicon != null) {
            json.addProperty("favicon", getBase64Favicon());
        }
        return json;
    }

    @Nullable
    public String getBase64Favicon() {
        if (favicon == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(favicon);
    }

    public boolean isReadOnly() {
        return readOnly;
    }
}
