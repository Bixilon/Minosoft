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
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketEntityAnimation implements ClientboundPacket {
    int entityId;
    Animations animation;

    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
            case VERSION_1_8:
            case VERSION_1_9_4:
                entityId = buffer.readVarInt();
                animation = Animations.byId(buffer.readByte());
                return true;
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Play entity animation (entityId=%d, animation=%s)", entityId, animation.name()));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public enum Animations {
        SWING_ARM(0),
        DAMAGE_ANIMATION(1),
        LEAVE_BED(2),
        EAT_FOOD(3),
        CRITICAL_EFFECT(4),
        MAGIC_CRITICAL_EFFECT(5),
        TO_DO_1(102), // name currently unknown //ToDo
        SNEAK(104),
        UN_SNEAK(105);

        final int id;

        Animations(int id) {
            this.id = id;
        }

        public static Animations byId(int id) {
            for (Animations a : values()) {
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
