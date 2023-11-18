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

package de.bixilon.minosoft.gui.rendering.entities.renderer.living

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.math.interpolation.Interpolator
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.entities.event.events.damage.DamageEvent
import de.bixilon.minosoft.data.entities.event.events.damage.DamageListener
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.ColorUtil
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateDegreesAssign

abstract class LivingEntityRenderer<E : LivingEntity>(renderer: EntitiesRenderer, entity: E) : EntityRenderer<E>(renderer, entity), DamageListener {
    val damage = Interpolator(ChatColors.WHITE, ColorUtil::interpolateRGB) // TODO delta^2 or no interpolation at all?

    override fun updateMatrix(delta: Float) {
        super.updateMatrix(delta)
        when (entity.pose) {
            Poses.SLEEPING -> matrix.rotateDegreesAssign(Vec3(90, 0, 0)) // TODO
            else -> Unit
        }
    }

    override fun update(millis: Long, delta: Float) {
        if (damage.delta >= 1.0f) {
            damage.push(ChatColors.WHITE)
        }
        damage.add(delta, 0.1f)
        super.update(millis, delta)
    }

    override fun onDamage(type: DamageEvent) {
        damage.push(ChatColors.RED)
    }
}
