/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.registries.blocks

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.VoxelShape
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.materials.Material
import de.bixilon.minosoft.data.registries.sounds.SoundEvent
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.TintColorCalculator
import de.bixilon.minosoft.gui.rendering.block.models.BlockModel
import de.bixilon.minosoft.gui.rendering.block.renderable.WorldEntryRenderer
import de.bixilon.minosoft.gui.rendering.block.renderable.block.BlockRenderer
import de.bixilon.minosoft.gui.rendering.block.renderable.block.MultipartRenderer
import de.bixilon.minosoft.util.KUtil.nullCast
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.booleanCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.compoundCast
import glm_.vec3.Vec3i
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

data class BlockState(
    val block: Block,
    val properties: Map<BlockProperties, Any> = mapOf(),
    val renderers: MutableList<WorldEntryRenderer> = mutableListOf(),
    val tintColor: RGBColor? = null,
    val material: Material,
    val collisionShape: VoxelShape,
    val occlusionShape: VoxelShape,
    val outlineShape: VoxelShape,
    val hardness: Float,
    val requiresTool: Boolean,
    val breakSoundEvent: SoundEvent?,
    val stepSoundEvent: SoundEvent?,
    val placeSoundEvent: SoundEvent?,
    val hitSoundEvent: SoundEvent?,
    val fallSoundEvent: SoundEvent?,
    val soundEventVolume: Float = 1.0f,
    val soundEventPitch: Float = 1.0f,
) {

    override fun hashCode(): Int {
        return Objects.hash(block, properties)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (other is WannabeBlockState) {
            if (block.resourceLocation != other.resourceLocation) {
                return false
            }

            other.properties?.let {
                for ((state, value) in it) {
                    if (properties[state] != value) {
                        return false
                    }
                }
            }

            return true
        }

        if (hashCode() != other.hashCode()) {
            return false
        }
        if (other is BlockState) {
            return block.resourceLocation == other.block.resourceLocation && properties == other.properties && block.resourceLocation.namespace == other.block.resourceLocation.namespace
        }
        if (other is ResourceLocation) {
            return super.equals(other)
        }
        return false
    }

    override fun toString(): String {
        val out = StringBuilder()
        if (properties.isNotEmpty()) {
            if (out.isNotEmpty()) {
                out.append(", ")
            } else {
                out.append(" (")
            }
            out.append("properties=")
            out.append(properties)
        }
        if (out.isNotEmpty()) {
            out.append(")")
        }
        return String.format("%s%s", block.resourceLocation, out)
    }

    fun getBlockRenderer(blockPosition: Vec3i): WorldEntryRenderer {
        if (renderers.isEmpty()) {
            throw IllegalArgumentException("$this has not renderer!")
        }
        if (renderers.size == 1 || !Minosoft.getConfig().config.game.other.antiMoirePattern) {
            return renderers[0]
        }
        val random = Random(getPositionSeed(blockPosition.x, blockPosition.y, blockPosition.z))
        return renderers[abs(random.nextLong().toInt() % renderers.size)]
    }

    companion object {

        fun deserialize(block: Block, registries: Registries, data: Map<String, Any>, models: Map<ResourceLocation, BlockModel>): BlockState {
            val properties = data["properties"]?.compoundCast()?.let {
                getProperties(it)
            } ?: mutableMapOf()

            val renderers: MutableList<WorldEntryRenderer> = mutableListOf()

            data["render"]?.let {
                when (it) {
                    is Collection<*> -> {
                        for (model in it) {
                            when (model) {
                                is Map<*, *> -> {
                                    addBlockModel(model.compoundCast()!!, renderers, models)
                                }
                                is Collection<*> -> {
                                    val modelList: MutableList<WorldEntryRenderer> = mutableListOf()
                                    for (singleModel in model) {
                                        addBlockModel(singleModel!!.compoundCast()!!, modelList, models)
                                    }
                                    renderers.add(MultipartRenderer(modelList.toList()))
                                }
                            }
                        }
                    }
                    is Map<*, *> -> {
                        addBlockModel(it.compoundCast()!!, renderers, models)
                    }
                    else -> error("Not a render json!")
                }
            }

            val tintColor: RGBColor? = data["tint_color"]?.nullCast<Int>()?.let { TintColorCalculator.getJsonColor(it) } ?: block.tintColor


            val material = registries.materialRegistry[ResourceLocation(data["material"]!!.unsafeCast())]!!


            fun Any.asShape(): VoxelShape {
                return if (this is Int) {
                    registries.shapes[this]
                } else {
                    VoxelShape(registries.shapes, this)
                }
            }

            val collisionShape = data["collision_shape"]?.asShape()
                ?: if (data["is_collision_shape_full_block"]?.booleanCast() == true) {
                    VoxelShape.FULL
                } else {
                    VoxelShape.EMPTY
                }

            val occlusionShape = data["occlusion_shapes"]?.asShape() ?: VoxelShape.EMPTY
            val outlineShape = data["outline_shape"]?.asShape() ?: VoxelShape.EMPTY

            block.renderOverride?.let {
                renderers.clear()
                renderers.addAll(it)
            }

            return BlockState(
                block = block,
                properties = properties.toMap(),
                renderers = renderers,
                tintColor = tintColor,
                material = material,
                collisionShape = collisionShape,
                occlusionShape = occlusionShape,
                outlineShape = outlineShape,
                hardness = data["hardness"]?.nullCast<Float>() ?: 1.0f,
                requiresTool = data["requires_tool"]?.booleanCast() ?: material.soft,
                breakSoundEvent = data["break_sound_type"]?.nullCast<Int>()?.let { registries.soundEventRegistry[it] },
                stepSoundEvent = data["step_sound_type"]?.nullCast<Int>()?.let { registries.soundEventRegistry[it] },
                placeSoundEvent = data["place_sound_type"]?.nullCast<Int>()?.let { registries.soundEventRegistry[it] },
                hitSoundEvent = data["hit_sound_type"]?.nullCast<Int>()?.let { registries.soundEventRegistry[it] },
                fallSoundEvent = data["fall_sound_type"]?.nullCast<Int>()?.let { registries.soundEventRegistry[it] },
                soundEventVolume = data["sound_type_volume"]?.nullCast<Float>() ?: 1.0f,
                soundEventPitch = data["sound_type_pitch"]?.nullCast<Float>() ?: 1.0f,
            )
        }

        fun getPositionSeed(x: Int, y: Int, z: Int): Long {
            var ret = (x * 3129871L) xor z * 116129781L xor y.toLong()
            ret = ret * ret * 42317861L + ret * 11L
            return ret shr 16
        }

        private fun getProperties(json: Map<String, Any>): MutableMap<BlockProperties, Any> {
            val properties: MutableMap<BlockProperties, Any> = mutableMapOf()
            for ((propertyGroup, propertyJsonValue) in json) {
                val propertyValue: Any = when (propertyJsonValue) {
                    is String -> propertyJsonValue.lowercase(Locale.getDefault())
                    else -> propertyJsonValue
                }
                try {
                    val (blockProperty, value) = BlockProperties.parseProperty(propertyGroup, propertyValue)
                    properties[blockProperty] = value
                } catch (exception: NullPointerException) {
                    throw NullPointerException("Invalid block property $propertyGroup or value $propertyValue")
                }
            }
            return properties
        }

        private fun addBlockModel(data: Map<String, Any>, renderer: MutableList<WorldEntryRenderer>, models: Map<ResourceLocation, BlockModel>) {
            val model = models[ResourceLocation(data["model"]!!.unsafeCast())] ?: error("Can not find block model ${data["model"]}")
            renderer.add(BlockRenderer(data, model))
        }
    }


    fun withProperties(vararg properties: Pair<BlockProperties, Any>): BlockState {
        return withProperties(properties.toMap())
    }

    fun withProperties(properties: Map<BlockProperties, Any>): BlockState {
        val newProperties = this.properties.toMutableMap()
        for ((key, value) in properties) {
            newProperties[key] = value
        }
        val wannabe = WannabeBlockState(resourceLocation = this.block.resourceLocation, properties = newProperties)
        for (blockState in this.block.states) {
            if (blockState.equals(wannabe)) {
                return blockState
            }
        }
        throw IllegalArgumentException("Can not find ${this.block.resourceLocation}, with properties: $properties")
    }


    fun cycle(property: BlockProperties): BlockState {
        val currentValue = properties[property] ?: throw IllegalArgumentException("$this has no property $property")

        return withProperties(property to block.properties[property]!!.next(currentValue))
    }

    private fun <T> List<T>.next(current: T): T {
        val index = this.indexOf(current)
        check(index >= 0) { "List does not contain $current" }

        if (index == this.size - 1) {
            return this[0]
        }
        return this[index + 1]
    }
}
