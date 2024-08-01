/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.models.item

import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

object ItemRenderUtil {

    @Deprecated("please let this be the last fucking hack in this game") // TODO
    fun Item.getModel(session: PlaySession): ItemRender? {
        val block = session.registries.block[identifier]
        return block?.model ?: block?.states?.default?.model ?: model
    }
}
