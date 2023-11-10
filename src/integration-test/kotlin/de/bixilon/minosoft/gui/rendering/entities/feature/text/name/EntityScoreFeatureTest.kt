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
import de.bixilon.minosoft.data.scoreboard.ScoreboardObjective
import de.bixilon.minosoft.data.scoreboard.ScoreboardPositions
import de.bixilon.minosoft.data.scoreboard.ScoreboardScore
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.entities.EntityRendererTestUtil.create
import de.bixilon.minosoft.gui.rendering.entities.feature.text.BillbaordTextTestUtil.assertEmpty
import de.bixilon.minosoft.gui.rendering.entities.feature.text.BillbaordTextTestUtil.assertText
import de.bixilon.minosoft.gui.rendering.entities.feature.text.BillboardTextFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.player.PlayerRenderer
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["entities", "rendering"])
class EntityScoreFeatureTest {
    private val updateScore = EntityScoreFeature::class.java.getDeclaredMethod("updateScore").apply { isAccessible = true }
    private val updateNameOffset = EntityScoreFeature::class.java.getDeclaredMethod("updateNameOffset").apply { isAccessible = true }

    private fun createScore(): EntityScoreFeature {
        val renderer = create().create(RemotePlayerEntity).unsafeCast<PlayerRenderer<*>>()
        renderer::score.forceSet(null) // remove
        renderer.name.text = TextComponent("not empty")

        return EntityScoreFeature(renderer)
    }

    private fun EntityScoreFeature.updateScore() {
        updateScore.invoke(this)
    }

    private fun EntityScoreFeature.updateNameOffset() {
        updateNameOffset.invoke(this)
    }

    private fun EntityScoreFeature.setScore() {
        val renderer = this.renderer.unsafeCast<PlayerRenderer<*>>()
        val objective = ScoreboardObjective("name", TextComponent("Score").color(ChatColors.LIGHT_PURPLE))
        renderer.renderer.connection.scoreboard.positions[ScoreboardPositions.BELOW_NAME] = objective
        objective.scores[renderer.entity.additional.name] = ScoreboardScore(null, 1)
        renderer.renderer.features.score.update()
    }

    fun `player without score`() {
        val score = createScore()
        score.updateScore()
        score.assertEmpty()
    }

    fun `player with score`() {
        val score = createScore()
        score.setScore()
        score.updateScore()
        score.assertText()
    }

    fun `name offset without score`() {
        val score = createScore()
        score.updateScore()
        score.updateNameOffset()
        assertEquals(score.renderer.name.offset, BillboardTextFeature.DEFAULT_OFFSET)
    }

    fun `name offset with score`() {
        val score = createScore()
        score.setScore()
        score.updateScore()
        score.updateNameOffset()
        assertEquals(score.renderer.name.offset, BillboardTextFeature.DEFAULT_OFFSET + 0.22f)
    }

    fun `profile disabled`() {
        val score = createScore()
        score.renderer.renderer.profile.features.score.enabled = false
        score.setScore()
        score.updateScore()
        score.assertEmpty()
    }

    fun `correct text`() {
        val score = createScore()
        score.setScore()
        score.updateScore()
        assertEquals(score.text, BaseComponent("1", " ", TextComponent("Score").color(ChatColors.LIGHT_PURPLE)))
    }
}
