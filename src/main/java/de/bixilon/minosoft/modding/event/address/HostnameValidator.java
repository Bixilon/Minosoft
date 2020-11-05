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

import java.util.Arrays;
import java.util.HashSet;

public class HostnameValidator implements ServerAddressValidator {
    private final HashSet<String> hostnames;

    public HostnameValidator(String... hostnames) {
        this(new HashSet<>(Arrays.asList(hostnames)));
    }

    public HostnameValidator(HashSet<String> hostnames) {
        this.hostnames = new HashSet<>();
        hostnames.forEach((hostname) -> this.hostnames.add(hostname.toLowerCase()));
    }

    @Override
    public boolean check(ServerAddress address) {
        return this.hostnames.contains(address.getHostname().toLowerCase());
    }

    public HashSet<String> getHostnames() {
        return hostnames;
    }

    @Override
    public int hashCode() {
        return hostnames.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return hostnames.equals(obj);
    }
}
