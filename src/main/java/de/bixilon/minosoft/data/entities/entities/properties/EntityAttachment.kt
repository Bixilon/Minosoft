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

package de.bixilon.minosoft.data.entities.entities.properties

import de.bixilon.kutil.collections.CollectionUtil.synchronizedSetOf
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.kutil.observer.set.SetObserver.Companion.observedSet
import de.bixilon.minosoft.data.entities.entities.Entity

class EntityAttachment(
    private val entity: Entity,
) {
    val passengers: MutableSet<Entity> by observedSet(synchronizedSetOf())

    private var _vehicle: Entity? = null // previous vehicle (used for detaching entities)
    var vehicle: Entity? by observed(null)

    var attached: Entity? by observed(null) // TODO: Not changeable for e.g. FireworkRocketEntity


    init {
        this::vehicle.observe(this) {
            _vehicle?.attachment?.passengers?.remove(entity)
            it?.attachment?.passengers?.add(entity)
            _vehicle = it
        }
    }

    fun getRootVehicle(): Entity? {
        var vehicle = this.vehicle ?: return null
        while (true) {
            vehicle = vehicle.attachment.vehicle ?: break
        }
        return vehicle
    }
}
