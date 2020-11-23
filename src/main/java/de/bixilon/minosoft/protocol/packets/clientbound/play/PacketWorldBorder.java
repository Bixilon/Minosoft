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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketWorldBorder implements ClientboundPacket {
    WorldBorderActions action;

    //fields depend on action
    double radius;

    double oldRadius;
    double newRadius;
    long speed;

    double x;
    double z;

    int portalBound;
    int warningTime;
    int warningBlocks;

    @Override
    public boolean read(InByteBuffer buffer) {
        action = WorldBorderActions.byId(buffer.readVarInt());
        switch (action) {
            case SET_SIZE -> radius = buffer.readDouble();
            case LERP_SIZE -> {
                oldRadius = buffer.readDouble();
                newRadius = buffer.readDouble();
                speed = buffer.readVarLong();
            }
            case SET_CENTER -> {
                x = buffer.readDouble();
                z = buffer.readDouble();
            }
            case INITIALIZE -> {
                x = buffer.readDouble();
                z = buffer.readDouble();
                oldRadius = buffer.readDouble();
                newRadius = buffer.readDouble();
                speed = buffer.readVarLong();
                portalBound = buffer.readVarInt();
                warningTime = buffer.readVarInt();
                warningBlocks = buffer.readVarInt();
            }
            case SET_WARNING_TIME -> warningTime = buffer.readVarInt();
            case SET_WARNING_BLOCKS -> warningBlocks = buffer.readVarInt();
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        switch (action) {
            case SET_SIZE -> Log.protocol(String.format("[IN] Receiving world border packet (action=%s, radius=%s)", action, radius));
            case LERP_SIZE -> Log.protocol(String.format("[IN] Receiving world border packet (action=%s, oldRadius=%s, newRadius=%s, speed=%s", action, oldRadius, newRadius, speed));
            case SET_CENTER -> Log.protocol(String.format("[IN] Receiving world border packet (action=%s, x=%s, z=%s)", action, x, z));
            case INITIALIZE -> Log.protocol(String.format("[IN] Receiving world border packet (action=%s, x=%s, z=%s, oldRadius=%s, newRadius=%s, speed=%s, portalBound=%s, warningTime=%s, warningBlocks=%s)", action, x, z, oldRadius, newRadius, speed, portalBound, warningTime, warningBlocks));
            case SET_WARNING_TIME -> Log.protocol(String.format("[IN] Receiving world border packet (action=%s, warningTime=%s)", action, warningTime));
            case SET_WARNING_BLOCKS -> Log.protocol(String.format("[IN] Receiving world border packet (action=%s, warningBlocks=%s)", action, warningBlocks));
        }
    }

    public double getRadius() {
        return radius;
    }

    public double getOldRadius() {
        return oldRadius;
    }

    public double getNewRadius() {
        return newRadius;
    }

    public double getX() {
        return x;
    }

    public double getZ() {
        return z;
    }

    public int getPortalBound() {
        return portalBound;
    }

    public int getWarningTime() {
        return warningTime;
    }

    public int getWarningBlocks() {
        return warningBlocks;
    }

    public enum WorldBorderActions {
        SET_SIZE,
        LERP_SIZE,
        SET_CENTER,
        INITIALIZE,
        SET_WARNING_TIME,
        SET_WARNING_BLOCKS;

        public static WorldBorderActions byId(int id) {
            return values()[id];
        }
    }
}
