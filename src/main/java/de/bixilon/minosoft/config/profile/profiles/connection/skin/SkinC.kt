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

package de.bixilon.minosoft.config.profile.profiles.connection.skin

import com.fasterxml.jackson.annotation.JsonIgnore
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.set.SetObserver.Companion.observedSet
import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.profiles.connection.ConnectionProfile
import de.bixilon.minosoft.data.entities.entities.player.SkinParts

class SkinC(profile: ConnectionProfile) {
    /**
     * The cape of use will be hidden
     * Will be sent to the server
     */
    var cape by BooleanDelegate(profile, true)

    /**
     * The jacket of use will be hidden
     * Will be sent to the server
     */
    var jacket by BooleanDelegate(profile, true)

    /**
     * The left sleeve of use will be hidden
     * Will be sent to the server
     */
    var leftSleeve by BooleanDelegate(profile, true)

    /**
     * The right sleeve of use will be hidden
     * Will be sent to the server
     */
    var rightSleeve by BooleanDelegate(profile, true)

    /**
     * The left pants of use will be hidden
     * Will be sent to the server
     */
    var leftPants by BooleanDelegate(profile, true)

    /**
     * The right pants of use will be hidden
     * Will be sent to the server
     */
    var rightPants by BooleanDelegate(profile, true)

    /**
     * The hat of use will be hidden
     * Will be sent to the server
     */
    var hat by BooleanDelegate(profile, true)


    @get:JsonIgnore val parts: MutableSet<SkinParts> by observedSet(mutableSetOf())

    private fun updateParts(part: SkinParts, add: Boolean) {
        if (add) {
            parts += part
        } else {
            parts -= part
        }
    }


    init {
        this::cape.observe(this, true) { updateParts(SkinParts.CAPE, it) }
        this::jacket.observe(this, true) { updateParts(SkinParts.JACKET, it) }
        this::leftSleeve.observe(this, true) { updateParts(SkinParts.LEFT_SLEEVE, it) }
        this::rightSleeve.observe(this, true) { updateParts(SkinParts.RIGHT_SLEEVE, it) }
        this::leftPants.observe(this, true) { updateParts(SkinParts.LEFT_PANTS, it) }
        this::rightPants.observe(this, true) { updateParts(SkinParts.RIGHT_PANTS, it) }
        this::hat.observe(this, true) { updateParts(SkinParts.HAT, it) }
    }
}
