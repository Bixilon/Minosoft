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

import de.bixilon.minosoft.data.entities.entities.item.PrimedTNT
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.factory.RegisteredEntityModelFactory
import de.bixilon.minosoft.gui.rendering.entities.feature.block.flashing.FlashingBlockFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer

class PrimedTNTEntityRenderer(renderer: EntitiesRenderer, entity: PrimedTNT) : EntityRenderer<PrimedTNT>(renderer, entity) {
    val block = FlashingBlockFeature(this, renderer.connection.registries.block[MinecraftBlocks.TNT]?.states?.default).register()


    companion object : RegisteredEntityModelFactory<PrimedTNT>, Identified {
        override val identifier get() = PrimedTNT.identifier

        override fun create(renderer: EntitiesRenderer, entity: PrimedTNT) = PrimedTNTEntityRenderer(renderer, entity)
    }
}
