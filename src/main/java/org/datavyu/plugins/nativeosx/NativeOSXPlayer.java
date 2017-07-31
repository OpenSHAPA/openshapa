package org.datavyu.plugins.nativeosx;

import org.datavyu.util.NativeLibraryLoader;

import java.awt.*;
import java.io.File;

public class NativeOSXPlayer extends Canvas {

    protected static int playerCount = 0;

    static {
        // Standard JNI: load the native library

    }

    public final int id;
    File fileToLoad;

    public NativeOSXPlayer(File fileToLoad) {
        super();
        try {
            //NativeLibraryLoader.load("NativeOSXCanvas");
            NativeLibraryLoader.extractAndLoad("NativeOSXCanvas");
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.id = NativeOSXPlayer.playerCount;
        NativeOSXPlayer.playerCount += 1;
        this.fileToLoad = fileToLoad;
    }

    public void addNotify() {
        super.addNotify();
        System.out.println("Opening video file: " + fileToLoad.toURI().getPath());
        try {
            addNativeOSXCoreAnimationLayer("file://" + fileToLoad.toURI().getPath());
        } catch (Exception e) {
            System.out.println("ERROR CAUGHT");
            e.printStackTrace();
        }
    }

    // This method is implemented in native code. See NativeOSXCanvas.m
    public native void addNativeOSXCoreAnimationLayer(String path);

    public native void stop(int id);

    public native void play(int id);

    public native void setTime(long time, int id);

    public native void setTimePrecise(long time, int id);

    public native void setTimeModerate(long time, int id);

    public native void setVolume(float time, int id);

    public native void release(int id);

    public native double getMovieHeight(int id);

    public native double getMovieWidth(int id);

    public native long getCurrentTime(int id);

    public native long getDuration(int id);

    public native float getRate(int id);

    public native void setRate(float rate, int id);

    public native boolean isPlaying(int id);

    public native float getFPS(int id);

}