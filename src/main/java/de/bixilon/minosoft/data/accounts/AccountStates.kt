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

package de.bixilon.minosoft.data.accounts

import de.bixilon.minosoft.data.language.Translatable
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.KUtil.toResourceLocation

enum class AccountStates : Translatable {
    /**
     * The account might work
     */
    UNCHECKED,

    /**
     * It is just being checked, it might fail
     */
    CHECKING,

    /**
     * The session key is not working anymore, but it can be refreshed
     */
    REFRESHING,

    /**
     * The session key is working
     */
    WORKING,

    /**
     * The account is expired and credentials needs to be provided again
     */
    EXPIRED,

    /**
     * The account is invalid because of some other reason
     */
    ERRORED,

    /**
     * Could not check account state, because we are offline
     */
    OFFLINE,
    ;

    override val translationKey: ResourceLocation = "minosoft:main.account.state.${name.lowercase()}".toResourceLocation()
}
