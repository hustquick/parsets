package edu.uncc.parsets.parsets;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.Screenshot;
import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureIO;

import edu.uncc.parsets.ParallelSets;
import edu.uncc.parsets.data.CategoryHandle;
import edu.uncc.parsets.data.CategoryNode;
import edu.uncc.parsets.data.CategoryTree;
import edu.uncc.parsets.data.DataSet;
import edu.uncc.parsets.data.DataType;
import edu.uncc.parsets.data.DimensionHandle;
import edu.uncc.parsets.gui.Controller;
import edu.uncc.parsets.gui.DataSetListener;
import edu.uncc.parsets.util.AnimationListener;
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

public class ParSetsView implements GLEventListener, DataSetListener,
									AnimationListener, MouseListener {

	private static final GLU glu = new GLU();
	private int width;
	private int height;
	private Component canvas;

	public static final Font dimensionFont = new Font("Sans-Serif", Font.BOLD, 18);
	public static final Font categoryFont = new Font("Sans-Serif", Font.PLAIN, 12);
	
	private TextRenderer dimensionTextRenderer = new TextRenderer(dimensionFont, true, false);
	private TextRenderer categoryTextRenderer = new TextRenderer(categoryFont, true, false);

	private FontMetrics dimensionFontMetrics;
	private FontMetrics categoryFontMetrics;
	
	private Controller controller;

	private ArrayList<VisualAxis> axes = new ArrayList<VisualAxis>();
	private VisualConnectionTree connectionTree = new VisualConnectionTree();
	private CategoryTree dataTree;
	
	private ArrayList<DimensionHandle> dimensionList;
	private ArrayList<ArrayList<CategoryHandle>> categoryLists;

	private static final String LOGOFILENAME = "/support/parsets-logo-medium.png";
	private Texture logoTexture;
	private Dimension logoDimensions;
	
	private Tooltip tooltip = null;
	
	private boolean needsLayout = true;
	
	private String screenShotFileName = null;
	private boolean showTooltips = true;

	boolean antialias = true;
	private boolean strongerSelection = true;
	private boolean mouseInDisplay = false;
	
	public ParSetsView(Component canv, Controller ctrl) {
		canvas = canv;
		controller = ctrl;
		controller.addDataSetListener(this);
		controller.parSetsView = this;
		ParSetsInteraction interaction = new ParSetsInteraction(this);
		canvas.addMouseListener(interaction);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(interaction);
		dimensionList = new ArrayList<DimensionHandle>();
		categoryLists = new ArrayList<ArrayList<CategoryHandle>>();
	}
	
	public final void display(GLAutoDrawable glDrawable) {

		GL gl = null;
		if (ParallelSets.isInstalled())
			gl = glDrawable.getGL();
		else
			gl = new DebugGL(glDrawable.getGL());

		if (antialias) {
			gl.glEnable(GL.GL_LINE_SMOOTH);
			gl.glEnable(GL.GL_POLYGON_SMOOTH);
//			gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
//			gl.glHint(GL.GL_POLYGON_SMOOTH_HINT, GL.GL_NICEST);
			gl.glEnable(GL.GL_MULTISAMPLE);
		} else {
			gl.glDisable(GL.GL_LINE_SMOOTH);
			gl.glDisable(GL.GL_POLYGON_SMOOTH);
//			gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_FASTEST);
//			gl.glHint(GL.GL_POLYGON_SMOOTH_HINT, GL.GL_FASTEST);
			gl.glDisable(GL.GL_MULTISAMPLE);
		}
		
		gl.glClearColor(1, 1, 1, 0);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		if (needsLayout)
			layout();
		
		if (axes.size() == 0) {
			renderSplashScreen(gl);
		} else {
			logoTexture = null;
			
			gl.glEnable(GL.GL_BLEND);

			if (strongerSelection && mouseInDisplay)
				connectionTree.display(gl, .5f);
			else
				connectionTree.display(gl, .8f);

			// passing all those values isn't very pretty, but it'll do for now.
			for (VisualAxis axis : axes)
				axis.display(gl, dimensionTextRenderer, dimensionFontMetrics, categoryTextRenderer, categoryFontMetrics);
			
			if (tooltip != null && showTooltips) 
				tooltip.display(gl, categoryTextRenderer, categoryFontMetrics);
			
			gl.glDisable(GL.GL_BLEND);
		}
		
		if (screenShotFileName != null) {
			try {
				Screenshot.writeToFile(new File(screenShotFileName), width, height);
			} catch (Exception e) {
				PSLogging.logger.error("Error taking screenshot", e);
			}
			screenShotFileName = null;
		}
	}

	private void renderSplashScreen(GL gl) {
		gl.glBegin(GL.GL_QUADS);
			gl.glColor3f(220/255f, 252/255f, 134/255f);
			gl.glVertex2i(width, 0);
			gl.glVertex2i(0, 0);
			gl.glColor3f(247/255f, 213/255f, 134/255f);
			gl.glVertex2i(0, height);
			gl.glVertex2i(width, height);
		gl.glEnd();
		if (logoTexture == null) {
			try {
				BufferedImage logoImage = ImageIO.read(getClass().getResourceAsStream(LOGOFILENAME));
				logoDimensions = new Dimension(logoImage.getWidth(), logoImage.getHeight());
				logoTexture = TextureIO.newTexture(logoImage, false);
				logoTexture.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
				logoTexture.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
				gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
			} catch (IOException e) {
				PSLogging.logger.warn("Could not open logo image.", e);
			} catch (IllegalArgumentException e) {
				PSLogging.logger.warn("Could not open logo image.", e);
			}
		}
		// another if to check if we were able to load the texture
		if (logoTexture != null) {
			logoTexture.enable();
			logoTexture.bind();
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			TextureCoords coords = logoTexture.getImageTexCoords();
			int leftX = Math.max(0, (width-logoDimensions.width)/2);
			int bottomY = Math.max(0,(height-logoDimensions.height)/2);
			gl.glBegin(GL.GL_QUADS);
				gl.glTexCoord2f(coords.left(), coords.bottom());
				gl.glVertex2i(leftX, bottomY);
				gl.glTexCoord2f(coords.right(), coords.bottom());
				gl.glVertex2i(leftX+logoDimensions.width, bottomY);
				gl.glTexCoord2f(coords.right(), coords.top());
				gl.glVertex2i(leftX+logoDimensions.width, bottomY+logoDimensions.height);
				gl.glTexCoord2f(coords.left(), coords.top());
				gl.glVertex2i(leftX, bottomY+logoDimensions.height);
			gl.glEnd();
			gl.glDisable(GL.GL_BLEND);
			logoTexture.disable();
		}
	}

	protected void layout() {
		float y = canvas.getHeight()-10;
		float axisSpacing;
		if (axes.size() < 2)
			axisSpacing = 0;
		else if (axes.size() == 2)
			axisSpacing = ((float)(y - 42) / 2);
		else
			axisSpacing = ((float)(y - 42) / (float)(axes.size()-1));
		
		for (VisualAxis axis : axes) {
			axis.layout((int)y, 10, canvas.getWidth()-20-40, 40, categoryFontMetrics.getHeight() + 2, dataTree);
			y -= axisSpacing;
		}
		connectionTree.doLayout(10, canvas.getWidth()-20-30);
		needsLayout = false;
	}
	
	public final void displayChanged(GLAutoDrawable glDrawable, boolean modeChanged, boolean deviceChanged) {
	}

	public final void init(GLAutoDrawable glDrawable) {
		if (dimensionFontMetrics == null) {
			dimensionFontMetrics = canvas.getGraphics().getFontMetrics(dimensionFont);
			categoryFontMetrics = canvas.getGraphics().getFontMetrics(categoryFont);
		}
		
		GL gl = glDrawable.getGL();
	    gl.glDisable(GL.GL_DEPTH_TEST);
	}

	public final void reshape(GLAutoDrawable glDrawable, int x, int y, int width, int height) {
		this.width = width;
		this.height = height;
		GL gl = glDrawable.getGL();
	    gl.glMatrixMode(GL.GL_PROJECTION);
	    gl.glLoadIdentity();
	    glu.gluOrtho2D(0, width, 0, height);
	    gl.glMatrixMode(GL.GL_MODELVIEW);
	    gl.glLoadIdentity();
	    needsLayout = true;
	}

	public void setDataSet(DataSet data) {
		clearScreen();
	}
	
	public void addAxis(DimensionHandle dimension) {

		if (dimension.getDataType() == DataType.categorical) {
			axes.add(new CategoricalAxis(dimension));
			if (axes.size() == 1)
				axes.get(0).setTopLevel(true);
			
			dimensionList.add(dimension);
			
			ArrayList<CategoryHandle> catList = new ArrayList<CategoryHandle>();
			
			for (CategoryHandle cat : dimension.getCategories()) {
				catList.add(cat);
			}
			
			categoryLists.add(catList);
			
			dataTree = dimension.getDataSet().getTree(dimensionList);
			updateVisibility(dataTree.getRootNode(), -1);
			connectionTree.buildConnectionTree(axes, dataTree);			
			dataTree.getRootNode().updateValues();
		}		
		needsLayout = true;
		canvas.repaint();

	}

	public void setRibbonStyle(RibbonLayoutStyle ribbonStyle) {
		connectionTree.setStyle(ribbonStyle);
		needsLayout = true;
		canvas.repaint();
	}

	public void clearScreen() {
		axes = new ArrayList<VisualAxis>();
		dimensionList = new ArrayList<DimensionHandle>();
		categoryLists = new ArrayList<ArrayList<CategoryHandle>>();
		connectionTree.clearConnections();
		dataTree = null;
		needsLayout = true;
		canvas.repaint();
	}

	public void removeAxis(DimensionHandle dimension) {
		
		VisualAxis removeAxis = null;
		for (VisualAxis axis : axes) {
			if (axis.getDimension() == dimension)  {
				removeAxis = axis;
			}
		}
		axes.remove(removeAxis);
		
		int i = dimensionList.indexOf(dimension);	
		categoryLists.remove(i);
		dimensionList.remove(dimension);
		
		if (axes.size() > 0) {
			axes.get(0).setTopLevel(true);
			dataTree = dimension.getDataSet().getTree(dimensionList);
			updateVisibility(dataTree.getRootNode(), -1);
			connectionTree.buildConnectionTree(axes, dataTree);
			dataTree.getRootNode().updateValues();
		}
		
		needsLayout = true;
		canvas.repaint();
	}
	

	public int getAxisPosition(int yPos, VisualAxis axis) {

		if (yPos < ((CategoricalAxis)axis).getBarHeight()*2) 
			return 0;
		
		int newIndex = (yPos ) / (getHeight() / (axes.size()-1));
		int oldIndex = dimensionList.indexOf(axis.getDimension());
		
		if (newIndex == oldIndex) 
			return -1;
		
		if (newIndex < oldIndex) 
			newIndex++;
		
		if (newIndex >= axes.size())
			return axes.size() - 1;
		
		return newIndex;
	}
	
	public void moveAxis(int newIndex, VisualAxis axis) {

		int oldIndex = axes.indexOf(axis);
		
		axes.get(0).setTopLevel(false);
		
		axes.remove(axis);		
		axes.add(newIndex, axis);

		ArrayList<CategoryHandle> catList = categoryLists.get(oldIndex);
		categoryLists.remove(oldIndex);
		categoryLists.add(newIndex, catList);

		dimensionList.remove(axis.getDimension());
		dimensionList.add(newIndex, axis.getDimension());
		
		axes.get(0).setTopLevel(true);
		dataTree = axis.getDimension().getDataSet().getTree(dimensionList);
		updateVisibility(dataTree.getRootNode(), -1);
		connectionTree.buildConnectionTree(axes, dataTree);
		dataTree.getRootNode().updateValues();
		
		needsLayout = true;
		canvas.repaint();

	}

	public void removeCategory(DimensionHandle dimension,
			CategoryHandle category) {
		
		for (VisualAxis axis : axes) {
			if (axis.getDimension() == dimension)  {
				((CategoricalAxis)axis).removeBar(category);
			}
		}

		int i = dimensionList.indexOf(dimension);
		categoryLists.get(i).remove(category);
		
		if (categoryLists.get(i).isEmpty()) {
			removeAxis(dimension);
		}
		
		else {
			updateVisibility(dataTree.getRootNode(), -1);
			dataTree.getRootNode().updateValues();
		}
		
		needsLayout = true;
		canvas.repaint();		
	}

	
	public void addCategory(DimensionHandle dimension,
			CategoryHandle category) {
		
		if (dimensionList.contains(dimension)) {
		
			for (VisualAxis axis : axes) {
				if (axis.getDimension() == dimension)  {
					((CategoricalAxis)axis).addBar(category);
				}
			}

			int i = dimensionList.indexOf(dimension);
			categoryLists.get(i).add(category);

			updateVisibility(dataTree.getRootNode(), -1);			
			dataTree.getRootNode().updateValues();	

		}
		
		else {
			
			addAxis(dimension);
			
			for (CategoryHandle cat : dimension.getCategories()) {
				if (!cat.equals(category)) 
					removeCategory(dimension, cat);
			}
			
		}
			
		needsLayout = true;
		canvas.repaint();		
	}
	
	public void moveCategory(CategoricalAxis axis, CategoryHandle category, int index) {
		connectionTree.moveCategory(axis.getDimension(), category, index);
	}
	
	private void updateVisibility(CategoryNode dataNode, int level) {

		if (level > -1) 
			dataNode.setVisible(categoryLists.get(level).contains(dataNode.getToCategory()) && dataNode.getParent().isVisible());
		
		for (CategoryNode child : dataNode.getChildren()) {			
			updateVisibility(child, level+1);
		}				
		
	}

	public void setNeedsLayout() {
		needsLayout = true;
	}
	
	public void repaint() {
		canvas.repaint();
	}
	
	public void takeScreenShot(String filename) {
		screenShotFileName = filename;
		repaint();
	}
	
	public int getHeight() {
		return height;
	}
	
	public List<VisualAxis> getAxes() {
		return axes;
	}
	
	public void animationEnded() {
	}

	public void animationStep(int segment, float factor) {
		canvas.repaint();
	}

	public void segmentEnded(int segment) {
	}

	public VisualConnectionTree getConnectionTree() {
		return connectionTree;
	}
	
	public void setTooltip(String text, int x, int y) {
		tooltip = new Tooltip(text, x, y);
	}

	public void clearTooltip() {
		tooltip = null;	
	}

	public void setShowTooltips(boolean show) {
		showTooltips  = show;
		repaint();
	}
	
	public void setAntiAlias(boolean aa) {
		antialias = aa;
		repaint();
	}
	
	public void setStrongerSelection(boolean stronger) {
		strongerSelection = stronger;
		repaint();
	}
	
	public CategoryTree getDataTree() {
		return dataTree;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		mouseInDisplay = true;
		repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		mouseInDisplay = false;
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
		
}
