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
package org.datavyu;

import ca.beq.util.win32.registry.Win32Exception;
import ch.randelshofer.quaqua.QuaquaManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.controllers.project.ProjectController;
import org.datavyu.models.db.TitleNotifier;
import org.datavyu.models.db.UserWarningException;
import org.datavyu.plugins.quicktime.QTDataViewer;
import org.datavyu.plugins.vlcfx.NativeLibraryManager;
import org.datavyu.undoableedits.SpreadsheetUndoManager;
import org.datavyu.util.*;
import org.datavyu.views.*;
import org.datavyu.views.discrete.SpreadsheetPanel;
import org.jdesktop.application.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.EventObject;

/**
 * The main class of the application.
 */
public final class Datavyu extends SingleFrameApplication implements KeyEventDispatcher, TitleNotifier {

    /**
     * All the supported platforms that Datavyu runs on.
     */
    public enum Platform {
        MAC, // Generic Mac platform. I.e. Tiger, Leopard, Snow Leopard
        WINDOWS, // Generic openWindows platform. I.e. XP, vista, etc.
        LINUX, // Generic Linux platform.
        UNKNOWN
    }
    /** The logger for this class */
    private static Logger logger = LogManager.getLogger(Datavyu.class);

    private static final NativeLibraryManager nativeLibraryManager;

    /** The desired minimum initial width */
    private static final int INIT_MIN_X = 600;

    /** The desired minimum initial height */
    private static final int INIT_MIN_Y = 700;

    private static boolean osxPressAndHoldEnabled;

    /**
     * Constant variable for the Datavyu main panel to send keyboard shortcuts while the QTController is in focus.
     * Initialized in startup().
     */
    private static DatavyuView datavyuView;

    /** The view to use for the video controller */
    private static VideoController dataController;

    /** The project controller instance */
    private static ProjectController projectController;

    /** Load native libraries */
    static {
        logger.info("Working directory is: " + System.getProperty("user.dir"));
        logger.info("Class path is: " + System.getProperty("java.class.path"));
        logger.info("The java library path is: " + System.getProperty("java.library.path"));

        // TODO: move this into respective viewers!
        nativeLibraryManager = new NativeLibraryManager(System.getProperty("java.io.tmpdir")
                + File.separator + "vlc" + File.separator);

        switch (getPlatform()) {
            case MAC:
                logger.info("Detected platform: MAC");
                try {
                    NativeLibraryLoader.extractAndLoad("quaqua64");
                } catch (Exception e) {
                    logger.error("Could not load library quaqua64 for mac OS " + e);
                }
                if (MacOS.isOSXPressAndHoldEnabled()) {
                    osxPressAndHoldEnabled = true;
                    MacOS.setOSXPressAndHoldValue(false);
                }
                break;

            case WINDOWS:
                logger.info("Detected platform: WINDOWS");
                try {
                    if (System.getProperty("sun.arch.data.model").equals("32")) {
                        logger.info("Loading libraries for 32 bit QT");
                        NativeLibraryLoader.extract("QTJNative");
                        NativeLibraryLoader.extractAndLoad("QTJavaNative");
                        QTDataViewer.librariesFound = true;
                    }
                } catch (Exception e) {
                    logger.error("Could not load libraries for QT " + e);
                }
                break;

            default:
                logger.error("Unknown or unsupported platform!");
                break;
        }
    }

    /**
     * @return The platform that Datavyu is running on.
     */
    public static Platform getPlatform() {
        String os = System.getProperty("os.name");
        if (os.contains("Mac")) {
            return Platform.MAC;
        }
        if (os.contains("Win")) {
            return Platform.WINDOWS;
        }
        if (os.contains("Linux")) {
            return Platform.LINUX;
        }
        return Platform.UNKNOWN;
    }

    public boolean ready = false;

    /** Tracks if a NumPad key has been pressed */
    private boolean numKeyDown = false;

    /** File path from the command line */
    private String commandLineFile;

