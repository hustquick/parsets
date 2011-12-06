package edu.uncc.parsets.parsets;

import java.awt.Graphics2D;
import java.awt.Polygon;

import edu.uncc.parsets.data.CategoryNode;
import edu.uncc.parsets.util.AnimatableProperty;
import edu.uncc.parsets.util.ColorBrewer;

import java.awt.geom.CubicCurve2D.Float;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.GeneralPath;



public class CurvedRibbon extends VisualConnection implements Comparable<BasicRibbon>{
		
	private CategoryBar upperBar, lowerBar;
	private AnimatableProperty upperOffset = new AnimatableProperty();
	private AnimatableProperty lowerOffset = new AnimatableProperty();
	private GeneralPath bounds;
	
	
	public CurvedRibbon(VisualConnection parent, CategoryNode categoryNode, CategoricalAxis upperAxis, CategoricalAxis lowerAxis) {
		super(parent);
		node = categoryNode;

	//	upperOffset.setDontAnimate(true);
	//	lowerOffset.setDontAnimate(true);
		
		this.upperBar = upperAxis.getCategoryBar(node.getParent().getToCategory());
		this.lowerBar = lowerAxis.getCategoryBar(node.getToCategory());
		
		lowerWidth.setValue(0);
	}
	
	public CurvedRibbon(CategoryNode categoryNode, CategoricalAxis upperAxis, CategoricalAxis lowerAxis) {
		
		node = categoryNode;

	//	upperOffset.setDontAnimate(true);
	//	lowerOffset.setDontAnimate(true);

		this.upperBar = upperAxis.getCategoryBar(node.getParent().getToCategory());
		this.lowerBar = lowerAxis.getCategoryBar(node.getToCategory());
		
		lowerWidth.setValue(0);
		
	}

