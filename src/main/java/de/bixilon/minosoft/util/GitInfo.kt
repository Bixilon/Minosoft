package de.bixilon.minosoft.util

import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import java.text.DateFormat
import java.text.SimpleDateFormat


object GitInfo {
    var GIT_BRANCH: String = ""
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
            val json = Util.readJsonAssetResource("git.json")
            GIT_BRANCH = json["git.branch"].asString
            GIT_BUILD_HOST_BRANCH = json["git.build.host"].asString
            GIT_BUILD_TIME = dateFormat.parse(json["git.build.time"].asString).time
            GIT_BUILD_USER_EMAIL = json["git.build.user.email"].asString
            GIT_BUILD_USER_NAME = json["git.build.user.name"].asString
            GIT_BUILD_VERSION = json["git.build.version"].asString
            GIT_CLOSEST_TAG_COMMIT_COUNT = json["git.closest.tag.commit.count"].asString
            GIT_CLOSEST_TAG_NAME = json["git.closest.tag.name"].asString
            GIT_COMMIT_ID = json["git.commit.id"].asString
            GIT_COMMIT_ID_ABBREV = json["git.commit.id.abbrev"].asString
            GIT_COMMIT_ID_DESCRIBE = json["git.commit.id.describe"].asString
            GIT_COMMIT_ID_DESCRIBE_SHORT = json["git.commit.id.describe-short"].asString
            GIT_COMMIT_MESSAGE_FULL = json["git.commit.message.full"].asString
            GIT_COMMIT_MESSAGE_SHORT = json["git.commit.message.short"].asString
            GIT_COMMIT_TIME = dateFormat.parse(json["git.commit.time"].asString).time
            GIT_COMMIT_USER_EMAIL = json["git.commit.user.email"].asString
            GIT_COMMIT_USER_NAME = json["git.commit.user.name"].asString
            GIT_DIRTY = json["git.dirty"].asBoolean
            GIT_LOCAL_BRANCH_AHEAD = json["git.local.branch.ahead"].asInt
            GIT_LOCAL_BRANCH_BEHIND = json["git.local.branch.behind"].asInt
            GIT_TAGS = if (json["git.tags"].asString.isBlank()) {
                0
            } else {
                json["git.tags"].asInt
            }
            GIT_TOTAL_COMMIT_COUNT = json["git.total.commit.count"].asInt
        } catch (exception: Throwable) {
            Log.printException(exception, LogLevels.DEBUG)
            Log.warn("Can not load git information.")
        }
    }
}
