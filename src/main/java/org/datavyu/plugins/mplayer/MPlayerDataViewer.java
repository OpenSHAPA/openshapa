package org.datavyu.plugins.mplayer;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import org.datavyu.models.db.DataStore;
import org.datavyu.plugins.CustomActions;
import org.datavyu.plugins.CustomActionsAdapter;
import org.datavyu.plugins.ViewerStateListener;
import org.datavyu.plugins.BaseDataViewer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class MPlayerDataViewer extends BaseDataViewer {

    /**
     * Data viewer offset.
     */
    private long offset;
    /**
     * Data to visualize.
     */
    private File data;
    /**
     * Boolean to keep track of whether or not we are playing
     */
    private boolean playing;


    /**
     * The last jog position, making sure we are only calling jog once
     * VLC has issues when trying to go to the same spot multiple times
     */
    private JDialog dialog = new JDialog();
    private MPlayerApplication javafxapp;
    private boolean assumedFPS = false;


    public MPlayerDataViewer(final Frame parent, final boolean modal) {
        super(parent, modal);
        javafxapp = new MPlayerApplication(null);
    }

    public static void runAndWait(final Runnable action) {
        if (action == null)
            throw new NullPointerException("action");

        // run synchronously on JavaFX thread
        System.out.println("AM I THE JAVAFX THREAD?:" + Platform.isFxApplicationThread());
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        // queue on JavaFX thread and wait for completion
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("RUNNING ACTION");
                    System.out.println("AM I THE JAVAFX THREAD?:" + Platform.isFxApplicationThread());
                    action.run();
                    System.out.println("I RAN THE ACTION");
                } finally {
//                    doneLatch.countDown();
                }
            }
        });

        System.out.println("TESTING WEHTEHR IT RETURNS");
    }

    @Override
    protected void setPlayerVolume(float volume) {
        javafxapp.setVolume(volume);
    }

    private void launchEdtTaskNow(Runnable edtTask) {
        if (SwingUtilities.isEventDispatchThread()) {
            edtTask.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(edtTask);
            } catch (Exception e) {
                e.printStackTrace();
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
                e.printStackTrace();
            }
        }
    }

    @Override
    public JDialog getParentJDialog() {
        return dialog;
    }

    @Override
    public float getFramesPerSecond() {
        return javafxapp.getFrameRate();
    }

    public void setFramesPerSecond(float fpsIn) {
        assumedFPS = false;
    }

    @Override
    public float getDetectedFrameRate() {
        return getFramesPerSecond();
    }

    @Override
    public long getStartTime() {
        return offset;
    }

    @Override
    public void setStartTime(final long offset) {
        this.offset = offset;
    }

    @Override
    public void setDataViewerVisible(final boolean isVisible) {
        javafxapp.setVisible(isVisible);
        this.isVisible = isVisible;
    }

    @Override
    public File getSourceFile() {
        return data;
    }

    @Override
    public void setSourceFile(final File sourceFile) {

        final CountDownLatch latch = new CountDownLatch(1);
        System.out.println("Setting datafeed");
        data = sourceFile;
        Platform.setImplicitExit(false);

        javafxapp = new MPlayerApplication(sourceFile);

        System.out.println(SwingUtilities.isEventDispatchThread());
        System.out.println(Platform.isFxApplicationThread());

        runAndWait(new Runnable() {
            @Override
            public void run() {

                javafxapp.start(new Stage());
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (!javafxapp.isInit()) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Inited, going");
        System.out.println(javafxapp.getDuration());
        // Hide our fake dialog box
        dialog.setVisible(false);
        // TODO Add in function to guess framerate
    }


    /**
     * Scales the video to the desired ratio.
     *
     * @param scale The new ratio to scale to, where 1.0 = original size, 2.0 = 200% zoom, etc.
     */
    @Override
    protected void resizeVideo(final float scale) {
        javafxapp.setScale(scale);

        notifyChange();
    }

    @Override
    protected void setPlayerSourceFile(File videoFile) {

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
        return javafxapp.getDuration();
    }

    @Override
    public long getCurrentTime() {
        return javafxapp.getCurrentTime();
    }

    @Override
    public void seek(final long position) {
        javafxapp.seek(position);
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public void stop() {
        playing = false;
        javafxapp.pause();
    }

    @Override
    public void setPlaybackSpeed(final float rate) {
        javafxapp.setRate(rate);
    }

    @Override
    public void play() {
        playing = true;
        javafxapp.play();
    }

    @Override
    protected void cleanUp() {

    }

    @Override
    public void clearSourceFile() {
        stop();
        javafxapp.setVisible(false);
        javafxapp.closeAndDestroy();
    }

    @Override
    public void setDataStore(final DataStore sDB) {
        // TODO Auto-generated method stub
    }

    public boolean isAssumedFramesPerSecond() {
        return assumedFPS;
    }

}
