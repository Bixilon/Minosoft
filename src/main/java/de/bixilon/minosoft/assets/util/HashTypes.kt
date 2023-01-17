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

package de.bixilon.minosoft.assets.util

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import java.security.MessageDigest

enum class HashTypes(
    val digestName: String,
    val length: Int,
) {
    SHA1("SHA-1", 40),
    SHA256("SHA-256", 64),
    ;

    fun createDigest(): MessageDigest {
        return MessageDigest.getInstance(digestName)
    }

    companion object : ValuesEnum<HashTypes> {
        override val VALUES: Array<HashTypes> = values()
        override val NAME_MAP: Map<String, HashTypes> = EnumUtil.getEnumValues(VALUES)

        val String.hashType: HashTypes
            get() {
                for (type in VALUES) {
                    if (this.length == type.length) {
                        return type
                    }
                }
                throw IllegalArgumentException("Can not determinate hash type: $this")
            }
    }
}
