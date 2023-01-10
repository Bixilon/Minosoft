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

import de.bixilon.minosoft.config.StaticConfiguration

object ResourceLocationUtil {

    fun validateNamespace(namespace: String) {
        if (!StaticConfiguration.VALIDATE_RESOURCE_LOCATION) {
            return
        }
        if (namespace.isEmpty()) {
            throw IllegalArgumentException("Namespace is blank!")
        }
        for (code in namespace.codePoints()) {
            if (
                code in 'a'.code..'z'.code ||
                code in '0'.code..'9'.code ||
                code == '_'.code ||
                code == '-'.code ||
                code == '.'.code
            ) {
                continue
            }

            throw IllegalArgumentException("Namespace is illegal: $namespace")
        }
    }
}
