/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.registries

import de.bixilon.kutil.string.StringUtil.isLowercase
import de.bixilon.minosoft.data.language.Translatable
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import java.util.*

open class ResourceLocation(
    val namespace: String = ProtocolDefinition.DEFAULT_NAMESPACE,
    val path: String,
) : Comparable<ResourceLocation>, Translatable { // compare is for moshi
    private val hashCode = Objects.hash(namespace, path)
    open val full: String = "$namespace:$path"

    override val translationKey: ResourceLocation
        get() = this

    constructor(full: String) : this(full.namespace, full.path)


    override fun hashCode(): Int {
        return hashCode
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (hashCode() != other.hashCode()) {
            return false
        }
        if (other is LegacyResourceLocation) {
            return path == other.path
        }
        if (other !is ResourceLocation) {
            return false
        }
        return path == other.path && namespace == other.namespace
    }

    override fun toString(): String {
        return full
    }

    fun toMinifiedString(): String {
        if (namespace == ProtocolDefinition.DEFAULT_NAMESPACE) {
            return path
        }
        return toString()
    }

    companion object {
        val String.namespace: String
            get() {
                val split = this.split(':', limit = 2)
                if (split.size == 1) {
                    return ProtocolDefinition.DEFAULT_NAMESPACE
                }
                return split[0]
            }

        val String.path: String
            get() {
                val split = this.split(':', limit = 2)
                if (split.size == 1) {
                    return split[0]
                }
                return split[1]
            }

        fun getResourceLocation(resourceLocation: String): ResourceLocation {
            // if (!ProtocolDefinition.RESOURCE_LOCATION_PATTERN.matcher(resourceLocation).matches()) {
            //     throw new IllegalArgumentException(String.format("%s in not a valid resource location!", resourceLocation));
            // }
            return if (!resourceLocation.isLowercase()) {
                // just a string but wrapped into a resourceLocation (like old plugin channels MC|BRAND or ...)
                LegacyResourceLocation(resourceLocation)
            } else ResourceLocation(resourceLocation)
        }

        fun getPathResourceLocation(resourceLocation: String): ResourceLocation {
            if (resourceLocation.contains(":")) {
                return ResourceLocation(resourceLocation)
            }
            val split = resourceLocation.split("/".toRegex(), 2).toTypedArray()
            return ResourceLocation(split[0], split[1])
        }
    }

    fun prefix(prefix: String): ResourceLocation {
        return ResourceLocation(namespace, prefix + path)
    }

    override fun compareTo(other: ResourceLocation): Int {
        return hashCode() - other.hashCode()
    }
}
