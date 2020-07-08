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

package de.bixilon.minosoft.protocol.modding.channels;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketPluginMessageSending;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.OutByteBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginChannelHandler {
    final Map<String, List<ChannelHandler>> channels;
    final List<String> registeredClientChannels;
    final List<String> registeredServerChannels;
    final Connection connection;

    public PluginChannelHandler(Connection connection) {
        channels = new HashMap<>();
        registeredClientChannels = new ArrayList<>();
        registeredServerChannels = new ArrayList<>();
        this.connection = connection;
    }

    public void registerClientHandler(String name, ChannelHandler handler) {
        if (channels.get(name) == null) {
            // no channel with that name was registered yet
            List<ChannelHandler> handlerList = new ArrayList<>();
            handlerList.add(handler);
            channels.put(name, handlerList);
            return;
        }
        // was registered, appending to list

        channels.get(name).add(handler);
    }

    public void unregisterClientHandler(String name, ChannelHandler handler) {
        if (channels.get(name) == null) {
            // not registered
            return;
        }
        channels.get(name).remove(handler);
    }

    public void handle(String name, byte[] data) {
        DefaultPluginChannels defaultPluginChannel = DefaultPluginChannels.byName(name, connection.getVersion());
        if (defaultPluginChannel == DefaultPluginChannels.REGISTER) {
            // register this channel
            String toRegisterName = new String(data);
            registeredClientChannels.add(toRegisterName);
            Log.debug(String.format("Server registered plugin channel \"%s\"", toRegisterName));
            return;
        }
        if (defaultPluginChannel == DefaultPluginChannels.UNREGISTER) {
            // register this channel
            String toUnregisterName = new String(data);
            registeredClientChannels.remove(toUnregisterName);
            Log.debug(String.format("Server unregistered plugin channel \"%s\"", toUnregisterName));
            return;
        }
        // check if channel was registered or if it is a default channel
        if (!registeredClientChannels.contains(name) && DefaultPluginChannels.byName(name, connection.getVersion()) == null) {
            Log.debug(String.format("Server tried to send data into unregistered plugin channel (name=\"%s\", messageLength=%d, string=\"%s\")", name, data.length, new String(data)));
            return;
        }
        if (channels.get(name) == null) {
            Log.debug(String.format("Can not handle plugin message in channel \"%s\" (messageLength=%d)", name, data.length));
            return;
        }

        for (ChannelHandler handler : channels.get(name)) {
            handler.handle(this, new InByteBuffer(data, connection.getVersion()));
        }
    }

    public void sendRawData(String channel, byte[] data) {
        connection.sendPacket(new PacketPluginMessageSending(channel, data));
    }

    public void sendRawData(String channel, OutByteBuffer buffer) {
        connection.sendPacket(new PacketPluginMessageSending(channel, buffer.getOutBytes()));
    }

    public void registerServerChannel(String name) {
        if (DefaultPluginChannels.byName(name, connection.getVersion()) != null) {
            // channel is a default channel, can not register
            throw new IllegalArgumentException(String.format("Can not register default Minecraft plugin channel (name=%s)", name));
        }
        sendRawData(DefaultPluginChannels.REGISTER.getIdentifier().get(connection.getVersion()), name.getBytes());
        registeredServerChannels.add(name);
    }

    public void unregisterServerChannel(String name) {
        if (DefaultPluginChannels.byName(name, connection.getVersion()) != null) {
            // channel is a default channel, can not unregister
            throw new IllegalArgumentException(String.format("Can not unregister default Minecraft plugin channel (name=%s)", name));
        }
        sendRawData(DefaultPluginChannels.UNREGISTER.getIdentifier().get(connection.getVersion()), name.getBytes());
        registeredServerChannels.remove(name);
    }

}
