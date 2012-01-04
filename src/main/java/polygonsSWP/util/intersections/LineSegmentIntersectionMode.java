package polygonsSWP.util.intersections;

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
