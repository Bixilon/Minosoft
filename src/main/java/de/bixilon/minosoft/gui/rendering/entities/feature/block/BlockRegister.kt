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

package de.bixilon.minosoft.gui.rendering.entities.feature.block

import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.feature.block.flashing.FlashingBlockShader
import de.bixilon.minosoft.gui.rendering.entities.feature.register.FeatureRegister

class BlockRegister(renderer: EntitiesRenderer) : FeatureRegister {
    val shader = renderer.context.system.shader.create(minosoft("entities/features/block"), ::BlockShader)
    val flashing = renderer.context.system.shader.create(minosoft("entities/features/block/flashing"), ::FlashingBlockShader)

    override fun postInit() {
        shader.load()
        flashing.load()
    }
}
