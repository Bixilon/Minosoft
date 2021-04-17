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

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W13A;

public class UpdateJigsawBlockC2SPacket implements PlayC2SPacket {
    private final Vec3i position;
    private final String targetPool;
    private final String finalState;
    String attachmentType;
    String name;
    String target;
    String jointType;

    public UpdateJigsawBlockC2SPacket(Vec3i position, String attachmentType, String targetPool, String finalState) {
        this.position = position;
        this.attachmentType = attachmentType;
        this.targetPool = targetPool;
        this.finalState = finalState;
    }

    public UpdateJigsawBlockC2SPacket(Vec3i position, String name, String target, String targetPool, String finalState, String jointType) {
        this.position = position;
        this.name = name;
        this.target = target;
        this.targetPool = targetPool;
        this.finalState = finalState;
        this.jointType = jointType;
    }

    @Override
    public void write(PlayOutByteBuffer buffer) {
        buffer.writePosition(this.position);
        if (buffer.getVersionId() < V_20W13A) {
            buffer.writeString(this.attachmentType);
            buffer.writeString(this.targetPool);
            buffer.writeString(this.finalState);
        } else {
            buffer.writeString(this.name);
            buffer.writeString(this.target);
            buffer.writeString(this.targetPool);
            buffer.writeString(this.finalState);
            buffer.writeString(this.jointType);
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Updating jigsaw block (position=%s, attachmentType=%s, targetPool=%s, finalState=%s)", this.position, this.attachmentType, this.targetPool, this.finalState));
    }
}
