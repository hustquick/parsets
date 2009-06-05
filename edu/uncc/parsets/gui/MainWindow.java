package edu.uncc.parsets.gui;

import java.awt.BorderLayout;

import javax.media.opengl.GLCanvas;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

import edu.uncc.parsets.ParallelSets;
import edu.uncc.parsets.parsets.ParSetsView;
import edu.uncc.parsets.util.PSLogging;

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

	public MainWindow() {
		controller = new Controller();

		JFrame f = new JFrame(WINDOWTITLE);
		f.setSize(WINDOWWIDTH, WINDOWHEIGHT);		
		f.setIconImage(new ImageIcon(ICONFILE).getImage());
		//f.setLayout(new MigLayout("insets 0,fill", "[min!]0[grow,fill]", "[grow,fill]"));
		f.setLayout(new BorderLayout());
		
		PSLogging.init(f, PSLogging.DEFAULTLOGLEVEL);
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				PSLogging.logger.fatal("Uncaught exception, program terminating.", e);
			}
		});
		
		SideBar sideBar = new SideBar(f, controller);
		f.add(sideBar, BorderLayout.WEST);

		GLCanvas glCanvas = new GLCanvas();
		glCanvas.addGLEventListener(new ParSetsView(glCanvas, controller));
		f.add(glCanvas, BorderLayout.CENTER);
		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
