/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.main;

import com.google.common.collect.Sets;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.data.mappings.versions.Version;
import de.bixilon.minosoft.data.text.BaseComponent;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.network.connection.StatusConnection;
import de.bixilon.minosoft.protocol.protocol.LANServerListener;
import de.bixilon.minosoft.util.ServerAddress;

import javax.annotation.Nullable;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Server {
    private static int highestServerId;
    private final int id;
    private final Set<PlayConnection> connections = Sets.newConcurrentHashSet();
    private ChatComponent name;
    private ChatComponent addressName;
    private String address;
    private int desiredVersion;
    private byte[] favicon;
    private StatusConnection lastPing;
    private boolean temporary;
    private ServerListCell cell;

    public Server(int id, ChatComponent name, String address, int desiredVersion, byte[] favicon) {
        this(id, name, address, desiredVersion);
        this.favicon = favicon;
    }

    public Server(int id, ChatComponent name, String address, int desiredVersion) {
        this.id = id;
        if (id > highestServerId) {
            highestServerId = id;
        }
        this.name = name;
        this.address = address;
        this.addressName = ChatComponent.Companion.of(address);
        this.desiredVersion = desiredVersion;
    }

    public Server(ChatComponent name, String address, Version version) {
        this(getNextServerId(), name, address, version.getVersionId());
    }

    public Server(ServerAddress address) {
        this.id = getNextServerId();
        this.name = ChatComponent.Companion.of(String.format("LAN Server #%d", LANServerListener.getServerMap().size()));
        this.address = address.toString();
        this.desiredVersion = -1; // Automatic
        this.temporary = true;
    }

    public static int getNextServerId() {
        return ++highestServerId;
    }

    public static Server deserialize(Map<String, Object> json) {
        Server server = new Server((int) (double) json.get("id"), ChatComponent.Companion.of(json.get("name")), (String) json.get("address"), (int) (double) json.get("version"));
        if (json.containsKey("favicon")) {
            server.setFavicon(Base64.getDecoder().decode((String) json.get("favicon")));
        }
        return server;
    }

    @Nullable
    public byte[] getFavicon() {
        return this.favicon;
    }

    public void setFavicon(byte[] favicon) {
        this.favicon = favicon;
    }

    public int getId() {
        return this.id;
    }

    public void saveToConfig() {
        if (isTemporary()) {
            return;
        }
        Minosoft.getConfig().getConfig().getServer().getEntries().put(this.getId(), this);
        Minosoft.getConfig().saveToFile();
    }

    public void delete() {
        if (isTemporary()) {
            return;
        }
        Minosoft.getConfig().getConfig().getServer().getEntries().remove(this.getId());
        Minosoft.getConfig().saveToFile();
    }

    public StatusConnection getLastPing() {
        return this.lastPing;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getName(), getAddress());
    }

    public ChatComponent getName() {
        if (this.name == null || ((BaseComponent) this.name).getParts().isEmpty()) {
            return this.addressName;
        }
        return this.name;
    }

    public void setName(ChatComponent name) {
        this.name = name;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
        this.addressName = ChatComponent.Companion.of(address);
    }

    public void ping() {
        if (this.lastPing == null) {
            this.lastPing = new StatusConnection(getAddress());
        }
        this.lastPing.ping(); // resolve dns address and ping
    }

    public int getDesiredVersionId() {
        return this.desiredVersion;
    }

    public void setDesiredVersionId(int versionId) {
        this.desiredVersion = versionId;
    }

    public Set<PlayConnection> getConnections() {
        return this.connections;
    }

    public void addConnection(PlayConnection connection) {
        this.connections.add(connection);
    }

    public boolean isConnected() {
        for (PlayConnection connection : this.connections) {
            if (connection.isConnected()) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> json = new HashMap<>();
        json.put("id", this.id);
        json.put("name", this.name.getLegacyText());
        json.put("address", this.address);
        json.put("version", this.desiredVersion);
        if (this.favicon != null) {
            json.put("favicon", getBase64Favicon());
        }
        return json;
    }

    @Nullable
    public String getBase64Favicon() {
        if (this.favicon == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(this.favicon);
    }

    public boolean isTemporary() {
        return this.temporary;
    }

    public ServerListCell getCell() {
        return this.cell;
    }

    public void setCell(ServerListCell cell) {
        this.cell = cell;
    }
}
