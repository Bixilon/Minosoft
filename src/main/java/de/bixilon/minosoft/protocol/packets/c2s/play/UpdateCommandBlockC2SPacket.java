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

package de.bixilon.minosoft.protocol.packets.c2s.play;

import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket;
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3i;

public class UpdateCommandBlockC2SPacket implements PlayC2SPacket {
    private final Vec3i position;
    private final String command;
    private final CommandBlockType type;
    private final boolean trackOutput;
    private final boolean isConditional;
    private final boolean isAutomatic;

    public UpdateCommandBlockC2SPacket(Vec3i position, String command, CommandBlockType type, boolean trackOutput, boolean isConditional, boolean isAutomatic) {
        this.position = position;
        this.command = command;
        this.type = type;
        this.trackOutput = trackOutput;
        this.isConditional = isConditional;
        this.isAutomatic = isAutomatic;
    }

    @Override
    public void write(PlayOutByteBuffer buffer) {
        buffer.writePosition(this.position);
        buffer.writeString(this.command);
        buffer.writeVarInt(this.type.ordinal());

        byte flags = 0x00;
        if (this.trackOutput) {
            flags |= 0x01;
        }
        if (this.isConditional) {
            flags |= 0x02;
        }
        if (this.isAutomatic) {
            flags |= 0x04;
        }

        buffer.writeByte(flags);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending update command block packet at %s (command=\"%s\", type=%s, trackOutput=%s, isConditional=%s, isAutomatic=%s)", this.position, this.command, this.type, this.trackOutput, this.isConditional, this.isAutomatic));
    }

    public enum CommandBlockType {
        SEQUENCE,
        AUTO,
        REDSTONE;

        private static final CommandBlockType[] COMMAND_BLOCK_TYPES = values();

        public static CommandBlockType byId(int id) {
            return COMMAND_BLOCK_TYPES[id];
        }
    }
}
