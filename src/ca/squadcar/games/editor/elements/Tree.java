package ca.squadcar.games.editor.elements;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;

import ca.squadcar.games.editor.Globals;
import ca.squadcar.games.editor.gui.PropertiesPanel;
import ca.squadcar.games.editor.gui.TreePanel;

public class Tree implements IDrawableElement {
	
	public float width;
	public float height;
	public int levels;
	public WorldPoint location;
	
	private transient Rectangle2D.Float boundingBox;
	private transient boolean selected;
	private transient float zoomFactor;
	private transient Rectangle2D.Float trunk;
	private transient Triangle [] triangles;

	public Tree(final float width, final float height, final int levels, final WorldPoint location) {
		
		this.width = width;
		this.height = height;
		this.levels = levels;
		this.location = new WorldPoint(location);
		
		init();
	}

	@Override
	public void init() {
		
		float trunkWidth = 0.3f * width;
		float trunkHeight = 0.15f * height;
		
		trunk = new Rectangle2D.Float(location.x - trunkWidth * 0.5f,
				location.y - trunkHeight,
				trunkWidth,
				trunkHeight);
		
		if(levels > 0) {

			float levelHeight = (height - trunkHeight) / (float)levels;
			float startWidth = width * 0.5f;
			float widthDec = (width - trunkWidth) / (float)levels * 0.65f;
			if(triangles == null || triangles.length != levels) {
			
				triangles = new Triangle[levels];
			}
			for(int ii = 0; ii < triangles.length; ii++) {
				
				float lower = trunkHeight + ii * levelHeight;
				float upper = lower + levelHeight;
				// adjust lower so it overlaps previous level
				if(ii > 0) {
					lower -= 0.25f * levelHeight;
				}
				float halfWidth = startWidth - ii * widthDec;
				triangles[ii] = new Triangle(new WorldPoint(location.x + halfWidth, location.y - lower),
						new WorldPoint(location.x - halfWidth, location.y - lower),
						new WorldPoint(location.x + 0.0f, location.y - upper));
			}
		}
		
		if(zoomFactor != 0.0f) {
			
			initBoundingBox();
		}
	}
	
	private void initBoundingBox() {
		
		boundingBox = new Rectangle2D.Float(trunk.x * zoomFactor,
				trunk.y * zoomFactor,
				trunk.width * zoomFactor,
				trunk.height * zoomFactor);
		
		if(triangles != null) {
			
			for(int ii = 0; ii < triangles.length; ii++) {

				if(triangles[ii].boundingBox != null) {
				
					boundingBox = (Float)boundingBox.createUnion(triangles[ii].boundingBox);
				}
			}
		}
	}

	@Override
	public void draw(Graphics gfx, float zoomFactor) {
		
		Color temp = gfx.getColor();
		if(selected) {
			
			gfx.setColor(Globals.SELECTED_COLOR);
		}
		
		// draw trunk
		gfx.drawRect(Math.round(trunk.x * zoomFactor), 
				Math.round(trunk.y * zoomFactor), 
				Math.round(trunk.width * zoomFactor), 
				Math.round(trunk.height * zoomFactor));
		
		// draw triangles
		if(triangles != null) {
			
			for(int ii = 0; ii < triangles.length; ii++) {
				
				triangles[ii].draw(gfx, zoomFactor);
			}
		}
		
		gfx.setColor(temp);
		
		// if zoom factor has changed, update our bounding box
		if(this.zoomFactor != zoomFactor) {
		
			this.zoomFactor = zoomFactor;
			initBoundingBox();
		}
		
		// for testing
//		if(boundingBox != null) {
//			
//			gfx.drawRect(Math.round(boundingBox.x), 
//					Math.round(boundingBox.y), 
//					Math.round(boundingBox.width), 
//					Math.round(boundingBox.height));
//		}
	}

	@Override
	public boolean hitTest(int x, int y) {
		
		if(boundingBox == null) {
			
			return false;
		}
		
		if(boundingBox.contains(x, y)) {
			
			if(trunk.contains(x / zoomFactor, y / zoomFactor)) {
				
				return true;
			}
			
			if(triangles != null) {
				
				for(int ii = 0; ii < triangles.length; ii++) {
					
					if(triangles[ii].hitTest(x, y)) {
						
						return true;
					}
				}
			}
		}
		
		return false;
	}

	@Override
	public PropertiesPanel getPropertiesPanel() {
		
		return new TreePanel(this);
	}

	@Override
	public void setSelected(boolean selected) {
		
		this.selected = selected;
	}

	@Override
	public WorldPoint getSelectedPoint() {
		
		return location;
	}
}
