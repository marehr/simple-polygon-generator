package polygonsSWP.data;

import polygonsSWP.geometry.Line;
import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.Ray;


/**
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 */
public interface Scene
{
  /**
   * This method safes the scene to the history object specified at construction
   * time.
   */
  public void safe();

  /**
   * Sets a rectangle bounding box.
   * 
   * @param height
   * @param width
   */
  public void setBoundingBox(int height, int width);

  /**
   * Sets a circular bounding box
   * 
   * @param radius
   */
  public void setBoundingBox(int radius);

  /**
   * Adds a polygon to the scene
   * 
   * @param polygon
   * @param highlight whether it should be highlighted or not
   */
  public void addPolygon(Polygon polygon, Boolean highlight);

  /**
   * Adds a line to the scene
   * 
   * @param line
   * @param highlight whether it should be highlighted or not
   */
  public void addLine(Line line, Boolean highlight);

  /**
   * Adds a lineSegment to the scene
   * 
   * @param linesegment
   * @param highlight whether it should be highlighted or not
   */
  public void addLineSegment(LineSegment linesegment, Boolean highlight);

  /**
   * Adds a ray to the scene
   * 
   * @param ray
   * @param highlight whether it should be highlighted or not
   */
  public void addRay(Ray ray, Boolean highlight);

  /**
   * Adds a point to the scene.
   * 
   * @param point
   * @param highlight whether it should be highlighted or not
   */
  public void addPoint(Point point, Boolean highlight);

  /**
   * Generates a string containing a valid SVG representation of the scene
   * 
   * @return SVG representation in a String
   */
  public String toSvg();
}
