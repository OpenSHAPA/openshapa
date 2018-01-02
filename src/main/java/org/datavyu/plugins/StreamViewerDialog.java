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
package org.datavyu.plugins;

import net.miginfocom.swing.MigLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.models.Identifier;
import org.datavyu.views.DatavyuDialog;
import org.datavyu.views.component.DefaultTrackPainter;
import org.datavyu.views.component.TrackPainter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * This dialog supports basic viewing operations that all video viewers have in common. These are
 *   - controlling the volume,
 *   - resizing the display, and
 *   - controls the video stream.
 */
public abstract class StreamViewerDialog extends DatavyuDialog implements StreamViewer {

    /** Text for volume icon */
    private static final String VOLUME_TOOLTIP = "Change volume";

    /** Text for resize icon */
    private static final String RESIZE_TOOLTIP = "Resize video";

    /** Logger for this class */
    private static Logger logger = LogManager.getLogger(StreamViewerDialog.class);

    /** Icon for volume slider */
    private final ImageIcon volumeIcon = new ImageIcon(getClass().getResource("/icons/volume.png"));

    /** Icon for when volume is muted */
    private final ImageIcon mutedIcon = new ImageIcon(getClass().getResource("/icons/volume-muted.png"));

    /** Icon for resizing the video */
    private final ImageIcon resizeIcon = new ImageIcon(getClass().getResource("/icons/resize.png"));

    /** List of listeners interested in changes made to the project */
    private final List<ViewerStateListener> viewerListeners = new LinkedList<>();

    /** Volume for play back in dB? */
    protected float volume = 1f;

    /** Controls visibility */
    protected boolean isVisible = true;

    /** Did we assume the frames per second? */
    protected boolean isAssumedFramesPerSecond = false;

    /** Original size of the video */
    private Dimension originalVideoSize;

    /** The playback speed */
    protected float playBackSpeed = 0;

    /** Frames per second */
    private float framesPerSecond = -1;

    /** Start time of stream in milliseconds */
    // TODO: Why do we have this startTime? Is it the current time in the stream?
    private long startTime = 0;

    /** Is this movie currently playing? */
    protected boolean playing = false;

    /** The current video file */
    private File sourceFile;

    /** Volume slider */
    private JSlider volumeSlider;

    /** Dialog containing volume slider */
    private JDialog volumeDialog;

    /** Volume button */
    private JButton volumeButton;

    /** Resize button */
    private JButton resizeButton;

    /** Custom actions handler */
    private CustomActions actions = new CustomActionsAdapter() {
        @Override
        public AbstractButton getActionButton1() {
            return volumeButton;
        }

        @Override
        public AbstractButton getActionButton2() {
            return resizeButton;
        }

        @Override
        public AbstractButton getActionButton3() {
            return null;
        }
    };

    /** A context menu for resizing the video */
    private JPopupMenu menuForResize = new JPopupMenu();

    /** Identifier of this data viewer */
    private Identifier id;

    /** Enable fake playback for this stream viewer */
    private boolean enableFakePlayback;

    /** Is this stream viewer is in fake play back*/
    private boolean fakePlayback;

