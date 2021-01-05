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

import de.bixilon.minosoft.data.mappings.ModIdentifier;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class PacketSculkVibrationSignal extends ClientboundPacket {
    private BlockPosition vibrationSourcePosition;
    private ModIdentifier vibrationTargetType;
    private Object vibrationTargetData;
    private int arrivalTicks;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.vibrationSourcePosition = buffer.readPosition();
        this.vibrationTargetType = buffer.readIdentifier();
        this.vibrationTargetData = switch (this.vibrationTargetType.getFullIdentifier()) {
            case "minecraft:block" -> buffer.readPosition(); // sculk source position
            case "minecraft:entity" -> buffer.readEntityId();
            default -> throw new IllegalArgumentException("Unexpected value: " + this.vibrationTargetType.getFullIdentifier());
        };
        this.arrivalTicks = buffer.readVarInt();
        return true;
    }

    public BlockPosition getVibrationSourcePosition() {
        return this.vibrationSourcePosition;
    }

    public ModIdentifier getVibrationTargetType() {
        return this.vibrationTargetType;
    }

    /**
     * @return Depends on vibration target type, if block: block postion, if entity: entity id
     */
    public Object getVibrationTargetData() {
        return this.vibrationTargetData;
    }

    public BlockPosition getVibrationTargetPosition() {
        return (BlockPosition) this.vibrationTargetData;
    }

    public int getVibrationTargetEntityId() {
        return (int) this.vibrationTargetData;
    }

    public int getArrivalTicks() {
        return this.arrivalTicks;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Sculk Vibration (position=%s, identifier=%s, additionalData=%s, arrivalTicks=%d)", this.vibrationSourcePosition, this.vibrationTargetType, this.vibrationTargetData, this.arrivalTicks));
    }
}
