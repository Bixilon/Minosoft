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

package de.bixilon.minosoft.util.task;

import de.bixilon.minosoft.modding.loading.Priorities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Task {
    public final TaskCallable task;
    public final String taskName;
    public final String taskDescription;
    public Priorities priority = Priorities.NORMAL;
    public TaskImportance importance = TaskImportance.OPTIONAL;
    public Set<String> dependsOns = new HashSet<>();

    public Task(TaskCallable task, String taskName, String taskDescription) {
        this.task = task;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
    }

    public Task(TaskCallable task, String taskName, String taskDescription, Priorities priority) {
        this.task = task;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.priority = priority;
    }

    public Task(TaskCallable task, String taskName, String taskDescription, Priorities priority, TaskImportance importance) {
        this.task = task;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.priority = priority;
        this.importance = importance;
    }

    public Task(TaskCallable task, String taskName, String taskDescription, Priorities priority, TaskImportance importance, Set<String> dependsOn) {
        this.task = task;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.priority = priority;
        this.importance = importance;
        this.dependsOns = dependsOn;
    }

    public Task(TaskCallable task, String taskName, String taskDescription, Priorities priority, TaskImportance importance, List<Task> dependsOn) {
        this.task = task;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.priority = priority;
        this.importance = importance;
        dependsOn.forEach((dependency) -> this.dependsOns.add(dependency.getTaskName()));
    }

    public Task(TaskCallable task, String taskName, String taskDescription, Priorities priority, TaskImportance importance, String... dependsOn) {
        this.task = task;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.priority = priority;
        this.importance = importance;
        this.dependsOns = new HashSet<>(Arrays.asList(dependsOn));
    }

    public TaskCallable getTask() {
        return task;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public Priorities getPriority() {
        return priority;
    }

    public TaskImportance getImportance() {
        return importance;
    }

    public Set<String> getDependsOns() {
        return dependsOns;
    }
}
