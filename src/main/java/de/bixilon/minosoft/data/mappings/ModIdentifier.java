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

package de.bixilon.minosoft.data.mappings;

import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;

import java.util.Objects;

public class ModIdentifier {
    protected final String mod;
    protected final String identifier;
    public final String fullIdentifier;

    public ModIdentifier(String mod, String identifier) {
        this.mod = mod;
        this.identifier = identifier;
        this.fullIdentifier = mod + ":" + identifier;
    }

    public ModIdentifier(String fullIdentifier) {
        String[] split = fullIdentifier.split(":");
        if (split.length == 1) {
            this.mod = ProtocolDefinition.DEFAULT_MOD;
            this.identifier = fullIdentifier;
        } else {
            this.mod = split[0];
            this.identifier = split[1];
        }
        this.fullIdentifier = this.mod + ":" + this.identifier;
    }

    public ModIdentifier(ModIdentifier identifier) {
        this.mod = identifier.getMod();
        this.identifier = identifier.getIdentifier();
        this.fullIdentifier = identifier.getFullIdentifier();
    }

    public static ModIdentifier getIdentifier(String identifier) throws IllegalArgumentException {
        if (!ProtocolDefinition.IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            throw new IllegalArgumentException(String.format("%s in not a valid identifier!", identifier));
        }

        return new ModIdentifier(identifier);

    }

    public String getMod() {
        return this.mod;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getFullIdentifier() {
        return this.fullIdentifier;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.mod, this.identifier);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (hashCode() != obj.hashCode()) {
            return false;
        }
        if (obj instanceof LegacyModIdentifier legacyModIdentifier) {
            return getIdentifier().equals(legacyModIdentifier.getIdentifier());
        }
        ModIdentifier their = (ModIdentifier) obj;
        return getIdentifier().equals(their.getIdentifier()) && getMod().equals(their.getMod());
    }

    @Override
    public String toString() {
        return getFullIdentifier();
    }
}
