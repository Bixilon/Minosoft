/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util;

// Thanks https://stackoverflow.com/questions/14255019/latch-that-can-be-incremented
public class CountUpAndDownLatch {
    private final Object lock = new Object();
    private long count;
    private long total;

    public CountUpAndDownLatch(int count) {
        total = count;
        this.count = count;
    }

    public void waitUntilZero() throws InterruptedException {
        synchronized (lock) {
            while (count > 0) {
                lock.wait();
            }
        }
    }

    public void countUp() {
        synchronized (lock) {
            total++;
            count++;
            lock.notifyAll();
        }
    }

    public void countDown() {
        synchronized (lock) {
            count--;
            lock.notifyAll();
        }
    }

    public long getCount() {
        synchronized (lock) {
            return count;
        }
    }

    public void setCount(int value) {
        synchronized (lock) {
            total += value;
            count = value;
            lock.notifyAll();
        }
    }

    public void addCount(int count) {
        synchronized (lock) {
            total += count;
            this.count += count;
            lock.notifyAll();
        }
    }

    public long getTotal() {
        return total;
    }

    public void waitForChange() throws InterruptedException {
        long latestCount = count;
        long latestTotal = this.total;
        synchronized (lock) {
            while (latestCount == count && latestTotal == total) {
                lock.wait();
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%d / %d", count, total);
    }
}
