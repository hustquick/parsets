package edu.uncc.parsets.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.media.opengl.GLCanvas;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import edu.uncc.parsets.ParallelSets;
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

	private Controller controller;

	private JMenuItem openDataSet;

	private JMenuItem editDataSet;

	private JMenuItem deleteDataSet;

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

		GLCanvas glCanvas = new GLCanvas();
		glCanvas.addGLEventListener(new ParSetsView(glCanvas, controller));
		add(glCanvas, BorderLayout.CENTER);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	private JMenuBar makeMenu(final Controller controller) {
		JMenuBar menuBar = new JMenuBar();
		
		JMenu database = new JMenu("Database");
		JMenuItem importcsv = new JMenuItem("Import CSV File");
		importcsv.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		database.add(importcsv);

		JMenuItem reinit = new JMenuItem("Reinitialize");
		database.add(reinit);
		
		database.addSeparator();

		JMenuItem savepng = new JMenuItem("Export PNG");
		savepng.setAccelerator(KeyStroke.getKeyStroke('P', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.ALT_MASK));
		database.add(savepng);

		if (!AbstractOS.getCurrentOS().isMacOSX()) {
			database.addSeparator();
			JMenuItem quit = new JMenuItem("Quit");
			quit.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			quit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();
				}
			});
			database.add(quit);
		}
		
		menuBar.add(database);
		
		JMenu dataset = new JMenu("Data Set");
		openDataSet = new JMenuItem("Open");
		openDataSet.setEnabled(false);
		dataset.add(openDataSet);
		
		editDataSet = new JMenuItem("Edit");
		editDataSet.setEnabled(false);
		dataset.add(editDataSet);
		
		deleteDataSet = new JMenuItem("Delete");
		deleteDataSet.setEnabled(false);
		deleteDataSet.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		dataset.add(deleteDataSet);

		dataset.addSeparator();
		
		JMenuItem closeDataSet = new JMenuItem("Close");
		closeDataSet.setEnabled(false);
		closeDataSet.setAccelerator(KeyStroke.getKeyStroke('W', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		dataset.add(closeDataSet);
		
		menuBar.add(dataset);
		
		JMenu view = new JMenu("View");
		final JMenuItem tooltips = new JCheckBoxMenuItem("Show tooltips", true);
		tooltips.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setShowTooltips(tooltips.isSelected());
			}
		});
		view.add(tooltips);

		JCheckBoxMenuItem strong = new JCheckBoxMenuItem("Stronger highlights", false);
		view.add(strong);
		
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
