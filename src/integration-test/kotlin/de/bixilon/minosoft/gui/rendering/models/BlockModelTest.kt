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

package de.bixilon.minosoft.gui.rendering.models

import de.bixilon.minosoft.gui.rendering.models.raw.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.raw.light.GUILights
import de.bixilon.minosoft.test.IT.OBJENESIS
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["models"])
class BlockModelTest {

    private fun createLoader(): ModelLoader {
        val instance = OBJENESIS.newInstance(ModelLoader::class.java)


        return instance
    }

    private fun loadModel(json: String): BlockModel = TODO()

    fun emptyModel() {
        val json = """{}"""

        val model = loadModel(json)

        assertEquals(model, BlockModel(GUILights.SIDE, null, null, null, true))
    }

    fun basicModel() {
        val json = """{"gui_scale":"front", "ambientocclusion":false}"""

        val model = loadModel(json)

        assertEquals(model, BlockModel(GUILights.FRONT, null, null, null, true))
    }

    fun cubeAll() {
        TODO()
    }

    companion object {
        const val BLOCK = """{"gui_light":"side","display":{"gui":{"rotation":[30,225,0],"translation":[0,0,0],"scale":[0.625,0.625,0.625]},"ground":{"rotation":[0,0,0],"translation":[0,3,0],"scale":[0.25,0.25,0.25]},"fixed":{"rotation":[0,0,0],"translation":[0,0,0],"scale":[0.5,0.5,0.5]},"thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[0.375,0.375,0.375]},"firstperson_righthand":{"rotation":[0,45,0],"translation":[0,0,0],"scale":[0.4,0.4,0.4]},"firstperson_lefthand":{"rotation":[0,225,0],"translation":[0,0,0],"scale":[0.4,0.4,0.4]}}}"""
        const val CUBE = """{"parent":"block/block","elements":[{"from":[0,0,0],"to":[16,16,16],"faces":{"down":{"texture":"#down","cullface":"down"},"up":{"texture":"#up","cullface":"up"},"north":{"texture":"#north","cullface":"north"},"south":{"texture":"#south","cullface":"south"},"west":{"texture":"#west","cullface":"west"},"east":{"texture":"#east","cullface":"east"}}}]}"""
        const val CUBE_ALL = """{"parent":"block/cube","textures":{"particle":"#all","down":"#all","up":"#all","north":"#all","east":"#all","south":"#all","west":"#all"}}"""


        val CUBE_ALL_MODEL: BlockModel = TODO()
    }
}
