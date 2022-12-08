/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.entity.models

import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.gui.rendering.entity.EntityRenderer
import de.bixilon.minosoft.gui.rendering.skeletal.baked.SkeletalModelStates
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance

abstract class SkeletalEntityModel<E : Entity>(renderer: EntityRenderer, entity: E) : EntityModel<E>(renderer, entity) {
    abstract val instance: SkeletalInstance?
    open val hideSkeletalModel: Boolean get() = false


    override fun prepare() {
        super.prepare()
        if (instance?.model?.state != SkeletalModelStates.LOADED) {
            instance?.model?.preload(renderWindow) // ToDo: load async
            instance?.model?.load()
        }
    }

    override fun draw() {
        super.draw()
        if (!hideSkeletalModel) {
            instance?.updatePosition(entity.cameraPosition, entity.rotation)
            instance?.draw()
        }
    }
}
