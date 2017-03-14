package org.datavyu.plugins.ffmpegplayer;

import org.datavyu.util.NativeLoader;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Hashtable;

public class FFmpegPlayer extends Canvas {

    protected static int playerCount = 0;

    static {
        // TODO put this into a package and load the native lib
        try {
            // NOTE: This load order is important, do not change.
            NativeLoader.LoadNativeLib("avutil-55");
            NativeLoader.LoadNativeLib("swscale-4");
            NativeLoader.LoadNativeLib("swresample-2");
            NativeLoader.LoadNativeLib("avcodec-57");
            NativeLoader.LoadNativeLib("avformat-57");

            NativeLoader.LoadNativeLib("ImagePlayer");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean playing;
    File fileToLoad;
    private float playbackSpeed;

    public FFmpegPlayer() {
        super();
        this.playing = false;
        FFmpegPlayer.playerCount += 1;
        this.fileToLoad = fileToLoad;
        this.playbackSpeed = 1;

        // Initialize with an empty image.
        nChannel = 3;
        width = 640;
        height = 480;
        data = new byte[width*height*nChannel];	// Allocate the bytes in java.
        DataBufferByte dataBuffer = new DataBufferByte(data, width*height);
        sm = cm.createCompatibleSampleModel(width, height);
        WritableRaster raster = WritableRaster.createWritableRaster(sm, dataBuffer, new Point(0,0));
        image = new BufferedImage(cm, raster, false, properties); // Create buffered image.
        setBounds(0, 0, width, height);

    }

//    public void addNotify() {
//        super.addNotify();
//        System.out.println("Opening video file: " + fileToLoad.toURI().getPath());
//        try {
//            addNativeCoreAnimationLayer("file://" + fileToLoad.toURI().getPath());
//        } catch (Exception e) {
//            System.out.println("ERROR CAUGHT");
//            e.printStackTrace();
//        }

    private static final long serialVersionUID = -6199180436635445511L;

    /** RGB sample model. */
    protected ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);

    /** Samples components without transparency using a byte format. */
    protected ComponentColorModel cm = new ComponentColorModel(cs,
            false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);

    /** These properties are used to create the buffered image. */
    protected Hashtable<String, String> properties =
            new Hashtable<String, String>();

    /** The number of channels, typically 3. */
    protected int nChannel = 0;

    /** The width of the current image in pixels. Changes with view. */
    protected int width = 0;

    /** The height of the current image in pixels. Changes with view. */
    protected int height = 0;

    /** This image buffer holds the image. */
    protected BufferedImage image = null;

    /** This byte buffer holds the raw data. */
    protected ByteBuffer buffer = null;

    /** A copy of the raw data used to be wrapped by the data byte buffer. */
    protected byte[] data = null;

    /** Used to buffer the image. */
    protected DataBufferByte dataBuffer = null;

    /** Used to create the buffered image. */
    protected SampleModel sm = null;

    /**
     * Get the frame buffer.
     *
     * If no movie was loaded this method returns null.
     *
     * @return A frame buffer object or null.
     */
    protected native ByteBuffer getFrameBuffer();

    /**
     * Load the next frame.
     *
     * If no movie was loaded this method returns -1.
     *
     * ATTENTION: This method blocks if there is no next frame. This avoids
     * active spinning of a display thread. However, when hooked up to a button
     * you need to make sure that you don't call this method if there are no
     * frames available otherwise your UI thread is blocked. The methods atStart
     * or and atEnd can help decide if you can safely make the call.
     *
     * @return The loaded number of frames.
     */
    protected native int loadNextFrame();

    /**
     * Load the movie with the file name.
     *
     * If this method is called multiple times the under laying implementation
     * releases resources and re-allocates them.
     *
     * @param fileName The file name.
     */
    protected native void openMovie(String fileName, String version);

    /**
     * Get the number of color channels for the movie.
     *
     * @return The number of channels.
     */
    public native int getNumberOfChannels();

    /**
     * Get the height of the movie frames. Returns 0 if no movie was loaded.
     *
     * @return Height of the image.
     */
    public native int getHeight();

    /**
     * Get the width of the movie frames. Returns 0 if no movie was loaded.
     *
     * @return Width of the image.
     */
    public native int getWidth();

    /**
     * Get the first time stamp of the movie in seconds.
     *
     * @return First time stamp in the stream in seconds.
     */
    public native double getStartTime();

    /**
     * Get the last time stamp of the movie in seconds.
     *
     * ATTENTION: This is an estimate based on duration.
     *
     * @return ESTIMATED last time stamp in the stream in seconds.
     */
    public native double getEndTime();

    /**
     * Get the duration of the movie in seconds.
     *
     * ATTENTION: This is a best effort estimate by ffmpeg and does not match
     * the actual duration when decoding the movie. Usually the actual duration
     * is shorter by less than one second.
     *
     * Returns 0 if no movie was loaded.
     *
     * @return Duration in SECONDS.
     */
    public native double getDuration();

    /**
     * Get the current time of the movie in seconds.
     *
     * Returns 0 if no movie was loaded.
     *
     * @return Current time in seconds.
     */
    public native double getCurrentTime();

    /**
     * Resets the movie either to the front or end of the file depending
     * on the play back direction.
     */
    public native void rewind();

    /**
     * Returns true if we are playing the video in forward direction and
     * false otherwise.
     *
     * @return True if forward play back otherwise false.
     */
    protected native boolean isForwardPlayback();

    /**
     * True if we reached the start when reading this file. At this point any
     * further loadNextFrame() will block.
     *
     * Blocking is intended to be used to stop any active pulling of frames when
     * the start or end of the file is reached.
     *
     * @return True if start of file is reached.
     */
    protected native boolean atStartForRead();

    /**
     * True if we reached the end while reading this file. At this point any
     * further loadNextFrame() will block.
     *
     * @return True if the end of the file is reached.
     */
    protected native boolean atEndForRead();

    /**
     * Releases all resources that have been allocated when loading a movie.
     * If this method is called when no movie was loaded nothing happens.
     */
    public native void release();

    /**
     * Set the play back speed.
     *
     * @param speed A floating point with the play back speed as factor of the
     * 				original play back speed.
     *
     * For instance, for the value of -0.5x the video is played back at half the
     * speed.
     *
     * We tested for the range -4x to +4x.
     */
    public native void setPlaybackSpeed(float speed);

    /**
     * Sets the time within the movie.
     *
     * @param time The time within the video in SECONDS.
     *
     * If the duration is set above the length of the video the video is set to
     * the end.
     */
    public native void setTime(double time);


    public void update(Graphics g){
        paint(g); // Instead of resetting, paint directly.
    }

    /**
     * If we are playing in forward mode and read the first frame or if we are
     * in backward mode and read the last frame then, in both cases, there are
     * no further frames.
     *
     * @return True if there is a next frame otherwise false.
     */
    public native boolean hasNextFrame();

    /**
     * Restrict the view of the video to the rectangular area defined by
     * the corner points (x0, y0) and (x0+width, y0+height).
     *
     * @param x0 The horizontally first coordinate in PIXELS.
     * @param y0 The vertically first coordinate in PIXELS.
     * @param width The width of the viewing window in PIXELS.
     * @param height The height of the viewing window in PIXELS.
     *
     * @return True if we could set the window.
     */
    private native boolean view(int x0, int y0, int width, int height);

    /**
     * Set the viewing area to the rectangle area defined by the corner points
     * (x0, y0) and (x0+width, y0+height).
     *
     * This method assumes that no showNextFrame is called!!!
     *
     * @param x0 The horizontally first coordinate in PIXELS.
     * @param y0 The vertically first coordinate in PIXELS.
     * @param width The width of the viewing window in PIXELS.
     * @param height The height of the viewing window in PIXELS.
     *
     * @return True if we could set the window.
     */
    public boolean setView(int x0, int y0, int width, int height) {
        boolean viewOk = view(x0, y0, width, height);

        // If we were able to set the viewing area adjust the height, width,
        // and sample model.
        if (viewOk) {
            this.width = width;
            this.height = height;

            // Update the sample model with the new width and height.
            sm = cm.createCompatibleSampleModel(width, height);
        }
        return viewOk;
    }

    /**
     * Get the next frame and load it into the buffered image.
     *
     * Notice that the number of returned frames can be larger than one if
     * frames are skipped. This happens during very fast play back where we load
     * more than one frame from the buffer but only show the last frame taken
     * from the buffer.
     *
     * @return The number of frames loaded.
     */
    public int showNextFrame() {
        int nFrame = loadNextFrame(); // Load the next frame(s). May skip frames.
        if (nFrame == -1) {
            return nFrame;
        }
        buffer = getFrameBuffer(); // Get the buffer.
        data = new byte[width*height*nChannel];	// Allocate the bytes in java.
        buffer.get(data); // Copy from the native buffer into the java buffer.
        DataBufferByte dataBuffer = new DataBufferByte(data, width*height); // Create data buffer.
        WritableRaster raster = WritableRaster.createWritableRaster(sm, dataBuffer, new Point(0, 0)); // Create writable raster.
        image = new BufferedImage(cm, raster, false, properties); // Create buffered image.
        return nFrame; // Return the number of frames.
    }

    /**
     * Sets a movie for this player.
     *
     * @param fileName Name of the movie file.
     */
    public void open(String fileName) {
        // TODO: ADD the true version string here.
        openMovie(fileName, "0.0.1");
        nChannel = getNumberOfChannels();
        width = getWidth();
        height = getHeight();
        System.out.println(hasNextFrame());
        System.out.println(getStartTime());
        System.out.println(getEndTime());
        showNextFrame();
        System.out.println(getCurrentTime());
        // Create a sampling model for this movie file.
        sm = cm.createCompatibleSampleModel(width, height);
        setBounds(0, 0, width, height);
    }

    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }



    public void stop() {
        setPlaybackSpeed(0);
    }

    public void play() {
        setPlaybackSpeed(playbackSpeed);
    }

//    public native void setTime(long time, int id);

    public void setVolume(float volume) {

    }

//    public native void release(int id);
//
//    public native double getMovieHeight(int id);
//
//    public native double getMovieWidth(int id);
//
//    public native long getCurrentTime(int id);
//
//    public native long getDuration(int id);
//
//    public native float getRate(int id);
//
//    public native void setRate(float rate, int id);
//
//    public native boolean isPlaying(int id);
//
//    public native float getFPS(int id);

}