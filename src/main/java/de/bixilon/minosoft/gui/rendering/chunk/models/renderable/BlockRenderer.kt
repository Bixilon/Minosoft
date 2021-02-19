package de.bixilon.minosoft.gui.rendering.chunk.models.renderable

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.mappings.ModIdentifier
import de.bixilon.minosoft.data.mappings.blocks.Block
import de.bixilon.minosoft.data.mappings.versions.VersionMapping
import de.bixilon.minosoft.gui.rendering.textures.Texture
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3

class BlockRenderer() {
    val textures: MutableMap<String, String> = mutableMapOf()
    private val fullFaceDirections: MutableSet<Directions> = mutableSetOf()
    private val elements: MutableSet<ElementRenderer> = mutableSetOf()
    private val rotation: Vec3 = Vec3()
    private val textureMapping: MutableMap<String, Texture> = mutableMapOf()

    constructor(entry: JsonObject, mapping: VersionMapping) : this() {
        loadElements(entry, mapping)
    }

    private fun loadElements(entry: JsonObject, mapping: VersionMapping) {
        this.elements.addAll(ElementRenderer.createElements(entry, mapping))
        val parent = mapping.blockModels[ModIdentifier(entry["model"].asString.replace("block/", ""))]
        textures.putAll(parent!!.textures)
    }

    constructor(models: List<JsonObject>, mapping: VersionMapping) : this() {
        for (state in models) {
            loadElements(state, mapping)
        }
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

    fun render(position: Vec3, data: MutableList<Float>, neighbourBlocks: Array<Block?>) {
        val modelMatrix = Mat4().translate(Vec3(position.x, position.y, position.z))
            .rotate(rotation.z, Vec3(0, 0, -1))
            .rotate(rotation.y, Vec3(0, -1, 0))
            .rotate(rotation.x, Vec3(1, 0, 0 ))
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
}
