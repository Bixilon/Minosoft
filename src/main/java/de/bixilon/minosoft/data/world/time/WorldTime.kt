package de.bixilon.minosoft.data.world.time

import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.MMath
import glm_.func.common.clamp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

class WorldTime(
    private val world: World,
) {
    var time = 0L
    var age = 0L


    val skyAngle: Float
        get() {
            val fractionalPath = MMath.fractionalPart(abs(time) / ProtocolDefinition.TICKS_PER_DAYf - 0.25)
            val angle = 0.5 - cos(fractionalPath * Math.PI) / 2.0
            return ((fractionalPath * 2.0 + angle) / 3.0).toFloat()
        }


    val lightBase: Double
        get() {
            var base = 1.0f - (cos(skyAngle * 2.0 * PI) * 2.0 + 0.2)
            base = base.clamp(0.0, 1.0)
            base = 1.0 - base

            base *= 1.0 - ((world.weather.rainGradient * 5.0) / 16.0)
            base *= 1.0 - (((world.weather.thunderGradient * world.weather.rainGradient) * 5.0) / 16.0)
            return base * 0.8 + 0.2
        }

}
