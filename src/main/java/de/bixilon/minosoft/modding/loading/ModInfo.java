/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.modding.loading;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ModInfo {
    private final String versionName;
    private final String name;
    private final String[] authors;
    private final int moddingAPIVersion;
    private final String resourceNamespace;
    private final String mainClass;
    private final HashSet<ModDependency> hardDependencies = new HashSet<>();
    private final HashSet<ModDependency> softDependencies = new HashSet<>();
    private final ModVersionIdentifier modVersionIdentifier;
    LoadingInfo loadingInfo;

    public ModInfo(JsonObject json) throws ModLoadingException {
        this.modVersionIdentifier = ModVersionIdentifier.serialize(json);
        this.versionName = json.get("version_name").getAsString();
        this.name = json.get("name").getAsString();
        JsonArray authors = json.get("authors").getAsJsonArray();
        this.authors = new String[authors.size()];
        AtomicInteger i = new AtomicInteger();
        authors.forEach((authorElement) -> this.authors[i.getAndIncrement()] = authorElement.getAsString());
        this.moddingAPIVersion = json.get("modding_api_version").getAsInt();
        if (this.moddingAPIVersion > ModLoader.CURRENT_MODDING_API_VERSION) {
            throw new ModLoadingException(String.format("Mod was written with for a newer version of minosoft (moddingAPIVersion=%d, expected=%d)", this.moddingAPIVersion, ModLoader.CURRENT_MODDING_API_VERSION));
        }
        this.resourceNamespace = json.get("resource_namespace").getAsString();
        this.mainClass = json.get("main_class").getAsString();
        if (json.has("loading")) {
            JsonObject loading = json.getAsJsonObject("loading");
            this.loadingInfo = new LoadingInfo();
            if (loading.has("priority")) {
                this.loadingInfo.setLoadingPriority(Priorities.valueOf(loading.get("priority").getAsString()));
            }
        }
        if (json.has("dependencies")) {
            JsonObject dependencies = json.getAsJsonObject("dependencies");
            if (dependencies.has("hard")) {
                this.hardDependencies.addAll(ModDependency.serializeDependencyArray(dependencies.getAsJsonArray("hard")));
            }
            if (dependencies.has("soft")) {
                this.softDependencies.addAll(ModDependency.serializeDependencyArray(dependencies.getAsJsonArray("soft")));
            }
        }
    }

    public String[] getAuthors() {
        return this.authors;
    }

    public String getResourceNamespace() {
        return this.resourceNamespace;
    }

    public String getMainClass() {
        return this.mainClass;
    }

    public LoadingInfo getLoadingInfo() {
        return this.loadingInfo;
    }

    @Deprecated
    public UUID getUUID() {
        return this.modVersionIdentifier.getUUID();
    }

    @Deprecated
    public int getVersionId() {
        return this.modVersionIdentifier.getVersionId();
    }

    public ModVersionIdentifier getModVersionIdentifier() {
        return this.modVersionIdentifier;
    }

    public String getVersionName() {
        return this.versionName;
    }

    public String getName() {
        return this.name;
    }

    public HashSet<ModDependency> getHardDependencies() {
        return this.hardDependencies;
    }

    public HashSet<ModDependency> getSoftDependencies() {
        return this.softDependencies;
    }

    @Override
    public String toString() {
        return String.format("name=\"%s\", uuid=%s, versionName=\"%s\", versionId=%d", getName(), getUUID(), getVersionName(), getVersionId());
    }
}
