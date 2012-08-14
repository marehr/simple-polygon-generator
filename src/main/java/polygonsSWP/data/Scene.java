package polygonsSWP.data;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import polygonsSWP.geometry.Line;
import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.Ray;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>

 */
public interface Scene
{
  /**
   * This method saves the scene to the history object specified at construction
   * time.
   */
  public void save();

  /**
   * Sets a rectangle bounding box.
   * 
   * @param height
   * @param width
   */
  public Scene setBoundingBox(int height, int width);

  /**
   * Sets a polygon as bounding box.
   * 
   * @param polygon
   */
  public Scene setBoundingBox(Polygon polygon);

  /**
   * Merges another scene in this scene
   * 
   * @param scene
   */
  public Scene mergeScene(Scene scene);

  /**
   * Adds a polygon to the scene
   * 
   * @param polygon
   * @param highlight whether it should be highlighted or not
   */
  public Scene addPolygon(Polygon polygon, Boolean highlight);

  /**
   * Adds a polygon to the scene
   * 
   * @param polygon
   * @param color highlighted color
   */
  public Scene addPolygon(Polygon polygon, Color color);

  /**
   * Adds a line to the scene
   * 
   * @param line
   * @param highlight whether it should be highlighted or not
   */
  public Scene addLine(Line line, Boolean highlight);

  /**
   * Adds a line to the scene
   * 
   * @param line
   * @param color highlighted color
   */
  public Scene addLine(Line line, Color color);

  /**
   * Adds a lineSegment to the scene
   * 
   * @param linesegment
   * @param highlight whether it should be highlighted or not
   */
  public Scene addLineSegment(LineSegment linesegment, Boolean highlight);

  /**
   * Adds a lineSegment to the scene
   * 
   * @param linesegment
   * @param color highlighted color
   */
  public Scene addLineSegment(LineSegment linesegment, Color color);

  /**
   * Adds a ray to the scene
   * 
   * @param ray
   * @param highlight whether it should be highlighted or not
   */
  public Scene addRay(Ray ray, Boolean highlight);

  /**
   * Adds a ray to the scene
   * 
   * @param ray
   * @param color highlighted color
   */
  public Scene addRay(Ray ray, Color color);

  /**
   * Adds a point to the scene.
   * 
   * @param point
   * @param highlight whether it should be highlighted or not
   */
  public Scene addPoint(Point point, Boolean highlight);

  /**
   * Adds a point to the scene.
   * 
   * @param point
   * @param color highlighted color
   */
  public Scene addPoint(Point point, Color color);

  /**
   * Adds points to the scene.
   * 
   * @param points
   * @param highlight whether it should be highlighted or not
   */
  public Scene addPoints(List<Point> points, Boolean highlight);

  /**
   * Adds points to the scene.
   * 
   * @param points
   * @param color highlighted color
   */
  public Scene addPoints(List<Point> points, Color color);

  /**
   * Generates a string containing a valid SVG representation of the scene
   * 
   * @return SVG representation in a String
   */
  public String toSvg();

  /**
   * Doodle.
   */
  public void paint(Graphics2D g2d);

  /**
   * Doodle Points.
   */
  public void paintPoints(Graphics2D g2d);
}
