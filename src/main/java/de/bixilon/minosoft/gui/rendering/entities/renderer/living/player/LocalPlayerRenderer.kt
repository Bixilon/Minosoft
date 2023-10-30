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

package de.bixilon.minosoft.gui.rendering.entities.renderer.living.player

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.properties.textures.metadata.SkinModel
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.model.human.PlayerModel

open class LocalPlayerRenderer(renderer: EntitiesRenderer, entity: LocalPlayerEntity) : PlayerRenderer<LocalPlayerEntity>(renderer, entity) {

    init {
        renderer.context.camera.view::view.observe(this, instant = true) { hitbox.enabled = it.renderSelf; model?.enabled = it.renderSelf }
    }

    override fun createModel(skin: SkinModel): PlayerModel? {
        val model = super.createModel(skin) ?: return null
        model.enabled = renderer.context.camera.view.view.renderSelf
        return model
    }
}
