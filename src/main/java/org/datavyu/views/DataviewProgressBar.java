/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.datavyu.views;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

// TODO: Why can we not use the progress bar that java provides?
public class DataviewProgressBar extends JDialog {

    private boolean isError;
    private boolean isCanceled;
    private int progress;
    private long startTime; // in milliseconds
    private JButton cancelButton;
    private JLabel messageLabel;
    private JProgressBar progressBar;
    private JScrollPane scrollPane1;
    private JTextArea infoTextArea;

    /**
     * Creates new form DataviewProgressBar
     */
    public DataviewProgressBar(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        isError = false;
        isCanceled = false;
        progress = 0;
        progressBar.setIndeterminate(true);
        startTime = new Date().getTime();
    }

    public void setProgress(final int value) {
        this.setProgress(value, "");
    }

    public boolean setProgress(final int value, final String update) {
        progress = value;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (value != -1) {
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
                infoTextArea.append(String.format("%5.2f - %2d%% - %s\n", (new Date().getTime() - startTime) / 1000.0,
                        value, update));
            }
        });
        return isCanceled;
    }

    public void setError(final String update) {
        setProgress(progress, update);
        isError = true;
        messageLabel.setText(update);
        messageLabel.setFont(new Font(messageLabel.getFont().getName(), Font.BOLD, messageLabel.getFont().getSize()));
        cancelButton.setText("Close");
        this.setVisible(false);  // Toggling the visibility is necessary to change the modal status.
        this.setModal(true);
        this.setVisible(true);
    }

    public void setIndeterminate(final boolean indeterminate) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setIndeterminate(indeterminate);
            }
        });
    }

    public int getProgress() {
        return progress;
    }

    public boolean isCancelled() {
        return isCanceled;
    }

    public void close() {
        this.dispose();
    }

    private void initComponents() {

        progressBar = new JProgressBar();
        messageLabel = new JLabel();
        scrollPane1 = new JScrollPane();
        infoTextArea = new JTextArea();
        cancelButton = new JButton();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        progressBar.setIndeterminate(true);

        messageLabel.setFont(new Font("Tahoma", 0, 12));
        messageLabel.setText("Running...");

        infoTextArea.setEditable(false);
        infoTextArea.setColumns(20);
        infoTextArea.setFont(new Font("Courier New", 0, 10));
        infoTextArea.setRows(5);
        scrollPane1.setViewportView(infoTextArea);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_cancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(messageLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(cancelButton))
                                        .addComponent(scrollPane1, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(messageLabel)
                                        .addComponent(cancelButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(scrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                                .addContainerGap())
        );

        pack();
    }

    private void jButton_cancelActionPerformed(java.awt.event.ActionEvent evt) {
        isCanceled = true;
        infoTextArea.append(String.format("Aborting..."));
        if (isError) {
            close();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DataviewProgressBar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DataviewProgressBar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DataviewProgressBar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DataviewProgressBar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                DataviewProgressBar dialog = new DataviewProgressBar(new javax.swing.JFrame(), true);
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
