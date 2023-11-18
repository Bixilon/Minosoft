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

import de.bixilon.minosoft.data.entities.entities.animal.Pig
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.entities.EntityRendererTestUtil
import de.bixilon.minosoft.gui.rendering.entities.EntityRendererTestUtil.create
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["entities", "rendering"])
class BillboardTextFeatureTest {

    private fun create(text: ChatComponent? = null): BillboardTextFeature {
        val renderer = EntityRendererTestUtil.create().create(Pig)
        return BillboardTextFeature(renderer, text)
    }

    fun `simple creation`() {
        val text = create(TextComponent("Text"))
        assertEquals(text.text?.message, "Text")
    }


    // TODO: unload on change(text, offset)/disable, render distance, comparing, text rendering, matrix (position, camera rotation, offset, entity dimensions, centering of it)
}
