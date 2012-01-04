package polygonsSWP.util.intersections;

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
