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
package org.datavyu.plugins.qtkitplayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.plugins.BaseDataViewer;

import java.awt.*;
import java.io.File;


/**
 * The viewer for a quicktime video file.
 * <b>Do not move this class, this is for backward compatibility with 1.07.</b>
 */
public final class QTKitViewer extends BaseDataViewer {

    /** The logger for this class */
    private static Logger logger = LogManager.getLogger(QTKitViewer.class);

    /** Previous seek time */
    private long previousSeekTime = -1;

    /** The player this viewer is displaying */
    private QTKitPlayer player;

    public QTKitViewer(final Frame parent, final boolean modal) {
        super(parent, modal);

        player = null;
    }

    @Override
    protected void setPlayerVolume(final float volume) {
        if (player != null) {
            EventQueue.invokeLater(() -> player.setVolume(volume, player.id));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDuration() {
        return player.getDuration(player.id);
    }

    @Override
    protected void setPlayerSourceFile(final File sourceFile) {
        // Ensure that the native hierarchy is set up
        this.addNotify();
        player = new QTKitPlayer(sourceFile);
        this.add(player, BorderLayout.CENTER);
        EventQueue.invokeLater(() -> {
            try {
                // Make sure that the player is loaded
                player.setVolume(0.7F, player.id);
            } catch (Exception e) {
                // Oops! Back out
                QTKitPlayer.decPlayerCount();
                throw e;
            }
        });
    }

    @Override
    protected Dimension getOriginalVideoSize() {
        System.err.println(player.id);
        return new Dimension((int) player.getMovieWidth(player.id),
                             (int) player.getMovieHeight(player.id));
    }

    @Override
    protected float getPlayerFramesPerSecond() {
        return player.getFPS(player.id);
    }

    @Override
    public void setPlaybackSpeed(final float rate) {
        super.setPlaybackSpeed(rate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void play() {
        super.play();
        System.err.println("Playing at " + getPlaybackSpeed());
        try {
            if (player != null) {
                EventQueue.invokeLater(() -> {
                    if (player.getRate(player.id) != 0) {
                        player.stop(player.id);
                    }
                    player.setRate(getPlaybackSpeed(), player.id);
                });
            }
        } catch (Exception e) {
            logger.error("Unable to play", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        super.stop();
        System.out.println("HIT STOP");
        final double time = System.currentTimeMillis();
        try {
            if (player != null) {
                EventQueue.invokeLater(() -> {
                    System.out.println("EXECUTING STOP");
                    System.out.println(System.currentTimeMillis() - time);
                    player.stop(player.id);
                    System.out.println("STOPPED");
                    System.out.println(System.currentTimeMillis() - time);
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
        try {
            if (player != null && (previousSeekTime != position)) {
                previousSeekTime = position;
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        boolean wasPlaying = isPlaying();
                        float prevRate = getPlaybackSpeed();
                        if (isPlaying())
                            player.stop(player.id);
                        player.setTime(position, player.id);
                        if (wasPlaying) {
                            player.setRate(prevRate, player.id);
                        }
                    }
                });

            }
        } catch (Exception e) {
            logger.error("Unable to find", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCurrentTime() {

        try {
            return player.getCurrentTime(player.id);
        } catch (Exception e) {
            logger.error("Unable to get time", e);
        }

        return 0;
    }

    @Override
    protected void cleanUp() {
        //TODO
//        player.release();
    }
}
