package de.bixilon.minosoft.gui.rendering.models

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.world.InChunkSectionLocation
import de.bixilon.minosoft.gui.rendering.textures.Texture
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3

open class BlockModel(val parent: BlockModel? = null, json: JsonObject) {
    private val textures: MutableMap<String, String> = parent?.textures?.toMutableMap() ?: mutableMapOf()
    private val textureMapping: MutableMap<String, Texture> = mutableMapOf()
    private var elements: MutableList<BlockModelElement> = parent?.elements?.toMutableList() ?: mutableListOf()
    private var rotation: Vec3
    private var uvLock = false // ToDo

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
            for (element in it.asJsonArray) {
                elements.add(BlockModelElement(element.asJsonObject))
            }
        }
        var rotateX = parent?.rotation?.x ?: 0
        var rotateY = parent?.rotation?.y ?: 0
        var rotateZ = parent?.rotation?.z ?: 0
        json["x"]?.let {
            rotateX = it.asInt
        }
        json["y"]?.let {
            rotateY = it.asInt
        }
        json["z"]?.let {
            rotateZ = it.asInt
        }
        json["uvlock"]?.let {
            uvLock = it.asBoolean
        }
        rotation = Vec3(rotateX, rotateY, rotateZ)
    }


    open fun render(position: InChunkSectionLocation, direction: Directions, data: MutableList<Float>) {
        var model = Mat4().translate(Vec3(position.x, position.y, position.z))
        if (rotation.x > 0 || rotation.y > 0 || rotation.z > 0) {
            model = model.rotate(glm.radians(rotation.x), Vec3(-1, 0, 0))
                .rotate(glm.radians(rotation.y), Vec3(0, -1, 0))
                .rotate(glm.radians(rotation.z), Vec3(0, 0, -1))
            // ToDo: this should be made easier/effizienter
        }


        for (element in elements) {
            element.render(textureMapping, model, direction, rotation, data)
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
        fun positionToFloat(uv: Float): Float {
            return (uv - 8f) / 16f
        }
    }

}
