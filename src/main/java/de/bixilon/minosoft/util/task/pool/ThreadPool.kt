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

package de.bixilon.minosoft.util.task.pool

import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedList
import de.bixilon.minosoft.util.KUtil.toSynchronizedSet
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

open class ThreadPool(
    val threadCount: Int = Runtime.getRuntime().availableProcessors(),
    private val name: String = "Worker#%d",
) : ExecutorService {
    private var state = ThreadPoolStates.STARTING
    private val threads: MutableSet<Thread> = synchronizedSetOf()
    private val availableThreads: MutableSet<Thread> = synchronizedSetOf()
    private var pending: MutableSet<ThreadPoolRunnable> = sortedSetOf<ThreadPoolRunnable>({ a, b -> a.priority - b.priority }).toSynchronizedSet()
    private var nextThreadId = 0

    init {
        check(threadCount >= 1) { "Can not have < 1 thread!" }
        checkThreads()
        state = ThreadPoolStates.STARTED
    }

    val pendingCount: Int
        get() = pending.size

    @Synchronized
    private fun checkThreads() {
        fun wait() {
            try {
                availableThreads += Thread.currentThread()
                Thread.sleep(Long.MAX_VALUE)
            } catch (exception: InterruptedException) {
                // Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Thread (${Thread.currentThread()} sleeping got interrupted" }
            }
            availableThreads -= Thread.currentThread()
        }

        for (i in 0 until threadCount - threads.size) {
            val thread = Thread {
                while (true) {
                    if (state == ThreadPoolStates.STOPPING) {
                        break
                    }
                    if (pending.isEmpty()) {
                        wait()
                    }

                    val runnable: ThreadPoolRunnable?
                    synchronized(pending) {
                        if (pending.isNotEmpty()) {
                            runnable = pending.iterator().next()
                            pending.remove(runnable)
                        } else {
                            runnable = null
                        }
                    }
                    if (runnable == null) {
                        continue
                    }
                    try {
                        runnable.thread = Thread.currentThread()
                        runnable.runnable.run()
                        runnable.thread = null
                    } catch (exception: Throwable) {
                        runnable.thread = null
                        if (exception is InterruptedException) {
                            // Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Thread ${Thread.currentThread()} was interrupted" }
                            runnable.wasInterrupted = true
                            if (!runnable.interuptable) {
                                pending += runnable
                            }

                            if (state == ThreadPoolStates.STOPPING) {
                                break
                            }
                        } else {
                            exception.printStackTrace()
                        }
                    }

                    if (pending.isNotEmpty()) {
                        continue
                    }
                    wait()
                }
                threads -= Thread.currentThread()
            }
            thread.name = name.format(nextThreadId++)
            threads += thread
            thread.start()
        }
    }

    fun execute(runnable: ThreadPoolRunnable) {
        pending += runnable
        synchronized(availableThreads) {
            if (availableThreads.isNotEmpty()) {
                val thread = availableThreads.iterator().next()
                availableThreads.remove(thread)
                thread.interrupt()
                // Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Interrupting thread $thread" }
            }
        }
    }

    override fun execute(runnable: Runnable) {
        execute(ThreadPoolRunnable(
            runnable = runnable,
        ))
    }

    operator fun plusAssign(runnable: ThreadPoolRunnable) {
        execute(runnable)
    }

    operator fun plusAssign(runnable: Runnable) {
        execute(runnable)
    }

    override fun shutdown() {
        state = ThreadPoolStates.STOPPING
        synchronized(threads) {
            for (thread in threads.toSynchronizedList()) {
                if (thread.state == Thread.State.TIMED_WAITING) {
                    thread.interrupt()
                }
            }
        }
        while (threads.isNotEmpty()) {
            Thread.sleep(1L)
        }
        state = ThreadPoolStates.STOPPED
    }

    override fun shutdownNow(): MutableList<Runnable> {
        state = ThreadPoolStates.STOPPING
        synchronized(threads) {
            for (thread in threads.toSynchronizedList()) {
                thread.interrupt()
            }
        }
        while (threads.isNotEmpty()) {
            Thread.sleep(1L)
        }
        state = ThreadPoolStates.STOPPED

        return mutableListOf() // ToDo
    }

    override fun isShutdown(): Boolean {
        return state == ThreadPoolStates.STOPPING
    }

    override fun isTerminated(): Boolean {
        return state == ThreadPoolStates.STOPPED
    }

    override fun awaitTermination(p0: Long, p1: TimeUnit): Boolean {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> submit(p0: Callable<T>): Future<T> {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> submit(p0: Runnable, p1: T): Future<T> {
        TODO("Not yet implemented")
    }

    override fun submit(p0: Runnable): Future<*> {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> invokeAll(p0: MutableCollection<out Callable<T>>): MutableList<Future<T>> {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> invokeAll(p0: MutableCollection<out Callable<T>>, p1: Long, p2: TimeUnit): MutableList<Future<T>> {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> invokeAny(p0: MutableCollection<out Callable<T>>): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> invokeAny(p0: MutableCollection<out Callable<T>>, p1: Long, p2: TimeUnit): T {
        TODO("Not yet implemented")
    }

    companion object Priorities {
        const val HIGHEST = Int.MAX_VALUE
        const val HIGHER = 500
        const val HIGH = 100
        const val NORMAL = 0
        const val LOW = -HIGH
        const val LOWER = -HIGHER
        const val LOWEST = Int.MIN_VALUE
    }
}
