package edu.uncc.parsets.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import edu.uncc.parsets.ParallelSets;
import edu.uncc.parsets.data.LocalDB;
import edu.uncc.parsets.parsets.ParSetsView;
import edu.uncc.parsets.util.PSLogging;
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
public class MainWindow extends JFrame {

	private static final int WINDOWHEIGHT = 600;

	private static final int WINDOWWIDTH = 900;

	public static final String WINDOWTITLE = ParallelSets.PROGRAMNAME+" V"+ParallelSets.VERSION;

	private static final String ICONFILE = "/support/parsets-512.gif";

	protected static final String MESSAGE = "Reinitializing the database will delete all datasets.";

	protected static final String TITLE = "Reinitialize DB";

	private Controller controller;

	private JMenuItem openDataSet;

	private JMenuItem editDataSet;

	private JMenuItem deleteDataSet;

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
	
	public MainWindow() {
		super(WINDOWTITLE);
		
		setSize(WINDOWWIDTH, WINDOWHEIGHT);		
		setIconImage(new ImageIcon(ICONFILE).getImage());
		//f.setLayout(new MigLayout("insets 0,fill", "[min!]0[grow,fill]", "[grow,fill]"));
		setLayout(new BorderLayout());
		
		PSLogging.init(this, PSLogging.DEFAULTLOGLEVEL);
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				PSLogging.logger.fatal("Uncaught exception, program terminating.", e);
			}
		});

		controller = new Controller();

		setJMenuBar(makeMenu(controller));

		SideBar sideBar = new SideBar(this, controller);
		add(sideBar, BorderLayout.WEST);

		GLCapabilities caps = new GLCapabilities();
		caps.setSampleBuffers(true);
		caps.setNumSamples(2);
		GLCanvas glCanvas = new GLCanvas(caps);
		glCanvas.addGLEventListener(new ParSetsView(glCanvas, controller));
		add(glCanvas, BorderLayout.CENTER);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	private JMenuBar makeMenu(final Controller controller) {
		JMenuBar menuBar = new JMenuBar();
		
		JMenu dataset = new JMenu("Data Set");
		openDataSet = new JMenuItem("Open");
		openDataSet.setEnabled(false);
		openDataSet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.dbTab.openSelectedDataSet();
			}
		});
		dataset.add(openDataSet);
		
		JMenuItem closeDataSet = new JMenuItem("Close");
		closeDataSet.setEnabled(false);
		closeDataSet.setAccelerator(KeyStroke.getKeyStroke('W', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		dataset.add(closeDataSet);

		editDataSet = new JMenuItem("Edit");
		editDataSet.setEnabled(false);
		dataset.add(editDataSet);
		
		deleteDataSet = new JMenuItem("Delete");
		deleteDataSet.setEnabled(false);
		deleteDataSet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.dbTab.deleteSelectedDataSet();
			}
		});
		deleteDataSet.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		dataset.add(deleteDataSet);
		
		dataset.addSeparator();

		JMenuItem importcsv = new JMenuItem("Import CSV File");
		importcsv.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		importcsv.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new DataWizard();
			}
		});
		dataset.add(importcsv);

		JMenuItem reinit = new JMenuItem("Reinitialize DB");
		reinit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int choice = JOptionPane.showConfirmDialog(MainWindow.this, MESSAGE,
						TITLE, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
				if (choice == 0) {
					LocalDB.getDefaultDB().initializeDB();
				}
			}
		});
		dataset.add(reinit);
		
		dataset.addSeparator();

		JMenuItem savepng = new JMenuItem("Export PNG");
		savepng.setAccelerator(KeyStroke.getKeyStroke('P', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.ALT_MASK));
		savepng.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String fileName = AbstractOS.getCurrentOS().showDialog(MainWindow.this, new PNGFileNameFilter(), FileDialog.SAVE);
				if (fileName != null)
					controller.parSetsView.takeScreenShot(fileName);
			}
		});
		dataset.add(savepng);

		if (!AbstractOS.getCurrentOS().isMacOSX()) {
			dataset.addSeparator();
			JMenuItem quit = new JMenuItem("Quit");
			quit.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			quit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();
				}
			});
			dataset.add(quit);
		}
		
		menuBar.add(dataset);
				
		JMenu view = new JMenu("View");
		final JMenuItem tooltips = new JCheckBoxMenuItem("Show Tooltips", true);
		tooltips.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.parSetsView.setShowTooltips(tooltips.isSelected());
			}
		});
		view.add(tooltips);

		final JCheckBoxMenuItem strong = new JCheckBoxMenuItem("Stronger Highlights", true);
		strong.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.parSetsView.setStrongerSelection(strong.isSelected());
			}
		});
		view.add(strong);
		
		final JCheckBoxMenuItem antialiasing = new JCheckBoxMenuItem("Anti-Aliasing", true);
		antialiasing.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.parSetsView.setAntiAlias(antialiasing.isSelected());
			}
		});
		view.add(antialiasing);
		
		
		menuBar.add(view);
		
		JMenu help = new JMenu("Help");
		if (!AbstractOS.getCurrentOS().isMacOSX()) {
			JMenuItem about = new JMenuItem("About Parallel Sets");
			help.add(about);
		}

		JMenuItem website = new JMenuItem("Visit Website");
		website.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					Desktop.getDesktop().browse(new URI(ParallelSets.WEBSITE));
				} catch (Exception e) {
					PSLogging.logger.error("Could not open website", e);
				}
			}
		});
		help.add(website);
		
		menuBar.add(help);
		
		return menuBar;
	}
	
	public void setDSMenuItemsEnabled(boolean enabled) {
		openDataSet.setEnabled(enabled);
		editDataSet.setEnabled(enabled);
		deleteDataSet.setEnabled(enabled);
	}
}
