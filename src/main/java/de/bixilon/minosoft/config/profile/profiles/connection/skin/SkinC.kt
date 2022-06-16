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

package de.bixilon.minosoft.config.profile.profiles.connection.skin

import com.fasterxml.jackson.annotation.JsonIgnore
import de.bixilon.minosoft.config.profile.profiles.connection.ConnectionProfileManager.delegate
import de.bixilon.minosoft.data.entities.entities.player.SkinParts

class SkinC {
    /**
     * The cape of use will be hidden
     * Will be sent to the server
     */
    var cape by delegate(true)

    /**
     * The jacket of use will be hidden
     * Will be sent to the server
     */
    var jacket by delegate(true)

    /**
     * The left sleeve of use will be hidden
     * Will be sent to the server
     */
    var leftSleeve by delegate(true)

    /**
     * The right sleeve of use will be hidden
     * Will be sent to the server
     */
    var rightSleeve by delegate(true)

    /**
     * The left pants of use will be hidden
     * Will be sent to the server
     */
    var leftPants by delegate(true)

    /**
     * The right pants of use will be hidden
     * Will be sent to the server
     */
    var rightPants by delegate(true)

    /**
     * The hat of use will be hidden
     * Will be sent to the server
     */
    var hat by delegate(true)


    @get:JsonIgnore val skinParts: Array<SkinParts>
        get() {
            val parts: MutableSet<SkinParts> = mutableSetOf()
            if (cape) {
                parts += SkinParts.CAPE
            }
            if (jacket) {
                parts += SkinParts.JACKET
            }
            if (leftSleeve) {
                parts += SkinParts.LEFT_SLEEVE
            }
            if (rightSleeve) {
                parts += SkinParts.RIGHT_SLEEVE
            }
            if (leftPants) {
                parts += SkinParts.LEFT_PANTS
            }
            if (rightPants) {
                parts += SkinParts.RIGHT_PANTS
            }
            if (hat) {
                parts += SkinParts.HAT
            }
            return parts.toTypedArray()
        }
}
