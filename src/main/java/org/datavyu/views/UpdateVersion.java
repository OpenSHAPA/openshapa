/*
 * Open this dialog: Help | Check for updates...
 */
package org.datavyu.views;

import org.datavyu.Configuration;
import org.datavyu.util.DatavyuVersion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// TODO: Load this font from the rest?
// TODO: What about language?
// TODO: Put the URLs into the config file
public class UpdateVersion extends JDialog {

    private static final String DOWNLOAD_PAGE = "http://www.datavyu.org/download.html";

    private static final String PRE_DOWNLOAD_PAGE = "http://www.datavyu.org/download.html";

    private JLabel localVersionLabel;
    private JLabel serverVersionLabel;
    private JLabel updateMessage;
    private JButton updateLaterButton;
    private JButton updateNeverButton;
    private JButton updateNowButton;
    private JSeparator jSeparator1;
    private JCheckBox checkPreRelease;

    private DatavyuVersion serverVersion;


    private void prepareDialog() {
        Configuration configuration = Configuration.getInstance();
        checkPreRelease.setSelected(configuration.getPrereleasePreference());

        DatavyuVersion localVersion = DatavyuVersion.getLocalVersion();
        serverVersion = DatavyuVersion.getServerVersion();

        localVersionLabel.setText(localVersion.getVersion());
        serverVersionLabel.setText(serverVersion.getVersion());

        // Show alternative dialog info, when update is not available
        if (!DatavyuVersion.isUpdateAvailable()) {
            if (!serverVersion.getVersion().equals("")) {
                setTitle("Datavyu is up to date!");
                updateMessage.setText("Datavyu is up to date!");
            } else {
                setTitle("Error Accessing Server");
                serverVersionLabel.setText("<unavailable>");
                updateMessage.setText("Error accessing version server");
            }
            updateLaterButton.setText("Close");
            updateLaterButton.setToolTipText("Close");
            updateLaterButton.setVisible(true);
            updateNowButton.setVisible(false);
            updateNeverButton.setVisible(false);
        }
    }

    /**
     * Creates new form UpdateVersion
     */
    public UpdateVersion(Frame parentFrame, boolean modal) {
        super(parentFrame, modal);
        initComponents();
        prepareDialog();
    }

    private void initComponents() {
        updateNowButton = new JButton();
        JLabel currentVersionLabel = new JLabel();
        JLabel updateAvailableLabel = new JLabel();
        JLabel checkingForUpdatesLabel = new JLabel();
        localVersionLabel = new JLabel();
        serverVersionLabel = new JLabel();
        updateLaterButton = new JButton();
        updateMessage = new JLabel();
        updateNeverButton = new JButton();
        checkPreRelease = new JCheckBox();
        jSeparator1 = new JSeparator();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Update isUpdateAvailable!");
        setBounds(new Rectangle(0, 0, 282, 123));
        setModal(true);
        setResizable(false);

        updateNowButton.setText("Now");
        updateNowButton.setToolTipText("Take me to the download page");
        updateNowButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                Configuration configuration = Configuration.getInstance();
                String url = configuration.getPrereleasePreference() ? PRE_DOWNLOAD_PAGE : DOWNLOAD_PAGE;
                try {
                    Desktop.getDesktop().browse(java.net.URI.create(url));
                    dispose();
                } catch (java.io.IOException e) {
                    System.out.println(e.getMessage());

                    setTitle("Error Opening Website");
                    updateMessage.setText("Please visit " + url);
                    updateLaterButton.setText("Close");
                    updateLaterButton.setToolTipText("Close");
                    updateLaterButton.setVisible(true);
                    updateNowButton.setVisible(false);
                    updateNeverButton.setVisible(false);
                }
            }
        });

        currentVersionLabel.setText("Datavyu Current Version:");

        localVersionLabel.setText("<version>");

        updateAvailableLabel.setText("Update available? ");

        serverVersionLabel.setText("<version>");

        updateLaterButton.setText("Later");
        updateLaterButton.setToolTipText("Remind me again");
        updateLaterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dispose();
                // If the 'later' button is clicked, clear the ignoreVersion; unless we're dealing with the case where
                // the server could not be accessed
                if (serverVersion.hasVersion()) {
                    Configuration.getInstance().setIgnoreVersion("");
                }
            }
        });

        updateMessage.setText("Get available update.");

        updateNeverButton.setText("Never");
        updateNeverButton.setToolTipText("Don't remind me of this update (but still notify me when the next version is released)");
        updateNeverButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dispose();
                Configuration.getInstance().setIgnoreVersion(serverVersion.getVersion());
            }
        });

        checkingForUpdatesLabel.setText("Checking for updates...");

        checkPreRelease.setText("Get pre-releases");
        checkPreRelease.setFocusPainted(false);
        checkPreRelease.setFocusable(false);
        checkPreRelease.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                // if we're selecting it, we need to confirm if the user is sure
                if (checkPreRelease.isSelected()) {
                    int dialogResult;
                    dialogResult = JOptionPane.showConfirmDialog(null,
                            "Pre-releases are not supported and may have bugs. Are you sure?", "Pre-release Warning!",
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        checkPreRelease.setSelected(true);
                    } else {
                        checkPreRelease.setSelected(false);
                    }
                }

                Configuration config = Configuration.getInstance();
                config.setPrereleasePreference(checkPreRelease.getSelectedObjects() != null);

                prepareDialog();
            }
        });

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(checkingForUpdatesLabel)
                                                                .addGap(30, 30, 30)
                                                                .addComponent(checkPreRelease))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addGap(28, 28, 28)
                                                                                .addComponent(updateNowButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addGap(18, 18, 18)
                                                                                .addComponent(updateLaterButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addGap(18, 18, 18)
                                                                                .addComponent(updateNeverButton))
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(updateAvailableLabel)
                                                                                .addGap(56, 56, 56)
                                                                                .addComponent(serverVersionLabel))
                                                                        .addComponent(updateMessage)
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(currentVersionLabel)
                                                                                .addGap(18, 18, 18)
                                                                                .addComponent(localVersionLabel)))
                                                                .addGap(0, 0, Short.MAX_VALUE))))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(205, 205, 205)
                                                .addComponent(jSeparator1)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(checkingForUpdatesLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(checkPreRelease)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(currentVersionLabel)
                                        .addComponent(localVersionLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(updateAvailableLabel)
                                        .addComponent(serverVersionLabel))
                                .addGap(18, 18, 18)
                                .addComponent(updateMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(updateNowButton)
                                        .addComponent(updateLaterButton)
                                        .addComponent(updateNeverButton))
                                .addContainerGap())
        );
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        // Create and display the dialog
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UpdateVersion dialog = new UpdateVersion(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
}
