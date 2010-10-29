package org.openshapa.views;

import com.usermetrix.jclient.Logger;

import java.awt.Frame;

import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

import org.openshapa.OpenSHAPA;

import org.openshapa.models.db.legacy.LogicErrorException;
import org.openshapa.models.db.legacy.MacshapaDatabase;
import org.openshapa.models.db.legacy.SystemErrorException;

import org.openshapa.util.Constants;

import com.usermetrix.jclient.UserMetrix;

import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;

import org.openshapa.controllers.NewProjectC;


/**
 * The dialog for users to create a new project.
 */
public final class NewProjectV extends OpenSHAPADialog {

    /** The logger for this class. */
    private Logger logger = UserMetrix.getLogger(NewProjectV.class);

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextArea descriptionField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField nameField;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates new form NewDatabaseView.
     *
     * @param parent
     *            The parent of this JDialog.
     * @param modal
     *            Is this dialog modal or not?
     */
    public NewProjectV(final Frame parent, final boolean modal) {
        super(parent, modal);
        logger.usage("newProj - show");
        initComponents();

        // Need to set a unique name so that we save and restore session data
        // i.e. window size, position, etc.
        setName(this.getClass().getSimpleName());
        getRootPane().setDefaultButton(okButton);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        descriptionField = new javax.swing.JTextArea();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        org.jdesktop.application.ResourceMap resourceMap =
            org.jdesktop.application.Application.getInstance(
                org.openshapa.OpenSHAPA.class).getContext().getResourceMap(
                NewProjectV.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N
        setResizable(false);

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setToolTipText(resourceMap.getString("jLabel1.toolTipText")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setToolTipText(resourceMap.getString("jLabel2.toolTipText")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        nameField.setName("nameField"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        descriptionField.setColumns(20);
        descriptionField.setFont(resourceMap.getFont("descriptionField.font")); // NOI18N
        descriptionField.setRows(5);
        descriptionField.setName("descriptionField"); // NOI18N
        jScrollPane1.setViewportView(descriptionField);

        cancelButton.setText(resourceMap.getString("cancelButton.text")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(
                    final java.awt.event.ActionEvent evt) {
                    cancelButtonActionPerformed(evt);
                }
            });

        okButton.setText(resourceMap.getString("okButton.text")); // NOI18N
        okButton.setName("okButton"); // NOI18N
        okButton.setPreferredSize(new java.awt.Dimension(65, 23));
        okButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(
                    final java.awt.event.ActionEvent evt) {
                    okButtonActionPerformed(evt);
                }
            });

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(
                GroupLayout.Alignment.LEADING).addGroup(
                layout.createSequentialGroup().addContainerGap().addGroup(
                    layout.createParallelGroup(
                        GroupLayout.Alignment.LEADING).addGroup(
                        layout.createSequentialGroup().addGroup(
                            layout.createParallelGroup(
                                GroupLayout.Alignment.LEADING).addComponent(
                                jLabel2).addComponent(jLabel1)).addPreferredGap(
                            LayoutStyle.ComponentPlacement.RELATED).addGroup(
                            layout.createParallelGroup(
                                GroupLayout.Alignment.TRAILING).addComponent(
                                nameField, GroupLayout.DEFAULT_SIZE, 274,
                                Short.MAX_VALUE).addComponent(jScrollPane1,
                                GroupLayout.Alignment.TRAILING,
                                GroupLayout.DEFAULT_SIZE, 274,
                                Short.MAX_VALUE))).addGroup(
                        GroupLayout.Alignment.TRAILING,
                        layout.createSequentialGroup().addComponent(okButton,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE).addPreferredGap(
                            LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cancelButton))).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(
                GroupLayout.Alignment.LEADING).addGroup(
                layout.createSequentialGroup().addContainerGap().addGroup(
                    layout.createParallelGroup(
                        GroupLayout.Alignment.BASELINE).addComponent(
                        jLabel1).addComponent(nameField,
                        GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    LayoutStyle.ComponentPlacement.RELATED).addGroup(
                    layout.createParallelGroup(
                        GroupLayout.Alignment.LEADING).addComponent(jLabel2)
                        .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE,
                            111, GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
                    layout.createParallelGroup(
                        GroupLayout.Alignment.BASELINE).addComponent(
                        cancelButton).addComponent(okButton,
                        GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE)).addContainerGap(
                    GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * The action to invoke when a user clicks on the CANCEL button.
     *
     * @param evt
     *            The event that triggered this action
     */
    private void cancelButtonActionPerformed(
        final java.awt.event.ActionEvent evt) { // GEN-FIRST:event_cancelButtonActionPerformed

        try {
            dispose();
            finalize();

            // Whoops, unable to destroy dialog correctly.
        } catch (Throwable e) {
            logger.error("Unable to release window NewProjectV.", e);
        }
    } // GEN-LAST:event_cancelButtonActionPerformed

    /**
     * The action to invoke when a user clicks on the OK button.
     *
     * @param evt
     *            The event that triggered this action.
     */
    private void okButtonActionPerformed(final java.awt.event.ActionEvent evt) { // GEN-FIRST:event_okButtonActionPerformed

        ResourceMap r = Application.getInstance(OpenSHAPA.class).getContext()
            .getResourceMap(NewProjectV.class);

        try {
            logger.usage("create new project");

            OpenSHAPAView s = (OpenSHAPAView) OpenSHAPA.getApplication()
                .getMainView();

            // clear the contents of the existing spreadsheet.
            OpenSHAPA.getProjectController().setLastCreatedCellId(0);

            if (!isValidProjectName(getProjectName())) {
                throw new LogicErrorException(r.getString("Error.invalidName"));
            }

            // BugzID:2352 - Clean up spreadsheet resources before creating a
            // new spreadsheet.
            s.clearSpreadsheet();

            MacshapaDatabase database = new MacshapaDatabase(
                    Constants.TICKS_PER_SECOND);
            database.setName(getProjectName());
            database.setDescription(getProjectDescription());

            OpenSHAPA.getProjectController().createNewProject(getProjectName());
            OpenSHAPA.getProjectController().setDatabase(database);

            s.showSpreadsheet();

            // Update the name of the window to include the name we just
            // set in the database.
            OpenSHAPA.getApplication().updateTitle();

            dispose();
            finalize();
        } catch (SystemErrorException ex) {
            logger.error("Unable to create new database", ex);
        } catch (LogicErrorException ex) {
            OpenSHAPA.getApplication().showWarningDialog(ex);
            new NewProjectC();
        } catch (Throwable ex) {
            logger.error("Unable to clean up the new project view.");
        }

        OpenSHAPA.getApplication().resetApp();

        // BugzID:2411 - Show data controller after creating a new project.
        OpenSHAPA.getApplication().show(OpenSHAPA.getDataController());
    } // GEN-LAST:event_okButtonActionPerformed

    private boolean isValidProjectName(final String name) {

        if (name == null) {
            return false;
        }

        if (name.length() == 0) {
            return false;
        }

        return true;
    }

    /**
     * @return The new name of the database as specified by the user.
     */
    public String getProjectName() {
        return nameField.getText();
    }

    /**
     * @return The new description of the database as specified by the user.
     */
    public String getProjectDescription() {
        return descriptionField.getText();
    }

}
