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

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.unsafe.UnsafeUtil.setUnsafeAccessible
import de.bixilon.minosoft.camera.target.targets.EntityTarget
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.animal.Pig
import de.bixilon.minosoft.data.entities.entities.decoration.armorstand.ArmorStand
import de.bixilon.minosoft.data.entities.entities.monster.Zombie
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.RemotePlayerEntity
import de.bixilon.minosoft.data.entities.entities.vehicle.boat.Boat
import de.bixilon.minosoft.data.language.lang.Language
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.scoreboard.NameTagVisibilities
import de.bixilon.minosoft.data.scoreboard.team.Team
import de.bixilon.minosoft.data.scoreboard.team.TeamVisibility
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.entities.EntityRendererTestUtil.create
import de.bixilon.minosoft.gui.rendering.entities.EntityRendererTestUtil.isInvisible
import de.bixilon.minosoft.gui.rendering.entities.feature.text.BillbaordTextTestUtil.assertEmpty
import de.bixilon.minosoft.gui.rendering.entities.feature.text.BillbaordTextTestUtil.assertText
import org.testng.Assert.assertNotEquals
import org.testng.Assert.assertSame
import org.testng.annotations.Test

@Test(groups = ["entities", "rendering"])
class EntityNameFeatureTest {
    private val updateName = EntityNameFeature::class.java.getDeclaredMethod("updateName").apply { setUnsafeAccessible() }

    private fun create(entity: EntityFactory<*>): EntityNameFeature {
        val renderer = create().create(entity)
        renderer::name.forceSet(null) // remove

        return EntityNameFeature(renderer)
    }

    private fun EntityNameFeature.customName(name: Any?) {
        renderer.entity.data[Entity.CUSTOM_NAME_DATA] = ChatComponent.of(name)
    }

    private fun EntityNameFeature.isNameVisible(visible: Boolean) {
        renderer.entity.data[Entity.CUSTOM_NAME_VISIBLE_DATA] = visible
    }

    private fun EntityNameFeature.isInvisible(invisible: Boolean) {
        renderer.entity.isInvisible(invisible)
    }

    private fun EntityNameFeature.cameraTeam(same: Boolean) {
        val team = if (same) renderer.entity.unsafeCast<PlayerEntity>().additional.team ?: throw IllegalArgumentException("Not in a team") else Team("other")
        renderer.renderer.connection.camera.entity.unsafeCast<PlayerEntity>().additional.team = team
    }

    private fun EntityNameFeature.team(invisibles: Boolean = true, name: NameTagVisibilities = NameTagVisibilities.ALWAYS) {
        val team = Team("own", visibility = TeamVisibility(invisibleTeam = invisibles, name = name))
        renderer.entity.unsafeCast<PlayerEntity>().additional.team = team
    }

    private fun EntityNameFeature.setTargeted(target: Boolean = true, distance: Double = 1.0) {
        val target = if (target) EntityTarget(Vec3d(0, 0, 0), distance, Directions.DOWN, renderer.entity) else null
        renderer.renderer.connection.camera.target::target.forceSet(DataObserver(target))
    }

    private fun EntityNameFeature.updateName() {
        updateName.invoke(this)
    }


    fun `animal without name`() {
        val name = create(Pig)
        name.updateName()
        name.assertEmpty()
    }

    fun `animal with custom name set`() {
        val name = create(Pig)
        name.customName("Pepper")
        name.updateName()
        name.assertEmpty()
    }

    fun `correct animal name`() {
        val name = create(Pig)
        val text = TextComponent("Pepper:")
        name.isNameVisible(true)
        name.customName(text)
        name.updateName()
        assertSame(name.text, text)
    }

    fun `animal with custom name visible`() {
        val name = create(Pig)
        name.customName("Pepper")
        name.isNameVisible(true)
        name.updateName()
        name.assertText()
    }

    fun `targeted animal without custom name visible`() {
        val name = create(Pig)
        name.customName("Pepper")
        name.setTargeted()
        name.updateName()
        name.assertText()
    }

    fun `targeted animal with name visible`() {
        val name = create(Pig)
        name.customName("Pepper")
        name.isNameVisible(true)
        name.setTargeted()
        name.updateName()
        name.assertText()
    }

    fun `targeted but out of reach animal without custom name visible`() {
        val name = create(Pig)
        name.customName("Pepper")
        name.setTargeted(distance = 10.0)
        name.updateName()
        name.assertEmpty()
    }

    fun `remote player entity`() {
        val name = create(RemotePlayerEntity)
        name.updateName()
        name.assertText()
    }

    fun `remote player not using custom name`() {
        val name = create(RemotePlayerEntity)
        val text = TextComponent("Me")
        name.customName(text)
        name.updateName()
        assertNotEquals(name.text, text)
    }

    fun `remote player using tab name`() {
        val name = create(RemotePlayerEntity)
        val additional = name.renderer.entity.unsafeCast<RemotePlayerEntity>().additional
        val text = TextComponent("Me")
        additional.displayName = text
        name.updateName()
        assertSame(name.text, text)
    }

    fun `armor stand without name`() {
        val name = create(ArmorStand)
        name.updateName()
        name.assertEmpty()
    }

    fun `armor stand with custom name set`() {
        val name = create(ArmorStand)
        name.customName("Jonny")
        name.updateName()
        name.assertEmpty()
    }

    fun `targeted armor stand with custom name set`() {
        val name = create(ArmorStand)
        name.customName("Jonny")
        name.setTargeted(true)
        name.updateName()
        name.assertEmpty()
    }


    fun `armor stand with custom visible name set`() {
        val name = create(ArmorStand)
        name.customName("Jonny")
        name.isNameVisible(true)
        name.updateName()
        name.assertText()
    }

