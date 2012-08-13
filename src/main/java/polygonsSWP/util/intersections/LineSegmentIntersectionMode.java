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
public class LineSegmentIntersectionMode
  implements IntersectionMode
{
  private boolean includeEndPoints;

  public LineSegmentIntersectionMode(boolean includeEndPoints) {
    this.includeEndPoints = includeEndPoints;
  }
  
  @Override
  public boolean test(double mu) {
    return ((includeEndPoints && mu >= 0 && mu <= 1) || 
        (!includeEndPoints && mu > 0 && mu < 1));
  }

}
