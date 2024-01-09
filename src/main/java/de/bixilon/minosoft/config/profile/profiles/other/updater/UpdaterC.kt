/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.profile.profiles.other.updater

import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.delegate.types.NullableStringDelegate
import de.bixilon.minosoft.config.profile.delegate.types.StringDelegate
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfile

class UpdaterC(profile: OtherProfile) {

    /**
     * The user did not decide whether to check for updates
     */
    var ask by BooleanDelegate(profile, true)

    /**
     * Check  for updates
     */
    var check by BooleanDelegate(profile, false)

    /**
     * Update channel
     * If automatic, it chooses either stable or beta matching the current executed version
     */
    var channel by StringDelegate(profile, "auto")

    /**
     * URL to check for updates
     * Possible variables:
     * - VERSION: current version string
     * - CHANNEL: target channel
     * - COMMIT: optional: the current commit
     * - OS: Operating system (windows, linux, mac, ...)
     * - ARCH: architecture of the current build (x86, x64, arm64)
     */
    var url by StringDelegate(profile, "https://minosoft.bixilon.de/api/v1/updates?version=\${VERSION}&channel=\${CHANNEL}&commit=\${COMMIT}&os=\${OS}&arch=\${ARCH}")

    /**
     * If the newest version matches this field, it won't be shown
     */
    var dismiss by NullableStringDelegate(profile, null)
}
