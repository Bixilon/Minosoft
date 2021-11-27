package de.bixilon.minosoft.data.world

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.util.VecUtil.centerf
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

interface AbstractAudioPlayer {

    fun playSoundEvent(sound: ResourceLocation, position: Vec3i? = null, volume: Float = 1.0f, pitch: Float = 1.0f) {
        playSoundEvent(sound, position?.centerf, volume, pitch)
    }

    fun playSoundEvent(sound: ResourceLocation, position: Vec3? = null, volume: Float = 1.0f, pitch: Float = 1.0f)

    fun stopSound(sound: ResourceLocation)

    // ToDo: Stop category
}