	public int compareTo(BasicRibbon o) {
		return node.compareTo(o.node);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof BasicRibbon)
			return this.getNode().equals(((BasicRibbon) o).getNode());
		else
			return false;
	}

	@Override
	public int hashCode() {
		assert false : "hashCode not designed";
		return 42; // any arbitrary constant will do
	}
	
	public String toString() {
		return node.toString();
	}
	
	public String getTooltip(int filteredTotal) { 
		return node.getTooltipText(filteredTotal);
	}
	
	public void layout(float parentWidth) {

		if (node == null || node.getCount() == 0) {
			width.setValue(0);
		} else {
			if(currentState == BarState.NORMAL){
				width.setValue(parentWidth * node.getRatio());
				upperOffset.setValue(upperBar.getBottomIndexPoint());
				upperBar.setBottomIndexPoint(upperOffset.getFutureValue() + width.getFutureValue());

				lowerOffset.setValue(lowerBar.getTopIndexPoint());
				lowerBar.setTopIndexPoint(lowerOffset.getFutureValue() + width.getFutureValue());
				lowerWidth.setValue(parentWidth* node.getRatio());
			}
			else if(currentState == BarState.OTHER){
				if(parent.getFutureLowerWidth() != 0){
					width.setValue(parent.getFutureLowerWidth() * node.getRatio());
				}
				else{
					width.setValue(upperBar.getFutureWidth() * node.getRatio());
				}
				upperOffset.setValue(upperBar.getBottomIndexPoint());
				upperBar.setBottomIndexPoint(upperOffset.getFutureValue() + width.getFutureValue());

				lowerOffset.setValue(lowerBar.getTopIndexPoint());
				lowerBar.setTopIndexPoint(lowerOffset.getFutureValue() + ((float)lowerBar.getFutureWidth()*((float)node.getCount()/(float)lowerBar.getFrequency())));
				
				lowerWidth.setValue((float)lowerBar.getFutureWidth()*((float)node.getCount()/(float)lowerBar.getFrequency()));
			}
			

		}
		
		
	}
	
	public void paint(Graphics2D g, float alpha){
		
		if (width.getValue() == 0 || isSelected)
			return;

		ColorBrewer.setColor(colorBrewerIndex, false, alpha, g);
		
		if (width.getValue() >= 1){
			int xPoints[] = new int[4];
			int yPoints[] = new int[4];
			
			if(currentState == BarState.NORMAL){
				xPoints[0] = upperBar.getLeftX() + (int)upperOffset.getValue(); 
				yPoints[0] = upperBar.getOutRibbonY();
				xPoints[1] = upperBar.getLeftX() + (int)upperOffset.getValue() + (int)Math.round(width.getValue()); 
				yPoints[1] = upperBar.getOutRibbonY();
				xPoints[2] = lowerBar.getLeftX() + (int)lowerOffset.getValue() + (int)Math.round(lowerWidth.getValue()); 
				yPoints[2] = lowerBar.getInRibbonY();
				xPoints[3] = lowerBar.getLeftX() + (int)lowerOffset.getValue(); 
				yPoints[3] = lowerBar.getInRibbonY();
			}
			else if(currentState == BarState.OTHER){
				xPoints[0] = upperBar.getLeftX() + (int)upperOffset.getValue(); 
				yPoints[0] = upperBar.getOutRibbonY();
				xPoints[1] = upperBar.getLeftX() + (int)upperOffset.getValue() + (int)Math.round(width.getValue()); 
				yPoints[1] = upperBar.getOutRibbonY();
				xPoints[2] = lowerBar.getLeftX() + (int)lowerOffset.getValue() + (int)Math.round((float)lowerBar.getWidth()*((float)node.getCount()/(float)lowerBar.getFrequency()));
				yPoints[2] = lowerBar.getInRibbonY();
				xPoints[3] = lowerBar.getLeftX() + (int)lowerOffset.getValue();
				yPoints[3] = lowerBar.getInRibbonY();
			}
			
			
			Line2D.Float top = new Line2D.Float(xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
			CubicCurve2D.Float right = new CubicCurve2D.Float(xPoints[1], yPoints[1], xPoints[1], ((yPoints[1]+yPoints[2])/2), xPoints[2], ((yPoints[1]+yPoints[2])/2), xPoints[2], yPoints[2]);
			Line2D.Float bottom = new Line2D.Float(xPoints[2], yPoints[2], xPoints[3], yPoints[3]);
			CubicCurve2D.Float left = new CubicCurve2D.Float(xPoints[3], yPoints[3], xPoints[3], ((yPoints[3]+yPoints[0])/2), xPoints[0], ((yPoints[3]+yPoints[0])/2), xPoints[0], yPoints[0]);
			GeneralPath ribb = new GeneralPath();
			ribb.append(top,true);
			ribb.append(right, true);
			ribb.append(bottom, true);
			ribb.append(left, true);
			bounds = ribb;
			g.draw(ribb);
			g.fill(ribb);
		}
		else{
			CubicCurve2D.Float lin = new CubicCurve2D.Float(upperBar.getLeftX() + (int)upperOffset.getValue(), upperBar.getOutRibbonY(), upperBar.getLeftX()+(int)upperOffset.getValue(), ((upperBar.getOutRibbonY()+lowerBar.getInRibbonY())/2),
					lowerBar.getLeftX() + (int)lowerOffset.getValue() + (int)width.getValue(), ((upperBar.getOutRibbonY()+lowerBar.getInRibbonY())/2) , lowerBar.getLeftX() + (int)lowerOffset.getValue() + (int)width.getValue(), lowerBar.getInRibbonY());
			g.draw(lin);
		}
		
		
		
		
	}
	
	public void paintSelected(Graphics2D g) {
		
		if (width.getValue() == 0)
			return;
	
		if (width.getValue() >= 1) {
			ColorBrewer.setColor(colorBrewerIndex, false, g);
			int xPoints[] = new int[4];
			int yPoints[] = new int[4];
			
			if(currentState == BarState.NORMAL){
				xPoints[0] = upperBar.getLeftX() + (int)upperOffset.getValue(); 
				yPoints[0] = upperBar.getOutRibbonY();
				xPoints[1] = upperBar.getLeftX() + (int)upperOffset.getValue() + (int)Math.round(width.getValue());
				yPoints[1] = upperBar.getOutRibbonY();
				xPoints[2] = lowerBar.getLeftX() + (int)lowerOffset.getValue() + (int)Math.round(width.getValue());
				yPoints[2] = lowerBar.getInRibbonY();
				xPoints[3] = lowerBar.getLeftX() + (int)lowerOffset.getValue();
				yPoints[3] = lowerBar.getInRibbonY();
			}
			else if(currentState == BarState.OTHER){
				xPoints[0] = upperBar.getLeftX() + (int)upperOffset.getValue(); 
				yPoints[0] = upperBar.getOutRibbonY();
				xPoints[1] = upperBar.getLeftX() + (int)upperOffset.getValue() + (int)Math.round(width.getValue()); 
				yPoints[1] = upperBar.getOutRibbonY();
				xPoints[2] = lowerBar.getLeftX() + (int)lowerOffset.getValue() + (int)Math.round((float)lowerBar.getWidth()*((float)node.getCount()/(float)lowerBar.getFrequency()));
				yPoints[2] = lowerBar.getInRibbonY();
				xPoints[3] = lowerBar.getLeftX() + (int)lowerOffset.getValue();
				yPoints[3] = lowerBar.getInRibbonY();
			}
			
			
			Line2D.Float top = new Line2D.Float(xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
			CubicCurve2D.Float right = new CubicCurve2D.Float(xPoints[1], yPoints[1], xPoints[1], ((yPoints[1]+yPoints[2])/2), xPoints[2], ((yPoints[1]+yPoints[2])/2), xPoints[2], yPoints[2]);
			Line2D.Float bottom = new Line2D.Float(xPoints[2], yPoints[2], xPoints[3], yPoints[3]);
			CubicCurve2D.Float left = new CubicCurve2D.Float(xPoints[3], yPoints[3], xPoints[3], ((yPoints[3]+yPoints[0])/2), xPoints[0], ((yPoints[3]+yPoints[0])/2), xPoints[0], yPoints[0]);
			GeneralPath ribb = new GeneralPath();
			ribb.append(top,true);
			ribb.append(right, true);
			ribb.append(bottom, true);
			ribb.append(left, true);
			bounds = ribb;
				
			ColorBrewer.setColor(colorBrewerIndex, true, .8f, g);
			g.draw(ribb);
			g.fill(ribb);
		} else {
			ColorBrewer.setColor(colorBrewerIndex, true, g);
			CubicCurve2D.Float lin = new CubicCurve2D.Float(upperBar.getLeftX() + (int)upperOffset.getValue(), upperBar.getOutRibbonY(), upperBar.getLeftX()+(int)upperOffset.getValue(), ((upperBar.getOutRibbonY()+lowerBar.getInRibbonY())/2),
					lowerBar.getLeftX() + (int)lowerOffset.getValue() + (int)width.getValue(), ((upperBar.getOutRibbonY()+lowerBar.getInRibbonY())/2) , lowerBar.getLeftX() + (int)lowerOffset.getValue() + (int)width.getValue(), lowerBar.getInRibbonY());
			g.draw(lin);
		}
	}
	
	public boolean contains(int x, int y){
		if(bounds == null)
			return false;
		if(bounds.contains(x,y))
			return true;
		else 
			return false;
	}
	
	public void setColorBrewerIndex(int colorBrewerIndex) {
		this.colorBrewerIndex = colorBrewerIndex;
	}
	
	
	
}
