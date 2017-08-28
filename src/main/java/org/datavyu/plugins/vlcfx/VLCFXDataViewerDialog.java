package org.datavyu.plugins.vlcfx;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.models.Identifier;
import org.datavyu.plugins.StreamViewerDialog;
import org.datavyu.plugins.VlcLibraryLoader;
import org.datavyu.views.component.DefaultTrackPainter;
import org.datavyu.views.component.TrackPainter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;


public class VLCFXDataViewerDialog extends StreamViewerDialog {

    /** Logger for this class */
    private static Logger logger = LogManager.getLogger(NativeLibraryManager.class);

    /** Data viewer Identifier */
    private Identifier id;

    /** Data viewer startTime */
    private long startTime;

    /** Data to visualize */
    private File sourceFile;

    /** The last jog position, making sure we are only calling jog once VLC has issues when trying to go to the same
     * spot multiple times */
    private JDialog dialog = new JDialog();

    /** VLC application */
    private VLCApplication vlcFxApp;

    public VLCFXDataViewerDialog(final Frame parent, final boolean modal) {
        super(parent, modal);
        dialog.setVisible(false);
    }

    public static void runAndWait(final Runnable action) {
        if (action == null) {
            throw new NullPointerException("Action object " + action + " is null!");
        }

        VlcLibraryLoader.load();

        // run synchronously on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        // queue on JavaFX thread and wait for completion
        final CountDownLatch doneLatch = new CountDownLatch(1);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    action.run();
                } catch (Exception e){
                    logger.error("Error when trying to run the application: ", e);
                } finally {
                    doneLatch.countDown();
                }
            }
        });

        try {
            doneLatch.await();
        } catch (InterruptedException ie) {
            logger.error("Interrupt when waiting for the latch: ", ie);
        }
    }

    protected void setPlayerVolume(float volume) {
        vlcFxApp.setVolume(volume);
    }

    private void launchEdtTaskNow(Runnable edtTask) {
        if (SwingUtilities.isEventDispatchThread()) {
            edtTask.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(edtTask);
            } catch (Exception e) {
                logger.error("Error: ", e);
            }
        }
    }

    private void launchEdtTaskLater(Runnable edtTask) {
        if (SwingUtilities.isEventDispatchThread()) {
            edtTask.run();
        } else {
            try {
                SwingUtilities.invokeLater(edtTask);
            } catch (Exception e) {
                logger.error("Error: ", e);
            }
        }
    }

    @Override
    public JDialog getParentJDialog() {
        return dialog;
    }

    @Override
    protected void setPlayerSourceFile(File playerSourceFile) { }

    @Override
    protected Dimension getOriginalVideoSize() {
        return null;
    }

    @Override
    protected float getPlayerFramesPerSecond() {
        return getFramesPerSecond();
    }

    @Override
    public float getFramesPerSecond() {
        return vlcFxApp.getFrameRate();
    }

    public void setFramesPerSecond(float framesPerSecond) {
        // TODO: Set the fps correctly
        //fps = fpsIn;
        //assumedFPS = false;
    }

    @Override
    public float getDetectedFrameRate() {
        // TODO: Correct to detect a frame rate
        return 30;
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    @Override
    public void setIdentifier(final Identifier id) {
        this.id = id;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(final long offset) {
        this.startTime = offset;
    }

    @Override
    public TrackPainter getTrackPainter() {
        return new DefaultTrackPainter();
    }

    @Override
    public void setViewerVisible(final boolean isVisible) {
        vlcFxApp.setVisible(isVisible);
    }

    @Override
    public void setVisible(final boolean isVisible) {
        vlcFxApp.setVisible(isVisible);
    }

    @Override
    public File getSourceFile() {
        return sourceFile;
    }

    @Override
    public void setSourceFile(final File sourceFile) {
        this.sourceFile = sourceFile;

        // Needed to init JavaFX stuff
        new JFXPanel();
        vlcFxApp = new VLCApplication(sourceFile);

        runAndWait(new Runnable() {
            @Override
            public void run() {
                vlcFxApp.start(new Stage());
            }
        });


        // Wait for javafx to initialize
        // TODO: We need to change this, because it can lead to hang-up (e.g. timeout)
        while (!vlcFxApp.isInitialized()) {
        }

        // Hide our fake dialog box
        dialog.setVisible(false);

        // TODO: Add in function to guess framerate
    }

    @Override
    public long getDuration() { return vlcFxApp.getDuration(); }

    @Override
    public long getCurrentTime() {
        return vlcFxApp.getCurrentTime();
    }

    @Override
    public void seek(final long position) {
        vlcFxApp.seek(position);
    }

    @Override
    public boolean isPlaying() {
        return vlcFxApp.isPlaying();
    }

    @Override
    public void stop() {
        super.stop();
        vlcFxApp.pause();
    }

    @Override
    public void setPlaybackSpeed(final float rate) {
        vlcFxApp.setRate(rate);
    }

    @Override
    public void play() {
        super.play();
        vlcFxApp.play();
    }

    @Override
    protected void cleanUp() {
        VlcLibraryLoader.purge();
        unsetSourceFile();
    }

    @Override
    public void unsetSourceFile() {
        stop();
        vlcFxApp.setVisible(false);
        vlcFxApp.closeAndDestroy();
    }

    public boolean isAssumedFramesPerSecond() {
        return vlcFxApp.isAssumedFps();
    }

    @Override
    public void storeSettings(final OutputStream os) {
        try {
            Properties settings = new Properties();
            settings.setProperty("startTime", Long.toString(getStartTime()));
            settings.setProperty("volume", Float.toString(getVolume()));
            settings.setProperty("visible", Boolean.toString(vlcFxApp.isVisible()));
            settings.setProperty("height", Integer.toString(vlcFxApp.getHeight()));
            settings.setProperty("fps", Float.toString(vlcFxApp.getFrameRate()));
            settings.store(os, null);
        } catch (IOException io) {
            logger.error("Unable to save the settings. Error: ", io);
        }
    }
}
