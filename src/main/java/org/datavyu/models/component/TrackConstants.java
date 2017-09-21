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
package org.datavyu.models.component;

import javax.swing.*;
import java.awt.*;


public interface TrackConstants {

    Color BORDER_COLOR = new Color(73, 73, 73);
    int CARRIAGE_HEIGHT = 75;
    int HEADER_WIDTH = 140;
    int ACTION_BUTTON_WIDTH = 20;
    int ACTION_BUTTON_HEIGHT = 20;

    /**
     * Icon for hiding a track video.
     */
    ImageIcon VIEWER_HIDE_ICON = new ImageIcon(TrackConstants.class.getResource("/icons/eye.png"));

    /**
     * Icon for showing the video.
     */
    ImageIcon VIEWER_SHOW_ICON = new ImageIcon(TrackConstants.class.getResource("/icons/eye-shut.png"));

    /**
     * Unlock icon.
     */
    ImageIcon UNLOCK_ICON = new ImageIcon(TrackConstants.class.getResource("/icons/track-unlock.png"));

    /**
     * Lock icon.
     */
    ImageIcon LOCK_ICON = new ImageIcon(TrackConstants.class.getResource("/icons/track-lock.png"));

    /**
     * Delete icon.
     */
    ImageIcon DELETE_ICON = new ImageIcon(TrackConstants.class.getResource("/icons/close-track-x.png"));

}
