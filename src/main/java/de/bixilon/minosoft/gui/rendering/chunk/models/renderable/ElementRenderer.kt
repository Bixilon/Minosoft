package de.bixilon.minosoft.gui.rendering.chunk.models.renderable

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.mappings.ModIdentifier
import de.bixilon.minosoft.data.mappings.versions.VersionMapping
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModelElement
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModelFace
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import glm_.Java.Companion.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class ElementRenderer(element: BlockModelElement, rotation: Vec3, uvlock: Boolean) {
    private val faces: MutableMap<Directions, BlockModelFace> = element.faces
    private var positions: Array<Vec3> = element.positions.clone()

    init {
        BlockModelElement.rotatePositionsAxes(positions, rotation)
        // TODO : uvlock
    }


    fun render(textureMapping: MutableMap<String, Texture>, modelMatrix: Mat4, direction: Directions, rotation: Vec3, data: MutableList<Float>) {
        val realDirection = BlockModelElement.getRotatedDirection(rotation, direction)
        val positionTemplate = BlockModelElement.FACE_POSITION_MAP_TEMPLATE[realDirection.ordinal]

        val face = faces[realDirection] ?: return // Not our face
        val texture = textureMapping[face.textureName] ?: TextureArray.DEBUG_TEXTURE
        // if (texture.isTransparent) {
        //     return // ToDo: force render transparent faces
        // }

        val drawPositions = arrayOf(positions[positionTemplate[0]], positions[positionTemplate[1]], positions[positionTemplate[2]], positions[positionTemplate[3]])

        fun addToData(vec3: Vec3, textureCoordinates: Vec2) {
            val input = Vec4(vec3, 1.0f)
            val output = modelMatrix * input
            data.add(output.x)
            data.add(output.y)
            data.add(output.z)
            data.add(textureCoordinates.x * texture.widthFactor)
            data.add(textureCoordinates.y * texture.heightFactor)
            data.add(Float.fromBits(texture.id)) // ToDo: Compact this

            // ToDo: Send this only once per texture
            data.add(texture.animationFrameTime.toFloat())
            data.add(texture.animations.toFloat())
            data.add(texture.heightFactor)
        }

        fun createQuad(drawPositions: Array<Vec3>, texturePositions: Array<Vec2?>) {
            addToData(drawPositions[0], texturePositions[1]!!)
            addToData(drawPositions[3], texturePositions[2]!!)
            addToData(drawPositions[2], texturePositions[3]!!)
            addToData(drawPositions[2], texturePositions[3]!!)
            addToData(drawPositions[1], texturePositions[0]!!)
            addToData(drawPositions[0], texturePositions[1]!!)
        }


        val texturePositions = face.getTexturePositionArray(realDirection)
        createQuad(drawPositions, texturePositions)
    }

    companion object {
        fun createElements(state: JsonObject, mapping: VersionMapping): MutableList<ElementRenderer> {
            val rotation = glm.radians(vec3InJsonObject(state))
            val uvlock = state["uvlock"]?.asBoolean ?: false
            val parentElements = mapping.blockModels[ModIdentifier(state["model"].asString.replace("block/", ""))]!!.elements
            val result: MutableList<ElementRenderer> = mutableListOf()
            for (parentElement in parentElements) {
                result.add(ElementRenderer(parentElement, rotation, uvlock))
            }
            return result
        }

        private fun vec3InJsonObject(json: JsonObject): Vec3 {
            return Vec3(json["x"]?.asFloat?: 0, json["y"]?.asFloat?: 0, json["z"]?.asFloat?: 0)
        }
    }
}
