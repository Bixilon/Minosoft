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

package de.bixilon.minosoft.gui.rendering.entities.feature.register

import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.feature.block.BlockRegister
import de.bixilon.minosoft.gui.rendering.entities.feature.hitbox.HitboxManager
import de.bixilon.minosoft.gui.rendering.entities.feature.text.BillboardTextRegister
import de.bixilon.minosoft.gui.rendering.entities.feature.text.score.ScoreRegister
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.player.PlayerRegister
import de.bixilon.minosoft.util.Initializable

class EntityRenderFeatures(renderer: EntitiesRenderer) : Initializable {
    val features: MutableList<FeatureRegister> = mutableListOf()

    val hitbox = HitboxManager(renderer).register()
    val player = PlayerRegister(renderer).register()
    val text = BillboardTextRegister(renderer).register()
    val score = ScoreRegister(renderer).register()
    val block = BlockRegister(renderer).register()


    override fun init() {
        for (feature in features) {
            feature.init()
        }
    }

    override fun postInit() {
        for (feature in features) {
            feature.postInit()
        }
    }

    fun update() {
        for (feature in features) {
            feature.update()
        }
    }

    operator fun plusAssign(register: FeatureRegister) {
        this.features += register
    }

    private fun <T : FeatureRegister> T.register(): T {
        this@EntityRenderFeatures += this
        return this
    }
}
