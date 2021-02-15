package de.bixilon.minosoft.gui.rendering.chunk.models

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.mappings.blocks.Block
import de.bixilon.minosoft.gui.rendering.textures.Texture
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3

open class BlockModel(val parent: BlockModel? = null, json: JsonObject) {
    private val textures: MutableMap<String, String> = parent?.textures?.toMutableMap() ?: mutableMapOf()
    private val textureMapping: MutableMap<String, Texture> = mutableMapOf()
    private var elements: MutableList<BlockModelElement> = parent?.elements?.toMutableList() ?: mutableListOf()
    private val fullFaceDirections: MutableSet<Directions> = parent?.fullFaceDirections?.toMutableSet() ?: mutableSetOf()
    private var rotation: Vec3
    private var uvLock = false // ToDo
    private var rescale = false // ToDo

    init {
        json["textures"]?.asJsonObject?.let {
            for ((type, value) in it.entrySet()) {
                textures[type] = value.asString
            }
        }
        for ((type, texture) in textures) {
            getTextureByType(texture).let {
                textures[type] = it
            }
        }
        json["elements"]?.let { it ->
            elements.clear()
            fullFaceDirections.clear()
            for (element in it.asJsonArray) {
                val blockModelElement = BlockModelElement(element.asJsonObject)
                elements.add(blockModelElement)
                fullFaceDirections.addAll(blockModelElement.fullFaceDirections)
            }
        }
        var rotateX = parent?.rotation?.x ?: 0f
        var rotateY = parent?.rotation?.y ?: 0f
        var rotateZ = parent?.rotation?.z ?: 0f
        json["x"]?.let {
            rotateX = it.asFloat
        }
        json["y"]?.let {
            rotateY = it.asFloat
        }
        json["z"]?.let {
            rotateZ = it.asFloat
        }
        json["uvlock"]?.let {
            uvLock = it.asBoolean
        }
        json["rescale"]?.let {
            rescale = it.asBoolean
        }
        rotation = glm.radians(Vec3(rotateX, rotateY, rotateZ))
    }


    open fun render(position: Vec3, data: MutableList<Float>, neighbourBlocks: Array<Block?>) {
        val modelMatrix = Mat4().translate(position)
            .rotate(rotation.z, Vec3(0, 0, -1))
            .rotate(rotation.y, Vec3(0, -1, 0))
            .rotate(rotation.x, Vec3(1, 0, 0))
        // ToDo: this should be made easier/more efficient

        for (direction in Directions.DIRECTIONS) {
            for (element in elements) {
                val blockFullFace = fullFaceDirections.contains(direction)

                var neighbourBlockFullFace = false
                neighbourBlocks[direction.ordinal]?.blockModels?.let { // ToDo: Improve this
                    for (model in it) {
                        if (model.fullFaceDirections.contains(direction.inverse())) {
                            neighbourBlockFullFace = true
                            break
                        }
                    }
                }

                if (blockFullFace && neighbourBlockFullFace) {
                    continue
                }
                if (!blockFullFace && neighbourBlockFullFace) {
                    continue
                }
                element.render(textureMapping, modelMatrix, direction, rotation, data)
            }
        }
    }


    private fun getTextureByType(type: String): String {
        var currentValue: String = type

        while (currentValue.startsWith("#")) {
            textures[currentValue.removePrefix("#")].let {
                if (it == null) {
                    return currentValue
                }
                currentValue = it
            }
        }

        return currentValue
    }

    fun isCullFace(direction: Directions): Boolean {
        for (element in elements) {
            if (element.isCullFace(direction)) {
                return true
            }
        }
        return false
    }

    fun resolveTextures(indexed: MutableList<Texture>, textureMap: MutableMap<String, Texture>) {
        for ((key, textureName) in textures) {
            if (!textureName.startsWith("#")) {
                var texture: Texture? = null
                var index: Int? = textureMap[textureName]?.let {
                    texture = it
                    indexed.indexOf(it)
                }
                if (index == null || index == -1) {
                    index = textureMap.size
                    texture = Texture(textureName, index)
                    textureMap[textureName] = texture!!
                    indexed.add(texture!!)
                }
                textureMapping[key] = texture!!
            }
        }
    }

    fun isTransparent(direction: Directions): Boolean {
        for (element in elements) {
            if (textureMapping[element.getTexture(direction)]?.isTransparent == true) {
                return true
            }
        }
        return false

    }


    companion object {
        fun transformPosition(position: Vec3): Vec3 {
            fun positionToFloat(uv: Float): Float {
                return (uv - 8f) / 16f
            }

            return Vec3(positionToFloat(position.x), positionToFloat(position.y), positionToFloat(position.z))
        }
    }

}
