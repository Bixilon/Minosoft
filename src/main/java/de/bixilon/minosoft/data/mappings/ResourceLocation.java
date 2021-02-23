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
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ResourceLocation implements Comparable<ResourceLocation> {
    public final String full;
    protected final String namespace;
    protected final String path;

    public ResourceLocation(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
        this.full = namespace + ":" + path;
    }

    public ResourceLocation(String full) {
        String[] split = full.split(":");
        if (split.length == 1) {
            this.namespace = ProtocolDefinition.DEFAULT_MOD;
            this.path = full;
        } else {
            this.namespace = split[0];
            this.path = split[1];
        }
        this.full = this.namespace + ":" + this.path;
    }

    public ResourceLocation(ResourceLocation path) {
        this.namespace = path.getNamespace();
        this.path = path.getPath();
        this.full = path.getFull();
    }

    public static ResourceLocation getResourceLocation(String resourceLocation) throws IllegalArgumentException {
        if (!ProtocolDefinition.RESOURCE_LOCATION_PATTERN.matcher(resourceLocation).matches()) {
            throw new IllegalArgumentException(String.format("%s in not a valid resource locaion!", resourceLocation));
        }

        return new ResourceLocation(resourceLocation);

    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getPath() {
        return this.path;
    }

    public String getFull() {
        return this.full;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.namespace, this.path);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (hashCode() != obj.hashCode()) {
            return false;
        }
        if (obj instanceof LegacyResourceLocation legacyModIdentifier) {
            return getPath().equals(legacyModIdentifier.getPath());
        }
        ResourceLocation their = (ResourceLocation) obj;
        return getPath().equals(their.getPath()) && getNamespace().equals(their.getNamespace());
    }

    @Override
    public String toString() {
        return getFull();
    }

    @Override
    public int compareTo(@NotNull ResourceLocation resourceLocation) {
        return resourceLocation.hashCode() - this.hashCode();
    }
}
