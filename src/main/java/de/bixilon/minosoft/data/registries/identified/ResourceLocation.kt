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
package de.bixilon.minosoft.data.registries.identified

import de.bixilon.minosoft.data.language.translate.Translatable
import de.bixilon.minosoft.data.registries.identified.ResourceLocation.Companion.ALLOWED_PATH_PATTERN
import java.util.*

/**
 * A resource location is a string that identifies a resource. It is composed of a namespace and a path, separated by a colon (:).
 * The namespace is optional and defaults to "minecraft". The path is the path to the resource, separated by forward slashes (/).
 * If possible, use minecraft() or minosoft() instead.
 *
 * @param namespace The namespace of the resource location
 * @param path The path of the resource location
 *
 * @throws IllegalArgumentException If the namespace or path does not match the allowed pattern. and [ALLOWED_PATH_PATTERN]
 *
 * @see <a href="https://minecraft.wiki/w/Resource_location">Resource location</a>
 */
open class ResourceLocation(
    val namespace: String = Namespaces.DEFAULT,
    val path: String,
) : Translatable {
    private val hashCode = Objects.hash(namespace, path)

    override val translationKey: ResourceLocation
        get() = this

    init {
        ResourceLocationUtil.validateNamespace(namespace)
        // TODO: Figure out a way to implement this but have backwards compatibility with pre flattening versions
        // if (!ProtocolDefinition.ALLOWED_PATH_PATTERN.matches(path) && path != "")
        //    throw IllegalArgumentException("Path '$path' is not allowed!")
    }

    fun prefix(prefix: String): ResourceLocation {
        if (path.startsWith(prefix)) return this
        return ResourceLocation(namespace, prefix + path)
    }

    fun suffix(suffix: String): ResourceLocation {
        if (path.endsWith(suffix)) return this
        return ResourceLocation(namespace, path + suffix)
    }

    /**
     * @return If the namespace is "minecraft", the path is returned. Otherwise, the full string is returned.
     */
    fun toMinifiedString(): String {
        if (namespace == Namespaces.DEFAULT) {
            return path
        }
        return toString()
    }

    override fun toString(): String {
        return "$namespace:$path"
    }

    override fun hashCode(): Int {
        return hashCode
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Identified) return this == other.identifier
        if (other !is ResourceLocation) return false
        if (hashCode() != other.hashCode()) return false

        return path == other.path && namespace == other.namespace
    }

    companion object {
        val ALLOWED_PATH_PATTERN = Regex("(?!.*//)[a-z0-9_./\\-]+")

        /**
         * Creates a resource location from a string.
         * If possible, use minecraft() or minosoft() instead.
         *
         * @param string The string to parse
         * @return The parsed resource location
         */
        fun of(string: String): ResourceLocation {
            val split = string.split(':', limit = 2)
            if (split.size == 1) {
                return ResourceLocation(Namespaces.DEFAULT, string)
            }
            return ResourceLocation(split[0], split[1])
        }

        /**
         * Creates a resource location from a string by splitting it at the first colon (:) or at the first slash (/) if there is no colon.
         * If possible, use minecraft() or minosoft() instead.
         *
         * @param path The path to parse
         * @return The parsed resource location
         */
        @Deprecated("Don't know why this was a thing")
        fun ofPath(path: String): ResourceLocation {
            if (path.contains(':') || !path.contains('/')) {
                return of(path)
            }
            val split = path.split('/', limit = 2)
            return ResourceLocation(split[0], split[1])
        }
    }
}
