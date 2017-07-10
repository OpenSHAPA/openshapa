/*
 * Copyright (c) 2011 OpenSHAPA Foundation, http://openshapa.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.datavyu.plugins;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JDialog;
import org.datavyu.models.db.DataStore;

import org.datavyu.models.id.Identifier;

import org.datavyu.views.DataController;
import org.datavyu.views.component.DefaultTrackPainter;
import org.datavyu.views.component.TrackPainter;


/**
 * DataViewer interface.
 */
public interface DataViewer {

    /**
     * Sets the identifier used to identify this data viewer.
     *
     * @param id Identifier to use.
     */
    void setIdentifier(Identifier id);

    /**
     * @return Identifier used to identify this data viewer.
     */
    Identifier getIdentifier();

    /**
     * Retrieve the duration of the underlying data stream.
     *
     * @return The duration in milliseconds.
     */
    long getDuration();

    /**
     * Retrieve the start time of the underlying data stream.
     *
     * @return Start time in milliseconds.
     */
    long getStartTime();

    /**
     * Set the start time of the underlying data stream.
     *
     * @param startTime Start time in milliseconds.
     */
    void setStartTime(final long startTime);

    /**
     * Get the display window.
     *
     * @return A JDialog that will be displayed.
     */
    JDialog getParentJDialog();

    /**
     * Hides or shows the windows associated with this data viewer.
     */
    void setDataViewerVisible(boolean isVisible);
    
    /**
     * Sets the data feed for this viewer.
     *
     * @param dataFeed The new data feed for this viewer.
     */
    void setSourceFile(final File dataFeed);

    /**
     * Return the file of the data.
     *
     * @return The data feed being used by this viewer.
     */
    File getSourceFile();

    /**
     * Sets the parent data controller for this data viewer.
     *
     * @param dataController The parent controller.
     */
    void setParentController(final DataController dataController);

    /**
     * @return Currently stored frames per second.
     */
    float getFramesPerSecond();
    
    /**
     * @param fps framerate to assign
     */
    void setFramesPerSecond(float fps);
    
    /**
     * @return Detected frames per second.
     */
    float getDetectedFrameRate();

    /**
     * @return The current position within the data feed in milliseconds.
     * @throws Exception If an error occurs.
     */
    long getCurrentTime() throws Exception;

    /**
     * Plays the continuous data stream at a regular 1x normal speed.
     */
    void play();

    /**
     * Stops the playback of the continuous data stream.
     */
    void stop();

    /**
     * Is this data viewer currently playing.
     */
    boolean isPlaying();

    /**
     * Set the playback speed.
     *
     * @param speed Positive implies forwards, while negative implies reverse.
     */
    void setPlaybackSpeed(float speed);

    /**
     * Set the stream position.
     *
     * @param position Position in milliseconds.
     */
    void seek(long position);

    /**
     * @return Custom track painter implementation. Must not return null.
     * Plugins that do not have a custom track painter implementation should
     * return {@link DefaultTrackPainter}.
     */
    TrackPainter getTrackPainter();

    /**
     * Read settings from the given input stream.
     *
     * @param is Input stream to load from.
     */
    void loadSettings(InputStream is);

    /**
     * Write settings to the given output stream.
     *
     * @param os Output stream to write to.
     */
    void storeSettings(OutputStream os);

    /**
     * Adds the given ViewerStateListener to the collection of listeners who
     * are interested in changes made to the project.
     * @param vsl The ViewerStateListener to add.
     */
    void addViewerStateListener(ViewerStateListener vsl);

    /**
     * Removes the given ViewerStateListener from the collection of listeners.
     * @param vsl The listener to remove.
     */
    void removeViewerStateListener(ViewerStateListener vsl);

    /**
     * Used to query the data viewer for custom actions.
     *
     * @return custom actions handler.
     * @see CustomActionsAdapter
     */
    CustomActions getCustomActions();

    /**
     * Sets the DataStore from which this viewer can extract data.
     *
     * @param sDB The DataStore to use.
     */
    void setDataStore(final DataStore sDB);

    /**
     * Unload all data, to prepare for being closed- essentially the opposite
     * of setSourceFile. Can be called to reduce the data viewer to a low-resource
     * state.
     */
    void clearDataFeed();

    /**
     * Did we assume the frames per second plaback rate?
     *
     * @return True if we assumed the playback rate; otherwise false.
     */
    boolean isAssumedFramesPerSecond();
}
