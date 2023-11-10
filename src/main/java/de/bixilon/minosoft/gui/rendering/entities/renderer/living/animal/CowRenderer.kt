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

import de.bixilon.minosoft.data.entities.entities.animal.Cow
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.factory.RegisteredEntityModelFactory
import de.bixilon.minosoft.gui.rendering.entities.model.animal.AnimalModel
import de.bixilon.minosoft.gui.rendering.models.loader.ModelLoader
import de.bixilon.minosoft.gui.rendering.models.loader.SkeletalLoader.Companion.sModel

open class CowRenderer(renderer: EntitiesRenderer, entity: Cow) : AnimalRenderer<Cow>(renderer, entity) {
    override var model: AnimalModel<*>? = null

    override fun getModel() = when {
        entity.isBaby -> CALF
        else -> COW
    }

    override fun unload() {
        val model = this.model ?: return
        this.model = null
        this.features -= model
    }


    companion object : RegisteredEntityModelFactory<Cow>, Identified {
        override val identifier get() = Cow.identifier
        private val COW = minecraft("entities/cow/cow").sModel()
        private val CALF = minecraft("entities/cow/calf").sModel()

        override fun create(renderer: EntitiesRenderer, entity: Cow) = CowRenderer(renderer, entity)

        override fun register(loader: ModelLoader) {
            loader.skeletal.register(COW)
            loader.skeletal.register(CALF)
        }
    }
}
