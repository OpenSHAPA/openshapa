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
package org.datavyu.models;

import org.datavyu.models.component.ViewportStateImpl;

/**
 * Model for playback
 */
// TODO: Merge this class with StreamViewer and convert it into an interface
public final class PlaybackModel {

    /** Stores the highest frame rate for all available viewers. */
    private float currentFramesPerSecond = 1F;

    /** The rate to use when resumed from pause. */
    private float pauseRate;

    /** The time the last sync was performed. */
    private long lastSyncTime;

    /** The maximum duration out of all data being played. */
    private long maxDuration = ViewportStateImpl.MINIMUM_MAX_END;

    /** Are we currently faking playback of the viewers?
     * Fake playback is used by most players to control the play back at rates of >=2x or <=-2x through a sequence
     * of subsequent seeks.
     **/
    private boolean fakePlayback = false;

    /** The start time of the playback window. */
    private long startTime;

    /** The end time of the playback window. */
    private long endTime;

    public float getCurrentFramesPerSecond() {
        return currentFramesPerSecond;
    }

    public void setCurrentFramesPerSecond(final float currentFramesPerSecond) {
        this.currentFramesPerSecond = currentFramesPerSecond;
    }

    public float getPauseRate() {
        return pauseRate;
    }

    public void setPauseRate(final float pauseRate) {
        this.pauseRate = pauseRate;
    }

    public long getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(final long lastSyncTime) { this.lastSyncTime = lastSyncTime; }

    public long getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(final long maxDuration) {
        this.maxDuration = Math.max(maxDuration, ViewportStateImpl.MINIMUM_MAX_END);
    }

    public boolean isFakePlayback() {
        return fakePlayback;
    }

    public void setFakePlayback(final boolean fakePlayback) {
        this.fakePlayback = fakePlayback;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(final long endTime) {
        this.endTime = endTime;
    }

}
