package polygonsSWP.generators;

import polygonsSWP.geometry.Polygon;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public interface PolygonGenerator
{
  /**
   * Generates a simply polygon.
   * 
   * @return a Polygon, or null if the generation was cancelled.
   */
  public Polygon generate();
  
  /**
   * Stop method. Used to stop running generators. Implementing classes should react
   * on this after a short while.
   */
  public void stop();
}
