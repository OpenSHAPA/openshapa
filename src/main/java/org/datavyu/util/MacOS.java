package org.datavyu.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Holds any MacOS specific functions.
 */
public class MacOS {
    /** The logger for this class */
    private static Logger logger = LogManager.getLogger(DatavyuVersion.class);

    /**
     * Test whether the Apple press and hold functionality is enable.d
     *
     * @return True if press and hold is enabled; otherwise false.
     */
    public static boolean isOSXPressAndHoldEnabled() {
        try {
            Process process = new ProcessBuilder(new String[]{"bash", "-c", "defaults read -g ApplePressAndHoldEnabled"})
                    .redirectErrorStream(true)
                    .directory(new File("./"))
                    .start();
            ArrayList<String> output = new ArrayList<String>();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                output.add(line);
            }
            return !(output.size() > 0 && output.get(0).equals("0"));
        } catch (Exception e) {
            logger.error("Error when checking press and hold on OSX " + e.getMessage());
        }
        return false;
    }

    /**
     * Sets the press and hold value.
     *
     * @param pressAndHold The press and hold state.
     */
    public static void setOSXPressAndHoldValue(boolean pressAndHold) {
        try {
            Process process = new ProcessBuilder(new String[]{
                    "bash", "-c", "defaults write -g ApplePressAndHoldEnabled -bool " + (pressAndHold ? "true" : "false")})
                    .redirectErrorStream(true)
                    .directory(new File("./"))
                    .start();
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append(System.getProperty("line.separator"));
            }
            logger.info("Press and hold for OSX command returned: " + stringBuilder);
        } catch (Exception e) {
            logger.error("Error occured when processing press and hold " + e);
        }
    }
}
