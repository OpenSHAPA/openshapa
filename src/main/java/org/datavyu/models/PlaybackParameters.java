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
 * Parameters for playback
 */
public final class PlaybackParameters {

    /** Stores the highest frame rate for all available viewers. */
    private float highestFramesPerSecond = 1F;

    /** The rate to use when resumed from pause. */
    private float resumeRate;

    /** The maximum duration out of all data being played. */
    private long maxDuration = ViewportStateImpl.MINIMUM_MAX_END;

    /** The start time of the playback window. */
    private long startTime;

    /** The end time of the playback window. */
    private long endTime;

    public float getHighestFramesPerSecond() {
        return highestFramesPerSecond;
    }

    public void setHighestFramesPerSecond(final float highestFramesPerSecond) {
        this.highestFramesPerSecond = highestFramesPerSecond;
    }

    public float getResumeRate() {
        return resumeRate;
    }

    public void setResumeRate(final float resumeRate) {
        this.resumeRate = resumeRate;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(final long maxDuration) {
        this.maxDuration = Math.max(maxDuration, ViewportStateImpl.MINIMUM_MAX_END);
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
