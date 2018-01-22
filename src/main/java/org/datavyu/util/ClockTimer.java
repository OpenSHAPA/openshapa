/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.datavyu.util;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Keeps multiple streams in periodicSync and does not play beyond the boundaries of a stream.
 */
public final class ClockTimer {

    /** Synchronization threshold in milliseconds */
    public static final long SYNC_THRESHOLD = 500L;

    /** Clock tick period in milliseconds */
    private static final long CLOCK_SYNC_INTERVAL = 500L;

    /** Clock initial delay in milliseconds */
    private static final long CLOCK_SYNC_DELAY = 0L;

    /** Convert nanoseconds to milliseconds */
    private static final long NANO_IN_MILLI = 1000000L;

    private long minStreamTime;

    private long maxStreamTime;

    private float maxFramesPerSecond;

    /** Current time of the clock in milliseconds */
    private double clockTime;

    /** Used to calculate elapsed time in nanoseconds */
    private double lastTime;

    /** Is the clock stopped */
    private boolean isStopped;

    /** The rate factor for the clock updates */
    private float rate = 1F;

    /** The set of objects that listen to this clock */
    private Set<ClockListener> clockListeners = new HashSet<>();

    /**
     * Default constructor.
     */
    public ClockTimer() {
        clockTime = 0;
        lastTime = 0;
        minStreamTime = Long.MAX_VALUE;
        maxStreamTime = Long.MIN_VALUE;
        isStopped = true;
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                periodicSync();
            }
        }, CLOCK_SYNC_DELAY, CLOCK_SYNC_INTERVAL);
    }

    public synchronized void setMinStreamTime(long minStreamTime) {
        this.minStreamTime = Long.min(minStreamTime, this.minStreamTime);
    }

    public synchronized void setMaxStreamTime(long maxStreamTime) {
        this.maxStreamTime = Long.max(maxStreamTime, this.maxStreamTime);
    }

    public synchronized long getMaxStreamTime() {
        return maxStreamTime;
    }

    public synchronized double getStreamTime() {
        return (long) clockTime + minStreamTime;
    }

    public synchronized double absDrift(long currentStreamTime) {
        return Math.abs(Math.abs(currentStreamTime - minStreamTime) - clockTime);
    }

    /**
     * @return Current clock rate.
     */
    public synchronized float getRate() {
        return rate;
    }

    public synchronized void setTime(long time) {
        if (minStreamTime <= time || time <= maxStreamTime) {
            clockTime = time;
            // Don't notify because we assume that the time will be corrected by periodic sync events
        }
    }

    public synchronized void setForceTime(long time) {
        if (minStreamTime <= time || time <= maxStreamTime) {
            clockTime = time;
            notifyForceSync();
        }
    }

    public synchronized void toggle() {
        if (isStopped) {
            start();
        } else {
            stop();
        }
    }

    /**
     * @param newRate Multiplier for CLOCK_TICK.
     */
    public synchronized void setRate(float newRate) {
        updateElapsedTime();
        rate = newRate;
        // FIRST notify about the rate change
        notifyRate();
        // SECOND start or stop
        if (Math.abs(rate) < Math.ulp(1f)) {
            stop();
        } else {
            start();
        }
    }

    /**
     * Initiate starting of clock.
     */
    public synchronized void start() {
        if (isStopped) {
            isStopped = false;
            lastTime = System.nanoTime();
            notifyStart();
        }
    }

    /**
     * Set flag to stop clock at next time update (boundary).
     */
    public synchronized void stop() {
        if (!isStopped) {
            updateElapsedTime();
            isStopped = true;
            notifyStop();
        }
    }

    /**
     * @return True if clock is stopped.
     */
    public synchronized boolean isStopped() {
        return isStopped;
    }

    /**
     * @param listener Listener requiring clockTick updates.
     */
    public synchronized void registerListener(final ClockListener listener) {
        clockListeners.add(listener);
    }

    private synchronized void updateElapsedTime() {
        double newTime = System.nanoTime();
        clockTime += isStopped ? 0 : rate * (newTime - lastTime) / NANO_IN_MILLI;
        lastTime = newTime;
    }

    /**
     * The "periodicSync" of the clock - updates listeners of changes in time.
     */
    private synchronized void periodicSync() {
        updateElapsedTime();
        notifyPeriodicSync();
    }

    /**
     * Notify clock listeners of a force periodicSync -- consumers must act on this
     */
    private void notifyForceSync() {
        for (ClockListener clockListener : clockListeners) {
            clockListener.clockForceSync(clockTime);
        }
    }

    /**
     * Notify clock listeners of a periodic periodicSync -- consumers may act on this
     */
    private void notifyPeriodicSync() {
        for (ClockListener clockListener : clockListeners) {
            clockListener.clockPeriodicSync(clockTime);
        }
    }

    /**
     * Notify clock listeners of rate update.
     */
    private void notifyRate() {
        for (ClockListener clockListener : clockListeners) {
            clockListener.clockRate(rate);
        }
    }

    /**
     * Notify clock listeners of start event.
     */
    private void notifyStart() {
        for (ClockListener clockListener : clockListeners) {
            clockListener.clockStart(clockTime);
        }
    }

    /**
     * Notify clock listeners of stop event.
     */
    private void notifyStop() {
        for (ClockListener clockListener : clockListeners) {
            clockListener.clockStop(clockTime);
        }
    }

    /**
     * Listener interface for clock 'ticks'.
     */
    public interface ClockListener {

        /**
         * @param clockTime Current time in milliseconds
         */
        void clockForceSync(double clockTime);

        /**
         * @param clockTime Current time in milliseconds
         */
        void clockPeriodicSync(double clockTime);

        /**
         * @param clockTime Current time in milliseconds
         */
        void clockStart(double clockTime);

        /**
         * @param clockTime Current time in milliseconds
         */
        void clockStop(double clockTime);

        /**
         * @param rate Current (updated) rate.
         */
        void clockRate(float rate);
    }
}
