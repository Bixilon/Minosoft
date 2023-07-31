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

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.createAssets
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.createLoader
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement
import de.bixilon.minosoft.gui.rendering.models.block.element.face.FaceUV
import de.bixilon.minosoft.gui.rendering.models.block.element.face.ModelFace
import de.bixilon.minosoft.gui.rendering.models.raw.display.DisplayPositions
import de.bixilon.minosoft.gui.rendering.models.raw.display.ModelDisplay
import de.bixilon.minosoft.gui.rendering.models.raw.light.GUILights
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rad
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["models"])
class BlockModelTest {


    private fun loadModel(json: String, files: Map<String, String> = emptyMap()): BlockModel {
        val loader = createLoader()
        val assets = loader.createAssets(files)
        assets.push(minosoft("models/block/named.json"), json)


        return loader.block.loadBlock(minosoft("block/named"))!!
    }

    fun emptyModel() {
        val json = """{}"""

        val model = loadModel(json)

        assertEquals(model, BlockModel(GUILights.SIDE, null, null, null, true))
    }

    fun basicModel() {
        val json = """{"gui_light":"front", "ambientocclusion":false}"""

        val model = loadModel(json)

        assertEquals(model, BlockModel(GUILights.FRONT, null, null, null, false))
    }

    fun cubeAll() {
        val model = loadModel(CUBE_ALL, FILES)

        assertEquals(model, CUBE_ALL_MODEL)
    }

    fun fallbackUV1() {
        val model = loadModel("""{"textures":{"a":"a:b"},"elements":[{"from":[0,0,0],"to":[16,16,16],"faces":{"down": {"texture":"#a"},"up":{"texture":"#a"},"north":{"texture":"#a"},"south":{"texture":"#a"},"west": {"texture":"#a"},"east": {"texture":"#a"}}}]}""")

        val faces = model.elements?.firstOrNull()?.faces ?: throw NullPointerException("no models?")
        assertEquals(faces[Directions.DOWN]?.uv, FaceUV(0, 16, 16, 0))
        assertEquals(faces[Directions.UP]?.uv, FaceUV(0, 16, 16, 0))
        assertEquals(faces[Directions.NORTH]?.uv, FaceUV(0, 16, 16, 0))
        assertEquals(faces[Directions.SOUTH]?.uv, FaceUV(0, 16, 16, 0))
        assertEquals(faces[Directions.WEST]?.uv, FaceUV(0, 16, 16, 0))
        assertEquals(faces[Directions.EAST]?.uv, FaceUV(0, 16, 16, 0))
    }

    fun fallbackUV2() {
        val model = loadModel("""{"textures":{"a":"a:b"},"elements":[{"from":[1,2,3],"to":[13,14,15],"faces":{"down": {"texture":"#a"},"up":{"texture":"#a"},"north":{"texture":"#a"},"south":{"texture":"#a"},"west": {"texture":"#a"},"east": {"texture":"#a"}}}]}""")

        val faces = model.elements?.firstOrNull()?.faces ?: throw NullPointerException("no models?")
        assertEquals(faces[Directions.DOWN]?.uv, FaceUV(1, 13, 13, 1))
        assertEquals(faces[Directions.UP]?.uv, FaceUV(1, 15, 13, 3))
        assertEquals(faces[Directions.NORTH]?.uv, FaceUV(3, 14, 15, 2))
        assertEquals(faces[Directions.SOUTH]?.uv, FaceUV(1, 14, 13, 2))
        assertEquals(faces[Directions.WEST]?.uv, FaceUV(3, 14, 15, 2))
        assertEquals(faces[Directions.EAST]?.uv, FaceUV(1, 14, 13, 2))
    }

    fun fallbackUV3() {
        val model = loadModel("""{"textures":{"a":"a:b"},"elements":[{"from":[5,3,1],"to":[15,13,11],"faces":{"down": {"texture":"#a"},"up":{"texture":"#a"},"north":{"texture":"#a"},"south":{"texture":"#a"},"west": {"texture":"#a"},"east": {"texture":"#a"}}}]}""")

        val faces = model.elements?.firstOrNull()?.faces ?: throw NullPointerException("no models?")
        assertEquals(faces[Directions.DOWN]?.uv, FaceUV(5, 15, 15, 5))
        assertEquals(faces[Directions.UP]?.uv, FaceUV(5, 11, 15, 1))
        assertEquals(faces[Directions.NORTH]?.uv, FaceUV(1, 13, 11, 3))
        assertEquals(faces[Directions.SOUTH]?.uv, FaceUV(5, 13, 15, 3))
        assertEquals(faces[Directions.WEST]?.uv, FaceUV(1, 13, 11, 3))
        assertEquals(faces[Directions.EAST]?.uv, FaceUV(5, 13, 15, 3))
    }

