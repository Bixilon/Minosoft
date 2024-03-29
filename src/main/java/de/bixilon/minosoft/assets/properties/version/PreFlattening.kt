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

package de.bixilon.minosoft.assets.properties.version

import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.assets.meta.MinosoftMeta
import de.bixilon.minosoft.assets.meta.MinosoftMeta.load
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.versions.Version

object PreFlattening {
    const val VERSION = "1.12.2"

    fun loadRegistry(profile: ResourcesProfile, version: Version, latch: AbstractLatch): Registries {
        val registries = Registries()

        val json: MutableJsonObject = synchronizedMapOf()

        var error: Throwable? = null
        val worker = UnconditionalWorker(errorHandler = { if (error == null) error = it else it.printStackTrace() })
        for ((type, data) in MinosoftMeta.root) {
            worker += add@{ json[type] = data.load(profile, version) ?: return@add }
        }
        worker.work(latch)

        error?.let { throw it }

        registries.load(version, json, latch)

        return registries
    }
}
