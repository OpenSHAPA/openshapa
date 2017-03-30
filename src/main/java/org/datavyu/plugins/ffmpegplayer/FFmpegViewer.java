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
package org.datavyu.plugins.ffmpegplayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.plugins.quicktime.BaseQuickTimeDataViewer;
import quicktime.std.movies.Track;
import quicktime.std.movies.media.Media;

import java.awt.*;
import java.io.File;


/**
 * The viewer for a quicktime video file.
 * <b>Do not move this class, this is for backward compatibility with 1.07.</b>
 */
public final class FFmpegViewer extends BaseQuickTimeDataViewer {

    /**
     * How many milliseconds in a second?
     */
    private static final int MILLI = 1000;
    /**
     * How many frames to check when correcting the FPS.
     */
    private static final int CORRECTIONFRAMES = 5;
    /**
     * The logger for this class.
     */
    private static Logger LOGGER = LogManager.getLogger(FFmpegViewer.class);
    private static float FALLBACK_FRAME_RATE = 24.0f;
    long prevSeekTime = -1;
    /**
     * The quicktime movie this viewer is displaying.
     */
    private FFmpegPlayer movie;
    /**
     * The visual track for the above quicktime movie.
     */
    private Track visualTrack;
    /**
     * The visual media for the above visual track.
     */
    private Media visualMedia;

    private long lastSeekTime = 0;
    private boolean isSeeking = false;

    public FFmpegViewer(final Frame parent, final boolean modal) {
        super(parent, modal);

        movie = null;
    }

    @Override
    protected void setQTVolume(final float volume) {

        if (movie == null) {
            return;
        }
//        EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                movie.setVolume(volume);
//            }
//        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDuration() {
        return (long)(movie.getEndTime() * 1000);
    }

    @Override
    protected void setQTDataFeed(final File videoFile) {

        // Ensure that the native hierarchy is set up
//        this.addNotify();

        movie = new FFmpegPlayer();
        System.out.println("Opening " + videoFile.getAbsolutePath());
        movie.open(videoFile.getAbsolutePath());

        this.add(movie, BorderLayout.CENTER);

        movie.setTime(8);

//        setBounds(getX(), getY(), (int) nativeVideoSize.getWidth(),
//                (int) nativeVideoSize.getHeight());
//


//        EventQueue.invokeLater(new Runnable() {
//            public void run() {
////                System.out.println(new Dimension(movie.getWidth(), movie.getHeight()));
//                try {
//                    // Make sure movie is actually loaded
//                    movie.setVolume(0.7F);
//                } catch (Exception e) {
//                    // Oops! Back out
//                    FFmpegPlayer.playerCount -= 1;
//                    throw e;
//                }
//            }
//        });

    }

    @Override
    protected Dimension getQTVideoSize() {
        return new Dimension(movie.getWidth(), movie.getHeight());
    }


    @Override
    protected float getQTFPS() {

//        return movie.getFPS();
        return 30.0f;
    }

    @Override
    public void setPlaybackSpeed(final float rate) {
        super.setPlaybackSpeed(rate);
        movie.setPlaybackSpeed(rate);
        System.out.println(rate);
//        try {
//        EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                movie.setRate(rate, movie.id);
//            }
//        });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void play() {
        super.play();
        System.err.println("Playing at " + getPlaybackSpeed());

        try {

            if (movie != null) {
                movie.play();
            }
        } catch (Exception e) {
            LOGGER.error("Unable to play", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        super.stop();

        System.out.println("HIT STOP");
        try {

            if (movie != null) {
                movie.stop();
            }
        } catch (Exception e) {
            LOGGER.error("Unable to stop", e);
        }
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public void seekTo(final long position) {

        long currentSeekTime = System.currentTimeMillis();

//        if(currentSeekTime - lastSeekTime > 200) {
        if(!isSeeking) {
            try {
                if (movie != null && (prevSeekTime != position)) {
                    prevSeekTime = position;
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            isSeeking = true;
                            boolean wasPlaying = isPlaying();
                            float prevRate = getPlaybackSpeed();
                            if (isPlaying())
                                movie.stop();
                            movie.setTime(position / 1000.0);
                            movie.showNextFrame();
                            movie.repaint();
                            if (wasPlaying) {
                                movie.setPlaybackSpeed(prevRate);
                            }
                            isSeeking = false;
                        }
                    });

                }
            } catch (Exception e) {
                LOGGER.error("Unable to find", e);
            }
            lastSeekTime = currentSeekTime;

        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCurrentTime() {

        try {
            return (long)(movie.getCurrentTime() * 1000);
        } catch (Exception e) {
            LOGGER.error("Unable to get time", e);
        }

        return 0;
    }

    @Override
    protected void cleanUp() {
        //TODO
//        movie.release();
    }
}
