package org.datavyu.plugins.ffmpegplayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.models.Identifier;
import org.datavyu.plugins.StreamViewerDialog;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class FFViewerDialog extends StreamViewerDialog {

    /** The logger for this class */
    private static Logger logger = LogManager.getLogger(FFViewerDialog.class);

    /** Previous seek time */
    private long previousSeekTime = -1;

    /** The player this viewer is displaying */
    private FFPlayer player;

    /** Currently is seeking */
    private boolean isSeeking = false;

    /** Identifier for this dialog */
    private Identifier id;

    FFViewerDialog(final Frame parent, final boolean modal) {
        super(parent, modal);
        player = new FFPlayer();
    }

    private void launch(Runnable task) {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            try {
                SwingUtilities.invokeLater(task);
            } catch (Exception e) {
                logger.error("Failed task. Error: ", e);
            }
        }
    }

    @Override
    protected void setPlayerVolume(float volume) {
        player.setVolume(volume);
    }

    @Override
    protected void setPlayerSourceFile(File playerSourceFile) {
        logger.info("Opening file: " + playerSourceFile.getAbsolutePath());
        player.openFile(playerSourceFile.getAbsolutePath());
        this.add(player, BorderLayout.CENTER);
    }

    @Override
    protected Dimension getOriginalVideoSize() {
        Dimension videoSize = player.getOriginalVideoSize();
        logger.info("The original video size: " + videoSize);
        return player.getOriginalVideoSize();
    }

    @Override
    public void seek(long position) {
        launch(() -> {
            try {
                logger.info("Seeking position: " + position);
                if (!isSeeking && player != null && (previousSeekTime != position)) {
                    previousSeekTime = position;
                    EventQueue.invokeLater(() -> {
                        logger.info("Starting seek.");
                        isSeeking = true;
                        boolean wasPlaying = isPlaying();
                        float playbackSpeed = getPlaybackSpeed();
                        if (isPlaying()) {
                            player.stop();
                        }
                        player.seek(position/1000.0);
                        player.repaint();
                        if (wasPlaying) {
                            player.setPlaybackSpeed(playbackSpeed);
                        }
                        isSeeking = false;
                    });
                }
            } catch (Exception e) {
                logger.error("Unable to find", e);
            }
        });
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return id;
    }

    @Override
    public void play() {
        launch(() -> {
            if (player != null && !playing) {
                player.play();
                FFViewerDialog.super.play();
            }
        });
    }

    @Override
    public void stop() {
        launch(() -> {
            if (player != null && playing) {
                player.stop();
                FFViewerDialog.super.stop();
            }
        });
    }

    @Override
    public void setPlaybackSpeed(float speed) {
        launch(() -> {
            playBackSpeed = speed;
            if (player != null) {
                if (speed == 0) {
                    player.stop();
                } else {
                    player.setPlaybackSpeed(speed);
                }
            }
        });
    }

    @Override
    protected float getPlayerFramesPerSecond() {
        return 30; // TODO: Get this frame the native stream
    }

    @Override
    public long getDuration() {
        return (long) (player.getDuration() * 1000);
    }

    @Override
    public long getCurrentTime() {
        return (long) (player.getCurrentTime() * 1000);
    }

    @Override
    protected void cleanUp() {
        player.cleanUp();
    }
}
