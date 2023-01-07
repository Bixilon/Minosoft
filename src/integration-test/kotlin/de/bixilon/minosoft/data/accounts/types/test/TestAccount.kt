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

package de.bixilon.minosoft.data.accounts.types.test

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.accounts.AccountStates
import de.bixilon.minosoft.data.entities.entities.player.properties.PlayerProperties
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import java.util.*

object TestAccount : Account("Bixilon") {
    override val id: String = "id"
    override val type: ResourceLocation = minosoft("test_account")
    override val properties: PlayerProperties? = null
    override val uuid: UUID = "9e6ce7c5-40d3-483e-8e5a-b6350987d65f".toUUID()
    override var state: AccountStates
        get() = AccountStates.WORKING
        set(value) {}
    override val supportsSkins: Boolean get() = true

    override fun join(serverId: String) = Unit

    override fun logout(clientToken: String) = Unit

    override fun check(latch: CountUpAndDownLatch?, clientToken: String) = Unit
}
