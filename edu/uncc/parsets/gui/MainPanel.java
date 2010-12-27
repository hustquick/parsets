/*
 * 
 * 
 */

package edu.uncc.parsets.gui;



import java.awt.BorderLayout;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

import edu.uncc.parsets.ParallelSets;
import edu.uncc.parsets.data.DataSet;
import edu.uncc.parsets.parsets.ParSetsView;
/**
 *
 * @author aacain
 */
@SuppressWarnings("serial")
public class MainPanel extends JPanel implements AbstractMainView {

    public static final String WINDOWTITLE = ParallelSets.PROGRAMNAME + " V" + ParallelSets.VERSION;
    protected static final String MESSAGE = "Reinitializing the database will delete all datasets.";
    protected static final String TITLE = "Reinitialize DB";
    private Controller controller;
    private JMenuItem openDataSet;
    private JMenuItem editDataSet;
    private JMenuItem deleteDataSet;
    private SideBar sideBar;
	private ParSetsView parSetsView;

    public String getTitle() {
        return "";
    }


    public MainPanel(DataSet dataset) {
        super();
        controller = new Controller();
        controller.setDataSet(dataset);
        sideBar = new SideBar(dataset, (AbstractMainView)this, controller);

        parSetsView = new ParSetsView(controller);

        initComponents();
    }
    
    private void initComponents(){
        setLayout(new BorderLayout());
        add(sideBar, BorderLayout.WEST);
        add(parSetsView, BorderLayout.CENTER);

    }

    @Override
    public void addNotify() {
        super.addNotify();
        initComponents();
    }



    public void setDSMenuItemsEnabled(boolean enabled) {
        openDataSet.setEnabled(enabled);
        editDataSet.setEnabled(enabled);
        deleteDataSet.setEnabled(enabled);
    }

    public Controller getController() {
        return controller;
    }
}
