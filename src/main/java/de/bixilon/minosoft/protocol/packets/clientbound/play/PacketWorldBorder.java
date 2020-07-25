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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketWorldBorder implements ClientboundPacket {
    WorldBorderAction action;

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
        action = WorldBorderAction.byId(buffer.readVarInt());
        switch (action) {
            case SET_SIZE:
                radius = buffer.readDouble();
                break;
            case LERP_SIZE:
                oldRadius = buffer.readDouble();
                newRadius = buffer.readDouble();
                speed = buffer.readVarLong();
                break;
            case SET_CENTER:
                x = buffer.readDouble();
                z = buffer.readDouble();
                break;
            case INITIALIZE:
                x = buffer.readDouble();
                z = buffer.readDouble();
                oldRadius = buffer.readDouble();
                newRadius = buffer.readDouble();
                speed = buffer.readVarLong();
                portalBound = buffer.readVarInt();
                warningTime = buffer.readVarInt();
                warningBlocks = buffer.readVarInt();
                break;
            case SET_WARNING_TIME:
                warningTime = buffer.readVarInt();
                break;
            case SET_WARNING_BLOCKS:
                warningBlocks = buffer.readVarInt();
                break;
        }
        return true;
    }

    @Override
    public void log() {
        switch (action) {
            case SET_SIZE:
                Log.protocol(String.format("Receiving world border packet (action=%s, radius=%s)", action, radius));
                break;
            case LERP_SIZE:
                Log.protocol(String.format("Receiving world border packet (action=%s, oldRadius=%s, newRadius=%s, speed=%s", action, oldRadius, newRadius, speed));
                break;
            case SET_CENTER:
                Log.protocol(String.format("Receiving world border packet (action=%s, x=%s, z=%s)", action, x, z));
                break;
            case INITIALIZE:
                Log.protocol(String.format("Receiving world border packet (action=%s, x=%s, z=%s, oldRadius=%s, newRadius=%s, speed=%s, portalBound=%s, warningTime=%s, warningBlocks=%s)", action, x, z, oldRadius, newRadius, speed, portalBound, warningTime, warningBlocks));
                break;
            case SET_WARNING_TIME:
                Log.protocol(String.format("Receiving world border packet (action=%s, warningTime=%s)", action, warningTime));
                break;
            case SET_WARNING_BLOCKS:
                Log.protocol(String.format("Receiving world border packet (action=%s, warningBlocks=%s)", action, warningBlocks));
                break;
        }
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
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

    public enum WorldBorderAction {
        SET_SIZE(0),
        LERP_SIZE(1),
        SET_CENTER(2),
        INITIALIZE(3),
        SET_WARNING_TIME(4),
        SET_WARNING_BLOCKS(5);

        final int id;

        WorldBorderAction(int id) {
            this.id = id;
        }

        public static WorldBorderAction byId(int id) {
            for (WorldBorderAction a : values()) {
                if (a.getId() == id) {
                    return a;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
