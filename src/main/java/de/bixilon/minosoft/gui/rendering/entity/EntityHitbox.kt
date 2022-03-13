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

package de.bixilon.minosoft.gui.rendering.entity

import de.bixilon.minosoft.data.container.InventorySlots
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.data.registries.items.armor.DyeableArmorItem
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.camera.frustum.Frustum
import de.bixilon.minosoft.gui.rendering.util.VecUtil.empty
import de.bixilon.minosoft.gui.rendering.util.mesh.LineMesh
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import glm_.vec3.Vec3
import glm_.vec3.Vec3d

class EntityHitbox(
    val renderer: EntityHitboxRenderer,
    val entity: Entity,
    val frustum: Frustum,
) {
    private lateinit var mesh: LineMesh
    private var visible = false
    private var aabb = AABB.EMPTY
    private var hitBoxColor = ChatColors.WHITE
    private var velocity = Vec3d.EMPTY
    private var rotation = EntityRotation.EMPTY
    private var checkVisibility = false


    private fun update() {
        val renderInfo = entity.renderInfo
        val aabb = renderInfo.aabb.shrink(0.01f)
        val hitBoxColor = entity.hitBoxColor
        val velocity = entity.physics.other.velocity
        val rotation = renderInfo.rotation
        val equals = aabb == this.aabb && hitBoxColor == this.hitBoxColor && this.velocity == velocity && this.rotation == rotation
        if (equals && !checkVisibility) {
            return
        }
        this.aabb = aabb
        this.hitBoxColor = hitBoxColor
        this.velocity = velocity
        this.rotation = rotation

        this.checkVisibility = false

        val visible = ((entity.isInvisible && renderer.profile.showInvisible) || !entity.isInvisible) && frustum.containsAABB(aabb)
        if (checkVisibility && equals && this::mesh.isInitialized) {
            // only visibility changed
            this.visible = visible
            return
        }
        if (this.visible) {
            this.mesh.unload()
        }
        if (visible) {
            val mesh = LineMesh(renderer.renderWindow)
            if (renderer.profile.lazy) {
                mesh.drawLazyAABB(aabb, color = hitBoxColor)
            } else {
                mesh.drawAABB(aabb = aabb, color = hitBoxColor, margin = 0.1f)
            }
            val center = Vec3(aabb.center)

            if (!velocity.empty) {
                mesh.drawLine(center, center + Vec3(velocity) * 3, color = ChatColors.YELLOW)
            }


            val eyeHeight = aabb.min.y + entity.renderInfo.eyeHeight
            val eyeAABB = AABB(Vec3(aabb.min.x, eyeHeight, aabb.min.z), Vec3(aabb.max.x, eyeHeight, aabb.max.z)).hShrink(RenderConstants.DEFAULT_LINE_WIDTH)
            mesh.drawAABB(eyeAABB, RenderConstants.DEFAULT_LINE_WIDTH, ChatColors.DARK_RED)


            val eyeStart = Vec3(center.x, eyeHeight, center.z)

            mesh.drawLine(eyeStart, eyeStart + Vec3(rotation.front) * 5, color = ChatColors.BLUE)
            mesh.load()
            this.mesh = mesh
        }
        this.visible = visible
    }

    fun draw() {
        update()
        if (this.visible) {
            mesh.draw()
        }
    }

    fun unload() {
        this.visible = false
        this.aabb = AABB.EMPTY
        if (this::mesh.isInitialized && this.mesh.state == Mesh.MeshStates.LOADED) {
            mesh.unload()
        }
    }

    fun updateVisibility() {
        this.checkVisibility = true
    }


    private val Entity.hitBoxColor: RGBColor
        get() = when {
            isInvisible -> ChatColors.GREEN
            this is PlayerEntity -> {
                val chestPlate = equipment[InventorySlots.EquipmentSlots.CHEST]
                if (chestPlate != null && chestPlate.item.item is DyeableArmorItem) {
                    chestPlate._display?.dyeColor?.let { return it }
                }
                val formattingCode = tabListItem.team?.formattingCode
                if (formattingCode is RGBColor) formattingCode else ChatColors.RED
            }
            else -> ChatColors.WHITE
        }
}
