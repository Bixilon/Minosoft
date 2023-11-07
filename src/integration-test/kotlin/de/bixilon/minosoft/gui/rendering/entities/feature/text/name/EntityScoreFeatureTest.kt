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

package de.bixilon.minosoft.gui.rendering.entities.feature.text.name

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.entities.entities.player.RemotePlayerEntity
import de.bixilon.minosoft.gui.rendering.entities.EntityRendererTestUtil.create
import de.bixilon.minosoft.gui.rendering.entities.feature.text.BillbaordTextTestUtil.assertEmpty
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.player.PlayerRenderer
import org.testng.annotations.Test

@Test(groups = ["entities", "rendering"])
class EntityScoreFeatureTest {
    private val updateScore = EntityScoreFeature::class.java.getDeclaredMethod("updateScore").apply { isAccessible = true }

    private fun createScore(): EntityScoreFeature {
        val renderer = create().create(RemotePlayerEntity).unsafeCast<PlayerRenderer<*>>()
        renderer::score.forceSet(null) // remove

        return EntityScoreFeature(renderer)
    }

    private fun EntityScoreFeature.updateScore() {
        updateScore.invoke(this)
    }

    fun `player without score`() {
        val score = createScore()
        score.updateScore()
        score.assertEmpty()
    }


    // TODO: teams, invisibility, score, profile, correct text
}
