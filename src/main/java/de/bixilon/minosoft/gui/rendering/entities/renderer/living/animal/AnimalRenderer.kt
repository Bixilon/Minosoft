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

package de.bixilon.minosoft.gui.rendering.entities.renderer.living.animal

import de.bixilon.kutil.random.RandomUtil.nextFloat
import de.bixilon.minosoft.data.entities.entities.AgeableMob
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.model.animal.AnimalModelFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.LivingEntityRenderer
import kotlin.random.Random
import kotlin.random.asJavaRandom
import kotlin.time.Duration
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

abstract class AnimalRenderer<E : AgeableMob>(renderer: EntitiesRenderer, entity: E) : LivingEntityRenderer<E>(renderer, entity) {
    protected open var model: AnimalModelFeature<*>? = null
    val scale = if (renderer.profile.animal.randomScale) Random.asJavaRandom().nextFloat(0.9f, 1.1f) else 1.0f
    protected var unloadModel = false

    init {
        entity.data.observe<Boolean>(AgeableMob.BABY) { unloadModel = true }
    }

    override fun enqueueUnload() {
        super.enqueueUnload()
        if (unloadModel) {
            val model = this.model ?: return
            this.model = null
            features -= model
            renderer.queue += { model.unload() }
            this.unloadModel = false
        }
    }

    override fun update(time: ValueTimeMark, delta: Duration) {
        super.update(time, delta)
        if (model == null) {
            this.model = createModel()
            model?.register()
        }

    }

    protected abstract fun getModel(): ResourceLocation?

    protected open fun createModel(): AnimalModelFeature<AnimalRenderer<E>>? {
        val type = getModel() ?: return null
        val skeletal = renderer.context.models.skeletal[type] ?: return null
        return AnimalModelFeature(this, skeletal)
    }

    override fun unload() {
        super.unload()
        this.model = null
    }
}
