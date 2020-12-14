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

    // fields depend on action
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
        this.action = WorldBorderActions.byId(buffer.readVarInt());
        switch (this.action) {
            case SET_SIZE -> this.radius = buffer.readDouble();
            case LERP_SIZE -> {
                this.oldRadius = buffer.readDouble();
                this.newRadius = buffer.readDouble();
                this.speed = buffer.readVarLong();
            }
            case SET_CENTER -> {
                this.x = buffer.readDouble();
                this.z = buffer.readDouble();
            }
            case INITIALIZE -> {
                this.x = buffer.readDouble();
                this.z = buffer.readDouble();
                this.oldRadius = buffer.readDouble();
                this.newRadius = buffer.readDouble();
                this.speed = buffer.readVarLong();
                this.portalBound = buffer.readVarInt();
                this.warningTime = buffer.readVarInt();
                this.warningBlocks = buffer.readVarInt();
            }
            case SET_WARNING_TIME -> this.warningTime = buffer.readVarInt();
            case SET_WARNING_BLOCKS -> this.warningBlocks = buffer.readVarInt();
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        switch (this.action) {
            case SET_SIZE -> Log.protocol(String.format("[IN] Receiving world border packet (action=%s, radius=%s)", this.action, this.radius));
            case LERP_SIZE -> Log.protocol(String.format("[IN] Receiving world border packet (action=%s, oldRadius=%s, newRadius=%s, speed=%s", this.action, this.oldRadius, this.newRadius, this.speed));
            case SET_CENTER -> Log.protocol(String.format("[IN] Receiving world border packet (action=%s, x=%s, z=%s)", this.action, this.x, this.z));
            case INITIALIZE -> Log.protocol(String.format("[IN] Receiving world border packet (action=%s, x=%s, z=%s, oldRadius=%s, newRadius=%s, speed=%s, portalBound=%s, warningTime=%s, warningBlocks=%s)", this.action, this.x, this.z, this.oldRadius, this.newRadius, this.speed, this.portalBound, this.warningTime, this.warningBlocks));
            case SET_WARNING_TIME -> Log.protocol(String.format("[IN] Receiving world border packet (action=%s, warningTime=%s)", this.action, this.warningTime));
            case SET_WARNING_BLOCKS -> Log.protocol(String.format("[IN] Receiving world border packet (action=%s, warningBlocks=%s)", this.action, this.warningBlocks));
        }
    }

    public double getRadius() {
        return this.radius;
    }

    public double getOldRadius() {
        return this.oldRadius;
    }

    public double getNewRadius() {
        return this.newRadius;
    }

    public double getX() {
        return this.x;
    }

    public double getZ() {
        return this.z;
    }

    public int getPortalBound() {
        return this.portalBound;
    }

    public int getWarningTime() {
        return this.warningTime;
    }

    public int getWarningBlocks() {
        return this.warningBlocks;
    }

    public enum WorldBorderActions {
        SET_SIZE,
        LERP_SIZE,
        SET_CENTER,
        INITIALIZE,
        SET_WARNING_TIME,
        SET_WARNING_BLOCKS;

        private static final WorldBorderActions[] WORLD_BORDER_ACTIONS = values();

        public static WorldBorderActions byId(int id) {
            return WORLD_BORDER_ACTIONS[id];
        }
    }
}
