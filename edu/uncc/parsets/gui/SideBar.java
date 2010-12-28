package edu.uncc.parsets.gui;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.DefaultCheckboxTreeCellRenderer;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingEvent;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingListener;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel.CheckingMode;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.miginfocom.swing.MigLayout;
import edu.uncc.parsets.data.CategoryHandle;
import edu.uncc.parsets.data.DataSet;
import edu.uncc.parsets.data.DataType;
import edu.uncc.parsets.data.DimensionHandle;
import edu.uncc.parsets.util.osabstraction.AbstractOS;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * Copyright (c) 2009, Robert Kosara, Caroline Ziemkiewicz,
 *                     and others (see Authors.txt for full list)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of UNC Charlotte nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *      
 * THIS SOFTWARE IS PROVIDED BY ITS AUTHORS ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
@SuppressWarnings("serial")
public class SideBar extends JPanel implements DataSetListener {

    private static final String DATASETTABTITLE = "Data Set";
    private static int dataSetTabNumber = 2;
    private CheckboxTree dimTree;
    private Controller controller;
    private JTabbedPane tabs;
    private JPanel dataSetTab;
    private DBTab dbTab;
    private OnlineDataTab onlineDataTab;
    private JLabel dsLabel;
	private JComboBox numDimComboBox;

    // from http://www.codeguru.com/java/articles/199.shtml
    private static class VerticalLabelUI extends BasicLabelUI {

        private static final int PADDING = 5;

        VerticalLabelUI() {
            super();
        }

        public Dimension getPreferredSize(JComponent c) {
            Dimension dim = super.getPreferredSize(c);
            return new Dimension(dim.height, dim.width + 2 * PADDING + 2);
        }
        private Rectangle paintIconR = new Rectangle();
        private Rectangle paintTextR = new Rectangle();
        private Rectangle paintViewR = new Rectangle();
        private Insets paintViewInsets = new Insets(0, 0, 0, 0);

        public void paint(Graphics g, JComponent c) {

            JLabel label = (JLabel) c;
            String text = label.getText();
            Icon icon = (label.isEnabled()) ? label.getIcon() : label.getDisabledIcon();

            if ((icon == null) && (text == null)) {
                return;
            }

            FontMetrics fm = g.getFontMetrics();
            paintViewInsets = c.getInsets(paintViewInsets);

            paintViewR.x = paintViewInsets.left;
            paintViewR.y = paintViewInsets.top;

            // Use inverted height & width
            paintViewR.height = c.getWidth()
                    - (paintViewInsets.left + paintViewInsets.right);
            paintViewR.width = c.getHeight()
                    - (paintViewInsets.top + paintViewInsets.bottom);

            paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
            paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;

            String clippedText = layoutCL(label, fm, text, icon, paintViewR,
                    paintIconR, paintTextR);

            Graphics2D g2 = (Graphics2D) g;
            AffineTransform tr = g2.getTransform();
            g2.rotate(-Math.PI / 2);
            g2.translate(-c.getHeight(), 0);

            if (icon != null) {
                icon.paintIcon(c, g, paintIconR.x, paintIconR.y);
            }

            int textX = paintTextR.x + PADDING;
            int textY = paintTextR.y + fm.getAscent();

            if (label.isEnabled()) {
                paintEnabledText(label, g, clippedText, textX, textY);
            } else {
                paintDisabledText(label, g, clippedText, textX, textY);
            }

            g2.setTransform(tr);
        }
    }

