package polygonsSWP.geometry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Polygon interface for simple _and_ complex polygons. Subclasses should 
 * never assume they contain a simple polygon.
 * 
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 */
public abstract class Polygon
{
  /**
   * @return Returns the ordered list of points associated with the polygon,
   * with an implicit edge between ret[ret.size()] and ret[0].
   */
  public abstract List<Point> getPoints();
  
  /**
   * @return Returns a copy of the polygon instance.
   */
  public abstract Polygon clone();
  
  /**
   * @return True if object equals polygon, false otherwise
   */
  public abstract boolean equals(Object obj);
  
  /**
   * @return If point is in Polygon.
   */
  public abstract boolean containsPoint(Point p, boolean onLine);
  
  /**
   * @return Surface area as double.
   */
  public abstract double getSurfaceArea();
  
  /**
   * @return a random point in the polygon area (including on the edges).
   */
  public abstract Point createRandomPoint();
  
  /**
   * @return a SVG representation of the polygon contained in a string
   */
  public String toSVG() {
    StringBuilder sb = new StringBuilder();  
    List<Point> points = getPoints();
    
    sb.append("<?xml version=\"1.0\"?>\n");
    sb.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
    sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n");
    if(points.size() != 0) { 
      sb.append("<polygon points=\"");
      for(Point p : points) {
        sb.append(p.x);
        sb.append(",");
        sb.append(p.y);
        sb.append(" ");
      }
      sb.append("\" style=\"fill:lime;stroke:purple;stroke-width:1\" />\n");
    }
    sb.append("</svg>\n");
    return sb.toString();
  }
  
  /**
   * Prints the polygon to a svg file, no questions asked.
   * Debug method to avoid having to write too much try..catch clauses.
   * 
   * @param filename filename to save svg into
   * 
   * TODO remove or change
   */
  public void toSVGFile(String filename) {
    try {
      File f = new File(filename);
      f.createNewFile();
      OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(f));
      out.write(toSVG());
      out.close();
    }
    catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
