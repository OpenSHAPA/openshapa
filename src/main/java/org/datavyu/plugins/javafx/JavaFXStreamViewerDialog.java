package org.datavyu.plugins.javafx;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.models.Identifier;
import org.datavyu.plugins.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.CountDownLatch;


public class JavaFXStreamViewerDialog extends StreamViewerDialog {

    private static Logger logger = LogManager.getLogger(JavaFXStreamViewerDialog.class);

    /** Data to visualize */
    private File sourceFile;

    /** Boolean to keep track of whether or not we are isPlaying */
    private boolean playing;

    /** FPS of the video, calculated on launch */
    private float fps;

    private JDialog dialog = new JDialog();

    private JavaFXApplication jfxApp;

    private boolean assumedFPS = false;


    JavaFXStreamViewerDialog(Identifier identifier, File sourceFile, final Frame parent, final boolean modal) {
        super(identifier, parent, modal);
        jfxApp = new JavaFXApplication(sourceFile);
        adjustFrameWithSourceFile(sourceFile);
    }

    static void runAndWait(final Runnable action) {
        if (action == null)
            throw new NullPointerException("action");

        VlcLibraryLoader.load();

        // run synchronously on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            logger.info("Javax thread running action.");
            action.run();
            return;
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("Running action " + (Platform.isFxApplicationThread() ? " as JavaFx thread." : "."));
                    action.run();
                    logger.info("Action ran successful.");
                } catch (Exception e) {
                    logger.error("Exception occurred when running action.", e);
                }
            }
        });
    }

    @Override
    protected void setPlayerVolume(float volume) {
        jfxApp.setVolume(volume);
    }

    @Override
    public JDialog getParentJDialog() {
        return dialog;
    }

    @Override
    public float getFramesPerSecond() {
        return jfxApp.getFrameRate();
    }

    public void setFramesPerSecond(float framesPerSecond) {
        fps = framesPerSecond;
        assumedFPS = false;
    }

    @Override
    public void setViewerVisible(final boolean isVisible) {
        jfxApp.setVisible(isVisible);
        this.isVisible = isVisible;
    }

    @Override
    public File getSourceFile() {
        return sourceFile;
    }

    @Override
    public void adjustFrameWithSourceFile(final File sourceFile) {
        logger.info("Set source file: " + sourceFile.getAbsolutePath());

        final CountDownLatch latch = new CountDownLatch(1);
        this.sourceFile = sourceFile;
        Platform.setImplicitExit(false);

        jfxApp = new JavaFXApplication(sourceFile);

        logger.info("Is event dispatch thread? " + (SwingUtilities.isEventDispatchThread() ? "Yes" : "No") + ".");
        logger.info("Is FX application thread? " + (Platform.isFxApplicationThread() ? "Yes" : "No") + ".");

        runAndWait(new Runnable() {
            @Override
            public void run() {
                jfxApp.start(new Stage());
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (Exception e) {
            logger.error("Latch await failed. Error: ", e);
        }

        while (!jfxApp.isInit()) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                logger.error("Thread awakened. Error: ", e);
            }
        }

        logger.info("Finished setting source: " + sourceFile);
        logger.info("Duration is: " + jfxApp.getDuration());

        // Hide our fake dialog box
        dialog.setVisible(false);

        // TODO Add in function to guess frame rate
    }


    /**
     * Scales the video to the desired ratio.
     *
     * @param scale The new ratio to scale to, where 1.0 = original size, 2.0 = 200% zoom, etc.
     */
    @Override
    protected void resizeVideo(final float scale) {
        jfxApp.setScale(scale);
        notifyChange();
    }

    @Override
    protected Dimension getOriginalVideoSize() {
        return null;
    }

    @Override
    protected float getPlayerFramesPerSecond() {
        return getFramesPerSecond();
    }

    @Override
    public long getDuration() {
        return jfxApp.getDuration();
    }

    @Override
    public long getCurrentTime() {
        return jfxApp.getCurrentTime();
    }

    @Override
    public void setCurrentTime(final long time) {
        jfxApp.seek(time);
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public void stop() {
        playing = false;
        jfxApp.pause();
    }

    @Override
    public void setRate(final float rate) {
        jfxApp.setRate(rate);
    }

    @Override
    public void start() {
        playing = true;
        jfxApp.play();
    }

    @Override
    protected void cleanUp() {
        VlcLibraryLoader.purge();
    }

    @Override
    public void close() {
        stop();
        jfxApp.setVisible(false);
        jfxApp.closeAndDestroy();
    }
}
