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

package de.bixilon.minosoft.data.entities.entities.properties.riding

import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.EntityAttachable
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity

interface ItemRideable : ItemSteerable, Saddleable, EntityAttachable {

    val primaryPassenger: Entity?
        get() {
            val passenger = attachment.passengers.firstOrNull() ?: return null

            if (!isSaddled) {
                return null
            }
            if (passenger !is PlayerEntity) {
                return null
            }
            if (!canEntitySteer(passenger)) {
                return null
            }

            return passenger
        }

}
