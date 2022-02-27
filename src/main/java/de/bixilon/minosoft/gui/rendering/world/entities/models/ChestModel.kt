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

package de.bixilon.minosoft.gui.rendering.world.entities.models

import de.bixilon.minosoft.data.entities.block.container.storage.ChestBlockEntity
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.RenderingCapabilities
import de.bixilon.minosoft.gui.rendering.world.entities.BlockEntityModel
import de.bixilon.minosoft.gui.rendering.world.entities.EntitiesMesh
import glm_.vec2.Vec2
import glm_.vec3.Vec3i
import glm_.vec3.swizzle.xz

class ChestModel(val entity: ChestBlockEntity) : BlockEntityModel<ChestBlockEntity> {
    var mesh: EntitiesMesh? = null


    override fun init(renderWindow: RenderWindow, state: BlockState, blockPosition: Vec3i) {
        val mesh = EntitiesMesh(renderWindow, 1000)
        mesh.addYQuad(Vec2(blockPosition.xz + 1.0f), blockPosition.y + 1.0f, Vec2(blockPosition.xz), vertexConsumer = { position, uv -> mesh.addVertex(position.array, uv, renderWindow.WHITE_TEXTURE.texture, 0xFF00FF, 0xFF) })
        this.mesh = mesh
    }


    override fun draw(renderWindow: RenderWindow) {
        renderWindow.shaderManager.entitiesShader.use()
        renderWindow.renderSystem[RenderingCapabilities.FACE_CULLING] = false
        mesh!!.draw()
    }

    override fun unload() {
        mesh!!.unload()
        this.mesh = null
    }

    override fun load() {
        mesh!!.load()
    }
}
