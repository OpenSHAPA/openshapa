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
package org.datavyu.util;

import com.google.common.collect.Iterables;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;


public class NativeLibraryLoader {

    /** Logger for this native library loader */
    private static Logger logger = LogManager.getLogger(NativeLibraryLoader.class);

    /** Buffer size for unzipping native libraries */
    private static final int BUFFER = 16*1024; // 16 kB

    /** Loaded library names */
    private static final ArrayList<File> libraryFiles = new ArrayList<>();

    /** Folder where we unzip libraries */
    // TODO: Currently won't work for multiple datavyu's running because the 1st will delete libs of the 2nd
    private static File libraryFolder = new File(System.getProperty("java.io.tmpdir"));

    private static boolean isMacOs = System.getProperty("os.name").contains("Mac");

    /**
     * Load a native library.
     *
     * @param libName Name of the library, extensions (dll, jnilib) removed. OSX libraries should have the "lib" suffix
     *                removed.
     * @throws Exception If the library cannot be loaded.
     */
    public static void load(final String libName) throws Exception {
        Enumeration<URL> resources;
        String extension;
        ClassLoader classLoader = NativeLibraryLoader.class.getClassLoader();
        if (isMacOs) {
            extension = ".jnilib";
            resources = classLoader.getResources("lib" + libName + extension);
            if (!resources.hasMoreElements()) {
                extension = ".dylib";
                resources = classLoader.getResources(libName + extension);
            }
        } else {
            extension = ".dll";
            resources = classLoader.getResources(libName + extension);
        }
        while (resources.hasMoreElements()) {
            extractAndLoad((libName + extension), resources.nextElement());
        }
        addToLibraryPath(libraryFolder.getAbsolutePath());
        unsetSysPath();
    }

    private static void addToLibraryPath(String path) throws Exception {
        logger.info("Adding to library path", path);
        System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + path);
    }

    private static void unsetSysPath() throws Exception {
        final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
        sysPathsField.setAccessible(true);
        sysPathsField.set(null, null);
    }

    private static File extractAndLoad(final String destName, final URL url) throws Exception {

        // Create a temporary file for the library
        logger.info("Attempting to extract " + url.toString());
        InputStream in = url.openStream();
        File outfile = new File(libraryFolder, destName);
        FileOutputStream out = new FileOutputStream(outfile);
        BufferedOutputStream dest = new BufferedOutputStream(out, BUFFER);
        int count;
        byte[] data = new byte[BUFFER];
        while ((count = in.read(data, 0, BUFFER)) != -1) {
            dest.write(data, 0, count);
        }
        dest.close();  // close flushes
        out.close();
        in.close();
        libraryFiles.add(outfile);
        logger.info("Extracted lib into tmp file " + outfile);

        System.load(outfile.toString());
        return outfile;
    }

    /**
     * Removes temporary files that were created by the native loader.
     */
    public static void unload() {
        logger.info("cleaning temp files");

        for (File loadedLib : Iterables.reverse(libraryFiles)) {
            if (!loadedLib.delete()) {
                logger.error("Unable to delete temp file: " + loadedLib);
            }
        }

        // Delete all of the other files
        /*
        try {
            FileUtils.deleteDirectory(libraryFolder);
        } catch (Exception e) {
            logger.error("Unable to remove library folder ", e);
        }
        if ((libraryFolder != null) && !libraryFolder.delete()) {
            logger.error("Unable to delete temp folder: + " + libraryFolder);
        }
        */
    }
}