    // TODO: Move this into the QTDataViewer class!!
    public static boolean hasQuicktimeLibs() {
        boolean found = false;
        try {
            Class.forName("quicktime.QTSession");
            found = true;
            QTDataViewer.librariesFound = true;
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

    /**
     * Gets the single instance of the data controller that is currently used
     * with Datavyu.
     *
     * @return The single data controller in use with this instance of
     * Datavyu.
     */
    public static VideoController getDataController() {
        return dataController;
    }

    /**
     * Sets the data controller.
     *
     * @param dataController The data controller instance to set.
     */
    public static void setDataController(VideoController dataController) {
        Datavyu.dataController = dataController;
    }

    /**
     * A convenient static getter for the application instance.
     *
     * @return The instance of the Datavyu application.
     */
    public static Datavyu getApplication() {
        return Application.getInstance(Datavyu.class);
    }

    /**
     * Main method launching the application.
     *
     * @param args The command line arguments passed to Datavyu.
     */
    public static void main(final String[] args) {
        // If we are running on a MAC set system properties
        if (Datavyu.getPlatform() == Platform.MAC) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Datavyu");
            System.setProperty("Quaqua.jniIsPreloaded", "true");
        }
        launch(Datavyu.class, args);
    }

    // TODO: Why do we have these two project controllers?
    public static ProjectController getProjectController() {
        if (datavyuView != null && datavyuView.getSpreadsheetPanel() != null
                && datavyuView.getSpreadsheetPanel().getProjectController() != null) {
            return datavyuView.getSpreadsheetPanel().getProjectController();
        }
        return projectController;
    }

    public static void setProjectController(ProjectController p) {
        projectController = p;
    }

    public static DatavyuView getView() {
        return datavyuView;
    }

    public void setCommandLineFile(String commandLineFile) {
        logger.info("Command line file set to " + commandLineFile);
        this.commandLineFile = commandLineFile;
    }

    /**
     * Dispatches the keystroke to the correct action.
     *
     * @param evt The event that triggered this action.
     * @return true if the KeyboardFocusManager should take no further action
     * with regard to the KeyEvent; false otherwise
     */
    @Override
    public boolean dispatchKeyEvent(final KeyEvent evt) {

        /**
         * This switch is for hot keys that are on the main section of the
         * keyboard.
         */
        int modifiers = evt.getModifiers();

        // BugzID:468 - Define accelerator keys based on OS.
        int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        // If we are typing a key that is a shortcut - we consume it immediately.
        if ((evt.getID() == KeyEvent.KEY_TYPED) && (modifiers == keyMask)) {

            // datavyuView also has the fun of key accelerator handling. If it is
            // focused, let it handle the fun or everything is done twice. If it
            // doesn't have focus we manually handle it in the switch below.
            if (getView().getFrame().isFocused()) {
                evt.consume();
                return true;
            }

            switch (getPlatform()) {

                // Code table used by Windows is different.
                case WINDOWS: {
                    switch (WindowsOS.remapKeyChar(evt.getKeyChar())) {

                        case '+':
                        case '-':
                            // Plus and minus do not respond. Uncomment
                            // the printout above to see what I mean.

                        case 'O':
                            getView().open();
                            evt.consume();

                            return true;

                        case 'S':
                            getView().save();
                            evt.consume();

                            return true;

                        case 'N':
                            getView().showNewProjectForm();
                            evt.consume();

                            return true;

                        case 'L':
                            getView().newCellLeft();
                            evt.consume();

                            return true;

                        case 'R':
                            getView().newCellRight();
                            evt.consume();

                            return true;

                        default:
                            break;
                    }
                }

                break;

                default: {

                    switch (evt.getKeyChar()) {

                        case '=': // Can't access + without shift.
                            getView().zoomIn();
                            evt.consume();

                            return true;

                        case '-':
                            getView().zoomOut();
                            evt.consume();

                            return true;

                        case 'o':
                            getView().open();
                            evt.consume();

                            return true;

                        case 's':
                            getView().save();
                            evt.consume();

                            return true;

                        case 'n':
                            getView().showNewProjectForm();
                            evt.consume();

                            return true;

                        case 'l':
                            getView().newCellLeft();
                            evt.consume();

                            return true;

                        case 'r':
                            getView().newCellRight();
                            evt.consume();

                            return true;

                        default:
                            break;
                    }
                }
            }
        }


        if ((evt.getID() == KeyEvent.KEY_PRESSED) && (evt.getKeyLocation() == KeyEvent.KEY_LOCATION_STANDARD)) {

            switch (evt.getKeyCode()) {
                /**
                 * This case is because VK_PLUS is not linked to a key on the
                 * English keyboard. So the GUI is bound to VK_PLUS and VK_SUBTACT.
                 * VK_SUBTRACT is on the numpad, but this is short-circuited above.
                 * The cases return true to let the KeyboardManager know that there
                 * is nothing left to be done with these keys.
                 */
                case KeyEvent.VK_EQUALS:
                    if (modifiers == keyMask) {
                        datavyuView.changeFontSize(DatavyuView.ZOOM_INTERVAL);
                    }
                    return true;

                case KeyEvent.VK_MINUS:
                    if (modifiers == keyMask) {
                        datavyuView.changeFontSize(-DatavyuView.ZOOM_INTERVAL);
                    }
                    return true;

                default:
                    break;
            }
        }

        // BugzID:784 - Shift key is passed to Data Controller.
        if (evt.getKeyCode() == KeyEvent.VK_SHIFT) {
            if (evt.getID() == KeyEvent.KEY_PRESSED) {
                dataController.setShiftMask(true);
            } else {
                dataController.setShiftMask(false);
            }
        }

        // BugzID:736 - Control key is passed to Data Controller.
        if (evt.getKeyCode() == KeyEvent.VK_CONTROL) {
            if (evt.getID() == KeyEvent.KEY_PRESSED) {
                dataController.setCtrlMask(true);
            } else {
                dataController.setCtrlMask(false);
            }
        }

        /**
         * The following cases handle numpad keystrokes.
         */
        if ((evt.getID() == KeyEvent.KEY_PRESSED) && (evt.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD)) {
            numKeyDown = true;
        } else if (numKeyDown && (evt.getID() == KeyEvent.KEY_TYPED)) {
            return true;
        }

        if ((evt.getID() == KeyEvent.KEY_RELEASED) && (evt.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD)) {
            numKeyDown = false;
        }

        if (!numKeyDown) {
            return false;
        }

        boolean result = true;

        switch (evt.getKeyCode()) {

            case KeyEvent.VK_DIVIDE:
                //Mac - Show/hide
                if (getPlatform().equals(Platform.MAC)) {
                    dataController.pressShowTracksSmall();
                }
                //Win - point cell
                else {
                    dataController.pressPointCell();
                }

                break;

            case KeyEvent.VK_EQUALS:
                //Mac - point cell
                if (getPlatform().equals(Platform.MAC)) {
                    dataController.pressPointCell();
                }
                //Win - nothing
                break;

            case KeyEvent.VK_ASTERISK:

                break;
            case KeyEvent.VK_MULTIPLY:
                //Win - Show/hide
                if (!getPlatform().equals(Platform.MAC)) {
                    dataController.pressShowTracksSmall();
                }
                break;

            case KeyEvent.VK_NUMPAD7:
                dataController.pressSetCellOnset();

                break;

            case KeyEvent.VK_NUMPAD8:
                dataController.pressPlay();

                break;

            case KeyEvent.VK_NUMPAD9:
                dataController.pressSetCellOffsetNine();

                break;

            case KeyEvent.VK_NUMPAD4:
                dataController.pressShuttleBack();

                break;

            case KeyEvent.VK_NUMPAD5:
                dataController.pressStop();

                break;

            case KeyEvent.VK_NUMPAD6:
                dataController.pressShuttleForward();

                break;

            case KeyEvent.VK_NUMPAD1:

                // We don't do the press Jog thing for jogging - as users often
                // just hold the button down... Which causes weird problems when
                // attempting to do multiple presses.
                dataController.jogBackAction();

                break;

            case KeyEvent.VK_NUMPAD2:
                dataController.pressPause();

                break;

            case KeyEvent.VK_NUMPAD3:

                // We don't do the press Jog thing for jogging - as users often
                // just hold the button down... Which causes weird problems when
                // attempting to do multiple presses.
                dataController.jogForwardAction();

                break;

            case KeyEvent.VK_NUMPAD0:
                dataController.pressCreateNewCellSettingOffset();

                break;

            case KeyEvent.VK_DECIMAL:
                dataController.pressSetCellOffsetPeriod();

                break;

            case KeyEvent.VK_SUBTRACT:

                if (modifiers == InputEvent.CTRL_MASK) {
                    dataController.clearRegionOfInterestAction();
                } else {
                    dataController.pressGoBack();
                }

                break;

            case KeyEvent.VK_ADD:

                if (modifiers == InputEvent.SHIFT_MASK) {
                    dataController.pressFind();
                    dataController.findOffsetAction();
                } else if (modifiers == InputEvent.CTRL_MASK) {
                    dataController.pressFind();
                    dataController.setRegionOfInterestAction();
                } else {
                    dataController.pressFind();
                }

                break;

            case KeyEvent.VK_ENTER:
                dataController.pressCreateNewCell();

                break;

            default:

                // Do nothing with the key.
                result = false;

                break;
        }

        return result;
    }

    /**
     * Action for showing the quicktime video controller.
     */
    public void showDataController() {
        Datavyu.getApplication().show(dataController);
        dataController.setShouldBeVisible(true);
    }

    /**
     * Action for showing the video converter.
     */
    public void showVideoConverter() {
        VideoConverterV videoConverter = new VideoConverterV();
        Datavyu.getApplication().show(videoConverter);
    }

    /**
     * Action for showing the variable list.
     */
    public void showVariableList() {
        JFrame mainFrame = Datavyu.getApplication().getMainFrame();
        VariableListV listVarView = new VariableListV(mainFrame, true, projectController.getDataStore());
        listVarView.registerListeners();
        Datavyu.getApplication().show(listVarView);
    }

    /**
     * Action for showing the Undo History.
     */
    public void showHistory() {
        JFrame mainFrame = Datavyu.getApplication().getMainFrame();
        SpreadsheetUndoManager undoManager = getView().getSpreadsheetUndoManager();
        UndoHistoryWindow history = new UndoHistoryWindow(mainFrame, false, undoManager);
        Datavyu.getApplication().show(history);
    }

    /**
     * Action for showing the about window.
     */
    public void showAboutWindow() {
        JFrame mainFrame = Datavyu.getApplication().getMainFrame();
        AboutV aboutWindow = new AboutV(mainFrame, false);
        Datavyu.getApplication().show(aboutWindow);
    }

    /**
     * Action for opening the support site
     */
    public void openSupportSite() {
        String url = ConfigProperties.getInstance().getSupportSiteUrl();
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (java.io.IOException e) {
            logger.error("Error while opening support site " + e);
        }
    }

    /**
     * Action for opening the guide site
     */
    public void openGuideSite() {
        String url = ConfigProperties.getInstance().getUserGuideUrl();
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (java.io.IOException e) {
            logger.error("Error while opening guide site " + e);
        }
    }

    /**
     * Action for showing the about window.
     */
    public void showUpdateWindow() {
        Datavyu.getApplication().show(new UpdateVersion(Datavyu.getApplication().getMainFrame(), true));
    }

    /**
     * Show a warning dialog to the user.
     *
     * @param s The message to present to the user.
     */
    public void showWarningDialog(final String s) {
        JFrame mainFrame = Datavyu.getApplication().getMainFrame();
        ResourceMap resourceMap = Application.getInstance(Datavyu.class).getContext().getResourceMap(Datavyu.class);
        JOptionPane.showMessageDialog(mainFrame, s, resourceMap.getString("WarningDialog.title"),
                JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Show a warning dialog to the user.
     *
     * @param e The UserWarningException to present to the user.
     */
    public void showWarningDialog(final UserWarningException e) {
        showWarningDialog(e.getMessage());
    }

    /**
     * User quits- check for save needed. Note that this can be used even in
     * situations when the application is not truly "quitting", but just the
     * database information is being lost (e.g. on an "open" or "new"
     * instruction). In all interpretations, "true" indicates that all unsaved
     * changes are to be discarded.
     *
     * @return True for quit, false otherwise.
     */
    public boolean safeQuit() {
        JFrame mainFrame = Datavyu.getApplication().getMainFrame();
        ResourceMap resourceMap = Application.getInstance(Datavyu.class).getContext().getResourceMap(Datavyu.class);
        if (getView().checkAllTabsForChanges()) {
            for (Component tab : getView().getTabbedPane().getComponents()) {
                if (tab instanceof SpreadsheetPanel) {
                    SpreadsheetPanel sp = (SpreadsheetPanel) tab;
                    getView().getTabbedPane().setSelectedComponent(sp);
                    // Ask to save if this spreadsheet has been changed
                    if (sp.getProjectController().isChanged()) {
                        String cancelOption = "Cancel";
                        String noOption = "Don't save";
                        String yesOption = "Save";

                        String[] options = getPlatform() == Platform.MAC ? MacOS.getOptions(yesOption, noOption, cancelOption) :
                                WindowsOS.getOptions(yesOption, noOption, cancelOption);

                        // Get name of spreadsheet.  Check in both project and datastore.
                        String projectName = sp.getProjectController().getProjectName();

                        if (projectName == null) {
                            projectName = sp.getDataStore().getName();
                        }

                        int selectedOption = JOptionPane.showOptionDialog(mainFrame,
                                resourceMap.getString("UnsavedDialog.message",projectName),
                                resourceMap.getString("UnsavedDialog.title"),
                                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                                null, options, yesOption);

                        if (selectedOption == 0) {
                            getView().save();
                        }

                        return getPlatform() == Platform.MAC ? !(selectedOption == 1) : !(selectedOption == 2);
                    }
                }
            }
            // User has been asked whether or not to save each file, we can return now
            return true;
        } else {
            // Project hasn't been changed.
            return true;
        }
    }

    /**
     * Function to check whether or not it is OK to close this tab
     */
    public boolean safeQuit(Component tab) {
        JFrame mainFrame = Datavyu.getApplication().getMainFrame();
        ResourceMap resourceMap = Application.getInstance(Datavyu.class).getContext().getResourceMap(Datavyu.class);
        SpreadsheetPanel spreadsheetPanel = (SpreadsheetPanel) tab;
        if (spreadsheetPanel.getProjectController().isChanged()) {
            getView().getTabbedPane().setSelectedComponent(spreadsheetPanel);

            String cancelOption = "Cancel";
            String noOption = "Don't save";
            String yesOption = "Save";

            String[] options = getPlatform() == Platform.MAC ? MacOS.getOptions(yesOption, noOption, cancelOption) :
                    WindowsOS.getOptions(yesOption, noOption, cancelOption);

            // Get project name.
            String projName = spreadsheetPanel.getProjectController().getProjectName();
            if (projName == null) {
                projName = spreadsheetPanel.getDataStore().getName();
            }
            
            int selectedOption = JOptionPane.showOptionDialog(mainFrame,
                    resourceMap.getString("UnsavedDialog.tabmessage", projName),
                    resourceMap.getString("UnsavedDialog.title"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, yesOption);

            if (selectedOption == 0) {
                getView().save();
            }

            // Mac: yes | cancel | no   and Windows: yes | no | cancel
            return getPlatform() == Platform.MAC ? !(selectedOption == 1) : !(selectedOption == 2);
        } else {
            return true;
        }
    }

    /**
     * Action to call when the application is exiting.
     *
     */
    @Override
    protected void end() {
        Datavyu.getApplication().getMainFrame().setVisible(false);
        shutdown();
        super.end();
    }

    /**
     * If the user is trying to save over an existing file, prompt them whether
     * they they wish to continue.
     *
     * @return True for overwrite, false otherwise.
     */
    public boolean overwriteExisting() {
        JFrame mainFrame = Datavyu.getApplication().getMainFrame();
        ResourceMap resourceMap = Application.getInstance(Datavyu.class).getContext().getResourceMap(Datavyu.class);
        String defaultOption = "Cancel";
        String alternativeOption = "Overwrite";
        String[] options = getPlatform() == Platform.MAC ? MacOS.getOptions(defaultOption, alternativeOption) :
                WindowsOS.getOptions(defaultOption, alternativeOption);

        int selectedOption = JOptionPane.showOptionDialog(
                mainFrame,
                resourceMap.getString("OverwriteDialog.message"),
                resourceMap.getString("OverwriteDialog.title"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                defaultOption);

        return getPlatform() == Platform.MAC ? selectedOption == 1 : selectedOption == 0;
    }

    @Override
    protected void initialize(final String[] args) {

        switch (getPlatform()) {
            case MAC:
                try {
                    UIManager.setLookAndFeel(QuaquaManager.getLookAndFeel());
                } catch (UnsupportedLookAndFeelException e) {
                    logger.error("Failed to set Quaqua LNF " + e);
                }
                MacOS.loadCompileHandler();
                break;
            case WINDOWS:
                // BugzID:1288
                try {
                    WindowsOS.associate(".opf", WindowsOS.cwd().toString());
                } catch (Win32Exception e) {
                    logger.error("Could not associate .opf " + e);
                }
                break;
        }

        // This is for handling files opened from the command line.
        if (args.length > 0) {
            commandLineFile = args[0];
        }

        // Check for updates
        if (DatavyuVersion.isUpdateAvailable() && !DatavyuVersion.isIgnoreVersion()) {
            Datavyu.getApplication().show(new UpdateVersion(Datavyu.getApplication().getMainFrame(), true));
        }
    }

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {

        // Make view the new view so we can keep track of it for hot keys
        datavyuView = new DatavyuView(this);
        show(datavyuView);
        datavyuView.getFileSplitPane().setDividerLocation(0.75);

        // BugzID:435 - Correct size if a small size is detected
        int width = (int) getMainFrame().getSize().getWidth();
        int height = (int) getMainFrame().getSize().getHeight();

        if ((width < INIT_MIN_X) || (height < INIT_MIN_Y)) {
            int x = Math.max(width, INIT_MIN_X);
            int y = Math.max(height, INIT_MIN_Y);
            getMainFrame().setSize(x, y);
        }

        addExitListener(new ExitListener() {
            @Override
            public boolean canExit(EventObject eventObject) {
                return safeQuit();
            }

            @Override
            public void willExit(EventObject eventObject) { /* nothing to do here */ }
        });

        // Create video controller.
        projectController = new ProjectController();
        dataController = datavyuView.getSpreadsheetPanel().getDataController();

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = getView().getFrame().getX();

        // don't let the data viewer fall below the bottom of the primary
        // screen, but also don't let it creep up above the screen either
        int y = getView().getFrame().getY() + getView().getFrame().getHeight();
        y = (int) Math.max(Math.min(y, screenSize.getHeight() - dataController.getHeight()), 0);
        dataController.setLocation(x, y);
        show(dataController);
        datavyuView.checkForAutosavedFile();

        // The DB we create by default doesn't really have any unsaved changes.
        projectController.getDataStore().markAsUnchanged();
        ready();
    }

    @Override
    protected void ready() {
        ready = true;
        if (commandLineFile != null) {
            getView().openExternalFile(new File(commandLineFile));
            commandLineFile = null;
        }
    }

    /**
     * Clean up after ourselves.
     */
    @Override
    public void shutdown() {
        if (getPlatform() == Platform.MAC && osxPressAndHoldEnabled) {
            MacOS.setOSXPressAndHoldValue(true);
        }
        nativeLibraryManager.purge();
        super.shutdown();
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     *
     * @param root The parent window.
     */
    @Override
    protected void configureWindow(final Window root) {}

    @Override
    public void updateTitle() {
        // Update the main title
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (datavyuView != null) {
                    datavyuView.updateTitle();
                }
            }
        });
    }
}
