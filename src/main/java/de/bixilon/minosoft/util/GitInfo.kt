/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.assets.util.FileUtil.readJsonObject
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.io.FileNotFoundException
import java.text.DateFormat
import java.text.SimpleDateFormat


object GitInfo {
    var IS_INITIALIZED: Boolean = false
        private set
    var GIT_BRANCH: String = "master"
        private set
    var GIT_BUILD_HOST_BRANCH: String = ""
        private set
    var GIT_BUILD_TIME: Long = 0L
        private set
    var GIT_BUILD_USER_EMAIL: String = ""
        private set
    var GIT_BUILD_USER_NAME: String = ""
        private set
    var GIT_BUILD_VERSION: String = ""
        private set
    var GIT_CLOSEST_TAG_COMMIT_COUNT: String = ""
        private set
    var GIT_CLOSEST_TAG_NAME: String = ""
        private set
    var GIT_COMMIT_ID: String = ""
        private set
    var GIT_COMMIT_ID_ABBREV: String = ""
        private set
    var GIT_COMMIT_ID_DESCRIBE: String = ""
        private set
    var GIT_COMMIT_ID_DESCRIBE_SHORT: String = ""
        private set
    var GIT_COMMIT_MESSAGE_FULL: String = ""
        private set
    var GIT_COMMIT_MESSAGE_SHORT: String = ""
        private set
    var GIT_COMMIT_TIME: Long = 0L
        private set
    var GIT_COMMIT_USER_EMAIL: String = ""
        private set
    var GIT_COMMIT_USER_NAME: String = ""
        private set
    var GIT_DIRTY: Boolean = false
        private set
    var GIT_LOCAL_BRANCH_AHEAD: Int = 0
        private set
    var GIT_LOCAL_BRANCH_BEHIND: Int = 0
        private set
    var GIT_TAGS: Int = 0
        private set
    var GIT_TOTAL_COMMIT_COUNT: Int = 0
        private set

    fun load() {
        try {
            val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ")
            val json = Minosoft.MINOSOFT_ASSETS_MANAGER[ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "git.json")].readJsonObject()
            GIT_BRANCH = json["git.branch"].unsafeCast()
            GIT_BUILD_HOST_BRANCH = json["git.build.host"].unsafeCast()
            GIT_BUILD_TIME = dateFormat.parse(json["git.build.time"].unsafeCast()).time
            GIT_BUILD_USER_EMAIL = json["git.build.user.email"].unsafeCast()
            GIT_BUILD_USER_NAME = json["git.build.user.name"].unsafeCast()
            GIT_BUILD_VERSION = json["git.build.version"].unsafeCast()
            GIT_CLOSEST_TAG_COMMIT_COUNT = json["git.closest.tag.commit.count"].unsafeCast()
            GIT_CLOSEST_TAG_NAME = json["git.closest.tag.name"].unsafeCast()
            GIT_COMMIT_ID = json["git.commit.id"].unsafeCast()
            GIT_COMMIT_ID_ABBREV = json["git.commit.id.abbrev"].unsafeCast()
            GIT_COMMIT_ID_DESCRIBE = json["git.commit.id.describe"].unsafeCast()
            GIT_COMMIT_ID_DESCRIBE_SHORT = json["git.commit.id.describe-short"].unsafeCast()
            GIT_COMMIT_MESSAGE_FULL = json["git.commit.message.full"].unsafeCast()
            GIT_COMMIT_MESSAGE_SHORT = json["git.commit.message.short"].unsafeCast()
            GIT_COMMIT_TIME = dateFormat.parse(json["git.commit.time"].unsafeCast()).time
            GIT_COMMIT_USER_EMAIL = json["git.commit.user.email"].unsafeCast()
            GIT_COMMIT_USER_NAME = json["git.commit.user.name"].unsafeCast()
            GIT_DIRTY = json["git.dirty"].toBoolean()
            GIT_LOCAL_BRANCH_AHEAD = json["git.local.branch.ahead"].toInt()
            GIT_LOCAL_BRANCH_BEHIND = json["git.local.branch.behind"].toInt()
            GIT_TAGS = if (json["git.tags"].unsafeCast<String>().isBlank()) {
                0
            } else {
                json["git.tags"].unsafeCast()
            }
            GIT_TOTAL_COMMIT_COUNT = json["git.total.commit.count"].toInt()


            RunConfiguration.VERSION_STRING = "Minosoft $GIT_COMMIT_ID_ABBREV"
            IS_INITIALIZED = true
        } catch (exception: FileNotFoundException) {
            Log.log(LogMessageType.OTHER, level = LogLevels.WARN) { "Failed to load git.json. Everything is fine, you have probably just not compiled with maven :)" }
        } catch (exception: Throwable) {
            Log.log(LogMessageType.OTHER, LogLevels.WARN) { exception }
        }
    }

    fun formatForCrashReport(intend: String = "    "): String {
        if (!IS_INITIALIZED) {
            return "${intend}Uninitialized :("
        }

        return """${intend}Branch: $GIT_BRANCH
${intend}Build version: $GIT_BUILD_VERSION
${intend}Commit: $GIT_COMMIT_ID_DESCRIBE_SHORT
${intend}Dirty: $GIT_DIRTY
        """.removeSuffix("\n")

    }
}
