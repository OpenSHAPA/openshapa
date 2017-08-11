package org.datavyu.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.Build;
import org.datavyu.Datavyu;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Container class for the version and build string.
 *
 * Static methods access the local and server datavyu version.
 */
public class DatavyuVersion {

    /** Version file on the datavyu web server */
    private static final String VERSION_FILE = "http://www.datavyu.org/version.txt";

    /** Version file including pre-releases on the datavyu web server */
    private static final String PRE_VERSION_FILE = "http://www.datavyu.org/pre_version.txt";

    /** The server side version is loaded and cached at start-up */
    private static final DatavyuVersion serverVersion = DatavyuVersion.initServerVersion();

    /** The local version is loaded and cached at start-up */
    private static final DatavyuVersion localVersion = DatavyuVersion.initLocalVersion();

    /** The logger for this class */
    private static Logger logger = LogManager.getLogger(DatavyuVersion.class);

    /** The version string */
    private String version;

    /** The build string */
    private String build;

    /**
     * Creates a new datavyu version container.
     *
     * @param version The version.
     * @param build The build.
     */
    public DatavyuVersion(String version, String build) {
        this.version = version;
        this.build = build;
    }

    /**
     * Get the version.
     *
     * @return version string.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the build.
     *
     * @return build string.
     */
    public String getBuild() {
        return build;
    }

    /**
     * Checks if the version string is present.
     *
     * @return True if the version string is present; otherwise false.
     */
    public boolean hasVersion() {
        return !version.equals("");
    }

    /**
     * Checks if the build string is present.
     *
     * @return True if the build string is present; otherwise false.
     */
    public boolean hasBuild() {
        return !build.equals("");
    }

    /**
     * Checks if an update is available given the presumed newer version.
     *
     * @param newerVersion is treated as the newer version.
     *
     * @return True if all versions are present and if the number of the newer version is greater than the current one.
     */
    public boolean updateAvailable(DatavyuVersion newerVersion) {
        return newerVersion.hasVersion() && hasVersion() && version.compareTo(newerVersion.getVersion()) < 0;
    }

    /**
     * Checks if there is an update available for the current local version.
     *
     * @return True if there is an update available; otherwise false.
     */
    public static boolean isUpdateAvailable() {
        return localVersion.updateAvailable(serverVersion);
    }

    /**
     * Check if the server side version is the ignore version.
     *
     * @return True if the server side version is the ignore version; otherwise false.
     */
    public static boolean isIgnoreVersion() {
        String ignoreVersion = ConfigProperties.getInstance().getIgnoreVersion();
        return !(ignoreVersion == null) && ignoreVersion.equals(serverVersion.getVersion());
    }

    /**
     * Get the server Datavyu version.
     *
     * @return Server side Datvyu version.
     */
    public static DatavyuVersion getServerVersion() {
        return serverVersion;
    }

    /**
     * Get the local Datavyu version.
     *
     * @return Local Datavyu version.
     */
    public static DatavyuVersion getLocalVersion() {
        return localVersion;
    }

    /**
     * Returns the locally installed version of datavyu for initialization purposes.
     *
     * @return The local datavyu version.
     */
    private static DatavyuVersion initLocalVersion() {
        ApplicationContext context = Application.getInstance(Datavyu.class).getContext();
        String version = context.getResourceMap(Datavyu.class).getString("Application.version");
        String build = context.getResourceMap(Build.class).getString("Application.build");
        return new DatavyuVersion(version, build);
    }

    /**
     * Returns the server side most recent version according to a text file that is published on the server.
     *
     * @param versionURL The URL where to find the currently available server version
     *
     * @return The server datavyu version.
     */
    private static DatavyuVersion initServerVersion(String versionURL) {
        String version = "";
        String build = "";
        try {
            URL url = new URL(versionURL);
            URLConnection urlConnection = url.openConnection();

            /* Bug 320: Add OS information to user-agent */
            String userAgentStr = Application.getInstance(Datavyu.class).getContext()
                    .getResourceMap(Build.class).getString("Application.version")
                    + "\t" +System.getProperty("java.version")
                    + "\t" + System.getProperty("os.name")
                    + "\t" + System.getProperty("os.version");
            urlConnection.setRequestProperty("User-Agent", userAgentStr);

            // Read version and build
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            version = bufferedReader.readLine();
            build = bufferedReader.readLine();
            bufferedReader.close();
        } catch (IOException e) {
            logger.error("Could not retrieve server version: " + e.getMessage());
        }
        return new DatavyuVersion(version, build);
    }

    /**
     * Returns the server side most recent version according to a text file that is published on the server.
     * Depending on the contents of the configuration this method may ignore pre-release versions.
     *
     * @return The server datavyu version.
     */
    private static DatavyuVersion initServerVersion() {
        return initServerVersion(ConfigProperties.getInstance().getUsePreRelease() ? PRE_VERSION_FILE : VERSION_FILE);
    }
}
