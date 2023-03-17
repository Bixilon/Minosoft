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

package de.bixilon.minosoft.physics.entities.vehicle.boat

import de.bixilon.minosoft.data.entities.entities.vehicle.boat.Boat
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.physics.entities.EntityPhysics
import de.bixilon.minosoft.physics.properties.SwimmingVehicle

class BoatPhysics(entity: Boat) : EntityPhysics<Boat>(entity), SwimmingVehicle {
    @Deprecated("TODO")
    val location = Boat.BoatLocations.WATER

    override val inWater: Boolean
        get() = location == Boat.BoatLocations.SUBMERGED || location == Boat.BoatLocations.SUBMERGED_FLOWING

    override fun canUpdatePassengerFluidMovement(fluid: Identified): Boolean {
        return inWater
    }
}
