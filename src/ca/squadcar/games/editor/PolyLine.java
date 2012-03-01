package ca.squadcar.games.editor;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

public class PolyLine extends DrawableElement {

	public static int counter = 0;
	
	private ArrayList<WorldPoint> points;
	private int pointSize;
	private int id;
	
	public PolyLine() {
		
		init();
	}
	
	public PolyLine(final WorldPoint point) {
		
		init();
		points.add(point);
	}
	
	public PolyLine(final PolyLine polyLine) {
		
		init();
		for(WorldPoint point : polyLine.getPoints()) {
			
			points.add(new WorldPoint(point));
		}
	}
	
	private void init() {
		
		points = new ArrayList<WorldPoint>();
		
		pointSize = 4;
		
		// store an ID so we can name it within the level file next time
		id = PolyLine.counter++;
	}
	
	// need to scale this properly for zoom and what not
	public void addPoint(WorldPoint point) {
	
		points.add(point);
	}
	
	@Override
	public void draw(Graphics gfx, float zoomFactor) {

		if(points.size() == 1) {
			
			// just draw the single point
			gfx.drawOval(Math.round(points.get(0).x * zoomFactor) - pointSize / 2, Math.round(points.get(0).y * zoomFactor) - pointSize / 2, pointSize, pointSize);
		} else if(points.size() > 1) {
		
			// get the points in array form
			int[] tempX = new int[points.size()];
			int[] tempY = new int[points.size()];
			for(int ii = 0; ii < points.size(); ii++) {
				
				tempX[ii] = Math.round(points.get(ii).x * zoomFactor);
				tempY[ii] = Math.round(points.get(ii).y * zoomFactor);
				gfx.drawOval(Math.round(points.get(ii).x * zoomFactor) - pointSize / 2, Math.round(points.get(ii).y * zoomFactor) - pointSize / 2, pointSize, pointSize);
			}
			
			gfx.drawPolyline(tempX, tempY, points.size());
		}
	}
	
	public ArrayList<WorldPoint> getPoints() {
		
		return points;
	}

	@Override
	public void saveToFile(Ini ini) {
		
		String name = String.format("polyline%d", id);
		ini.put("level", "polyline", name);
		Section section = ini.add(name);
		List<Float> xPoints = new ArrayList<Float>();
		List<Float> yPoints = new ArrayList<Float>();
		WorldPoint startPoint = points.get(0);
		for(WorldPoint point : points) {

			// Translate all points such that the first is at our origin
			xPoints.add(point.x - startPoint.x);
			
			// invert y axis
			yPoints.add(-(point.y - startPoint.y));
		}
		section.putAll("x", xPoints.toArray());
		section.putAll("y", yPoints.toArray());
	}
}