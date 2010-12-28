package edu.uncc.parsets.parsets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

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
import gnu.jpdf.PDFJob;

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
public class ParSetsView extends JPanel implements DataSetListener,
							AnimationListener, ComponentListener {

	private int width;
	private int height;

	public static final Font DIMENSIONFONT = new Font("Arial", Font.BOLD, 18);
	public static final Font CATEGORYFONT = new Font("Arial", Font.PLAIN, 12);
	
	private FontMetrics dimensionFontMetrics;
	private FontMetrics categoryFontMetrics;
	
	private Controller controller;

	private ArrayList<VisualAxis> axes = new ArrayList<VisualAxis>();
	private VisualConnectionTree connectionTree = new VisualConnectionTree();
	private CategoryTree dataTree;
	
	private ArrayList<DimensionHandle> dimensionList;
	private ArrayList<ArrayList<CategoryHandle>> categoryLists;

	private static final String LOGOFILENAME = "/support/parsets-logo-medium.png";
	private Dimension logoDimensions;
	
	private Tooltip tooltip = new Tooltip(null, 0, 0);
	
	private boolean needsLayout = true;
	
	private boolean showTooltips = true;

	boolean antialias = true;
	private boolean strongerSelection = true;
	private boolean mouseInDisplay = false;
	private BufferedImage logoImage;
	
	public ParSetsView(Controller ctrl) {
		super();
		controller = ctrl;
		controller.addDataSetListener(this);
		controller.parSetsView = this;
		
		setBackground(Color.WHITE);
		
		dimensionList = new ArrayList<DimensionHandle>();
		categoryLists = new ArrayList<ArrayList<CategoryHandle>>();

		ParSetsInteraction interaction = new ParSetsInteraction(this);
		addMouseListener(interaction);
		addMouseMotionListener(interaction);
		addComponentListener(this);
	}
	
	@Override
	public void paint(Graphics g) {

		super.paint(g);
		
		Graphics2D g2 = (Graphics2D) g;

		if (antialias)
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
		else
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF); 			

		if (needsLayout) {
			componentResized(null);
			layoutAxes();
		}
		
		if (dimensionFontMetrics == null) {
			dimensionFontMetrics = g2.getFontMetrics(DIMENSIONFONT);
			categoryFontMetrics = g2.getFontMetrics(CATEGORYFONT);
		}
		
		if (axes.size() == 0) {
			renderSplashScreen(g2);
		} else {
			
			if (strongerSelection && mouseInDisplay)
				connectionTree.display(g2, .5f);
			else
				connectionTree.display(g2, .8f);

			// passing all those values isn't very pretty, but it'll do for now.
			for (VisualAxis axis : axes)
				axis.paint(g2, DIMENSIONFONT, dimensionFontMetrics, CATEGORYFONT, categoryFontMetrics);
			
			if (tooltip != null && showTooltips) 
				tooltip.paint(g2, CATEGORYFONT, categoryFontMetrics, width);
			
		}
		
	}

	private void renderSplashScreen(Graphics2D g) {
		GradientPaint gradient = new GradientPaint(0, 0, new Color(220, 252, 134), 0, height, new Color(247, 213, 134));
		Paint p = g.getPaint();
		g.setPaint(gradient);
		g.fillRect(0, 0, width, height);
		g.setPaint(p);
		
		if (logoImage == null) {
			try {
				logoImage = ImageIO.read(getClass().getResourceAsStream(LOGOFILENAME));
				logoDimensions = new Dimension(logoImage.getWidth(), logoImage.getHeight());
			} catch (IOException e) {
				PSLogging.logger.warn("Could not open logo image.", e);
			} catch (IllegalArgumentException e) {
				PSLogging.logger.warn("Could not open logo image.", e);
			}
		}
		
		g.drawImage(logoImage, null, Math.max(0, (width-logoDimensions.width)/2), Math.max(0,(height-logoDimensions.height)/2));
	}

	protected void layoutAxes() {
		float y = 10;
		float axisSpacing = 0;
		if (axes.size() == 2)
			axisSpacing = ((float)(height - 52) / 2);
		else if (axes.size() > 2)
			axisSpacing = ((float)(height - 52) / (float)(axes.size()-1));
		
		for (VisualAxis axis : axes) {
			axis.layout((int)y, 10, width-20-40, 40, categoryFontMetrics.getHeight() + 2, dataTree);
			y += axisSpacing;
		}
		connectionTree.doLayout(10, width-20-30);
		needsLayout = false;
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
			
			dataTree = dimension.getDataSet().getTree(dimensionList, controller.getAccumulationDimension());
			updateVisibility(dataTree.getRootNode(), -1);
			connectionTree.buildConnectionTree(axes, dataTree);			
			dataTree.getRootNode().updateValues();
		}		
		needsLayout = true;
		repaint();

	}

	public void setRibbonStyle(RibbonLayoutStyle ribbonStyle) {
		connectionTree.setStyle(ribbonStyle);
		needsLayout = true;
		repaint();
	}

	public void clearScreen() {
		axes = new ArrayList<VisualAxis>();
		dimensionList = new ArrayList<DimensionHandle>();
		categoryLists = new ArrayList<ArrayList<CategoryHandle>>();
		connectionTree.clearConnections();
		dataTree = null;
		needsLayout = true;
		repaint();
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
			dataTree = dimension.getDataSet().getTree(dimensionList, null);
			updateVisibility(dataTree.getRootNode(), -1);
			connectionTree.buildConnectionTree(axes, dataTree);
			dataTree.getRootNode().updateValues();
		}
		
		needsLayout = true;
		repaint();
	}
	
	// TODO: This needs to be rewritten and combined with moveAxis
	public int getAxisPosition(int yPos, VisualAxis axis) {

		int newIndex = yPos / (height / (axes.size()-1));
		int oldIndex = dimensionList.indexOf(axis.getDimension());

		// up movements create unnecessary reconfigurations.
		// this is to catch this special case where the movement has not
		// been far enough
		if (newIndex < oldIndex) {
			if (axes.get(newIndex).getBarY() < yPos)
				newIndex++;
		}
		
		if (newIndex == oldIndex) 
			return -1;
		
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
		dataTree = axis.getDimension().getDataSet().getTree(dimensionList, null);
		updateVisibility(dataTree.getRootNode(), -1);
		connectionTree.buildConnectionTree(axes, dataTree);
		dataTree.getRootNode().updateValues();
		
		needsLayout = true;
		repaint();

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
		repaint();		
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
		repaint();		
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
		
	public void takePNGScreenShot(String filename) {
		BufferedImage screenshot = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = screenshot.getGraphics();
		paint(g);
		try {
			ImageIO.write(screenshot, "png", new File(filename));
		} catch (Exception e) {
			PSLogging.logger.error("Error taking screenshot", e);
		}
	}

	public void takePDFScreenShot(String filename) {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(new File(filename));
			PDFJob job = new PDFJob(fileOutputStream, "Parallel Sets");
			Graphics2D pdfGraphics = (Graphics2D)job.getGraphics();
			Dimension d = job.getPageDimension();
			float pageRatio = d.height/(float)d.width;
			float windowRatio = height/(float)width;
			float scale = 1;
			if (pageRatio > windowRatio)
				scale = d.width/(float)width;
			else
				scale = d.height/(float)height;
			pdfGraphics.scale(scale, scale);
		    paint(pdfGraphics);
			pdfGraphics.dispose();
			job.end();
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			PSLogging.logger.error("Error exporting PDF", e);
		} catch (IOException e) {
			PSLogging.logger.error("Error exporting PDF", e);
		}
	}
		
	public List<VisualAxis> getAxes() {
		return axes;
	}
	
	public void animationEnded() {
	}

	public void animationStep(int segment, float factor) {
		repaint();
	}

	public void segmentEnded(int segment) {
	}

	public VisualConnectionTree getConnectionTree() {
		return connectionTree;
	}
	
	public void setTooltip(String text, int x, int y) {
		tooltip.newValues(text, x, y);
	}

	public void clearTooltip() {
		tooltip.newValues(null, 0, 0);	
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

	public void setMouseInDisplay(boolean b) {
		mouseInDisplay = b;
	}
        
	public Controller getController() {
		return this.controller;
	}

	@Override
	public void componentResized(ComponentEvent e) {
		width = getWidth();
		height = getHeight();
		needsLayout = true;
		repaint();
	}

	@Override
	public void componentHidden(ComponentEvent e) {	}

	@Override
	public void componentMoved(ComponentEvent e) {	}

	@Override
	public void componentShown(ComponentEvent e) {	}

}
