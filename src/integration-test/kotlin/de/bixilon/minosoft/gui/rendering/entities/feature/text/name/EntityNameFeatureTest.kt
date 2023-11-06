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

import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.entities.entities.animal.Pig
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.gui.rendering.entities.EntityRendererTestUtil.create
import org.testng.Assert.assertNull
import org.testng.annotations.Test

@Test(groups = ["entities", "rendering"])
class EntityNameFeatureTest {
    private val updateName = EntityNameFeature::class.java.getDeclaredMethod("updateName").apply { isAccessible = true }

    private fun create(entity: EntityFactory<*>): EntityNameFeature {
        val renderer = create().create(entity)
        renderer::name.forceSet(null) // remove

        return EntityNameFeature(renderer)
    }

    private fun EntityNameFeature.updateName() {
        updateName.invoke(this)
    }


    fun noLabel() {
        val name = create(Pig)
        name.updateName()
        assertNull(name.text)
    }
}