    /**
     * Constructs a base data video viewer.
     */
    public StreamViewerDialog(final Frame parent, final boolean modal) {

        super(parent, modal);

        startTime = 0;
        playing = false;

        volumeButton = new JButton();
        volumeButton.setIcon(getVolumeButtonIcon());
        volumeButton.setBorderPainted(false);
        volumeButton.setContentAreaFilled(false);
        volumeButton.setToolTipText(VOLUME_TOOLTIP);
        volumeButton.addActionListener(e -> handleActionButtonEvent1(e));
        volumeSlider = new JSlider(JSlider.VERTICAL, 0, 100, 70);
        volumeSlider.setMajorTickSpacing(10);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setName("volumeSlider");
        volumeSlider.addChangeListener(e -> handleVolumeSliderEvent(e));
        volumeDialog = new JDialog(parent, false);
        volumeDialog.setUndecorated(true);
        volumeDialog.setVisible(false);
        volumeDialog.setLayout(new MigLayout("", "[center]", ""));
        volumeDialog.setSize(50, 125);
        volumeDialog.setName("volumeDialog");
        volumeDialog.getContentPane().add(volumeSlider, "pushx, pushy");
        volumeDialog.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                volumeDialog.setVisible(false);
            }
        });
        volumeDialog.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(final WindowEvent e) {
                volumeDialog.setVisible(false);
            }
        });

        resizeButton = new JButton();
        resizeButton.setIcon(resizeIcon);
        resizeButton.setBorderPainted(false);
        resizeButton.setContentAreaFilled(false);
        resizeButton.setToolTipText(RESIZE_TOOLTIP);
        resizeButton.addActionListener(e -> handleActionButtonEvent2(e));
        JMenuItem menuItemQuarter = new JMenuItem("25% size");
        menuItemQuarter.addActionListener(e -> resizeVideo(0.25f));
        JMenuItem menuItemHalf = new JMenuItem("50% size");
        menuItemHalf.addActionListener(e -> resizeVideo(0.5f));
        JMenuItem menuItemThreeQuarters = new JMenuItem("75% size");
        menuItemThreeQuarters.addActionListener(e -> resizeVideo(0.75f));
        JMenuItem menuItemFull = new JMenuItem("100% size");
        menuItemFull.addActionListener(e -> resizeVideo(1.0f));
        JMenuItem menuItemOneAndHalf = new JMenuItem("150% size");
        menuItemOneAndHalf.addActionListener(e -> resizeVideo(1.5f));
        JMenuItem menuItemDouble = new JMenuItem("200% size");
        menuItemDouble.addActionListener(e -> resizeVideo(2.0f));
        menuForResize.add(menuItemQuarter);
        menuForResize.add(menuItemHalf);
        menuForResize.add(menuItemThreeQuarters);
        menuForResize.add(menuItemFull);
        menuForResize.add(menuItemOneAndHalf);
        menuForResize.add(menuItemDouble);
        menuForResize.setName("menuForResize");

        initComponents();
    }

    @Override
    public void setEnableFakePlayback(boolean enableFakePlayback) {
        this.enableFakePlayback = enableFakePlayback;
    }

    @Override
    public boolean isEnableFakePlayback() {
        return enableFakePlayback;
    }

    @Override
    public void setFakePlayback(boolean fakePlayback) {
        this.fakePlayback = fakePlayback;
    }

    @Override
    public boolean isFakePlayback() {
        return fakePlayback;
    }

    private void handleVolumeSliderEvent(final ChangeEvent e) {
        volume = volumeSlider.getValue() / 100F;
        setVolume();
        notifyChange();
    }

    /**
     * Sets the volume of the movie to the level of the slider bar, or to 0
     * if the track is hidden from view (this means hiding the track mutes
     * the volume).
     */
    private void setVolume() {
        setPlayerVolume(isVisible ? volume : 0F);
        volumeButton.setIcon(getVolumeButtonIcon());
    }

    public void setVolume(float volume) {
        setPlayerVolume(volume);
    }

    public float getVolume() {
        return volume;
    }

    protected abstract void setPlayerVolume(float volume);

    protected abstract void setPlayerSourceFile(final File playerSourceFile);

    protected abstract Dimension getOriginalVideoSize();

    /**
     * {@inheritDoc}
     */
    public abstract void seek(final long position);

    protected abstract float getPlayerFramesPerSecond();

    /**
     * @return The duration of the movie in milliseconds. If -1 is returned, the
     * movie's duration cannot be determined.
     */
    public abstract long getDuration();

    /**
     * Get the aspect ratio for the images.
     *
     * @return Aspect ratio as width/height.
     */
    private double getAspectRatio() {
        return (originalVideoSize != null) ? (originalVideoSize.getWidth() / originalVideoSize.getHeight()) : 1;
    }

    @Override
    public void validate() {

        // BugzID:753 - Locks the window to the videos aspect ratio.
        int newHeight = getHeight();
        int newWidth = (int) (getVideoHeight() * getAspectRatio()) + getInsets().left + getInsets().right;
        setSize(newWidth, newHeight);

        super.validate();
    }

    /**
     * Resize the video to the desired scale.
     *
     * @param scale The new ratio to scale to, where 1.0 = original size, 2.0 = 200% zoom, etc.
     */
    protected void resizeVideo(final float scale) {

        // Resampling is assumed to be done in the plugin
        int scaleHeight = (int) (originalVideoSize.getHeight() * scale);

        // lock the aspect ratio
        if (getAspectRatio() > 0.0) {
            int newWidth = (int) (scaleHeight * getAspectRatio()) + getInsets().left + getInsets().right;
            int newHeight = scaleHeight + getInsets().bottom + getInsets().top;

            setSize(newWidth, newHeight);
            validate();
        }
        notifyChange();
    }

    /**
     * Get the width of the video image.
     *
     * @return
     */
    public int getVideoHeight() {
        // TODO:Have variables for JUST the video size
        return getHeight() - getInsets().bottom - getInsets().top;
    }

    /**
     * Set the height of the video.
     *
     * @param height
     */
    private void setVideoHeight(final int height) {

        // TODO: How can that happen?
        if (!(getAspectRatio() > 0)) {
            return;
        }

        int newWidth = (int) (height * getAspectRatio()) + getInsets().left + getInsets().right;
        int newHeight = height + getInsets().bottom + getInsets().top;

        setSize(newWidth, newHeight);
        validate();
    }

    public int getVideoWidth() {
        return getWidth() - getInsets().left - getInsets().right;
    }

    /**
     * @return The playback startTime of the movie in milliseconds.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @param offset The playback startTime of the movie in milliseconds.
     */
    public void setStartTime(final long offset) {
        this.startTime = offset;
        // TODO: Should set startTime in stream?
    }

    /**
     * @return The parent JDialog that this data viewer resides within.
     */
    public JDialog getParentJDialog() {
        return this;
    }

    public float getDetectedFrameRate() {
        return getPlayerFramesPerSecond();
    }

    /**
     * @return The file used to display this data feed.
     */
    public File getSourceFile() {
        return sourceFile;
    }

    @Override
    public void setSourceFile(final File sourceFile) {
        this.sourceFile = sourceFile;
        setTitle(sourceFile.getName());
        setName(getClass().getSimpleName() + "-" + sourceFile.getName());
        pack();
        invalidate();

        // BugzID:679 + 2407: Need to make the window visible before we know the
        // dimensions because of a QTJava bug
        setViewerVisible(true);
        setPlayerSourceFile(sourceFile);

        originalVideoSize = getOriginalVideoSize();
        logger.info("Setting video size to:" + originalVideoSize);
        setPreferredSize(originalVideoSize);
        setBounds(getX(), getY(), (int) originalVideoSize.getWidth(), (int) originalVideoSize.getHeight());
        pack();

        if (framesPerSecond == -1) {
            framesPerSecond = getPlayerFramesPerSecond();
        } // otherwise we loaded it from the settings

        logger.info("Frames per second: " + framesPerSecond);

        // Display the first frame
        seek(0L);
    }

    /**
     * @return The frames per second.
     */
    public float getFramesPerSecond() {
        return framesPerSecond;
    }
    
    public void setFramesPerSecond(float framesPerSecond) {
        this.framesPerSecond = framesPerSecond;
        isAssumedFramesPerSecond = false;
    }

    public float getPlaybackSpeed() {
        return playBackSpeed;
    }

    /**
     * {@inheritDoc}
     */
    public void setPlaybackSpeed(final float speed) {
        playBackSpeed = speed;
    }

    /**
     * {@inheritDoc}
     */
    public void play() {
        playing = true;
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        playing = false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * {@inheritDoc}
     */
    public abstract long getCurrentTime();

    /**
     * {@inheritDoc}
     */
    public TrackPainter getTrackPainter() {
        return new DefaultTrackPainter();
    }

    /**
     * Shows an interface for toggling the playback volume.
     *
     * #handleActionButtonEvent1(java.awt.event.ActionEvent)
     */
    private void handleActionButtonEvent1(final ActionEvent event) {
        // BugzID:1400 - We don't allow volume changes while the track is hidden.
        if (isVisible) {
            volumeDialog.setLocation(volumeButton.getLocationOnScreen());
            volumeDialog.setVisible(true);
        }
    }

    private void handleActionButtonEvent2(final ActionEvent event) {
        if (isVisible) {
            menuForResize.show(resizeButton.getParent(), resizeButton.getX(), resizeButton.getY());
        }
    }

    /**
     * Notifies listeners about a change
     */
    protected void notifyChange() {
        for (ViewerStateListener listener : viewerListeners) {
            listener.notifyStateChanged(null, null);
        }
    }

    public void loadSettings(final InputStream is) {
        Properties settings = new Properties();
        try {
            settings.load(is);

            String property = settings.getProperty("startTime");
            if ((property != null) && !property.equals("")) {
                setStartTime(Long.parseLong(property));
            }

            property = settings.getProperty("volume");
            if ((property != null) && !property.equals("")) {
                volumeSlider.setValue((int) (Float.parseFloat(property) * 100));
            }

            property = settings.getProperty("visible");
            if ((property != null) && !property.equals("")) {
                this.setVisible(Boolean.parseBoolean(property));
                setVolume();
            }

            property = settings.getProperty("height");
            if ((property != null) && !property.equals("")) {
                setVideoHeight(Integer.parseInt(property));
            }

            property = settings.getProperty("framesPerSecond");
            if ((property != null) && !property.equals("")) {
                framesPerSecond = Float.parseFloat(property);
            }
        } catch (IOException e) {
            logger.error("Error loading settings", e);
        }
    }

    public void storeSettings(final OutputStream os) {
        Properties settings = new Properties();
        settings.setProperty("startTime", Long.toString(getStartTime()));
        settings.setProperty("volume", Float.toString(volume));
        settings.setProperty("visible", Boolean.toString(isVisible));
        settings.setProperty("height", Integer.toString(getVideoHeight()));
        settings.setProperty("framesPerSecond", Float.toString(framesPerSecond));
        try {
            settings.store(os, null);
        } catch (IOException e) {
            logger.error("Error saving settings", e);
        }
    }

    @Override
    public void addViewerStateListener(final ViewerStateListener vsl) {
        viewerListeners.add(vsl);
    }

    @Override
    public void removeViewerStateListener(final ViewerStateListener vsl) {
        viewerListeners.remove(vsl);
    }

    private ImageIcon getVolumeButtonIcon() {
        return isVisible && (volume > 0) ? volumeIcon : mutedIcon;
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        pack();
    }

    /**
     * Action to invoke when the window is hidden.
     *
     * @param evt The event that triggered this action.
     */
    private void formWindowClosing(final WindowEvent evt) {
        stop();
        volumeDialog.setVisible(false);
        isVisible = false;
        setVolume();
    }

    protected abstract void cleanUp();

    @Override
    public CustomActions getCustomActions() {
        return actions;
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
    public void unsetSourceFile() {
        cleanUp();
    }

    @Override
    public void setViewerVisible(final boolean isVisible) {
        setVisible(isVisible);
        this.isVisible = isVisible;
        setVolume();
    }

    public boolean isAssumedFramesPerSecond() {
        return isAssumedFramesPerSecond;
    }
}