    fun `invisible armor stand with visible custom name set`() {
        val name = create(ArmorStand)
        name.customName("Jonny")
        name.isNameVisible(true)
        name.isInvisible(true)
        name.updateName()
        name.assertText()
    }

    fun `boat without name`() {
        val name = create(Boat)
        name.updateName()
        name.assertEmpty()
    }

    fun `boat with custom name`() {
        val name = create(Boat)
        name.customName("Titanic")
        name.updateName()
        name.assertEmpty()
    }

    fun `boat targeted and custom name set`() {
        val name = create(Boat)
        name.customName("Titanic")
        name.setTargeted(true) // TODO: a boat is somehow not targetable?
        name.updateName()
        //    name.assertEmpty()
    }

    fun `boat targeted and just custom name visible`() {
        val name = create(Boat)
        name.isNameVisible(true)
        name.setTargeted(true)
        name.updateName()
        name.assertEmpty()
    }

    fun `boat with custom visible name`() {
        val name = create(Boat)
        name.customName("Titanic")
        name.isNameVisible(true)
        name.updateName()
        name.assertText()
    }

    fun `zombie with custom name`() {
        val name = create(Zombie)
        name.customName("Notch")
        name.updateName()
        name.assertEmpty()
    }

    fun `zombie with visible name`() {
        val name = create(Zombie)
        name.isNameVisible(true)
        name.renderer.renderer.connection.language = Language("abc", mutableMapOf("key" to "Zombie"))
        name.updateName()
        name.assertText()
    }

    fun `zombie with visible custom name`() {
        val name = create(Zombie)
        name.customName("Notch")
        name.isNameVisible(true)
        name.updateName()
        name.assertText()
    }

    fun `zombie with invisibility and custom name`() {
        val name = create(Zombie)
        name.customName("Notch")
        name.isNameVisible(true)
        name.isInvisible(true)
        name.updateName()
        name.assertEmpty()
    }

    fun `player with invisibility`() {
        val name = create(RemotePlayerEntity)
        name.isInvisible(true)
        name.updateName()
        name.assertEmpty()
    }

    fun `profile disabled`() {
        val name = create(Pig)
        name.renderer.renderer.profile.features.name.enabled = false
        name.customName("Pepper")
        name.isNameVisible(true)
        name.updateName()
        name.assertEmpty()
    }

    fun `camera not in team and always visible`() {
        val name = create(RemotePlayerEntity)
        name.team(name = NameTagVisibilities.ALWAYS)
        name.updateName()
        name.assertText()
    }

    fun `camera not in team and hide for other team`() {
        val name = create(RemotePlayerEntity)
        name.team(name = NameTagVisibilities.HIDE_FOR_ENEMIES)
        name.updateName()
        name.assertText()
    }

    fun `camera not in team and hide for own team`() {
        val name = create(RemotePlayerEntity)
        name.team(name = NameTagVisibilities.HIDE_FOR_MATES)
        name.updateName()
        name.assertText()
    }

    fun `camera not in team and never`() {
        val name = create(RemotePlayerEntity)
        name.team(name = NameTagVisibilities.NEVER)
        name.updateName()
        name.assertEmpty()
    }

    fun `camera different team and always visible`() {
        val name = create(RemotePlayerEntity)
        name.team(name = NameTagVisibilities.ALWAYS)
        name.cameraTeam(false)
        name.updateName()
        name.assertText()
    }

    fun `camera different team and hide for other team`() {
        val name = create(RemotePlayerEntity)
        name.team(name = NameTagVisibilities.HIDE_FOR_ENEMIES)
        name.cameraTeam(false)
        name.updateName()
        name.assertEmpty()
    }

    fun `camera different team and hide for own team`() {
        val name = create(RemotePlayerEntity)
        name.team(name = NameTagVisibilities.HIDE_FOR_MATES)
        name.cameraTeam(false)
        name.updateName()
        name.assertText()
    }

    fun `camera different team and never visible`() {
        val name = create(RemotePlayerEntity)
        name.team(name = NameTagVisibilities.NEVER)
        name.cameraTeam(false)
        name.updateName()
        name.assertEmpty()
    }

    fun `same team and always visible`() {
        val name = create(RemotePlayerEntity)
        name.team(name = NameTagVisibilities.ALWAYS)
        name.cameraTeam(true)
        name.updateName()
        name.assertText()
    }

    fun `same team and hide for other teams`() {
        val name = create(RemotePlayerEntity)
        name.team(name = NameTagVisibilities.HIDE_FOR_ENEMIES)
        name.cameraTeam(true)
        name.updateName()
        name.assertText()
    }

    fun `same team and hide for own team`() {
        val name = create(RemotePlayerEntity)
        name.team(name = NameTagVisibilities.HIDE_FOR_MATES)
        name.cameraTeam(true)
        name.updateName()
        name.assertEmpty()
    }

    fun `same team and hide for never visible`() {
        val name = create(RemotePlayerEntity)
        name.team(name = NameTagVisibilities.NEVER)
        name.cameraTeam(true)
        name.updateName()
        name.assertEmpty()
    }

    fun `same team and invisible`() {
        val name = create(RemotePlayerEntity)
        name.team(invisibles = true)
        name.cameraTeam(true)
        name.isInvisible(true)
        name.updateName()
        name.assertText()
    }

    fun `same team and invisible but invisible to mates`() {
        val name = create(RemotePlayerEntity)
        name.team(invisibles = false)
        name.cameraTeam(true)
        name.isInvisible(true)
        name.updateName()
        name.assertEmpty()
    }

    // TODO: item frame
    // TODO: profile
    // TODO: render distance, sneaking
}
