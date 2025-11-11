/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.versions.registries.pixlyzer

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.blocks.factory.VerifyIntegratedBlockRegistry
import de.bixilon.minosoft.data.registries.items.VerifyIntegratedItemRegistry
import de.bixilon.minosoft.data.registries.registries.PixLyzerUtil
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.versions.registries.RegistryLoadingTest
import de.bixilon.minosoft.test.ITUtil
import org.testng.annotations.Test

@Test(groups = ["pixlyzer"], dependsOnGroups = ["version"], priority = Int.MAX_VALUE, timeOut = 15000L)
abstract class PixLyzerLoadingTest(version: String) : RegistryLoadingTest(version) {
    protected val data by lazy {
        val registries = PixLyzerUtil.load(ResourcesProfile(), this.version).unsafeCast<MutableJsonObject>()
        registries -= "models"

        return@lazy registries
    }

    @Test(priority = 100000)
    open fun loadRegistries() {
        ITUtil.registries[version]?.let { this._registries = it; return }

        val registries = Registries(false, version)
        registries.load(this.data, SimpleLatch(0))
        this.data["blocks"]!!
        this.data["items"]!!

        this.data -= "sound_events"
        this.data -= "entities"

        this._registries = registries
    }

    fun `blocks integrated`() {
        VerifyIntegratedBlockRegistry.verify(registries, version, this.data["blocks"]!!.unsafeCast())
        this.data -= "blocks"
    }

    fun `items integrated`() {
        VerifyIntegratedItemRegistry.verify(registries, version, this.data["items"]!!.unsafeCast())
        this.data -= "items"
    }
}
