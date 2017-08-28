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
package org.datavyu.plugins.quicktime;

import com.google.common.collect.Lists;
import com.sun.jna.Platform;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.Datavyu;
import org.datavyu.plugins.StreamViewer;
import org.datavyu.plugins.Filter;
import org.datavyu.plugins.FilterNames;
import org.datavyu.plugins.Plugin;
import org.datavyu.util.NativeLibraryLoader;

import javax.swing.*;
import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.List;


public final class QTPlugin implements Plugin {

    /** The logger for this class */
    private static Logger logger = LogManager.getLogger(QTPlugin.class);

    private static final List<Datavyu.Platform> VALID_OPERATING_SYSTEMS = Lists.newArrayList(Datavyu.Platform.WINDOWS);

    private static final Filter VIDEO_FILTER = new Filter() {
        final SuffixFileFilter ff;
        final List<String> ext;

        {
            ext = Lists.newArrayList(".avi", ".mov", ".mpg", ".mp4");
            ff = new SuffixFileFilter(ext, IOCase.INSENSITIVE);
        }

        @Override
        public FileFilter getFileFilter() {
            return ff;
        }

        @Override
        public String getName() {
            return FilterNames.VIDEO.getFilterName();
        }

        @Override
        public Iterable<String> getExtensions() {
            return ext;
        }
    };

    private static boolean librariersLoaded = false;

    // TODO: Move this into the QTDataViewerDialog class!!
    public static boolean hasQuicktimeLibs() {
        boolean found = false;
        try {
            Class.forName("quicktime.QTSession");
            found = true;
            librariersLoaded = true;
        } catch (UnsatisfiedLinkError noLink) {
            logger.error("No link: " + noLink.getMessage());
        } catch (NoClassDefFoundError noClass) {
            logger.error("No class found: " + noClass.getMessage());
        } catch (ClassNotFoundException ce) {
            logger.error("Class not found: " + ce.getMessage());
        } catch (Exception e) {
            logger.error("General exception: " + e.getMessage());
        }
        return found;
    }

    public static boolean isLibrariersLoaded() {
        return librariersLoaded;
    }

    static {
        if (Datavyu.getPlatform() == Datavyu.Platform.WINDOWS) {
            logger.info("Detected platform: WINDOWS");
            try {
                if (System.getProperty("sun.arch.data.model").equals("32")) {
                    logger.info("Loading libraries for 32 bit QT");
                    File libraryFile = NativeLibraryLoader.extract("QTJNative");
                    System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator
                            + libraryFile.getAbsolutePath());
                    NativeLibraryLoader.extractAndLoad("QTJavaNative");
                    //QTDataViewerDialog.librariesFound = true;
                    librariersLoaded = true;
                }
            } catch (Exception e) {
                logger.error("Could not load libraries for QT " + e);
            }
        }
    }

    @Override
    public StreamViewer getNewStreamViewer(final java.awt.Frame parent,
                                           final boolean modal) {

        if (Platform.isMac() || Platform.isWindows()) {
            return new QTDataViewerDialog(parent, modal);
        } else {
            return null;
        }
    }

    /**
     * @return icon representing this plugin.
     */
    @Override
    public ImageIcon getTypeIcon() {
        URL typeIconURL = getClass().getResource(
                "/icons/gstreamerplugin-icon.png");

        return new ImageIcon(typeIconURL);
    }

    @Override
    public String getNamespace() {
        return "datavyu.video";
    }

    @Override
    public Filter[] getFilters() {
        return new Filter[]{VIDEO_FILTER};
    }

    @Override
    public String getPluginName() {
        return "QuickTime Video";
    }

    @Override
    public Class<? extends StreamViewer> getViewerClass() {

        if (Platform.isWindows()) {
            return QTDataViewerDialog.class;
        }

        return null;
    }

    @Override
    public List<Datavyu.Platform> getValidPlatforms() {
        return VALID_OPERATING_SYSTEMS;
    }

}
