package polygonsSWP.data;

import polygonsSWP.geometry.Line;
import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.Ray;

public interface Scene
{
  public void safe();
  
  public void setBoundingBox(int height, int width);
  
  public void setBoundingBox(int radius);
  
  public void addPolygon(Polygon polygon, Boolean highlight);
  
  public void addLine(Line line, Boolean highlight);
  
  public void addLineSegment(LineSegment linesegment, Boolean highlight);
  
  public void addRay(Ray ray, Boolean highlight);
  
  public void addPoint(Point point, Boolean highlight);
  
  public String toSvg();
}
