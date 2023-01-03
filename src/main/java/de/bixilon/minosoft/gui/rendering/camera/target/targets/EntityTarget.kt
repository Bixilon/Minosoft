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

package de.bixilon.minosoft.gui.rendering.camera.target.targets

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.string.StringUtil.toSnakeCase
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.formatting.TextFormattable
import java.util.*

class EntityTarget(
    position: Vec3d,
    distance: Double,
    direction: Directions,
    val entity: Entity,
) : GenericTarget(position, distance, direction), TextFormattable {

    override fun toString(): String {
        return toText().legacyText
    }

    override fun toText(): ChatComponent {
        val text = BaseComponent()

        text += "Entity target "
        text += entity.position
        text += ": "
        text += entity.type.identifier

        text += "\n"
        text += "Id: ${entity.id}"
        text += "\n"
        text += "UUID: ${entity.uuid}"


        val data = entity.entityDataFormatted
        if (data.isNotEmpty()) {
            text += "\n"
        }

        for ((property, value) in data) {
            text += "\n"
            text += property
            text += ": "
            text += value
        }
        return text
    }

    val Entity.entityDataFormatted: TreeMap<String, Any>
        get() {
            // scan all methods of current class for SynchronizedEntityData annotation and write it into a list
            val values = TreeMap<String, Any>()
            var clazz: Class<*> = this.javaClass
            while (clazz != Any::class.java) {
                for (method in clazz.declaredMethods) {
                    if (!method.isAnnotationPresent(SynchronizedEntityData::class.java)) {
                        continue
                    }
                    if (method.parameterCount > 0) {
                        continue
                    }
                    method.isAccessible = true
                    val name = method.name.toSnakeCase().removePrefix("get_").removePrefix("is_").removePrefix("has_")
                    if (values.containsKey(name)) {
                        continue
                    }
                    values[name] = method(this) ?: continue
                }
                clazz = clazz.superclass
            }
            return values
        }


    override fun hashCode(): Int {
        return Objects.hash(position, entity, distance)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is EntityTarget) return false
        return distance == other.distance && position == other.position && entity === other.entity
    }
}
