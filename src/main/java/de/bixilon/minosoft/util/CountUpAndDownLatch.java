/*
 * Codename Minosoft
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

import java.util.concurrent.CountDownLatch;

// Thanks https://stackoverflow.com/questions/14255019/latch-that-can-be-incremented
public class CountUpAndDownLatch {
    private final Object lock = new Object();
    private CountDownLatch latch;
    private long total = 0;

    public CountUpAndDownLatch(int count) {
        total += count;
        this.latch = new CountDownLatch(count);
    }

    public CountUpAndDownLatch() {
        total = 1;
        this.latch = new CountDownLatch(1);
    }

    public void countDownOrWaitIfZero() throws InterruptedException {
        synchronized (lock) {
            while (latch.getCount() == 0) {
                lock.wait();
            }
            latch.countDown();
            lock.notifyAll();
        }
    }

    public void waitUntilZero() throws InterruptedException {
        synchronized (lock) {
            while (latch.getCount() != 0) {
                lock.wait();
            }
        }
    }

    public void countUp() { //should probably check for Integer.MAX_VALUE
        synchronized (lock) {
            total++;
            latch = new CountDownLatch((int) latch.getCount() + 1);
            lock.notifyAll();
        }
    }

    public void countDown() { //should probably check for Integer.MAX_VALUE
        synchronized (lock) {
            latch.countDown();
            lock.notifyAll();
        }
    }

    public int getCount() {
        synchronized (lock) {
            return (int) latch.getCount();
        }
    }

    public void setCount(int value) {
        synchronized (lock) {
            total += value;
            latch = new CountDownLatch(value);
            lock.notifyAll();
        }
    }

    public void addCount(int count) {
        synchronized (lock) {
            total += count;
            latch = new CountDownLatch((int) latch.getCount() + count);
            lock.notifyAll();
        }
    }

    public long getTotal() {
        return total;
    }

    public void waitForChange() throws InterruptedException {
        long current = latch.getCount();
        long total = this.total;
        synchronized (lock) {
            while (current == latch.getCount() && total == this.total) {
                lock.wait();
            }
        }
    }
}