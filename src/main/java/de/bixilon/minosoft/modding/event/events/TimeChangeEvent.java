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

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketTimeUpdate;

public class TimeChangeEvent extends CancelableEvent {
    private final long worldAge;
    private final long timeOfDay;

    public TimeChangeEvent(Connection connection, long worldAge, long timeOfDay) {
        super(connection);
        this.worldAge = worldAge;
        this.timeOfDay = timeOfDay;
    }

    public TimeChangeEvent(Connection connection, PacketTimeUpdate pkg) {
        super(connection);
        this.worldAge = pkg.getWorldAge();
        this.timeOfDay = pkg.getTimeOfDay();
    }

    public long getWorldAge() {
        return worldAge;
    }

    public long getTimeOfDay() {
        return timeOfDay;
    }
}