    fun fallbackUV4() {
        val model = loadModel("""{"textures":{"a":"a:b"},"elements":[{"from":[2,3,4],"to":[15,14,13],"faces":{"down": {"texture":"#a"},"up":{"texture":"#a"},"north":{"texture":"#a"},"south":{"texture":"#a"},"west": {"texture":"#a"},"east": {"texture":"#a"}}}]}""")

        val faces = model.elements?.firstOrNull()?.faces ?: throw NullPointerException("no models?")
        //  assertEquals(faces[Directions.DOWN]?.uv, FaceUV(5, 15, 15, 5))
        assertEquals(faces[Directions.UP]?.uv, FaceUV(4, 2, 15, 13))
        //    assertEquals(faces[Directions.NORTH]?.uv, FaceUV(1, 13, 11, 3))
        //    assertEquals(faces[Directions.SOUTH]?.uv, FaceUV(5, 13, 15, 3))
        //    assertEquals(faces[Directions.WEST]?.uv, FaceUV(1, 13, 11, 3))
        //    assertEquals(faces[Directions.EAST]?.uv, FaceUV(5, 13, 15, 3))
    }

    companion object {
        const val BLOCK = """{"gui_light":"side","display":{"gui":{"rotation":[30,225,0],"translation":[0,0,0],"scale":[0.625,0.625,0.625]},"ground":{"rotation":[0,0,0],"translation":[0,3,0],"scale":[0.25,0.25,0.25]},"fixed":{"rotation":[0,0,0],"translation":[0,0,0],"scale":[0.5,0.5,0.5]},"thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[0.375,0.375,0.375]},"firstperson_righthand":{"rotation":[0,45,0],"translation":[0,0,0],"scale":[0.4,0.4,0.4]},"firstperson_lefthand":{"rotation":[0,225,0],"translation":[0,0,0],"scale":[0.4,0.4,0.4]}}}"""
        const val CUBE = """{"parent":"block/block","elements":[{"from":[0,0,0],"to":[16,16,16],"faces":{"down":{"texture":"#down","cullface":"down"},"up":{"texture":"#up","cullface":"up"},"north":{"texture":"#north","cullface":"north"},"south":{"texture":"#south","cullface":"south"},"west":{"texture":"#west","cullface":"west"},"east":{"texture":"#east","cullface":"east"}}}]}"""
        const val CUBE_ALL = """{"parent":"block/cube","textures":{"particle":"#all","down":"#all","up":"#all","north":"#all","east":"#all","south":"#all","west":"#all"}}"""

        val FILES = mapOf(
            "block/cube_all" to CUBE_ALL,
            "block/cube" to CUBE,
            "block/block" to BLOCK,
        )

        val CUBE_ALL_MODEL: BlockModel = BlockModel(
            GUILights.SIDE,
            display = mapOf(
                DisplayPositions.GUI to ModelDisplay(
                    rotation = Vec3(30, 225, 0).rad,
                    translation = Vec3(0, 0, 0),
                    scale = Vec3(0.625, 0.625, 0.625),
                ),
                DisplayPositions.GROUND to ModelDisplay(
                    rotation = Vec3(0, 0, 0),
                    translation = Vec3(0, 3, 0),
                    scale = Vec3(0.25, 0.25, 0.25),
                ),
                DisplayPositions.FIXED to ModelDisplay(
                    rotation = Vec3(0, 0, 0),
                    translation = Vec3(0, 0, 0),
                    scale = Vec3(0.5, 0.5, 0.5),
                ),
                DisplayPositions.THIRD_PERSON_RIGHT_HAND to ModelDisplay(
                    rotation = Vec3(75, 45, 0).rad,
                    translation = Vec3(0, 2.5, 0),
                    scale = Vec3(0.375, 0.375, 0.375),
                ),
                DisplayPositions.FIRST_PERSON_RIGHT_HAND to ModelDisplay(
                    rotation = Vec3(0, 45, 0).rad,
                    translation = Vec3(0, 0, 0),
                    scale = Vec3(0.40, 0.40, 0.40),
                ),
                DisplayPositions.FIRST_PERSON_LEFT_HAND to ModelDisplay(
                    rotation = Vec3(0, 225, 0).rad,
                    translation = Vec3(0, 0, 0),
                    scale = Vec3(0.40, 0.40, 0.40),
                )
            ),
            elements = listOf(ModelElement(
                from = Vec3(0, 0, 0),
                to = Vec3(1, 1, 1),
                faces = mapOf(
                    Directions.DOWN to ModelFace("#down", null, 0, -1),
                    Directions.UP to ModelFace("#up", null, 0, -1),
                    Directions.NORTH to ModelFace("#north", null, 0, -1),
                    Directions.SOUTH to ModelFace("#south", null, 0, -1),
                    Directions.WEST to ModelFace("#west", null, 0, -1),
                    Directions.EAST to ModelFace("#east", null, 0, -1),
                ),
                shade = true,
                rotation = null,
            )),
            textures = mapOf(
                "particle" to "all",
                "down" to "all",
                "up" to "all",
                "north" to "all",
                "east" to "all",
                "south" to "all",
                "west" to "all"
            ),
            ambientOcclusion = true,
        )
    }
}
