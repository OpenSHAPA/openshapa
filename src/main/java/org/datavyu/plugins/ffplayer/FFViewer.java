package org.datavyu.plugins.ffplayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.plugins.BaseDataViewer;

import java.awt.*;
import java.io.File;

// TODO: Add id's
public class FFViewer extends BaseDataViewer {

    /** The logger for this class */
    private static Logger logger = LogManager.getLogger(FFViewer.class);

    /** Previous seek time */
    private long previousSeekTime = -1;

    /** The player this viewer is displaying */
    private FFPlayer player;

    /** Currently is seeking */
    private boolean isSeeking = false;

    protected FFViewer(final Frame parent, final boolean modal) {
        super(parent, modal);
        player = new FFPlayer();
    }

    @Override
    protected void setPlayerVolume(float volume) {
        player.setVolume(volume);
    }

    @Override
    protected void setPlayerSourceFile(File videoFile) {
        logger.info("Opening file: " + videoFile.getAbsolutePath());
        player.openFile(videoFile.getAbsolutePath());
    }

    @Override
    protected Dimension getOriginalVideoSize() {
        Dimension videoSize = player.getOriginalVideoSize();
        logger.info("The original video size: " + videoSize);
        return player.getOriginalVideoSize();
    }

    @Override
    public void seek(long position) {
        logger.info("Seeking position: " + position);
        try {
            if (!isSeeking && player != null && (previousSeekTime != position)) {
                previousSeekTime = position;
                EventQueue.invokeLater(() -> {
                    isSeeking = true;
                    boolean wasPlaying = isPlaying();
                    float playbackSpeed = getPlaybackSpeed();
                    if (isPlaying()) {
                        player.stop();
                    }
                    player.seek(position/1000.0);
                    if (wasPlaying) {
                        player.setPlaybackSpeed(playbackSpeed);
                    }
                    isSeeking = false;
                });
            }
        } catch (Exception e) {
            logger.error("Unable to find", e);
        }
    }

    @Override
    protected float getPlayerFramesPerSecond() {
        return 30; // TODO: Get this frame the native stream
    }

    @Override
    public long getDuration() {
        logger.info("The duration of the video is: " + player.getDuration() + " seconds.");
        return (long) (player.getDuration() * 1000);
    }

    @Override
    public long getCurrentTime() {
        logger.info("The current time is: " + player.getCurrentTime() + " seconds");
        return (long) (player.getCurrentTime() * 1000);
    }

    @Override
    protected void cleanUp() {
        player.cleanUp();
    }
}
