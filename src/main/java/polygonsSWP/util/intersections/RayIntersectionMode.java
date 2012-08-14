package polygonsSWP.util.intersections;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class RayIntersectionMode implements IntersectionMode
{
  private boolean includeEndPoint;
   
  public RayIntersectionMode(boolean includeEndPoint) {
    this.includeEndPoint = includeEndPoint;
  }
  
  @Override
  public boolean test(double mu) {
    return ((includeEndPoint && mu >= 0) || (!includeEndPoint && mu > 0));
  }
}
