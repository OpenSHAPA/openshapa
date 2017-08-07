package org.datavyu.util;

/**
 * This class represents a version range.
 */
public class VersionRange {

    /** Minimum version in range */
    private int minVersion;

    /** Maximum version in range */
    private int maxVersion;

    /**
     * Create a version range from [min, ..., max]. Both values are inclusive.
     *
     * @param min The minimum version number.
     * @param max The maximum version number.
     */
    public VersionRange(int min, int max) {
        minVersion = min;
        maxVersion = max;
    }

    /**
     * Check if the version is within range.
     *
     * @param version The version number.
     *
     * @return True if in range; otherwise false.
     */
    public boolean isInRange(int version) {
        return version >= minVersion && version <= maxVersion;
    }
}
