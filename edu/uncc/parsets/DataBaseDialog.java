/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DataBaseDialog.java
 *
 * Created on Feb 15, 2010, 8:10:40 PM
 */

package edu.uncc.parsets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.hibernate.SessionFactory;

import edu.uncc.parsets.gui.MessageDialog;
import genosets.interaction.GenoSetsSessionManager;

/**
 *
 * @author aacain
 */
public class DataBaseDialog extends javax.swing.JDialog {

    private final String propertiesFileName = "DatabaseProperties.properties";
    Properties properties;
    private Map<String, DatabaseProperties> dbPropertiesMap = new HashMap(20);
    private boolean creatingNew = false;

    /** Creates new form DataBaseDialog */
    public DataBaseDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        readPropertiesFile();
        this.setTitle("GenoSets: Select Database");
        initComponents();
    }


    private class DatabaseProperties{
        Integer id;
        String host;
        String port;
        String database;
        String userName;
        String password;
        String connectionName;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        dbComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        testConnectionButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        passwordSaveBox = new javax.swing.JCheckBox();
        jLabel9 = new javax.swing.JLabel();
        userField = new javax.swing.JTextField();
        dbField = new javax.swing.JTextField();
        portField = new javax.swing.JTextField();
        hostField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        connectionNameField = new javax.swing.JTextField();
        deleteButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();
        connectButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setText("Connect to database:");
        jLabel1.setName("jLabel1"); // NOI18N

        dbComboBox.setModel(getComboBoxModel());
        dbComboBox.setName("dbComboBox"); // NOI18N
        dbComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbComboBoxActionPerformed(evt);
            }
        });

        jLabel3.setText("Host:");
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setText("Port:");
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setText("Database:");
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel6.setText("Password:");
        jLabel6.setName("jLabel6"); // NOI18N

        passwordField.setName("passwordField"); // NOI18N
        passwordField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldChanged();
            }
        });

        testConnectionButton.setText("Test Connection");
        testConnectionButton.setName("testConnectionButton"); // NOI18N
        testConnectionButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                testConnectionButtonMouseClicked(evt);
            }
        });

        saveButton.setText("Save");
        saveButton.setName("saveButton"); // NOI18N
        saveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                saveButtonMouseClicked(evt);
            }
        });

        passwordSaveBox.setText("Save Password");
        passwordSaveBox.setName("passwordSaveBox"); // NOI18N
        passwordSaveBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                passwordSaveBoxMouseClicked(evt);
            }
        });


        jLabel9.setText("User:");
        jLabel9.setName("jLabel9"); // NOI18N

        userField.setText("");
        userField.setName("userField"); // NOI18N
        userField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldChanged();
            }
        });

        dbField.setText("");
        dbField.setName("dbField"); // NOI18N
        dbField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldChanged();
            }
        });

        portField.setText("");
        portField.setName("portField"); // NOI18N
        portField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldChanged();
            }
        });

        hostField.setText("");
        hostField.setName("hostField"); // NOI18N
        hostField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fieldChanged();
            }
        });

        jLabel7.setText("ConnectionName:");
        jLabel7.setName("jLabel7"); // NOI18N

        connectionNameField.setName("connectionNameField"); // NOI18N

        deleteButton.setText("Delete");
        deleteButton.setName("deleteButton"); // NOI18N
        deleteButton.setEnabled(false);
        deleteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteButtonClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dbComboBox, 0, 474, Short.MAX_VALUE)
                    .addComponent(jLabel1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(passwordField, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                            .addComponent(passwordSaveBox)
                            .addComponent(userField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                            .addComponent(dbField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                            .addComponent(portField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                            .addComponent(hostField, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(testConnectionButton))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(connectionNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(dbComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(hostField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(dbField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addComponent(passwordSaveBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7)
                    .addComponent(connectionNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(testConnectionButton)
                    .addComponent(deleteButton)
                    .addComponent(saveButton))
                .addContainerGap())
        );

        cancelButton.setText("Cancel");
        cancelButton.setName("cancelButton"); // NOI18N
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cancelButtonMouseClicked(evt);
            }
        });

        statusLabel.setText("Select database or configure new connection.");
        statusLabel.setName("statusLabel"); // NOI18N

        connectButton.setText("Connect");
        connectButton.setName("connectButton"); // NOI18N
        connectButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                connectButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cancelButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(connectButton, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))
                        .addGap(5, 5, 5))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                        .addGap(334, 334, 334)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(connectButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusLabel)
                .addContainerGap(20, Short.MAX_VALUE))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    /** Reads existing properties file and will create one if doesn't exist.
    *
    */
   private void readPropertiesFile(){
       File file = new File(propertiesFileName);
       System.out.println(file.getAbsolutePath());
       try {
           boolean isNew = file.createNewFile();
           properties = new Properties();
           System.out.println(file.getAbsolutePath());
           if(!isNew){
               FileInputStream fs = new FileInputStream(file);
               properties.load(fs);
               fs.close();
           }else{ //file needs to be created
               properties = createNewPropertiesFile();
           }
           String numString = properties.getProperty("numberOfDatabases");
           int numDb = 0;
           if(numString != null)
               numDb = Integer.parseInt(numString);
           else
        	   properties.setProperty("numberOfDatabases", "0");
           for (int i = 0; i < numDb; i++) {
               DatabaseProperties db = new DatabaseProperties();
               db.host = properties.getProperty("database" + i + ".host");
               db.port = properties.getProperty("database" + i + ".port");
               db.database = properties.getProperty("database" + i + ".database");
               db.userName = properties.getProperty("database" + i + ".userName");
               db.password = properties.getProperty("database" + i + ".password");
               db.connectionName = properties.getProperty("database" + i + ".connectionName");
               if(db.connectionName != null)
                   dbPropertiesMap.put(db.connectionName, db);
           }
       } catch (IOException ex) {
           JOptionPane.showMessageDialog(this, "Database properties file is corrupted. A new file will be created.");
           createNewPropertiesFile();
           Logger.getLogger(DataBaseDialog.class.getName()).log(Level.SEVERE, null, ex);
       }
   }

   private Properties createNewPropertiesFile(){
       Properties properties = new Properties();
       properties.setProperty("numberOfDatabases", Integer.toString(0));
       try {
           FileOutputStream fs = new FileOutputStream(propertiesFileName);
           properties.store(fs, null);
           fs.close();
       } catch (IOException ex) {
           JOptionPane.showMessageDialog(this, "Database Properties file could not be created. System is exiting.");
           Logger.getLogger(DataBaseDialog.class.getName()).log(Level.SEVERE, null, ex);
           System.exit(1);
       }
       return properties;
   }

   /** Creates a new ComboBoxModel from the properties file
    *
    */
   private ComboBoxModel getComboBoxModel(){
       Set<String> keySet = dbPropertiesMap.keySet();
       Vector v = new Vector(keySet.size() + 1);
       v.add("Saved connections");
       v.addAll(keySet);
       saveButton.setEnabled(false);
       return new DefaultComboBoxModel(v);
   }
    private void connectButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_connectButtonMouseClicked
        statusLabel.setText("Testing Connection...");
        final JDialog me = this;
        final MessageDialog message = new MessageDialog(me, false, "Connecting");
        message.setVisible(true);
        me.setEnabled(false);
        SwingWorker worker = new SwingWorker<Boolean, Void>(){
        	@Override
        	public Boolean doInBackground(){
            	String host = hostField.getText();
                String port = portField.getText();
                String db = dbField.getText();
                String user = userField.getText();
                String encryptedPassword = passwordField.getText();
                SessionFactory factory = GenoSetsSessionManager.testConnection(host, port, db, user, encryptedPassword);
                if(factory != null){
                	statusLabel.setText("Connection Successful");
                	GenoSetsSessionManager.setSessionFactory(factory);
                	return true;
                }else{
                	statusLabel.setText("Connection Failed");
                	return false;
                }
        	}
        	@Override
        	public void done(){
        		try {
					Boolean status = get();
					if(status == true){
						message.setText("Loading ParallelSets.");
						SwingWorker w2 = new SwingWorker<Boolean, Void>(){
				        	@Override
				        	public Boolean doInBackground(){
				        		ParallelSets.main(null);
				        		return true;
				        	}
				        	@Override
				        	public void done(){
				        		message.dispose();
				        		me.dispose();
				        	}
						};
						
						w2.execute();
					}else{
						statusLabel.setText("ConnectionFailed");
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        };
        worker.execute();

    }//GEN-LAST:event_connectButtonMouseClicked
    
    private void runParSets(){
        SwingWorker worker = new SwingWorker<Boolean, Void>(){
        	@Override
        	public Boolean doInBackground(){
    			ParallelSets.main(null);
    			return true;
        	}
        };
        worker.execute();
    }

    private void cancelButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseClicked
        System.exit(0);
    }//GEN-LAST:event_cancelButtonMouseClicked

    private void deleteButtonClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseClicked
        System.exit(0);
    }
    private void dbComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbComboBoxActionPerformed
        JComboBox cb = (JComboBox)evt.getSource();
        //Get item selected
        Object item = cb.getSelectedItem();
        //Lookup in properties map
        DatabaseProperties dbProp = dbPropertiesMap.get((String)item);
        if(dbProp == null){ //then nothing is selected
            hostField.setText("");
            portField.setText("");
            dbField.setText("");
            userField.setText("");
            passwordField.setText("");
            passwordSaveBox.setSelected(false);
            connectionNameField.setText("");
            deleteButton.setEnabled(false);
        }else{
            hostField.setText(dbProp.host);
            portField.setText(dbProp.port);
            dbField.setText(dbProp.database);
            userField.setText(dbProp.userName);
            passwordField.setText(dbProp.password);
            connectionNameField.setText(dbProp.connectionName);
            if(dbProp.password == null)
                passwordSaveBox.setSelected(false);
            else
                passwordSaveBox.setSelected(true);
            saveButton.setEnabled(false);
            deleteButton.setEnabled(true);
        }
}//GEN-LAST:event_dbComboBoxActionPerformed

    private void testConnectionButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_testConnectionButtonMouseClicked
    	statusLabel.setText("Testing Connection...");
    	final JDialog me = this;
    	me.setEnabled(false);
    	final MessageDialog dialog = new MessageDialog(this, false, "TestingConnection");
    	dialog.setAlwaysOnTop(true);
    	dialog.setVisible(true);
    	SwingWorker worker = new SwingWorker<Boolean, Void>(){
        	@Override
        	public Boolean doInBackground(){
            	String host = hostField.getText();
                String port = portField.getText();
                String db = dbField.getText();
                String user = userField.getText();
                String encryptedPassword = passwordField.getText();
                if(GenoSetsSessionManager.testConnection(host, port, db, user, encryptedPassword) != null){
                	statusLabel.setText("Connection Successful");
                	return true;
                }else{
                	statusLabel.setText("Connection Failed");
                	return false;
                }  
        	}
        	@Override
        	public void done(){
        		dialog.dispose();
        		me.setEnabled(true);
        		//dialog.setVisible(false);
        	}
        };
        worker.execute();
  
    }//GEN-LAST:event_testConnectionButtonMouseClicked

    private void saveButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveButtonMouseClicked
        //Get all values
    	String host = hostField.getText();
        String port = portField.getText();
        String db = dbField.getText();
        String user = userField.getText();
        String password = passwordField.getText();
        String connectionName = connectionNameField.getText();
        String numString = properties.getProperty("numberOfDatabases");
        int numDb = 0;
        if(numString != null)
            numDb = Integer.parseInt(numString);
        
        //Lookup database name to add or update map
        DatabaseProperties dbItem = dbPropertiesMap.get(connectionName);
        if(dbItem == null){
        	dbItem = new DatabaseProperties();
        	dbItem.id = numDb;
        	numDb++;
        	dbPropertiesMap.put(connectionName, dbItem);
        }
        dbItem.connectionName = connectionName;
        dbItem.database = db;
        dbItem.host = host;
        dbItem.password = password;
        dbItem.port = port;
        dbItem.userName = user;
        
        //Update combo box
        dbComboBox.setModel(getComboBoxModel());
        dbComboBox.setSelectedItem(connectionName);

        //Set properties in properies file
        int index = numDb - 1;
        properties.setProperty("database" + index + ".connectionName", connectionName);
        properties.setProperty("database" + index + ".host", host);
        properties.setProperty("database" + index + ".port", port);
        properties.setProperty("database" + index + ".database", db);
        properties.setProperty("database" + index + ".userName", user);
        properties.setProperty("numberOfDatabases", Integer.toString(numDb));
        if(passwordSaveBox.isSelected())
        	properties.setProperty("database" + index + ".password", password);
        FileOutputStream fs;
		try {
			fs = new FileOutputStream(propertiesFileName);
			properties.store(fs, null);
	        fs.close();
		} catch (IOException e) {
			statusLabel.setText("Error writing to database properties file");
			e.printStackTrace();
		}       
    }//GEN-LAST:event_saveButtonMouseClicked

    private void passwordSaveBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_passwordSaveBoxMouseClicked
        saveButton.setEnabled(true);
    }//GEN-LAST:event_passwordSaveBoxMouseClicked
    
    private void fieldChanged(){
    	statusLabel.setText("");
    	saveButton.setEnabled(true);
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                DataBaseDialog dialog = new DataBaseDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton connectButton;
    private javax.swing.JTextField connectionNameField;
    private javax.swing.JComboBox dbComboBox;
    private javax.swing.JTextField dbField;
    private javax.swing.JButton deleteButton;
    private javax.swing.JTextField hostField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField passwordField;
    private javax.swing.JCheckBox passwordSaveBox;
    private javax.swing.JTextField portField;
    private javax.swing.JButton saveButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JButton testConnectionButton;
    private javax.swing.JTextField userField;
    // End of variables declaration//GEN-END:variables
    
    //--------------------------------------------------------------
    
}
