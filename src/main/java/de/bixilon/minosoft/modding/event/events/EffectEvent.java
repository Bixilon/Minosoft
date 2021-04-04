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

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketEffect;
import glm_.vec3.Vec3i;

public class EffectEvent extends CancelableEvent {
    private final PacketEffect.EffectEffects effect;
    private final Vec3i position;
    private final int data;
    private final boolean disableRelativeVolume;

    public EffectEvent(PlayConnection connection, PacketEffect.EffectEffects effect, Vec3i position, int data, boolean disableRelativeVolume) {
        super(connection);
        this.effect = effect;
        this.position = position;
        this.data = data;
        this.disableRelativeVolume = disableRelativeVolume;
    }

    public EffectEvent(PlayConnection connection, PacketEffect pkg) {
        super(connection);
        this.effect = pkg.getEffect();
        this.position = pkg.getPosition();
        this.data = pkg.getData();
        this.disableRelativeVolume = pkg.isDisableRelativeVolume();
    }

    public PacketEffect.EffectEffects getEffect() {
        return this.effect;
    }

    public Vec3i getPosition() {
        return this.position;
    }

    public int getData() {
        return this.data;
    }

    public boolean isDisableRelativeVolume() {
        return this.disableRelativeVolume;
    }
}
