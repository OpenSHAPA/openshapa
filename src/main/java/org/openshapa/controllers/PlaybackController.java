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
package org.openshapa.controllers;

import java.awt.event.InputEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang.NotImplementedException;

import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

import org.openshapa.OpenSHAPA;

import org.openshapa.controllers.component.MixerController;
import org.openshapa.controllers.id.IDController;

import org.openshapa.event.PlaybackEvent;
import org.openshapa.event.PlaybackListener;
import org.openshapa.event.component.CarriageEvent;
import org.openshapa.event.component.TracksControllerEvent;
import org.openshapa.event.component.TracksControllerListener;

import org.openshapa.logging.PlaybackLogging;

import org.openshapa.models.PlaybackModel;
import org.openshapa.models.component.MixerConstants;
import org.openshapa.models.component.RegionState;
import org.openshapa.models.component.ViewportStateImpl;
import org.openshapa.models.component.ViewportState;
import org.openshapa.models.id.Identifier;

import org.openshapa.plugins.PluginManager;

import org.openshapa.util.ClockTimer;
import org.openshapa.util.ClockTimer.ClockListener;
import org.openshapa.util.FloatUtils;

import org.openshapa.views.LinuxJFC;
import org.openshapa.views.MacOSJFC;
import org.openshapa.views.PlaybackV;
import org.openshapa.views.PluginChooser;
import org.openshapa.views.WindowsJFC;
import org.openshapa.views.component.TrackPainter;


import com.usermetrix.jclient.Logger;
import com.usermetrix.jclient.UserMetrix;
import org.openshapa.plugins.DataViewer;
import org.openshapa.plugins.Plugin;
import org.openshapa.views.DataController;


/**
 * Quicktime video controller.
 */
