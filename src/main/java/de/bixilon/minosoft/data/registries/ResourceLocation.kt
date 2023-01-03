/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.minosoft.data.language.translate.Translatable
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import java.util.*

/**
 * A resource location is a string that identifies a resource. It is composed of a namespace and a path, separated by a colon (:).
 * The namespace is optional and defaults to "minecraft". The path is the path to the resource, separated by forward slashes (/).
 *
 * @param namespace The namespace of the resource location
 * @param path The path of the resource location
 *
 * @throws IllegalArgumentException If the namespace or path does not match the allowed pattern. See [ProtocolDefinition.ALLOWED_NAMESPACE_PATTERN] and [ProtocolDefinition.ALLOWED_PATH_PATTERN]
 *
 * @see <a href="https://minecraft.fandom.com/wiki/Resource_location">Resource location</a>
 */
open class ResourceLocation private constructor (
    val namespace: String = ProtocolDefinition.DEFAULT_NAMESPACE,
    val path: String
) : Comparable<ResourceLocation>, Translatable { // compare is for moshi
    private val hashCode = Objects.hash(namespace, path)

    override val translationKey: ResourceLocation
        get() = this

    init {
        if (!ProtocolDefinition.ALLOWED_NAMESPACE_PATTERN.matches(namespace))
            throw IllegalArgumentException("Namespace '$namespace' is not allowed!")
        if (!ProtocolDefinition.ALLOWED_PATH_PATTERN.matches(path))
            throw IllegalArgumentException("Path '$path' is not allowed!")
    }

    /**
     * Adds a prefix to the path of this resource location.
     *
     * @param prefix The prefix to add
     */
    fun prefix(prefix: String): ResourceLocation {
        return ResourceLocation(namespace, prefix + path)
    }

    /**
     * @return If the namespace is "minecraft", the path is returned. Otherwise, the full string is returned.
     */
    fun toMinifiedString(): String {
        return  if (namespace == ProtocolDefinition.DEFAULT_NAMESPACE) path
                else toString()
    }

    override fun toString(): String {
        return "$namespace:$path"
    }

    override fun hashCode(): Int {
        return hashCode
    }

    override fun compareTo(other: ResourceLocation): Int {
        return hashCode() - other.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (hashCode() != other.hashCode()) {
            return false
        }
        if (other !is ResourceLocation) {
            return false
        }
        return path == other.path && namespace == other.namespace
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

        fun of(resourceLocation: String): ResourceLocation {
            return ResourceLocation(resourceLocation.namespace, resourceLocation.path)
        }

        fun of(namespace: String, path: String): ResourceLocation {
            return ResourceLocation(namespace, path)
        }

        fun ofPath(path: String): ResourceLocation {
            if (path.contains(':') || !path.contains('/')) return of(path)
            val split = path.split('/', limit = 2)
            return ResourceLocation(split[0], split[1])
        }
    }

}
