package org.datavyu.plugins;

import com.sun.jna.NativeLibrary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.plugins.vlcfx.NativeLibraryManager;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import java.io.File;

/**
 * Loads the libraries for the VLC plugin.
 *
 * The libraries for the windows part are not available.
 */
public class VlcLibraryLoader {

    /** The logger for this class */
    private static Logger logger = LogManager.getLogger(VlcLibraryLoader.class);

    /** Set whenever the libraries were loaded and not purged */
    private static boolean loaded = false;

    /** The native library manager from the VLC code */
    private static NativeLibraryManager nativeLibraryManager;

    /**
     * Loads the library.
     *
     * This method can be called multiple time but loads the library only once.
     */
    public static void load() {
        String vlcLibDir = System.getProperty("java.io.tmpdir") + File.separator + "vlc" + File.separator;
        logger.info("Loading vlc libraries into folder: " + vlcLibDir);
        if (!loaded) {
            try {
                nativeLibraryManager = new NativeLibraryManager(vlcLibDir);
                nativeLibraryManager.unpackNativePackage();
                NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcLibDir);
                loaded = true;
                logger.info("Added vlc libraries from " + vlcLibDir);
            } catch (Exception e) {
                logger.info("Unable to find the vlc libraries: " + e.getMessage());
            }
        }
    }

    /**
     * Removes the libraries.
     *
     * When called multiple times it only removes the libraries once; if it present.
     */
    public static void purge() {
        if (loaded) {
            nativeLibraryManager.purge();
            loaded = false;
        }
    }
}
