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

import de.bixilon.minosoft.modding.event.events.UpdateHealthEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W04A;

public class PacketUpdateHealth extends ClientboundPacket {
    private final int food;
    private final float saturation;
    private float health;

    public PacketUpdateHealth(InByteBuffer buffer) {
        this.health = buffer.readFloat();
        if (this.health < 0.0F) {
            this.health = 0.0F;
        }
        if (buffer.getVersionId() < V_14W04A) {
            this.food = buffer.readUnsignedShort();
        } else {
            this.food = buffer.readVarInt();
        }
        this.saturation = buffer.readFloat();
    }

    @Override
    public void handle(Connection connection) {
        connection.fireEvent(new UpdateHealthEvent(connection, this));

        connection.getPlayer().getHealthCondition().setFood(getFood());
        connection.getPlayer().getHealthCondition().setHealth(getHealth());
        connection.getPlayer().getHealthCondition().setSaturation(getSaturation());
        if (getHealth() <= 0.0F) {
            // do respawn
            connection.getSender().respawn();
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Health update. Now at %s hearts and %s food level and %s saturation", this.health, this.food, this.saturation));
    }

    public int getFood() {
        return this.food;
    }

    public float getHealth() {
        return this.health;
    }

    public float getSaturation() {
        return this.saturation;
    }
}
