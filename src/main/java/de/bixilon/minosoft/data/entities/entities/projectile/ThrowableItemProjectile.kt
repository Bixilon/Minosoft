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
package de.bixilon.minosoft.data.entities.entities.projectile

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.container.ItemStackUtil
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

abstract class ThrowableItemProjectile(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : ThrowableProjectile(session, entityType, data, position, rotation) {

    abstract val defaultItemType: ResourceLocation

    @get:SynchronizedEntityData
    open val item get() = data.get(ITEM_DATA, defaultItem)
    open val defaultItem get() = session.registries.item[defaultItemType]?.let { ItemStackUtil.of(it) }


    companion object {
        private val ITEM_DATA = EntityDataField("THROWABLE_ITEM_PROJECTILE_ITEM")
    }
}
