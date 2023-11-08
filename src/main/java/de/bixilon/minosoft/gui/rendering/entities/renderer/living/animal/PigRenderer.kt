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

package de.bixilon.minosoft.gui.rendering.entities.renderer.living.animal

import de.bixilon.minosoft.data.entities.entities.AgeableMob
import de.bixilon.minosoft.data.entities.entities.animal.Pig
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.factory.RegisteredEntityModelFactory
import de.bixilon.minosoft.gui.rendering.entities.model.animal.AnimalModel
import de.bixilon.minosoft.gui.rendering.models.loader.ModelLoader
import de.bixilon.minosoft.gui.rendering.models.loader.SkeletalLoader.Companion.sModel

class PigRenderer(renderer: EntitiesRenderer, entity: Pig) : AnimalRenderer<Pig>(renderer, entity) {
    override var model: AnimalModel<PigRenderer>? = null


    init {
        entity.data.observe<Boolean>(AgeableMob.BABY) { unload() }
        entity.data.observe<Boolean>(Pig.SADDLED) { unload() }
    }

    override fun update(millis: Long, delta: Float) {
        if (model == null) {
            updateModel()
        }
        super.update(millis, delta)
    }


    private fun updateModel() {
        val type = when {
            entity.isBaby -> PIGLET
            entity.isSaddled -> SADDLED
            else -> PIG
        }
        val model = renderer.context.models.skeletal[type]?.let { AnimalModel(this, it) } ?: return

        this.model = model
        model.register()
    }

    override fun unload() {
        val model = this.model ?: return
        this.model = null
        this.features -= model
    }


    companion object : RegisteredEntityModelFactory<Pig>, Identified {
        override val identifier get() = Pig.identifier
        private val PIG = minecraft("entities/pig/pig").sModel()
        private val SADDLED = minecraft("entities/pig/saddled").sModel()
        private val PIGLET = minecraft("entities/pig/piglet").sModel()

        override fun create(renderer: EntitiesRenderer, entity: Pig) = PigRenderer(renderer, entity)

        override fun register(loader: ModelLoader) {
            loader.skeletal.register(PIG)
            loader.skeletal.register(SADDLED)
            loader.skeletal.register(PIGLET)
        }
    }
}
