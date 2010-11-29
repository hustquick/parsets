/*
 * 
 * 
 */

package edu.uncc.parsets.gui;



import java.awt.BorderLayout;

import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.swing.JMenuItem;

import edu.uncc.parsets.ParallelSets;
import edu.uncc.parsets.data.DataSet;
import edu.uncc.parsets.parsets.ParSetsView;
import java.awt.Color;
import javax.swing.JPanel;
/**
 *
 * @author aacain
 */
public class MainPanel extends JPanel implements AbstractMainView{

    private static final int WINDOWHEIGHT = 600;
    private static final int WINDOWWIDTH = 900;
    public static final String WINDOWTITLE = ParallelSets.PROGRAMNAME + " V" + ParallelSets.VERSION;
    private static final String ICONFILE = "/support/parsets-512.gif";
    protected static final String MESSAGE = "Reinitializing the database will delete all datasets.";
    protected static final String TITLE = "Reinitialize DB";
    private Controller controller;
    private JMenuItem openDataSet;
    private JMenuItem editDataSet;
    private JMenuItem deleteDataSet;
    private DataSet dataset;
    private SideBar sideBar;
    private GLCanvas glCanvas;

    public String getTitle() {
        return this.getTitle();
    }

    private static class PNGFileNameFilter extends CombinedFileNameFilter {

        @Override
        public String getDescription() {
            return "PNG Files";
        }

        @Override
        public String getExtension() {
            return ".png";
        }
    }

    public MainPanel() {

    }

    public MainPanel(DataSet dataset) {
        super();
        controller = new Controller();
        this.dataset = dataset;
        controller.setDataSet(dataset);
        sideBar = new SideBar(dataset, (AbstractMainView)this, controller);
        GLCapabilities caps = new GLCapabilities();
        caps.setSampleBuffers(true);
        caps.setNumSamples(2);
        glCanvas = new GLCanvas(caps);
        glCanvas.addGLEventListener(new ParSetsView(glCanvas, (AbstractMainView)this, controller));
        initComponents();
    }
    
    private void initComponents(){
        setLayout(new BorderLayout());
        add(sideBar, BorderLayout.WEST);
        add(glCanvas, BorderLayout.CENTER);

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
