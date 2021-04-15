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

import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket;
import de.bixilon.minosoft.protocol.protocol.OutPlayByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3i;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class UpdateSignC2SPacket implements PlayC2SPacket {
    private final Vec3i position;
    private final ChatComponent[] lines;

    public UpdateSignC2SPacket(Vec3i position, ChatComponent[] lines) {
        this.position = position;
        this.lines = lines;
    }

    @Override
    public void write(OutPlayByteBuffer buffer) {
        if (buffer.getVersionId() < V_14W04A) {
            buffer.writeVec3iByte(this.position);
        } else {
            buffer.writePosition(this.position);
        }
        if (buffer.getVersionId() < V_14W25A || buffer.getVersionId() >= V_15W35A) {
            for (int i = 0; i < ProtocolDefinition.SIGN_LINES; i++) {
                buffer.writeString(this.lines[i].getMessage());
            }
        } else {
            for (int i = 0; i < ProtocolDefinition.SIGN_LINES; i++) {
                buffer.writeChatComponent(this.lines[i]);
            }
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending Sign Update: %s", this.position));
    }
}
