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

/**
 * Utilities for float points
 */
public final class FloatingPointUtils {

    /** Tolerance value for comparing two doubles for equality */
    private static final double THRESHOLD = 1e-6;

    /** Factor to use when converting doubles to fraction strings */
    private static final int FACTOR = 100000;

    /**
     * Almost equal for two floating point numbers using the default threshold.
     *
     * @param fp1 First double
     * @param fp2 Second double
     * @return True if almost equal; otherwise false
     */
    public static boolean almostEqual(final double fp1, final double fp2) {
        return almostEqual(fp1, fp2, THRESHOLD);
    }

    /**
     * Almost equal for two floating point numbers.
     *
     * @param fp1 First double
     * @param fp2 Second double
     * @param threshold Threshold for equality
     * @return True if almost equal; otherwise false
     */
    public static boolean almostEqual(final double fp1, final double fp2, final double threshold) {
        return Math.abs(fp1 - fp2) < threshold;
    }

    /**
     * Computes the greatest common divisor of two integers.
     *
     * @param a First integer.
     * @param b Second integer.
     *
     * @return The greatest common divisor of the two integers.
     */
    private static int greatestCommonDivisor(final int a, final int b) {
        return a >= b ? gcdFirstGreater(a, b) : gcdFirstGreater(b, a);
    }

    /**
     * Computes the greatest common divisor of two integers. Assumes a >= b.
     *
     * @param a First integer
     * @param b Second integer
     *
     * @return The greatest common divisor of the two integers.
     */
    private static int gcdFirstGreater(final int a, final int b) {
        return b == 0 ? a : gcdFirstGreater(b, a % b);
    }

    /**
     * Converts a double to a string fraction. E.g. converts 0.5 into a string reading "1/2".
     *
     * @param v The double to convert into a string fraction.
     *
     * @return A fraction string.
     */
    public static String doubleToFractionStr(double v) {
        boolean negative = false;

        // Determine if we are dealing with a negative value.
        if (v < 0) {
            negative = true;
            v = -v;
        }

        // Determine the the fraction: "whole numerator/denominator"
        int w = (int) ((v - Math.floor(v)) * FACTOR);
        int gcd = greatestCommonDivisor(w, FACTOR);
        int whole = (int) Math.floor(v);
        int numerator = w / gcd;

        // If a negative value was supplied, display a '-'
        String str = negative ? "-" : "";

        // If we have a whole number component add it to the output
        str += whole != 0 ? whole : "";

        // If we have a whole part, and a fraction add a space to the output
        if (whole != 0 && numerator != 0) {
            str += " ";
        } else if (whole == 0 && numerator == 0) {
            str = "0";
        }

        // If we have a fraction component, add it to the output
        if (numerator != 0) {
            int denominator = FACTOR / gcd;
            str += numerator + "/" + denominator;
        }

        return str;
    }
}
