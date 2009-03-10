/*
 * SpreadsheetView.java
 *
 * Created on 01/12/2008, 3:15:07 PM
 */

package au.com.nicta.openshapa.views.discrete;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * SpreadsheetView implements the Scrollable interface and
 * is the view to use in the viewport of the JScrollPane in Spreadsheet.
 * @author swhitcher
 */
public class SpreadsheetView extends javax.swing.JPanel
                                implements Scrollable {

    /** Maximum unit scroll amount. */
    private int maxUnitIncrement = 50;

    /** Creates new form SpreadsheetView. */
    public SpreadsheetView() {
    }

    /**
     * Returns the preferred size of the viewport for a view component.
     * In this instance it returns getPreferredSize
     *
     * @return the preferredSize of a <code>JViewport</code> whose view
     *    is this <code>SpreadsheetView</code>
     */
    public final Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    /**
     * @return False - the spreadsheet can scroll left to right if needed.
     */
    public final boolean getScrollableTracksViewportWidth() {
        return false;
    }

    /**
     * @return False - the spreadsheet can scroll up and down if needed.
     */
    public final boolean getScrollableTracksViewportHeight() {
        return false;
    }

    /**
     * Computes the scroll increment that will completely expose one new row
     * or column, depending on the value of orientation.
     *
     * @param visibleRect The view area visible within the viewport
     * @param orientation VERTICAL or HORIZONTAL.
     * @param direction Less than zero up/left, greater than zero down/right.
     * @return The "unit" increment for scrolling in the specified direction.
     *         This value should always be positive.
     */
    public final int getScrollableUnitIncrement(final Rectangle visibleRect,
                                                final int orientation,
                                                final int direction) {
        //Get the current position.
        int currentPosition = 0;
        if (orientation == SwingConstants.HORIZONTAL) {
            currentPosition = visibleRect.x;
        } else {
            currentPosition = visibleRect.y;
        }

        //Return the number of pixels between currentPosition
        //and the nearest tick mark in the indicated direction.
        if (direction < 0) {
            int newPosition = currentPosition
                                - (currentPosition / maxUnitIncrement)
                                * maxUnitIncrement;
            if (newPosition == 0) {
                return maxUnitIncrement;
            } else {
                return newPosition;
            }
        } else {
            return ((currentPosition / maxUnitIncrement) + 1)
                   * maxUnitIncrement
                   - currentPosition;
        }
    }

    /**
     * Computes the block scroll increment that will completely expose a row
     * or column, depending on the value of orientation.
     *
     * @param visibleRect The view area visible within the viewport
     * @param orientation VERTICAL or HORIZONTAL.
     * @param direction Less than zero up/left, greater than zero down/right.
     * @return The "block" increment for scrolling in the specified direction.
     *         This value should always be positive.
     */
    public final int getScrollableBlockIncrement(final Rectangle visibleRect,
                                                 final int orientation,
                                                 final int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return visibleRect.width - maxUnitIncrement;
        } else {
            return visibleRect.height - maxUnitIncrement;
        }
    }

}
