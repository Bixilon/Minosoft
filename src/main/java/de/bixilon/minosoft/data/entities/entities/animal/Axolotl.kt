/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.entities.animal

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import glm_.vec3.Vec3d

class Axolotl(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : Animal(connection, entityType, position, rotation) {

    @get:EntityMetaDataFunction(name = "Axolotl variant")
    val variant: AxolotlVariants
        get() = AxolotlVariants.byId(entityMetaData.sets.getInt(EntityMetaDataFields.AXOLOTL_VARIANT))

    @get:EntityMetaDataFunction(name = "Is playing dead")
    val isPlayingDead: Boolean
        get() = entityMetaData.sets.getBoolean(EntityMetaDataFields.AXOLOTL_PLAYING_DEAD)

    @get:EntityMetaDataFunction(name = "Is from bucket")
    val isFromBucket: Boolean
        get() = entityMetaData.sets.getBoolean(EntityMetaDataFields.AXOLOTL_FROM_BUCKET)

    enum class AxolotlVariants {
        LUCY,
        WILD,
        GOLD,
        CYAN,
        BLUE,
        ;

        companion object {
            private val AXOLOTL_VARIANTS = values()
            fun byId(id: Int): AxolotlVariants {
                return AXOLOTL_VARIANTS[id]
            }
        }
    }

    companion object : EntityFactory<Axolotl> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("axolotl")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): Axolotl {
            return Axolotl(connection, entityType, position, rotation)
        }
    }
}
