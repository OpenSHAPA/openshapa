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
package org.datavyu.plugins.nativeosx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.plugins.StreamViewerDialog;

import java.awt.*;
import java.io.File;


/**
 * The viewer for a quicktime video file.
 * <b>Do not move this class, this is for backward compatibility with 1.07.</b>
 */
public final class NativeOSXViewerDialog extends StreamViewerDialog {

    private long timeOfPrevSeek = 0;
    /**
     * The logger for this class.
     */
    private static Logger logger = LogManager.getLogger(NativeOSXViewerDialog.class);

    long prevSeekTime = -1;
    /**
     * The quicktime movie this viewer is displaying.
     */
    private NativeOSXPlayer movie;

    private boolean seeking = false;

    private long duration = 0;

    public NativeOSXViewerDialog(final Frame parent, final boolean modal) {
        super(parent, modal);

        movie = null;
    }

    @Override
    protected void setPlayerVolume(final float volume) {

        if (movie == null) {
            return;
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                movie.setVolume(volume, movie.id);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDuration() {

        // If we cannot read the duration of the video wait for 2 sec and the retry!
        if (movie.getDuration(movie.id) < 0) {
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                logger.error("Thread slept. Error: ", e);
            }
        }

        // Retry getting the duration after 2 sec wait time
        if (duration == 0) {
            duration = movie.getDuration(movie.id);
        }

        return duration;
    }

    @Override
    protected void setPlayerSourceFile(final File playerSourceFile) {

        // Ensure that the native hierarchy is set up
        this.addNotify();

        movie = new NativeOSXPlayer(playerSourceFile);

        this.add(movie, BorderLayout.CENTER);

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Make sure movie is actually loaded
                    movie.setVolume(0.7F, movie.id);
                } catch (Exception e) {
                    // Oops! Back out
                    NativeOSXPlayer.decPlayerCount();
                    throw e;
                }
            }
        });
    }

    @Override
    protected Dimension getOriginalVideoSize() {
        System.err.println(movie.id);
        return new Dimension((int) movie.getMovieWidth(movie.id), (int) movie.getMovieHeight(movie.id));
    }

    @Override
    protected float getPlayerFramesPerSecond() {
        float fps = movie.getFPS(movie.id);
        if (fps <= 1) {
            try {
                Thread.sleep(2000);
                fps = movie.getFPS(movie.id);
                if(fps > 1) {
                    return fps;
                }
            } catch (InterruptedException e) {
                logger.error("Thread slept. Error: ", e);
            }
            isAssumedFramesPerSecond = true;
            return 29.97f;
        }
        return movie.getFPS(movie.id);
    }

    @Override
    public void setPlaybackSpeed(final float rate) {
        super.setPlaybackSpeed(rate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        super.start();
        logger.info("Playing at speed: " + getPlaybackSpeed());

        try {
            if (movie != null) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        if (movie.getRate(movie.id) != 0) {
                            movie.stop(movie.id);
                        }
                        movie.setRate(getPlaybackSpeed(), movie.id);
                    }
                });
            } else {
                logger.info("No movie loaded!");
            }
        } catch (Exception e) {
            logger.error("Unable to start! Error: ", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        super.stop();
        logger.info("Player stopped");
        final double time = System.currentTimeMillis();
        try {

            if (movie != null) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        logger.info("STOPPING EXECUTING AT: " + (System.currentTimeMillis() - time));
                        movie.stop(movie.id);
                        logger.info("STOPPED EXECUTION AT: " + (System.currentTimeMillis() - time));
                    }
                });
            }
        } catch (Exception e) {
            logger.error("Unable to stop", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void seek(final long position) {

        // TODO: Check what is this 35 magic (this seems the only place)?
        if(System.currentTimeMillis() - timeOfPrevSeek < 35) {
            return;
        }

        if (!seeking) {
            seeking = true;
            try {
                if (movie != null) {
                    prevSeekTime = position;
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            logger.info("Seeking to position: " + position);
                            boolean wasPlaying = isPlaying();
                            float prevRate = getPlaybackSpeed();
                            if (isPlaying()) {
                                movie.stop(movie.id);
                            }
                            if (prevRate >= 0 && prevRate <= 8) {
                                movie.setTimePrecise(position, movie.id);
                            } else if (prevRate < 0  && prevRate > -8) {
                                movie.setTimeModerate(position, movie.id);
                            } else {
                                movie.setTime(position, movie.id);
                            }
                            if (wasPlaying) {
                                movie.setRate(prevRate, movie.id);
                            }
                            movie.repaint();
                            timeOfPrevSeek = System.currentTimeMillis();
                            seeking = false;
                        }
                    });
                }
            } catch (Exception e) {
                logger.error("Unable to find", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCurrentTime() {

        try {
            return movie.getCurrentTime(movie.id);
        } catch (Exception e) {
            logger.error("Unable to get time", e);
        }

        return 0;
    }

    @Override
    protected void cleanUp() {
        // TODO: Check if the release is required?
//        movie.release();
    }

    @Override
    public boolean isStepEnabled() {
        return false;
    }

    @Override
    public void step() {
        // Nothing to do here
    }
}
