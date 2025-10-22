/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.container.types.processing.smelting

import de.bixilon.minosoft.data.registries.containers.ContainerFactory
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class BlastFurnaceContainer(session: PlaySession, type: ContainerType, title: ChatComponent?, id: Int) : SmeltingContainer(session, type, title, id) {

    companion object : ContainerFactory<BlastFurnaceContainer> {
        override val identifier: ResourceLocation = "minecraft:blast_furnace".toResourceLocation()

        override fun build(session: PlaySession, type: ContainerType, title: ChatComponent?, slots: Int, id: Int): BlastFurnaceContainer {
            return BlastFurnaceContainer(session, type, title, id)
        }
    }
}
