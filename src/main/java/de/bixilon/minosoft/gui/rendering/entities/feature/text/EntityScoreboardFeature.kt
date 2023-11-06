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

package de.bixilon.minosoft.gui.rendering.entities.feature.text

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.scoreboard.ScoreboardPositions
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.player.PlayerRenderer

class EntityScoreboardFeature(renderer: PlayerRenderer<*>) : BillboardTextFeature(renderer, null) {

    override fun update(millis: Long, delta: Float) {
        updateScore()
        super.update(millis, delta)
    }

    private fun updateScore() {
        // TODO: self, offset name, render distance (10), performance
        this.text = getScore()
    }

    private fun getScore(): ChatComponent? {
        val objective = renderer.renderer.connection.scoreboard.positions[ScoreboardPositions.BELOW_NAME] ?: return null
        val score = objective.scores[renderer.entity.unsafeCast<PlayerEntity>().additional.name] ?: return null
        val text = BaseComponent()
        text += TextComponent(score.value)
        text += " "
        text += objective.displayName

        return text
    }
}
