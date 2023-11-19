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

package de.bixilon.minosoft.config.profile

import de.bixilon.minosoft.config.profile.profiles.account.AccountProfile
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager
import de.bixilon.minosoft.config.profile.profiles.audio.AudioProfile
import de.bixilon.minosoft.config.profile.profiles.audio.AudioProfileManager
import de.bixilon.minosoft.config.profile.profiles.block.BlockProfile
import de.bixilon.minosoft.config.profile.profiles.block.BlockProfileManager
import de.bixilon.minosoft.config.profile.profiles.connection.ConnectionProfile
import de.bixilon.minosoft.config.profile.profiles.connection.ConnectionProfileManager
import de.bixilon.minosoft.config.profile.profiles.controls.ControlsProfile
import de.bixilon.minosoft.config.profile.profiles.controls.ControlsProfileManager
import de.bixilon.minosoft.config.profile.profiles.entity.EntityProfile
import de.bixilon.minosoft.config.profile.profiles.entity.EntityProfileManager
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfile
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.config.profile.profiles.gui.GUIProfile
import de.bixilon.minosoft.config.profile.profiles.gui.GUIProfileManager
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfile
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfile
import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfileManager
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfile
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfileManager
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager

class ConnectionProfiles(
    overrides: Map<ProfileType<*>, String> = emptyMap(),
    val eros: ErosProfile = ErosProfileManager.selected,
    val account: AccountProfile = overrides[AccountProfile]?.let { AccountProfileManager[it] } ?: AccountProfileManager.selected,
    val particle: ParticleProfile = overrides[ParticleProfile]?.let { ParticleProfileManager[it] } ?: ParticleProfileManager.selected,
    val audio: AudioProfile = overrides[AudioProfile]?.let { AudioProfileManager[it] } ?: AudioProfileManager.selected,
    val entity: EntityProfile = overrides[EntityProfile]?.let { EntityProfileManager[it] } ?: EntityProfileManager.selected,
    val resources: ResourcesProfile = overrides[ResourcesProfile]?.let { ResourcesProfileManager[it] } ?: ResourcesProfileManager.selected,
    val rendering: RenderingProfile = overrides[RenderingProfile]?.let { RenderingProfileManager[it] } ?: RenderingProfileManager.selected,
    val block: BlockProfile = overrides[BlockProfile]?.let { BlockProfileManager[it] } ?: BlockProfileManager.selected,
    val connection: ConnectionProfile = overrides[ConnectionProfile]?.let { ConnectionProfileManager[it] } ?: ConnectionProfileManager.selected,
    val gui: GUIProfile = overrides[GUIProfile]?.let { GUIProfileManager[it] } ?: GUIProfileManager.selected,
    val controls: ControlsProfile = overrides[ControlsProfile]?.let { ControlsProfileManager[it] } ?: ControlsProfileManager.selected,
    val other: OtherProfile = overrides[OtherProfile]?.let { OtherProfileManager[it] } ?: OtherProfileManager.selected,
)
