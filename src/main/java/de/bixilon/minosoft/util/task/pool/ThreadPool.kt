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

import de.bixilon.minosoft.util.KUtil.synchronizedListOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedList
import java.util.concurrent.*

open class ThreadPool(
    val threadCount: Int = Runtime.getRuntime().availableProcessors(),
    private val name: String = "Worker#%d",
) : ExecutorService {
    private var state = ThreadPoolStates.STARTING
    private var threads: MutableList<Thread> = synchronizedListOf()
    private var queue: PriorityBlockingQueue<ThreadPoolRunnable> = PriorityBlockingQueue()
    private var nextThreadId = 0

    init {
        check(threadCount >= 1) { "Can not have < 1 thread!" }
        checkThreads()
        state = ThreadPoolStates.STARTED
    }

    val queueSize: Int
        get() = queue.size

    @Synchronized
    private fun checkThreads() {
        for (i in 0 until threadCount - threads.size) {
            var runnable: ThreadPoolRunnable
            val thread = Thread {
                while (true) {
                    if (state == ThreadPoolStates.STOPPING) {
                        break
                    }
                    try {
                        runnable = queue.take()
                    } catch (exception: InterruptedException) {
                        break
                    }

                    try {
                        runnable.thread = Thread.currentThread()
                        runnable.runnable?.run()
                        runnable.thread = null
                    } catch (exception: Throwable) {
                        runnable.thread = null
                        if (exception is InterruptedException) {
                            // Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Thread ${Thread.currentThread()} was interrupted" }
                            runnable.wasInterrupted = true
                            queue += runnable

                            if (state == ThreadPoolStates.STOPPING) {
                                break
                            }
                        } else {
                            exception.printStackTrace()
                        }
                    }
                }
                threads -= Thread.currentThread()
            }
            thread.name = name.format(nextThreadId++)
            thread.start()
            threads += thread
        }
    }

    @Synchronized
    fun execute(runnable: ThreadPoolRunnable) {
        queue += runnable
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
                thread.interrupt()
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
        state = ThreadPoolStates.STOPPED
        return mutableListOf()
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
