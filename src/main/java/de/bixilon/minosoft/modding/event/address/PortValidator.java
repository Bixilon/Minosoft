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

package de.bixilon.minosoft.modding.event.address;

import de.bixilon.minosoft.util.ServerAddress;

import java.util.HashSet;

public class PortValidator implements ServerAddressValidator {
    private final HashSet<Integer> ports;

    public PortValidator(int port) {
        this.ports = new HashSet<>();
        ports.add(port);
    }

    public PortValidator(HashSet<Integer> ports) {
        this.ports = ports;
    }

    @Override
    public boolean check(ServerAddress address) {
        return ports.contains(address.getPort());
    }

    @Override
    public int hashCode() {
        return ports.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (hashCode() != obj.hashCode()) {
            return false;
        }
        PortValidator their = (PortValidator) obj;
        return ports.equals(their.getPorts());
    }

    public HashSet<Integer> getPorts() {
        return ports;
    }
}
