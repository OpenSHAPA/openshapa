package org.datavyu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.application.LocalStorage;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * A class that maintains N most recently remembered files.
 */
public final class RecentFiles implements Serializable{

    /** Logger for this class */
    private static Logger logger = LogManager.getLogger(RecentFiles.class);

    /** Limit for the file listing */
    private int limit;

    /** A listing of the N most recent files */
    private List<String> recentFiles;

    /** Maximum number of files per list */
    private static final int NUM_MAX_HISTORY = 5;

    /**
     * Create recent files.
     *
     * @param limit The limit for the recent files.
     */
    public RecentFiles(int limit) {
        this.limit = limit;
        this.recentFiles = new LinkedList<>();
    }

    public RecentFiles() {
        this(NUM_MAX_HISTORY);
    }

    /**
     * Remember the file as most recent one among the others.
     *
     * @param file The file.
     */
    public void remember(String file) {
        logger.info("Remember file: " + file);
        // possibly remove the file if it exists (must be most recent)
        recentFiles.remove(file);
        // add the file at the first place
        recentFiles.add(0, file);
        // If we exceed the limit remove the last entry
        if (recentFiles.size() == limit) {
            recentFiles.remove(limit-1);
        }
    }

    public List<String> getRecentFiles() {
        return recentFiles;
    }

    public void setRecentFiles(List<String> recentFiles) {
        this.recentFiles = recentFiles;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public static RecentFiles load(String fileName) throws IOException {
        LocalStorage localStorage = Datavyu.getApplication().getContext().getLocalStorage();
        logger.info("Loading recent files from: "
                + localStorage.getDirectory().getAbsolutePath() + File.separator + fileName);
        return (RecentFiles) localStorage.load(fileName);
    }

    public static void save(RecentFiles recentFiles, String fileName) throws IOException {
        LocalStorage localStorage = Datavyu.getApplication().getContext().getLocalStorage();
        logger.info("Saving recent files to: "
                + localStorage.getDirectory().getAbsolutePath() + File.separator + fileName);
        localStorage.save(recentFiles, fileName);
    }
}
