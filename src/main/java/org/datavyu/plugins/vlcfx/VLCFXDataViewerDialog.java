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

    /** The last jog position, making sure we are only calling jog once VLC has issues when trying to go to the same
     * spot multiple times */
    private JDialog dialog = new JDialog();

    /** VLC application */
    private VLCApplication vlcApp;

    VLCFXDataViewerDialog(final Identifier identifier, final File sourceFile, final Frame parent, final boolean modal) {
        super(identifier, parent, modal);
        dialog.setVisible(false);
        this.sourceFile = sourceFile;
    }

    static void runAndWait(final Runnable action) {
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
        vlcApp.setVolume(volume);
    }


    @Override
    public JDialog getParentJDialog() {
        return dialog;
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
    public float getFramesPerSecond() {
        return vlcApp.getFrameRate();
    }

    public void setFramesPerSecond(float framesPerSecond) {
        // TODO: Set the fps correctly
        //fps = fpsIn;
        //assumedFPS = false;
    }

    @Override
    public TrackPainter getTrackPainter() {
        return new DefaultTrackPainter();
    }

    @Override
    public void setViewerVisible(final boolean isVisible) {
        vlcApp.setVisible(isVisible);
    }

    @Override
    public void setVisible(final boolean isVisible) {
        vlcApp.setVisible(isVisible);
    }


    @Override
    public void adjustFrameWithSourceFile(final File sourceFile) {

        // Needed to init JavaFX stuff
        new JFXPanel();
        vlcApp = new VLCApplication(sourceFile);

        runAndWait(new Runnable() {
            @Override
            public void run() {
                vlcApp.start(new Stage());
            }
        });


        // Wait for javafx to initialize
        // TODO: We need to change this, because it can lead to hang-up (e.g. timeout)
        while (!vlcApp.isInitialized()) {
        }

        // Hide our fake dialog box
        dialog.setVisible(false);

        // TODO: Add in function to guess framerate
    }

    @Override
    public long getDuration() { return vlcApp.getDuration(); }

    @Override
    public long getCurrentTime() {
        return vlcApp.getCurrentTime();
    }

    @Override
    public void setCurrentTime(final long time) {
        vlcApp.seek(time);
    }

    @Override
    public boolean isPlaying() {
        return vlcApp.isPlaying();
    }

    @Override
    public void stop() {
        super.stop();
        vlcApp.pause();
    }

    @Override
    public void setRate(final float rate) {
        vlcApp.setRate(rate);
    }

    @Override
    public void start() {
        super.start();
        vlcApp.play();
    }

    @Override
    protected void cleanUp() {
        VlcLibraryLoader.purge();
        close();
    }

    @Override
    public void close() {
        stop();
        vlcApp.setVisible(false);
        vlcApp.closeAndDestroy();
    }

    public boolean isAssumedFramesPerSecond() {
        return vlcApp.isAssumedFps();
    }

    @Override
    public void storeSettings(final OutputStream os) {
        try {
            Properties settings = new Properties();
            settings.setProperty("startTime", Long.toString(getOffset()));
            settings.setProperty("volume", Float.toString(getVolume()));
            settings.setProperty("visible", Boolean.toString(vlcApp.isVisible()));
            settings.setProperty("height", Integer.toString(vlcApp.getHeight()));
            settings.setProperty("fps", Float.toString(vlcApp.getFrameRate()));
            settings.store(os, null);
        } catch (IOException io) {
            logger.error("Unable to save the settings. Error: ", io);
        }
    }
}
