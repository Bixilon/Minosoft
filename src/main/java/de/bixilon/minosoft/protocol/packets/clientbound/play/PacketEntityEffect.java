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

import de.bixilon.minosoft.data.entities.StatusEffectInstance;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.BitByte;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketEntityEffect extends ClientboundPacket {
    private final int entityId;
    private final StatusEffectInstance effect;
    private boolean isAmbient;
    private boolean hideParticles;
    private boolean showIcon = true;

    public PacketEntityEffect(InByteBuffer buffer) {
        this.entityId = buffer.readEntityId();
        if (buffer.getVersionId() < V_14W04A) {
            this.effect = new StatusEffectInstance(buffer.getConnection().getMapping().getStatusEffectRegistry().get(buffer.readByte()), buffer.readByte() + 1, buffer.readShort());
            return;
        }
        this.effect = new StatusEffectInstance(buffer.getConnection().getMapping().getStatusEffectRegistry().get(buffer.readByte()), buffer.readByte() + 1, buffer.readVarInt());
        if (buffer.getVersionId() < V_1_9_4) { // ToDo
            if (buffer.getVersionId() >= V_14W06B) {
                this.hideParticles = buffer.readBoolean();
                return;
            }
        }
        byte flags = buffer.readByte();
        this.isAmbient = BitByte.isBitMask(flags, 0x01);
        this.hideParticles = !BitByte.isBitMask(flags, 0x02);
        if (buffer.getVersionId() >= V_1_14_4) { // ToDo
            this.showIcon = BitByte.isBitMask(flags, 0x04);
        }
    }

    @Override
    public void handle(Connection connection) {
        Entity entity = connection.getWorld().getEntity(getEntityId());
        if (entity == null) {
            // thanks mojang
            return;
        }
        entity.addEffect(getEffect());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Entity effect added: %d %s", this.entityId, this.effect.toString()));
    }

    public int getEntityId() {
        return this.entityId;
    }

    public StatusEffectInstance getEffect() {
        return this.effect;
    }

    public boolean hideParticles() {
        return this.hideParticles;
    }

    public boolean showIcon() {
        return this.showIcon;
    }

    public boolean isAmbient() {
        return this.isAmbient;
    }
}