public final class PlaybackController implements PlaybackListener,
    ClockListener, TracksControllerListener, DataController, PropertyChangeListener {

    /** One second in milliseconds. */
    private static final long ONE_SECOND = 1000L;

    /** Rate of playback for rewinding. */
    private static final float REWIND_RATE = -32F;

    /** Rate of normal playback. */
    private static final float PLAY_RATE = 1F;

    /** Rate of playback for fast forwarding. */
    private static final float FFORWARD_RATE = 32F;

    /** Sequence of allowable shuttle rates. */
    private static final float[] SHUTTLE_RATES;

    /**
     * The threshold to use while synchronising viewers (augmented by rate).
     */
    private static final long SYNC_THRESH = 200;

    /**
     * How often to synchronise the viewers with the master clock.
     */
    private static final long SYNC_PULSE = 500;

    // Initialize SHUTTLE_RATES
    // values: [ (2^-5), ..., (2^0), ..., (2^5) ]

    /** The max power used for playback rates; i.e. 2^POWER = max. */
    private static final int POWER = 5;

    static {
        SHUTTLE_RATES = new float[(2 * POWER) + 1];

        float value = 1;
        SHUTTLE_RATES[POWER] = value;

        for (int i = 1; i <= POWER; ++i) {
            value *= 2;
            SHUTTLE_RATES[POWER + i] = value;
            SHUTTLE_RATES[POWER - i] = 1F / value;
        }
    }

    /** The jump multiplier for shift-jogging. */
    private static final int SHIFTJOG = 5;

    /** The jump multiplier for ctrl-shift-jogging. */
    private static final int CTRLSHIFTJOG = 10;

    /** Format for representing time. */
    private static final DateFormat CLOCK_FORMAT;

    // initialize standard date format for clock display.
    static {
        CLOCK_FORMAT = new SimpleDateFormat("HH:mm:ss:SSS");
        CLOCK_FORMAT.setTimeZone(new SimpleTimeZone(0, "NO_ZONE"));
    }

    /**
     * Enumeration of shuttle directions.
     */
    enum ShuttleDirection {

        /** The backwards playrate. */
        BACKWARDS(-1),

        /** Playrate for undefined shuttle speeds. */
        UNDEFINED(0),

        /** The playrate for forwards (normal) playrate. */
        FORWARDS(1);

        /** Stores the shuttle direction. */
        private int parameter;

        /**
         * Sets the shuttle direction.
         *
         * @param p
         *            The new shuttle direction.
         */
        ShuttleDirection(final int p) {
            parameter = p;
        }

        /** @return The shuttle direction. */
        public int getParameter() {
            return parameter;
        }
    }

    // -------------------------------------------------------------------------
    // [static]
    //
    /** The logger for this class. */
    private static Logger LOGGER = UserMetrix.getLogger(PlaybackController.class);

    // -------------------------------------------------------------------------
    //
    //
    /** The list of viewers associated with this controller. */
    private Set<DataViewer> viewers;

    /** Shuttle status flag. */
    private ShuttleDirection shuttleDirection = ShuttleDirection.UNDEFINED;

    /** Clock timer. */
    private final ClockTimer clock;

    /** Is the tracks panel currently shown? */
    private boolean tracksPanelEnabled = false;

    /** The controller for manipulating tracks. */
    private final MixerController mixerController;

    /** Model containing playback information. */
    private final PlaybackModel playbackModel;

    /** Playback view. */
    private final PlaybackV playbackView;

    /** Executor for running tasks outside of the EDT. */
    private final ExecutorService executor;

    /**
     * Constructor. Creates a new DataControllerV.
     */
    public PlaybackController() {
        executor = Executors.newSingleThreadExecutor();

        clock = new ClockTimer();
        clock.registerListener(this);

        viewers = new HashSet<DataViewer>();

        playbackModel = new PlaybackModel();
        playbackModel.setPauseRate(0);
        playbackModel.setLastSync(0);
        playbackModel.setMaxDuration(ViewportStateImpl.MINIMUM_MAX_END);

        playbackModel.setWindowPlayStart(0);
        playbackModel.setWindowPlayEnd(MixerConstants.DEFAULT_DURATION);

        mixerController = new MixerController();
        mixerController.addTracksControllerListener(this);
        mixerController.getMixerModel().getRegionModel().addPropertyChangeListener(this);

        playbackView = new PlaybackV(OpenSHAPA.getApplication().getMainFrame(),
                false, mixerController.getTracksPanel());
        playbackView.addPlaybackListener(this);
        playbackView.addPlaybackListener(new PlaybackLogging());
        playbackView.showTracksPanel(false);
    }

    private void runInEDT(final Runnable task) {
    	if (SwingUtilities.isEventDispatchThread()) {
    		task.run();
    	} else {
    		SwingUtilities.invokeLater(task);
    	}
    }
    
    public JDialog getDialog() {
        return playbackView;
    }

    public void addDataEvent(final PlaybackEvent evt) {
    	runInEDT(new Runnable() {
            public void run() {
                PluginChooser chooser = null;

                // TODO finish this
                switch (OpenSHAPA.getPlatform()) {

                case WINDOWS:
                    chooser = new WindowsJFC();

                    break;

                case MAC:
                    chooser = new MacOSJFC();

                    break;

                case LINUX:
                    chooser = new LinuxJFC();

                    break;

                default:
                    throw new NotImplementedException(
                        "Plugin chooser unimplemented.");
                }

                PluginManager pm = PluginManager.getInstance();
                chooser.addPlugin(pm.getPlugins());

                for (FileFilter ff : pm.getFileFilters()) {
                    chooser.addChoosableFileFilter(ff);
                }

                if (JFileChooser.APPROVE_OPTION
                        == chooser.showOpenDialog(playbackView)) {
                    openVideo(chooser);
                }
            }
        });
    }

    public void findEvent(final PlaybackEvent evt) {

        Runnable task = new Runnable() {
                public void run() {
                    final int modifiers = evt.getModifiers();

                    // BugzID:1312
                    if (!clock.isStopped()) {
                        clock.stop();
                        clock.setRate(0);
                    }

                    if ((modifiers & InputEvent.SHIFT_MASK)
                            == InputEvent.SHIFT_MASK) {
                        clock.stop();
                        clock.setTime(evt.getOffsetTime());
                        jumpTo(evt.getOffsetTime());
                    } else if ((modifiers & InputEvent.CTRL_MASK)
                            == InputEvent.CTRL_MASK) {
                        clock.stop();
                        clock.setTime(evt.getOnsetTime());
                        setRegionOfInterestAction();
                    } else {
                        clock.stop();
                        clock.setTime(evt.getOnsetTime());
                        jumpTo(evt.getOnsetTime());
                    }
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }
    }

    public void forwardEvent(final PlaybackEvent evt) {

        Runnable task = new Runnable() {
                public void run() {
                    playAt(FFORWARD_RATE);
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }
    }

    public void goBackEvent(final PlaybackEvent evt) {
        Runnable task = new Runnable() {
                public void run() {
                    jump(-evt.getGoTime());

                    // BugzID:721 - After going back - start playing again.
                    playAt(PLAY_RATE);
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }
    }

    public void jogBackEvent(final PlaybackEvent evt) {

        Runnable task = new Runnable() {

                public void run() {
                    int mul = 1;

                    // BugzID:1720
                    final int modifiers = evt.getModifiers()
                        & ~InputEvent.BUTTON1_MASK;

                    if ((modifiers & InputEvent.SHIFT_MASK)
                            == InputEvent.SHIFT_MASK) {
                        mul = SHIFTJOG;
                    }

                    final int ctrlShiftJogMask = (InputEvent.SHIFT_MASK
                            | InputEvent.CTRL_MASK);

                    if ((modifiers & ctrlShiftJogMask) == ctrlShiftJogMask) {
                        mul = CTRLSHIFTJOG;
                    }

                    /* Bug1361: Do not allow jog to skip past the region boundaries. */
                    long nextTime = (long) (mul * (-ONE_SECOND)
                            / playbackModel.getCurrentFPS());

                    if ((clock.getTime() + nextTime)
                            > playbackModel.getWindowPlayStart()) {
                        jump(nextTime);
                    } else {
                        jumpTo(playbackModel.getWindowPlayStart());
                    }
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }
    }

    public void jogForwardEvent(final PlaybackEvent evt) {

        Runnable task = new Runnable() {
                public void run() {
                    int mul = 1;

                    // BugzID:1720
                    final int modifiers = evt.getModifiers()
                        & ~InputEvent.BUTTON1_MASK;

                    if ((modifiers & InputEvent.SHIFT_MASK)
                            == InputEvent.SHIFT_MASK) {
                        mul = SHIFTJOG;
                    }

                    final int ctrlShiftJogMask = (InputEvent.SHIFT_MASK
                            | InputEvent.CTRL_MASK);

                    if ((modifiers & ctrlShiftJogMask) == ctrlShiftJogMask) {
                        mul = CTRLSHIFTJOG;
                    }

                    /* Bug1361: Do not allow jog to skip past the region boundaries. */
                    long nextTime = (long) (mul * (ONE_SECOND)
                            / playbackModel.getCurrentFPS());

                    if ((clock.getTime() + nextTime)
                            < playbackModel.getWindowPlayEnd()) {
                        jump(nextTime);
                    } else {
                        jumpTo(playbackModel.getWindowPlayEnd());
                    }
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }
    }

    public void newCellEvent(final PlaybackEvent evt) {
        // TODO: Uncomment in the EDT branch.
        // executor.submit(new CreateNewCellC());
    }

    public void setNewCellOffsetEvent(final PlaybackEvent evt) {
        Runnable task = new Runnable() {
                public void run() {
                    new SetNewCellStopTimeC(getCurrentTime());
                    setFindOffsetField(getCurrentTime());
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }
    }

    public void newCellSetOnsetEvent(final PlaybackEvent evt) {

        if (!SwingUtilities.isEventDispatchThread()) {
            new CreateNewCellC(getCurrentTime());
        } else {
            // TODO: Uncomment in the EDT branch.
            // executor.submit(new CreateNewCellC(getCurrentTimeEDT()));
        }
    }

    public void pauseEvent(final PlaybackEvent evt) {

        Runnable task = new Runnable() {

                public void run() {

                    // Resume from pause at playback rate prior to pause.
                    if (clock.isStopped()) {
                        shuttleAt(playbackModel.getPauseRate());

                        // Pause views - store current playback rate.
                    } else {
                        playbackModel.setPauseRate(clock.getRate());
                        clock.stop();

                        final StringBuilder sb = new StringBuilder();
                        sb.append("[");
                        sb.append(FloatUtils.doubleToFractionStr(
                                Double.valueOf(playbackModel.getPauseRate())));
                        sb.append("]");

                        Runnable edtTask = new Runnable() {

                                public void run() {
                                    playbackView.setSpeedLabel(sb.toString());
                                }
                            };

                        SwingUtilities.invokeLater(edtTask);

                    }
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }

    }

    public void playEvent(final PlaybackEvent evt) {

        // BugzID:464 - When stopped at the end of the region of interest.
        // pressing play jumps the stream back to the start of the video before
        // starting to play again.

        Runnable task = new Runnable() {

                public void run() {

                    if ((getCurrentTime() >= playbackModel.getWindowPlayEnd())
                            && clock.isStopped()) {
                        jumpTo(playbackModel.getWindowPlayStart());
                    }

                    playAt(PLAY_RATE);
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }
    }

    public void rewindEvent(final PlaybackEvent evt) {
        Runnable task = new Runnable() {
                public void run() {
                    playAt(REWIND_RATE);
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }
    }

    public void setCellOffsetEvent(final PlaybackEvent evt) {

        Runnable task = new Runnable() {
                public void run() {
                    new SetSelectedCellStopTimeC(getCurrentTime());
                    setFindOffsetField(getCurrentTime());
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }
    }

    public void setCellOnsetEvent(final PlaybackEvent evt) {

        Runnable task = new Runnable() {
                public void run() {
                    new SetSelectedCellStartTimeC(getCurrentTime());
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }
    }

    public void showTracksEvent(final PlaybackEvent evt) {

        Runnable task = new Runnable() {

                public void run() {
                    final Icon buttonIcon;

                    ResourceMap resourceMap = Application.getInstance(
                            org.openshapa.OpenSHAPA.class).getContext()
                        .getResourceMap(PlaybackV.class);

                    if (tracksPanelEnabled) {

                        // Panel is being displayed, hide it
                        buttonIcon = resourceMap.getIcon(
                                "showTracksButton.show.icon");
                    } else {

                        // Panel is hidden, show it
                        buttonIcon = resourceMap.getIcon(
                                "showTracksButton.hide.icon");
                    }

                    tracksPanelEnabled ^= true;

                    Runnable edtTask = new Runnable() {
                            public void run() {
                                playbackView.showTracksPanel(
                                    tracksPanelEnabled);
                                playbackView.setShowTracksButtonIcon(
                                    buttonIcon);
                            }
                        };
                    SwingUtilities.invokeLater(edtTask);
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }
    }

    public void shuttleBackEvent(final PlaybackEvent evt) {

        Runnable task = new Runnable() {

                public void run() {

                    if ((clock.getTime() <= 0)
                            && ((playbackModel.getShuttleRate() != 0)
                                || (shuttleDirection
                                    != ShuttleDirection.UNDEFINED))) {
                        playbackModel.setShuttleRate(0);
                        playbackModel.setPauseRate(0);
                        shuttleDirection = ShuttleDirection.UNDEFINED;
                    } else {

                        // BugzID:794 - Previously ignored pauseRate if paused
                        if (clock.isStopped()) {
                            playbackModel.setShuttleRate(findShuttleIndex(
                                    playbackModel.getPauseRate()));
                            shuttle(ShuttleDirection.BACKWARDS);
                            // shuttle(ShuttleDirection.FORWARDS);
                            // This makes current tests fail, but may be the desired
                            // functionality.
                        } else {
                            shuttle(ShuttleDirection.BACKWARDS);
                        }
                    }
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }
    }

    public void shuttleForwardEvent(final PlaybackEvent evt) {

        Runnable task = new Runnable() {
                public void run() {

                    if ((clock.getTime() <= 0)
                            && ((playbackModel.getShuttleRate() != 0)
                                || (shuttleDirection
                                    != ShuttleDirection.UNDEFINED))) {
                        playbackModel.setShuttleRate(0);
                        playbackModel.setPauseRate(0);
                        shuttleDirection = ShuttleDirection.UNDEFINED;
                        shuttle(ShuttleDirection.FORWARDS);
                    } else {

                        // BugzID:794 - Previously ignored pauseRate if paused
                        if (clock.isStopped()) {
                            playbackModel.setShuttleRate(findShuttleIndex(
                                    playbackModel.getPauseRate()));
                            shuttle(ShuttleDirection.FORWARDS);
                            // shuttle(ShuttleDirection.BACKWARDS);
                            // This makes current tests fail, but may be the desired
                            // functionality.
                        } else {
                            shuttle(ShuttleDirection.FORWARDS);
                        }
                    }
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }
    }

    public void stopEvent(final PlaybackEvent evt) {

        Runnable task = new Runnable() {

                public void run() {
                    clock.stop();
                    clock.setRate(0);
                    playbackModel.setShuttleRate(0);
                    playbackModel.setPauseRate(0);
                    shuttleDirection = ShuttleDirection.UNDEFINED;
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }
    }

    /**
     * Sets the playback region of interest to lie from the find time to offset
     * time.
     */
    public void setRegionOfInterestAction() {
    	Runnable task = new Runnable() {
            public void run() {
                final long newWindowPlayStart = playbackView.getFindTime();
                final long newWindowPlayEnd = playbackView.getFindOffsetTime() > newWindowPlayStart ? playbackView.getFindOffsetTime() : newWindowPlayStart;
                mixerController.getMixerModel().getRegionModel().setPlaybackRegion(newWindowPlayStart, newWindowPlayEnd);
                mixerController.getMixerModel().getNeedleModel().setCurrentTime(newWindowPlayStart);
            }
        };
        
        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }
    }

    // -------------------------------------------------------------------------
    // [interface] org.openshapa.util.ClockTimer.Listener
    //
    /**
     * @param time
     *            Current clock time in milliseconds.
     */
    public void clockStart(final long time) {
        assert !SwingUtilities.isEventDispatchThread();

        resetSync();

        long playTime = time;
        final long windowPlayStart = playbackModel.getWindowPlayStart();

        if (playTime < windowPlayStart) {
            playTime = windowPlayStart;
            clockStep(playTime);

            float currentRate = clock.getRate();
            clock.stop();
            clock.setTime(playTime);
            clock.setRate(currentRate);
            clock.start();
        }

        setCurrentTime(playTime);
    }

    /**
     * @param time
     *            Current clock time in milliseconds.
     */
    public void clockTick(final long time) {
        assert !SwingUtilities.isEventDispatchThread();

        try {
            setCurrentTime(time);

            // We are playing back at a rate which is too fast and probably
            // won't allow us to stream all the information at the file. We fake
            // playback by doing a bunch of seekTo's.
            if (playbackModel.isFakePlayback()) {

                for (DataViewer v : viewers) {

                    if (isWithinPlayRange(time, v)) {
                        v.seekTo(time - v.getOffset());
                    }
                }

                // DataViewer is responsible for playing video.
            } else {

                // Synchronise viewers only if we have exceded our pulse time.
                if ((time - playbackModel.getLastSync())
                        > (SYNC_PULSE * clock.getRate())) {
                    long thresh = (long) (SYNC_THRESH
                            * Math.abs(clock.getRate()));
                    playbackModel.setLastSync(time);

                    for (DataViewer v : viewers) {

                        /*
                         * Use offsets to determine if the video file should
                         * start playing.
                         */

                        if (!v.isPlaying() && isWithinPlayRange(time, v)) {
                            v.seekTo(time - v.getOffset());
                            v.play();
                        }

                        // BugzID:1797 - Viewers who are "playing" outside their
                        // timeframe should be asked to stop.
                        if (v.isPlaying() && !isWithinPlayRange(time, v)) {
                            v.stop();
                        }

                        /*
                         * Only synchronise the data viewers if we have a
                         * noticable drift.
                         */
                        if (v.isPlaying()
                                && (Math.abs(
                                        v.getCurrentTime()
                                        - (time - v.getOffset())) > thresh)) {
                            v.seekTo(time - v.getOffset());
                        }
                    }
                }
            }

            // BugzID:466 - Prevent rewind wrapping the clock past the start
            // point of the view window.
            final long windowPlayStart = playbackModel.getWindowPlayStart();

            if (time < windowPlayStart) {
                setCurrentTime(windowPlayStart);
                clock.stop();
                clock.setTime(windowPlayStart);
                clockStop(windowPlayStart);
            }

            // BugzID:756 - don't play video once past the max duration.
            final long windowPlayEnd = playbackModel.getWindowPlayEnd();

            if ((time >= windowPlayEnd) && (clock.getRate() >= 0)) {
                setCurrentTime(windowPlayEnd);
                clock.stop();
                clock.setTime(windowPlayEnd);
                clockStop(windowPlayEnd);

                return;
            }
        } catch (Exception e) {
            LOGGER.error("Unable to Sync viewers", e);
        }
    }

    /**
     * Determines whether a DataViewer has data for the desired time.
     *
     * @param time
     *            The time we wish to play at or seek to.
     * @param view
     *            The DataViewer to check.
     * @return True if data exists at this time, and false otherwise.
     */
    private boolean isWithinPlayRange(final long time, final DataViewer view) {
        return (time >= view.getOffset())
            && (time < (view.getOffset() + view.getDuration()));
    }

    /**
     * @param time
     *            Current clock time in milliseconds.
     */
    public void clockStop(final long time) {
        assert !SwingUtilities.isEventDispatchThread();

        resetSync();
        setCurrentTime(time);

        for (DataViewer viewer : viewers) {
            viewer.stop();

            if (isWithinPlayRange(time, viewer)) {
                viewer.seekTo(time - viewer.getOffset());
            }
        }
    }

    /**
     * @param rate
     *            Current (updated) clock rate.
     */
    public void clockRate(final float rate) {
        assert !SwingUtilities.isEventDispatchThread();

        resetSync();

        Runnable edtTask = new Runnable() {
                public void run() {
                    playbackView.setSpeedLabel(FloatUtils.doubleToFractionStr(
                            Double.valueOf(rate)));
                }
            };

        SwingUtilities.invokeLater(edtTask);

        long time = getCurrentTime();

        // If rate is faster than two times - we need to fake playback to give
        // the illusion of 'smooth'. We do this by stopping the dataviewer and
        // doing many seekTo's to grab individual frames.

        if (rate > 4.0 || rate < 0) {
            playbackModel.setFakePlayback(true);

            for (DataViewer viewer : viewers) {
                viewer.stop();

                if (isWithinPlayRange(time, viewer)) {
                    viewer.setPlaybackSpeed(rate);
                }
            }

            // Rate is less than two times - use the data viewer internal code
            // to draw every frame.
        } else {
            playbackModel.setFakePlayback(false);

            for (DataViewer viewer : viewers) {
                viewer.setPlaybackSpeed(rate);

                if (!clock.isStopped()) {
                    viewer.play();
                }
            }
        }
    }

    /**
     * @param time
     *            Current clock time in milliseconds.
     */
    public void clockStep(final long time) {
        assert !SwingUtilities.isEventDispatchThread();

        resetSync();
        setCurrentTime(time);

        for (DataViewer viewer : viewers) {

            if (isWithinPlayRange(time, viewer)) {
                viewer.seekTo(time - viewer.getOffset());
            }
        }
    }

    public void dispose() {
        executor.shutdown();
        mixerController.removeAll();
    }

    /**
     * @return the mixer controller.
     */
    public MixerController getMixerController() {
        return mixerController;
    }

    /**
     * Set time location for data streams.
     *
     * @param milliseconds
     *            The millisecond time.
     */
    public void setCurrentTime(final long milliseconds) {
        assert !SwingUtilities.isEventDispatchThread();

        resetSync();

        Runnable edtTask = new Runnable() {
                public void run() {
                    playbackView.setTimestampLabelText(CLOCK_FORMAT.format(milliseconds));
                    mixerController.getMixerModel().getNeedleModel().setCurrentTime(milliseconds);
                }
            };

        SwingUtilities.invokeLater(edtTask);

    }

    /**
     * Remove the specified viewer from the controller.
     *
     * @param viewer
     *            The viewer to shutdown.
     * @return True if the controller contained this viewer.
     */
    public boolean shutdown(final DataViewer viewer) {

        // Was the viewer removed.
        final boolean removed = viewers.remove(viewer);

        Runnable task = new Runnable() {

                public void run() {

                    if (removed) {

                        // BugzID:2000
                        viewer.removeViewerStateListener(
                            mixerController.getTracksEditorController()
                                .getViewerStateListener(
                                    viewer.getIdentifier()));

                        // Recalculate the maximum playback duration.
                        long maxDuration = ViewportStateImpl.MINIMUM_MAX_END;
                        Iterator<DataViewer> it = viewers.iterator();

                        while (it.hasNext()) {
                            DataViewer dv = it.next();

                            if ((dv.getDuration() + dv.getOffset())
                                    > maxDuration) {
                                maxDuration = dv.getDuration() + dv.getOffset();
                            }
                        }

                        if (viewers.isEmpty()) {
                        	mixerController.getMixerModel().getViewportModel().resetViewport();
                        	mixerController.getMixerModel().getRegionModel().resetPlaybackRegion();
                        	mixerController.getMixerModel().getNeedleModel().setCurrentTime(0);
                        } else {
                            mixerController.getMixerModel().getViewportModel().setViewportMaxEnd(maxDuration, true);
                        }

                        // Remove the data viewer from the tracks panel.
                        mixerController.deregisterTrack(viewer.getIdentifier());

                        // Data viewer removed, mark project as changed.
                        OpenSHAPA.getProjectController().projectChanged();
                    }
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }

        return removed;
    }

    /**
     * Returns set of dataviewers.
     *
     * @return set of dataviewers.
     */
    public Set<DataViewer> getDataViewers() {

        try {
            return executor.submit(new Callable<Set<DataViewer>>() {
                        public Set<DataViewer> call() throws Exception {
                            return viewers;
                        }
                    }).get();
        } catch (InterruptedException e) {
            LOGGER.error("Executor thread interrupted", e);
        } catch (ExecutionException e) {
            LOGGER.error("Failed to retrieve result", e);
        }

        return viewers;
    }

    /**
     * Adds a track to the tracks panel.
     *
     * @param id
     *            Track identifier.
     * @param icon
     *            Icon associated with the track
     * @param mediaPath
     *            Absolute file path to the media file.
     * @param name
     *            The name of the track to add.
     * @param duration
     *            The duration of the data feed in milliseconds.
     * @param offset
     *            The time offset of the data feed in milliseconds.
     * @param trackPainter
     *            Track painter to use.
     */
    public void addTrack(final Identifier id, final ImageIcon icon,
        final String mediaPath, final String name, final long duration,
        final long offset, final TrackPainter trackPainter) {

        Runnable edtTask = new Runnable() {
                public void run() {
                    mixerController.addNewTrack(id, icon, mediaPath, name,
                        duration, offset, trackPainter);
                }
            };

        SwingUtilities.invokeLater(edtTask);
    }

    /**
     * Add a viewer to the data controller with the given offset.
     *
     * @param viewer
     *            The data viewer to add.
     * @param offset
     *            The offset value in milliseconds.
     */
    public void addViewer(final DataViewer viewer, final long offset) {

        Runnable task = new Runnable() {

                public void run() {

                    // Add the QTDataViewer to the list of viewers we are
                    // controlling.
                    viewers.add(viewer);
                    viewer.setParentController(PlaybackController.this);
                    viewer.setOffset(offset);

                    boolean visible = viewer.getParentJDialog().isVisible();
                    OpenSHAPA.getApplication().show(viewer.getParentJDialog());

                    if (!visible) {
                        viewer.getParentJDialog().setVisible(false);
                    }

                    // adjust the overall frame rate.
                    float fps = viewer.getFrameRate();

                    if (fps > playbackModel.getCurrentFPS()) {
                        playbackModel.setCurrentFPS(fps);
                    }

                    final ViewportState viewport = mixerController.getMixerModel().getViewportModel().getViewport();
                    final long trackDuration = viewer.getOffset() + viewer.getDuration();
                    if (viewers.size() == 1 || viewport.getMaxEnd() < trackDuration) {
                    	mixerController.getMixerModel().getViewportModel().setViewportMaxEnd(trackDuration, true);
                    }
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }
    }

    /**
     * Handler for a TracksControllerEvent.
     *
     * @param e
     *            event
     */
    public void tracksControllerChanged(final TracksControllerEvent e) {

        Runnable task = new Runnable() {

                public void run() {

                    switch (e.getTracksEvent()) {
                    case CARRIAGE_EVENT:
                        handleCarriageEvent((CarriageEvent) e.getEventObject());

                        break;

                    default:
                        break;
                    }
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }
    }

    /** Simulates play button clicked. */
    public void pressPlay() {
        playbackView.pressPlay();
    }

    /** Simulates forward button clicked. */
    public void pressForward() {
        playbackView.pressForward();
    }

    /** Simulates rewind button clicked. */
    public void pressRewind() {
        playbackView.pressRewind();
    }

    /** Simulates pause button clicked. */
    public void pressPause() {
        playbackView.pressPause();
    }

    /** Simulates stop button clicked. */
    public void pressStop() {
        playbackView.pressStop();
    }

    /** Simulates shuttle forward button clicked. */
    public void pressShuttleForward() {
        playbackView.pressShuttleForward();
    }

    /** Simulates shuttle back button clicked. */
    public void pressShuttleBack() {
        playbackView.pressShuttleBack();
    }

    /** Simulates find button clicked. */
    public void pressFind() {
        playbackView.pressFind();
    }

    /** Simulates set cell onset button clicked. */
    public void pressSetCellOnset() {
        playbackView.pressSetCellOnset();
    }

    /** Simulates set cell offset button clicked. */
    public void pressSetCellOffset() {
        playbackView.pressSetCellOffset();
    }

    /** Simulates set new cell onset button clicked. */
    public void pressSetNewCellOnset() {
        playbackView.pressSetNewCellOnset();
    }

    /** Simulates go back button clicked. */
    public void pressGoBack() {
        playbackView.pressGoBack();
    }

    /** Simulates create new cell button clicked. */
    public void pressCreateNewCell() {
        playbackView.pressCreateNewCell();
    }

    /** Simulates create new cell setting offset button clicked. */
    public void pressCreateNewCellSettingOffset() {
        playbackView.pressCreateNewCellSettingOffset();
    }

    /** Simulates sync button clicked. */
    public void pressSyncButton() {
        playbackView.pressSyncButton();
    }

    /** Simulates jog back button click. */
    public void pressJogBackButton(final int modifiers) {
        playbackView.pressJogBackButton(modifiers);
    }

    /** Simulates jog forward button click. */
    public void pressJogForwardButton(final int modifiers) {
        playbackView.pressJogForwardButton(modifiers);
    }

    /**
     * Populates the find time in the controller.
     *
     * @param milliseconds
     *            The time to use when populating the find field.
     */
    public void setFindTime(final long milliseconds) {

        Runnable edtTask = new Runnable() {

                public void run() {
                    playbackView.setFindTime(milliseconds);
                }
            };

        SwingUtilities.invokeLater(edtTask);
    }

    /**
     * Populates the find offset time in the controller.
     *
     * @param milliseconds
     *            The time to use when populating the find field.
     */
    public void setFindOffsetField(final long milliseconds) {

        Runnable edtTask = new Runnable() {

                public void run() {
                    playbackView.setFindOffsetField(milliseconds);
                }
            };

        SwingUtilities.invokeLater(edtTask);
    }

    /**
     * Action to invoke when the user holds shift down.
     */
    public void findOffsetAction() {

        Runnable task = new Runnable() {
                public void run() {
                    jumpTo(playbackView.getFindOffsetTime());
                }
            };

        if (!SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            executor.submit(task);
        }

    }

    // ------------------------------------------------------------------------
    // [private] play back action helper functions
    //
    /**
     * @param rate
     *            Rate of play.
     */
    private void playAt(final float rate) {
        assert !SwingUtilities.isEventDispatchThread();

        shuttleDirection = ShuttleDirection.UNDEFINED;
        playbackModel.setShuttleRate(0);
        playbackModel.setPauseRate(0);
        shuttleAt(rate);
    }

    /**
     * @param direction
     *            The required direction of the shuttle.
     */
    private void shuttle(final ShuttleDirection direction) {
        assert !SwingUtilities.isEventDispatchThread();

        int shuttleRate = playbackModel.getShuttleRate();
        float rate = SHUTTLE_RATES[shuttleRate];

        if (ShuttleDirection.UNDEFINED == shuttleDirection) {
            shuttleDirection = direction;
            rate = SHUTTLE_RATES[0];

        } else if (direction == shuttleDirection) {

            if (shuttleRate < (SHUTTLE_RATES.length - 1)) {
                rate = SHUTTLE_RATES[++shuttleRate];
                playbackModel.setShuttleRate(shuttleRate);
            }

        } else {

            if (shuttleRate > 0) {
                rate = SHUTTLE_RATES[--shuttleRate];
                playbackModel.setShuttleRate(shuttleRate);

                // BugzID: 676 - Shuttle speed transitions between zero.
            } else {
                rate = 0;
                shuttleDirection = ShuttleDirection.UNDEFINED;
            }
        }

        shuttleAt(shuttleDirection.getParameter() * rate);
    }

    /**
     * @param rate
     *            Rate of shuttle.
     */
    private void shuttleAt(final float rate) {
        assert !SwingUtilities.isEventDispatchThread();

        clock.setRate(rate);
        clock.start();
    }

    /**
     * @param step
     *            Milliseconds to jump.
     */
    private void jump(final long step) {
        assert !SwingUtilities.isEventDispatchThread();

        clock.stop();
        clock.setRate(0);

        playbackModel.setShuttleRate(0);
        playbackModel.setPauseRate(0);
        shuttleDirection = ShuttleDirection.UNDEFINED;
        clock.stepTime(step);
    }

    /**
     * @param time
     *            Absolute time to jump to.
     */
    private void jumpTo(final long time) {
        assert !SwingUtilities.isEventDispatchThread();

        clock.stop();
        clock.setRate(PLAY_RATE);
        clock.setTime(time);
    }

    /**
     * Handles opening a data source.
     *
     * @param jd
     *            The file chooser used to open the data source.
     */
    private void openVideo(final PluginChooser chooser) {
        final Plugin plugin = chooser.getSelectedPlugin();
        final File f = chooser.getSelectedFile();

        if (plugin != null) {
            executor.submit(new Runnable() {
                    @Override public void run() {
                        DataViewer dataViewer = plugin.getNewDataViewer(
                                OpenSHAPA.getApplication().getMainFrame(),
                                false);
                        dataViewer.setIdentifier(
                            IDController.generateIdentifier());
                        dataViewer.setDataFeed(f);
                        dataViewer.setDatastore(
                            OpenSHAPA.getProjectController().getDB());
                        dataViewer.seekTo(clock.getTime());
                        addDataViewer(plugin.getTypeIcon(), dataViewer, f,
                            dataViewer.getTrackPainter());
                        mixerController.bindTrackActions(
                            dataViewer.getIdentifier(),
                            dataViewer.getCustomActions());
                        dataViewer.addViewerStateListener(
                            mixerController.getTracksEditorController()
                                .getViewerStateListener(
                                    dataViewer.getIdentifier()));
                    }
                });
        }
    }

    /**
     * Reset the sync.
     */
    private void resetSync() {
        assert !SwingUtilities.isEventDispatchThread();

        playbackModel.setLastSync(0);
    }

    /**
     * Get the current master clock time for the controller.
     *
     * @return Time in milliseconds.
     */
    private long getCurrentTime() {
        assert !SwingUtilities.isEventDispatchThread();

        return clock.getTime();
    }

    private long getCurrentTimeEDT() {

        try {
            return executor.submit(new Callable<Long>() {
                        public Long call() throws Exception {
                            return clock.getTime();
                        }
                    }).get();
        } catch (InterruptedException e) {
            LOGGER.error("Executor thread interrupted", e);
        } catch (ExecutionException e) {
            LOGGER.error("Failed to retrieve result", e);
        }

        return clock.getTime();
    }

    /**
     * Adds a data viewer to this data controller.
     *
     * @param icon
     *            The icon associated with the data viewer.
     * @param viewer
     *            The new viewer that we are adding to the data controller.
     * @param f
     *            The parent file that the viewer represents.
     */
    private void addDataViewer(final ImageIcon icon, final DataViewer viewer,
        final File f, final TrackPainter trackPainter) {
        assert !SwingUtilities.isEventDispatchThread();
        assert viewer.getIdentifier() != null;

        addViewer(viewer, 0);

        OpenSHAPA.getProjectController().projectChanged();

        // Add the file to the tracks information panel
        addTrack(viewer.getIdentifier(), icon, f.getAbsolutePath(), f.getName(),
            viewer.getDuration(), viewer.getOffset(), trackPainter);
    }

    /**
     * Searches the shuttle rates array for the given rate, and returns the
     * index.
     *
     * @param pRate
     *            The rate to search for.
     * @return The index of the rate, or -1 if not found.
     */
    private int findShuttleIndex(final float pRate) {
        assert !SwingUtilities.isEventDispatchThread();

        if (pRate == 0) {
            return 0;
        }

        for (int i = 0; i < SHUTTLE_RATES.length; i++) {

            if ((SHUTTLE_RATES[i] == pRate)
                    || (SHUTTLE_RATES[i] == (pRate * (-1)))) {
                return i;
            }
        }

        return -1;
    }

    private void gotoTime(final long time) {
        assert !SwingUtilities.isEventDispatchThread();
        final long newTime = Math.min(Math.max(time, playbackModel.getWindowPlayStart()), playbackModel.getWindowPlayEnd()); 
        clockStop(newTime);
        clockStep(newTime);
        setCurrentTime(newTime);
        clock.setTime(newTime);
    }

	private void handleRegionChanged(final PropertyChangeEvent e) {
        assert !SwingUtilities.isEventDispatchThread();
        final RegionState region = mixerController.getMixerModel().getRegionModel().getRegion();
        playbackModel.setWindowPlayStart(region.getRegionStart());
        playbackModel.setWindowPlayEnd(region.getRegionEnd());
	}

    private void handleNeedleChanged(final PropertyChangeEvent e) {
        gotoTime(mixerController.getMixerModel().getNeedleModel().getCurrentTime());
    }
    
	private void handleViewportChanged(final PropertyChangeEvent e) {
		final ViewportState viewport = mixerController.getMixerModel().getViewportModel().getViewport();
        playbackModel.setMaxDuration(viewport.getMaxEnd());
	}
	
    /**
     * Handles a CarriageEvent (when the carriage moves due to user
     * interaction).
     *
     * @param e
     *            The carriage event that triggered this action.
     */
    private void handleCarriageEvent(final CarriageEvent e) {
        assert !SwingUtilities.isEventDispatchThread();

        switch (e.getEventType()) {

        case OFFSET_CHANGE:
            handleCarriageOffsetChangeEvent(e);

            break;

        case CARRIAGE_LOCK:
        case BOOKMARK_CHANGED:
        case BOOKMARK_SAVE:
            OpenSHAPA.getProjectController().projectChanged();
            OpenSHAPA.getApplication().updateTitle();

            break;

        default:
            throw new IllegalArgumentException("Unknown carriage event.");
        }
    }

    /**
     * @param e
     */
    private void handleCarriageOffsetChangeEvent(final CarriageEvent e) {
        assert !SwingUtilities.isEventDispatchThread();

        // Look through our data viewers and update the offset
        for (DataViewer viewer : viewers) {
            File feed = viewer.getDataFeed();

            /*
             * Found our data viewer, update the DV offset and the settings in
             * the project file.
             */
            if (viewer.getIdentifier().equals(e.getTrackId())) {
                viewer.setOffset(e.getOffset());
            }
        }

        OpenSHAPA.getProjectController().projectChanged();
        OpenSHAPA.getApplication().updateTitle();

        // Recalculate the maximum playback duration.
        long maxDuration = ViewportStateImpl.MINIMUM_MAX_END;
        for (DataViewer viewer : viewers) {
        	final long trackDuration = viewer.getDuration() + viewer.getOffset();
            if (trackDuration > maxDuration) {
                maxDuration = trackDuration;
            }
        }
        
        mixerController.getMixerModel().getViewportModel().setViewportMaxEnd(maxDuration, false);
    }

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if (e.getSource() == mixerController.getNeedleController().getNeedleModel()) {
			handleNeedleChanged(e);
		} else if (e.getSource() == mixerController.getMixerModel().getViewportModel()) {
			handleViewportChanged(e);
		} else if (e.getSource() == mixerController.getRegionController().getModel()) {
			handleRegionChanged(e);
		}
	}
}
