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

package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.protocol.packets.serverbound.PlayServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPlayByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3i;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W33A;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W31A;

public class PacketTabCompleteSending implements PlayServerboundPacket {
    private final String text;
    private final Vec3i position;
    private final boolean assumeCommand;

    public PacketTabCompleteSending(String text) {
        this.text = text;
        this.position = null;
        this.assumeCommand = false;
    }

    public PacketTabCompleteSending(String text, Vec3i position) {
        this.text = text;
        this.position = position;
        this.assumeCommand = false;
    }

    public PacketTabCompleteSending(String text, boolean assumeCommand, Vec3i position) {
        this.text = text;
        this.position = position;
        this.assumeCommand = assumeCommand;
    }

    @Override
    public void write(OutPlayByteBuffer buffer) {
        buffer.writeString(this.text);
        if (buffer.getVersionId() >= V_15W31A) {
            buffer.writeBoolean(this.assumeCommand);
        }
        if (buffer.getVersionId() >= V_14W33A) {
            if (this.position == null) {
                buffer.writeBoolean(false);
            } else {
                buffer.writeBoolean(true);
                buffer.writePosition(this.position);
            }
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending tab complete for message=\"%s\"", this.text.replace("\"", "\\\"")));
    }
}
