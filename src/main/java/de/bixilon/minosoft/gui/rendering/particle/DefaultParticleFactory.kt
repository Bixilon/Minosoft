/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.particle

import de.bixilon.minosoft.data.mappings.DefaultFactory
import de.bixilon.minosoft.gui.rendering.particle.types.Particle
import de.bixilon.minosoft.gui.rendering.particle.types.norender.ExplosionEmitterParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.advanced.block.BlockDustParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.CampfireSmokeParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.ExplosionParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.damage.EnchantedHitParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.dust.DustParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.fire.SmokeParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.lava.LavaParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.spell.AmbientEntityEffectParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.spell.EntityEffectParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.suspend.DolphinParticle

object DefaultParticleFactory : DefaultFactory<ParticleFactory<out Particle>>(
    ExplosionEmitterParticle,
    ExplosionParticle,
    CampfireSmokeParticle.CosyFactory,
    CampfireSmokeParticle.SignalFactory,
    LavaParticle,
    SmokeParticle,
    DolphinParticle,
    DustParticle,
    EntityEffectParticle,
    AmbientEntityEffectParticle,
    BlockDustParticle,
    EnchantedHitParticle,
)
