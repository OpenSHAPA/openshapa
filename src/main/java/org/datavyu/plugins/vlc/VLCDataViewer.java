package org.datavyu.plugins.vlc;

import org.datavyu.models.Identifier;
import org.datavyu.plugins.CustomActions;
import org.datavyu.plugins.DataViewer;
import org.datavyu.plugins.ViewerStateListener;
import org.datavyu.util.DataViewerUtils;
import org.datavyu.views.component.DefaultTrackPainter;
import org.datavyu.views.component.TrackPainter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class VLCDataViewer implements DataViewer {

    private static final float FALLBACK_FRAME_RATE = 24.0f;

    /** Data viewer Identifier */
    private Identifier id;

    /** Dialog for showing our visualizations */
    private JDialog vlcDialog;

    /** Data viewer offset */
    private long offset;

    /** Data to visualize */
    private File sourceFile;

    /** Boolean to keep track of whether or not we are playing */
    private boolean playing;

    /** Data viewer state listeners */
    private List<ViewerStateListener> stateListeners;

    /** Factory for building our mediaPlayer */
    private MediaPlayerFactory mediaPlayerFactory;

    /** Surface on which we will display video */
    private Canvas videoSurface;

    /** The VLC mediaPlayer */
    private EmbeddedMediaPlayer mediaPlayer;

    /** FPS of the video, calculated on launch */
    private float fps;

    /** Length of the video, calculated on launch */
    private long duration;

    private boolean assumedFPS = false;

    public VLCDataViewer(final Frame parent, final boolean modal) {

        playing = false;
        vlcDialog = new JDialog(parent, modal);
        vlcDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        vlcDialog.setName("VLCDataViewer");
        vlcDialog.setResizable(true);

        // Set an initial size
        vlcDialog.setSize(800, 600);

        videoSurface = new Canvas();
        videoSurface.setBackground(Color.black);

        // Set some options for libvlc
        String libvlcArgs = String.format(
                "-I rc --rc-fake-tty --video-on-top --disable-screensaver --no-video-title-show " +
                        "--no-mouse-events --no-keyboard-events --no-fullscreen --no-video-deco " +
                        "--video-x %d --video-y %d --width %d --height %d --file-caching=10",
                200,      // X
                200,      //Y
                800,    //Width
                600     //Height
        );

        // Create a factory instance (once), you can keep a reference to this
        mediaPlayerFactory = new MediaPlayerFactory(libvlcArgs);

        // Create a media player instance
        mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();

        // Add it to the dialog and place the video onto the surface
        vlcDialog.setLayout(new BorderLayout());
        vlcDialog.add(videoSurface, BorderLayout.CENTER);
        mediaPlayer.setVideoSurface(mediaPlayerFactory.newVideoSurface(videoSurface));
        mediaPlayer.setFullScreen(false);


        stateListeners = new ArrayList<>();

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
        return vlcDialog;
    }

    @Override
    public float getFramesPerSecond() {
        return fps;
    }

    public void setFramesPerSecond(float fpsIn) {
        fps = fpsIn;
        assumedFPS = false;
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
        return offset;
    }

    @Override
    public void setStartTime(final long offset) {
        this.offset = offset;
    }

    @Override
    public TrackPainter getTrackPainter() {
        return new DefaultTrackPainter();
    }

    @Override
    public void setDataViewerVisible(final boolean isVisible) {
        vlcDialog.setVisible(isVisible);
    }

    @Override
    public File getSourceFile() {
        return sourceFile;
    }

    @Override
    public void setSourceFile(final File dataFeed) {
        sourceFile = dataFeed;
        vlcDialog.setVisible(true);
        vlcDialog.setName(vlcDialog.getName() + "-" + dataFeed.getName());
        mediaPlayer.startMedia(dataFeed.getAbsolutePath());

        // Grab FPS and duration

        // Because of the way VLC works, we have to wait for the metadata to become
        // available a short time after we start playing.
        // TODO: reimplement this using the video output event
        try {
            int i = 0;
            while (mediaPlayer.getVideoDimension() == null) {
                if (i > 100)
                    break;
                Thread.sleep(5);
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        fps = mediaPlayer.getFps();
        duration = mediaPlayer.getLength();
        Dimension d = mediaPlayer.getVideoDimension();

        System.out.println(String.format("FPS: %f", fps));
        System.out.println(String.format("Length: %d", duration));

        // Test to see if we should prompt user to convert the video to
        // the ideal format

        // Stop the player. This will rewind whatever
        // frames we just played to get the FPS and duration
        mediaPlayer.pause();
        mediaPlayer.setTime(0);

        playing = false;

        if (d != null) {
            vlcDialog.setSize(d);
        }

        // Test to make sure we got the framerate.
        // If we didn't, alert the user that this
        // may not work right.
        if (fps < 1.0) {
            // VLC can't read the framerate for this video for some reason.
            // Set it to the fallback rate so it is still usable for coding.
            fps = FALLBACK_FRAME_RATE;
            /*
            JOptionPane.showMessageDialog(vlcDialog,
                    "Warning: Unable to detect framerate in video.\n"
                            + "This video may not behave properly. "
                            + "Please try converting to H.264.\n\n"
                            + "This can be done under Controller->Convert Videos.\n"
                            + "Setting framerate to " + FALLBACK_FRAME_RATE);
                    */
        }
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public long getCurrentTime() throws Exception {
        return mediaPlayer.getTime();
    }

    @Override
    public void seek(final long position) {
        Runnable edtTask = new Runnable() {
            @Override
            public void run() {

                long current = mediaPlayer.getTime();

                if (position == current) {
                    return;
                }

                if (!playing) {
                    if (position > 0) {
                        mediaPlayer.setTime(position);
                    } else {
                        mediaPlayer.setTime(0);
                    }
                }
            }
        };

        launchEdtTaskLater(edtTask);
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public void stop() {
        Runnable edtTask = new Runnable() {
            @Override
            public void run() {
                if (playing) {
                    mediaPlayer.pause();
                    playing = false;
                }
            }
        };

        launchEdtTaskLater(edtTask);
    }

    @Override
    public void setPlaybackSpeed(final float rate) {
        Runnable edtTask = new Runnable() {
            @Override
            public void run() {
                if (rate < 0) {
                    // VLC cannot play in reverse, so we're going to rely
                    // on the clock to do fake jumping
                    mediaPlayer.setRate(0);
                    if (playing) {
                        mediaPlayer.pause();
                        playing = false;
                    }
                }
                mediaPlayer.setRate(rate);
            }
        };
        launchEdtTaskLater(edtTask);
    }

    @Override
    public void play() {
        Runnable edtTask = new Runnable() {
            @Override
            public void run() {
                if (!playing && mediaPlayer.getRate() > 0) {
                    mediaPlayer.play();
                    playing = true;
                }
            }
        };

        launchEdtTaskLater(edtTask);
    }

    @Override
    public void storeSettings(final OutputStream os) {
        try {
            Properties settings = new Properties();
            settings.setProperty("offset", Long.toString(getStartTime()));
            settings.setProperty("height", Integer.toString(vlcDialog.getHeight()));
            settings.setProperty("fps", Float.toString(fps));
            settings.store(os, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void loadSettings(final InputStream is) {

        try {
            DataViewerUtils.loadDefaults(this, is);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void addViewerStateListener(
            final ViewerStateListener vsl) {

        if (vsl != null) {
            stateListeners.add(vsl);
        }
    }

    @Override
    public void removeViewerStateListener(
            final ViewerStateListener vsl) {

        if (vsl != null) {
            stateListeners.remove(vsl);
        }
    }

    @Override
    public CustomActions getCustomActions() {
        return null;
    }

    @Override
    public void clearSourceFile() {
        stop();
        videoSurface.setVisible(false);
        vlcDialog.setVisible(false);
        mediaPlayerFactory.release();
    }

    //@Override
    //public void setDataStore(final DataStore sDB) {
    //    // TODO Auto-generated method stub
    //}

    public boolean isAssumedFramesPerSecond() {
        return assumedFPS;
    }

}
