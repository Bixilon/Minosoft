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

package de.bixilon.minosoft.gui.rendering.entities.renderer.item

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.entities.entities.item.FallingBlockEntity
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.factory.RegisteredEntityModelFactory
import de.bixilon.minosoft.gui.rendering.entities.feature.block.BlockFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer

class FallingBlockEntityRenderer(renderer: EntitiesRenderer, entity: FallingBlockEntity) : EntityRenderer<FallingBlockEntity>(renderer, entity) {
    val block = BlockFeature(this, null).register()

    init {
        entity::blockState.observe(this, true) { block.state = it }
    }


    companion object : RegisteredEntityModelFactory<FallingBlockEntity>, Identified {
        override val identifier get() = FallingBlockEntity.identifier

        override fun create(renderer: EntitiesRenderer, entity: FallingBlockEntity) = FallingBlockEntityRenderer(renderer, entity)
    }
}
