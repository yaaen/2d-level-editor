package ca.squadcar.games.editor.gui;

import java.awt.Graphics;

import javax.swing.JPanel;

import ca.squadcar.games.editor.JsonElement;
import ca.squadcar.games.editor.JsonElementList;
import ca.squadcar.games.editor.JsonLevel;
import ca.squadcar.games.editor.elements.BipedReference;
import ca.squadcar.games.editor.elements.IDrawableElement;
import ca.squadcar.games.editor.elements.Line;
import ca.squadcar.games.editor.elements.QuadraticBezierCurve;
import ca.squadcar.games.editor.elements.WorldPoint;
import ca.squadcar.games.editor.export.Level;

import com.google.gson.Gson;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class LevelCanvas extends JPanel {

	private ArrayList<ArrayList<IDrawableElement>> elements;
	private ArrayList<IDrawableElement> currList;
	private float zoomFactor;
	private IDrawableElement temp;
	private Dimension canvasDim;
	private BipedReference bipedRef;
	private IDrawableElement lastHitElement;
	private ArrayList<IDrawableElement> lastHitList;
	private JsonLevel level;
	private Line guideLine; // line drawn from last point to mouse-moved point when in drawing mode
	
	/**
	 * Custom panel for drawing onto
	 */
	public LevelCanvas() {
		
		setBackground(Color.WHITE);
		
		elements = new ArrayList<ArrayList<IDrawableElement>>();
		currList = null;
		zoomFactor = 10.0f;
		temp = null;
		canvasDim = null;
		bipedRef = new BipedReference();
		lastHitElement = null;
		guideLine = null;
		
		reset();
	}

	@Override
	public void paint(Graphics gfx) {
		
		super.paint(gfx);
		
		for(ArrayList<IDrawableElement> list : elements) {
			
			for(IDrawableElement element : list) {
				
				element.draw(gfx, zoomFactor);
			}
		}
		
		if(temp != null) {
			
			temp.draw(gfx, zoomFactor);
		}
		
		bipedRef.draw(gfx, zoomFactor);
		
		gfx.setColor(Color.LIGHT_GRAY);
		if(guideLine != null) {
			
			guideLine.draw(gfx, zoomFactor);
		}
	}
	
	public void setCursor(int cursor) {
		
		setCursor(Cursor.getPredefinedCursor(cursor));
	}
	
	public void addDrawableElement(final IDrawableElement element, boolean currElemIsNew) {
		
		if(currElemIsNew) {
			
			currList = new ArrayList<IDrawableElement>();
			elements.add(currList);
		}
		
		currList.add(element);
	}
	
	public void setZoomFactor(final float zoomFactor) {
		
		this.zoomFactor = zoomFactor;
		
		if(canvasDim != null) {
			
			Dimension dim = new Dimension(canvasDim);
			dim.height *= zoomFactor;
			dim.width *= zoomFactor;
			setPreferredSize(dim);
		}
	}
	
	public float getZoomFactor() {
		
		return zoomFactor;
	}
	
	public void zoomIn(final int factor) {
		
		zoomFactor *= factor;
	}
	
	public void zoomOut(final int factor) {
		
		zoomFactor /= factor;
	}
	
	/**
	 * The temp element is used to draw an element that hasn't been finalized; e.g.: the current poly line that is being drawn.
	 */
	public void setTempDrawableElement(IDrawableElement temp) {
		
		this.temp = temp;
	}
	
	/**
	 * The guideline will show on mouse movement when in drawing mode
	 * @param guideLine: the temporary guideline to draw
	 */
	public void setGuideLine(Line guideLine) {
		
		this.guideLine = guideLine;
	}
	
	public void setCanvasDimension(Dimension dim) {
		
		canvasDim = new Dimension(dim);
	}
	
	public boolean hasElements() {
		
		return (elements.size() > 0);
	}
	
	public Level getLevelForExport() {
		
		if(elements.size() == 0) {
			
			return null;
		}
	
		Level level = new Level();
		level.polyLines = new ca.squadcar.games.editor.export.PolyLine[elements.size()];
		
		// we need to translate all points relative to the first
		IDrawableElement firstElem = elements.get(0).get(0);
		WorldPoint transPoint = getStartPoint(firstElem);
		ArrayList<WorldPoint> points = new ArrayList<WorldPoint>();
		WorldPoint currPoint;
		for(int ii = 0; ii < elements.size(); ii++) {
			
			points.clear();
			level.polyLines[ii] = new ca.squadcar.games.editor.export.PolyLine();
			boolean isFirst = true;
			for(IDrawableElement element : elements.get(ii)) {
				
				// we add the first point, and then add mid and end points for each successive chain
				if(isFirst) {
					
					if(ii == 0) {
					
						points.add(new WorldPoint(0.0f, 0.0f)); // assume very first point is always at the origin
					} else {
						
						currPoint = new WorldPoint(getStartPoint(element));
						currPoint.x -= transPoint.x;
						currPoint.y -= transPoint.y;
						currPoint.y *= -1.0f;
						points.add(currPoint);
					}
					isFirst = false;
					continue;
				}
				
				if(element instanceof WorldPoint) {
					
					currPoint = new WorldPoint((WorldPoint)element);
					currPoint.x -= transPoint.x;
					currPoint.y -= transPoint.y;
					currPoint.y *= -1.0f;
					points.add(currPoint);
				} else if(element instanceof Line) {
					
					Line line = (Line)element;
					currPoint = new WorldPoint(line.end);
					currPoint.x -= transPoint.x;
					currPoint.y -= transPoint.y;
					currPoint.y *= -1.0f;
					points.add(new WorldPoint(currPoint));
				} else if(element instanceof QuadraticBezierCurve) {
					
					QuadraticBezierCurve curve = (QuadraticBezierCurve)element;
					for(Line line : curve.getLines()) {
						
						currPoint = new WorldPoint(line.end);
						currPoint.x -= transPoint.x;
						currPoint.y -= transPoint.y;
						currPoint.y *= -1.0f;
						points.add(new WorldPoint(currPoint));
					}
				}
			}
		
			if(points.size() <= 1) {
				
				continue;
			}
			
			level.polyLines[ii].points = new WorldPoint[points.size()];
			points.toArray(level.polyLines[ii].points);
		}
		
		return level;
	}
	
	public JsonLevel getLevelForSave() {
		
		if(elements.size() == 0) {
			
			return null;
		}

		JsonLevel level = new JsonLevel(elements, this.level);
		return level;
	}
	
	public void reset() {
		
		elements.clear();
		level = null;
		lastHitElement = null;
		lastHitList = null;
	}
	
	public boolean loadLevelFromFile(final File levelFile) throws IOException {
		
		level = null;
		
		BufferedReader br = null;
		try {
			
			br = new BufferedReader(new FileReader(levelFile));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
			
			Gson gson = new Gson();
			level = gson.fromJson(sb.toString(), JsonLevel.class);
		} catch(Exception ex) {
			
			ex.printStackTrace();
			return false;
		} finally {
			
			if(br != null) {
				
				br.close();
			}
		}
		
		if(level == null) {
			
			return false;
		}
		
		for(JsonElementList list : level.elementLists) {
		
			currList = new ArrayList<IDrawableElement>();
			elements.add(currList);
			for(JsonElement jsonElement : list.elements) {
				
				IDrawableElement element = jsonElement.toDrawableElement();
				if(element == null) {
					
					return false;
				}
				
				currList.add(element);
			}
		}
		
		return true;
	}
	
	public Dimension getCanvasDimension() {
		
		return canvasDim;
	}
	
	public void updateForViewportChange(final Point point) {
		
		bipedRef.setOffset(point);
	}
	
	public boolean hitTest(final WorldPoint point) {

		lastHitElement = null;
		lastHitList = null;
		
		// convert the mouse point to its world point
		for(ArrayList<IDrawableElement> list : elements) {
			
			for(IDrawableElement element : list) {
				
				if(element.hitTest(point.x, point.y)) {
					
					lastHitElement = element;
					lastHitList = list;
					return true;
				}
			}
		}
		
		return false;
	}
	
	public IDrawableElement getLastHitElement() {
		
		return lastHitElement;
	}
	
	public void updateNeighbors(final IDrawableElement element) {
		
		if(lastHitList == null) {
			
			return;
		}
		
		int index = lastHitList.indexOf(element);
		if(index == -1) {
			
			return;
		}
		
		int prev = index - 1;
		int next = index + 1;
		
		// update the previous neighbor...
		if(prev >= 0) {
			
			// get the point that we need to update
			WorldPoint point = getStartPoint(element);
			
			// update the appropriate point on the neighbor
			if(point != null) {
				
				setEndPoint(point, lastHitList.get(prev));
			}
		}
	
		// update the next neighbor...
		if(next < lastHitList.size()) {
			
			// get the point that we need to update
			WorldPoint point = getEndPoint(element);
			
			// update the appropriate point on the neighbor
			if(point != null) {
				
				setStartPoint(point, lastHitList.get(next));
			}
		}
	}
	
	public void selectNone() {
	
		for(ArrayList<IDrawableElement> list : elements) {
			
			for(IDrawableElement element : list) {
				
				element.setSelected(false);
			}
		}
		
		lastHitElement = null;
		lastHitList = null;
	}
	
	public void deleteElement(IDrawableElement element) {
		
		if(lastHitList == null) {
			
			return;
		}
		
		int index = lastHitList.indexOf(element);
		if(index == -1) {
			
			return;
		}
		
		// check if removing the first element
		if(index == 0) {
			
			// if it just the start point, delete it
			// otherwise do nothing, since we need the start point...
			if(lastHitList.size() == 1) {
				
				lastHitList.clear();
				elements.remove(lastHitList);
				lastHitList = null;
			} else {
				
				lastHitList.remove(index);
			}
		} else {

			boolean isLast = (index == (lastHitList.size() - 1));
			
			// if its the last element, remove it
			if(isLast) {
				
				lastHitList.remove(index);
			} else { // otherwise we need to update the next element
				
				setStartPoint(getStartPoint(element), lastHitList.get(index + 1));
				lastHitList.remove(index);
			}
		}
		
		return;
	}
	
	private static WorldPoint getStartPoint(IDrawableElement element) {
		
		WorldPoint point = null;
		if(element instanceof WorldPoint) {
			
			point = (WorldPoint)element;
		} else if(element instanceof Line) {
			
			point = ((Line)element).start;
		} else if(element instanceof QuadraticBezierCurve) {
			
			point = ((QuadraticBezierCurve)element).first;
		}
		
		return point;
	}
	
	private static WorldPoint getEndPoint(IDrawableElement element) {
		
		WorldPoint point = null;
		if(element instanceof WorldPoint) {
			
			point = (WorldPoint)element;
		} else if(element instanceof Line) {
			
			point = ((Line)element).end;
		} else if(element instanceof QuadraticBezierCurve) {
			
			point = ((QuadraticBezierCurve)element).third;
		}
		
		return point;
	}
	
	private static void setStartPoint(final WorldPoint point, IDrawableElement element) {
		
		if(element instanceof WorldPoint) {
			
			((WorldPoint)element).x = point.x;
			((WorldPoint)element).y = point.y;
		} else if(element instanceof Line) {
			
			((Line)element).start.x = point.x;
			((Line)element).start.y = point.y;
			((Line)element).init();
		} else if(element instanceof QuadraticBezierCurve) {
			
			((QuadraticBezierCurve)element).first.x = point.x;
			((QuadraticBezierCurve)element).first.y = point.y;
			((QuadraticBezierCurve)element).init();
		}
	}
	
	private static void setEndPoint(final WorldPoint point, IDrawableElement element) {
		
		if(element instanceof WorldPoint) {
			
			((WorldPoint)element).x = point.x;
			((WorldPoint)element).y = point.y;
		} else if(element instanceof Line) {
			
			((Line)element).end.x = point.x;
			((Line)element).end.y = point.y;
			((Line)element).init();
		} else if(element instanceof QuadraticBezierCurve) {
			
			((QuadraticBezierCurve)element).third.x = point.x;
			((QuadraticBezierCurve)element).third.y = point.y;
			((QuadraticBezierCurve)element).init();
		}
	}
}