    public SideBar(final MainWindow mainWin, Controller mainController) {
        super(new BorderLayout());

        controller = mainController;
        controller.addDataSetListener(this);

        tabs = new JTabbedPane();
        tabs.setTabPlacement(JTabbedPane.LEFT);
        add(tabs, BorderLayout.CENTER);

        onlineDataTab = new OnlineDataTab(mainWin);
        tabs.addTab(OnlineDataTab.TABTITLE, onlineDataTab);
        makeRotatedLabel(tabs, 0, OnlineDataTab.TABTITLE);

        dbTab = new DBTab(mainWin, controller);
        tabs.addTab(DBTab.TABTITLE, dbTab);
        makeRotatedLabel(tabs, 1, DBTab.TABTITLE);

        dataSetTab = makeDataSetTab();

        tabs.addTab(DATASETTABTITLE, dataSetTab);
        dsLabel = makeRotatedLabel(tabs, 2, DATASETTABTITLE);
        if (dsLabel != null) {
            dsLabel.setEnabled(false);
        }
        tabs.setEnabledAt(dataSetTabNumber, false);
        tabs.setSelectedIndex(1);

        tabs.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                mainWin.setDSMenuItemsEnabled(false);
            }
        });
    }

    public SideBar(DataSet dataset, final AbstractMainView mainPanel, Controller mainController) {
        super(new BorderLayout());

        controller = mainController;
        controller.addDataSetListener(this);

        tabs = new JTabbedPane();
        tabs.setTabPlacement(JTabbedPane.LEFT);
        add(tabs, BorderLayout.CENTER);
        dataSetTabNumber = 0;

        dataSetTab = makeDataSetTab();

        tabs.addTab(DATASETTABTITLE, dataSetTab);
        dsLabel = makeRotatedLabel(tabs, 0, DATASETTABTITLE);
        if (dsLabel != null) {
            dsLabel.setEnabled(true);
        }
        //tabs.setEnabledAt(0, false);
        tabs.setSelectedIndex(dataSetTabNumber);


        tabs.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                mainPanel.setDSMenuItemsEnabled(false);
            }
        });

        setDataSet(dataset);
    }

    private JPanel makeDataSetTab() {
        JPanel p = new JPanel(new MigLayout("wrap 1,fillx,insets 0", "[]", "[grow,fill]r[]r"));
        p.setOpaque(false);

        Box b = new Box(BoxLayout.Y_AXIS);
        b.setBorder(BorderFactory.createTitledBorder("Dimensions"));
        b.setOpaque(false);

        b.add(createDimensionsBox());
        p.add(b, "growx");

        JPanel numDimPanel = new JPanel();
        numDimPanel.setBorder(new TitledBorder("Accumulate by"));
        numDimComboBox = new JComboBox();
        numDimComboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (numDimComboBox.getSelectedIndex() == 0)
					controller.setAccumulationDimension(null);
				else
					controller.setAccumulationDimension((DimensionHandle)numDimComboBox.getSelectedItem());
			}
		});
        numDimPanel.add(numDimComboBox);
        
        p.add(numDimPanel, "growx");
        
        JButton clearButton = new JButton("Clear Canvas");
        clearButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                controller.parSetsView.clearScreen();
                dimTree.clearChecking();
            }
        });
        p.add(clearButton, "center");
        return p;
    }

    private JLabel makeRotatedLabel(JTabbedPane tabs, int index, String tabtitle) {
        if (!AbstractOS.getCurrentOS().isMacOSX()) {
            JLabel label = new JLabel(tabtitle);
            label.setUI(new VerticalLabelUI());
            tabs.setTabComponentAt(index, label);
            return label;
        } else {
            return null;
        }
    }

    public void setDataSet(DataSet data) {
        dimTree.setModel(new DefaultTreeModel(data.getCategoricalDimensionsAsTree()));

        Vector<DimensionHandle> dims = new Vector<DimensionHandle>();
        dims.add(new DimensionHandle("Count", "count", DataType.numerical, -1, null));
        for (DimensionHandle dim : data.getNumericDimensions())
        	dims.add(dim);
        
        numDimComboBox.setModel(new DefaultComboBoxModel(dims));
        
        tabs.setEnabledAt(dataSetTabNumber, true);
        String name = data.getName();
        if (name.length() > 10) {
            tabs.setToolTipTextAt(dataSetTabNumber, name);
            name = name.substring(0, 10) + "\u2026";
        } else {
            tabs.setToolTipTextAt(dataSetTabNumber, null);
        }
        tabs.setTitleAt(dataSetTabNumber, name);
        if (dsLabel != null) {
            dsLabel.setText(name);
            dsLabel.setEnabled(true);
        }
        tabs.setSelectedComponent(dataSetTab);
    }

    private JComponent createDimensionsBox() {
//		dimBox.setBorder(BorderFactory.createTitledBorder("Categorical"));
        dimTree = new CheckboxTree(new DefaultMutableTreeNode());
        dimTree.setRootVisible(false);

        // Remove the folder and file icons.
        ((DefaultCheckboxTreeCellRenderer) dimTree.getCellRenderer()).setLeafIcon(null);
        ((DefaultCheckboxTreeCellRenderer) dimTree.getCellRenderer()).setClosedIcon(null);
        ((DefaultCheckboxTreeCellRenderer) dimTree.getCellRenderer()).setOpenIcon(null);

        dimTree.getCheckingModel().setCheckingMode(CheckingMode.PROPAGATE_PRESERVING_UNCHECK);

        dimTree.addTreeCheckingListener(new TreeCheckingListener() {

            public void valueChanged(TreeCheckingEvent event) {

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();

                if (node.isLeaf()) {
                    if (event.isCheckedPath()) {
                        controller.parSetsView.addCategory((DimensionHandle) ((DefaultMutableTreeNode) node.getParent()).getUserObject(),
                                (CategoryHandle) node.getUserObject());
                    } else {
                        controller.parSetsView.removeCategory((DimensionHandle) ((DefaultMutableTreeNode) node.getParent()).getUserObject(),
                                (CategoryHandle) node.getUserObject());
                    }
                } else if (!node.isRoot()) {
                    if (event.isCheckedPath()) {
                        controller.parSetsView.addAxis((DimensionHandle) node.getUserObject());
                    } else {
                        controller.parSetsView.removeAxis((DimensionHandle) node.getUserObject());
                    }
                }
            }
        });

        JScrollPane dimPane = new JScrollPane(dimTree);

        return dimPane;
    }
}
