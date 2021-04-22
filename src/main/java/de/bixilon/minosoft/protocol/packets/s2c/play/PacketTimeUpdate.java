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

package de.bixilon.minosoft.protocol.packets.s2c.play;

import de.bixilon.minosoft.modding.event.events.TimeChangeEvent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class PacketTimeUpdate extends PlayS2CPacket {
    private final long worldAge;
    private final long timeOfDay;

    public PacketTimeUpdate(PlayInByteBuffer buffer) {
        this.worldAge = buffer.readLong();
        this.timeOfDay = buffer.readLong();
    }

    @Override
    public void handle(PlayConnection connection) {
        if (connection.fireEvent(new TimeChangeEvent(connection, this))) {
            return;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Time update packet received. Time is now %st (total %st, moving=%s)", Math.abs(this.timeOfDay), this.worldAge, this.timeOfDay > 0));
    }

    public long getWorldAge() {
        return this.worldAge;
    }

    public long getTimeOfDay() {
        return this.timeOfDay;
    }
}
