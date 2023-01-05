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
package de.bixilon.minosoft.data.entities.entities.animal

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.entities.entities.TamableAnimal
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.entities.variants.CatVariant
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil

class Cat(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : TamableAnimal(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val variant: CatVariant?
        get() {
            val variant: Any? = data.get(VARIANT_DATA, null)
            if (variant is CatVariant) {
                return variant
            }
            if (variant is Int) {
                return connection.registries.catVariants[variant]
            }
            return null
        }

    @get:SynchronizedEntityData
    val isLying: Boolean
        get() = data.getBoolean(IS_LYING_DATA, false)

    @get:SynchronizedEntityData
    val isRelaxed: Boolean
        get() = data.getBoolean(IS_RELAXED_DATA, false)

    @get:SynchronizedEntityData
    val collarColor: RGBColor
        get() = ChatColors.VALUES.getOrNull(data.get(COLLAR_COLOR_DATA, 0x0C)) ?: ChatColors.RED

    companion object : EntityFactory<Cat> {
        override val identifier: ResourceLocation = minecraft("cat")
        private val VARIANT_DATA = EntityDataField("CAT_VARIANT")
        private val IS_LYING_DATA = EntityDataField("CAT_IS_LYING")
        private val IS_RELAXED_DATA = EntityDataField("CAT_IS_RELAXED")
        private val COLLAR_COLOR_DATA = EntityDataField("CAT_GET_COLLAR_COLOR")

        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Cat {
            return Cat(connection, entityType, data, position, rotation)
        }
    }
}
