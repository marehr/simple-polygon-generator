package polygonsSWP.generators.other;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polygonsSWP.data.History;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.util.GeneratorUtils;


public class EnumeratingPermuteAndRejectFactory
  implements PolygonGeneratorFactory
{

  @Override
  public boolean acceptsUserSuppliedPoints() {
    return true;
  }

  @Override
  public List<Parameters> getAdditionalParameters() {
    return new LinkedList<Parameters>();
  }

  @Override
  public String toString() {
    return "Enumerating Permute & Reject";
  }

  @Override
  public PolygonGenerator createInstance(Map<Parameters, Object> params,
      PolygonStatistics stats, History steps)
    throws IllegalParameterizationException {

    List<Point> points = GeneratorUtils.createOrUsePoints(params, true);
    return new EnumeratingPermuteAndReject(points, steps, stats);
  }

  /**
   * In this optimized version of Permute & Reject, we enumerate all the
   * permutations of 1..N, where N is the number of points, in order to
   * avoid re-checking polygon chains for simplicity.
   * 
   * Furthermore, we choose the enumeration scheme to find a solution
   * more quickly by the following optimizations.
   * 
   * - For a point set of N points, we can generate up to N! permutations.
   * - We know, that for every point set of N points, there exists at
   * least 1 polygon chain which is simple. Let P = [x0, x1, x2, ..., xn]
   * be the list of indices describing this simple polygon chain, i.e.
   * xi = pj means that point pj is to be inserted at position i in the polygon
   * chain.  
   * - Knowing this list of indices, we also know that we can 
   * rotate that chain around and still have a valid simple polygon. This gives
   * at least N valid solutions for every point set of N points. Additionally,
   * every rotated solution can be mirrored (i.e., read from the back), which
   * increases this number to 2N. As a conclusion, for every point set of N points
   * we have _at least_ 2N valid simple polygons. For example, this would be the
   * case if all points are lying on the convex hull of the point set.
   * - Following thereof, we only have to test N!/2N permutations for simplicity.
   * In order to enumerate these permutations without coming across rotated/mirrored
   * ones, the following implementation makes use of two simple facts:
   *   1. We only create permutations for the trailing N-1 points and always prepend
   *      these permutations with the first point. This eliminates all 'rotations'.
   *   2. We use the Steinhaus-Johnson-Trotter algorithm to enumerate the permutations
   *      iteratively. This algorithm has the habit of enumerating 'mirrored' permutations
   *      only in the 2nd half of the list, i.e. after the first n!/2 permutations
   *      have been named. We have no proof for this assumption, but experimentation
   *      suggests that we're right (see the below main() function!). 
   *      This in combination with the above remarks ensures that we first find a 
   *      solution before testing any 'mirrored' polygon chains.
   * 
   * REMARK: At the time of this writing, EP&R has still not succeeded in creating a
   * 14-point polygon, whereas P&R already did 18. Although theoretically we established
   * an upper bound for the runtime in EP&R, the used enumeration scheme may still yield
   * worse expected runtimes than pure random permutation?
   * 
   * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
   */
  private static class EnumeratingPermuteAndReject
    implements PolygonGenerator
  {
    
    private boolean doStop = false;
    private List<Point> points;
    
    final private History steps;
    final private PolygonStatistics statistics;

    EnumeratingPermuteAndReject(List<Point> points, History steps,
        PolygonStatistics statistics) {
      this.points = points;
      this.steps = steps;
      this.statistics = statistics;
    }

    @Override
    public Polygon generate() {
      OrderedListPolygon p = null;
      final int permN = points.size() - 1;
      final int[] perm = new int[permN];
      final int[] dir = new int[permN];

      // Initial permutation.
      for(int i = 1; i <= permN; i++) {
        perm[i - 1] = i;
        dir[i - 1] = -1; // Left.
      }
      
      // Initialize history and statistic. 
      if (steps != null) steps.clear();
      if (statistics != null) statistics.iterations = 0;

      do {
        // Step 1: Construct a polygon from the permutation.
        List<Point> poly = new ArrayList<Point>();
        poly.add(points.get(0));
        for (int i = 0; i < permN; i++)
          poly.add(points.get(perm[i]));
        p = new OrderedListPolygon(poly);

        // Create a new scene for each polygon and update statistic.
        if (steps != null) steps.newScene().addPolygon(p, true).save();
        if (statistics != null) statistics.iterations++;
        
        // Step 2: Accept simple polygons.
        if (p.isSimple()) {
          // If not in counterclockwise orientation, reverse orientation.
          if (p.isClockwise() == 1) p.reverse();

          break;
        }
        
        // Step 3: Create a new Steinhaus-Johnson-Trotter permutation.
        pjt(perm, dir);
        
      } while (!doStop);

      if (doStop) return null;
      else return p;
    }

    private static void pjt(final int[] perm, final int[] dir) {
      final int n = perm.length;
      
      // Find the largest mobile integer.
      int lmi = -1; // Largest mobile number's index.
      for(int i = 0; i < n; i++) {
        // If an integer is on the rightmost column pointing to the right, it’s not mobile.
        // If an integer is on the leftmost column pointing to the left, it’s not mobile.
        if((i == (n - 1) && dir[i] == 1) || (i == 0 && dir[i] == -1))
          continue;
        
        // Integer is mobile, if the adjacent integer in its associated direction is smaller.
        if(perm[i] > perm[i + dir[i]]) {
          // Check if it's larger than the current lmn.
          if((lmi == -1) || (perm[i] > perm[lmi]))
            lmi = i;
        }
      }
      
      // No mobile integer? Algorithm terminates. Here, we should
      // really already have found a simple polygon.
      if(lmi < 0) {
        throw new RuntimeException("Our assumptions are false.");
      }
        
      // Swap it with the adjacent element.
      int ai = lmi + dir[lmi];
      int permtmp = perm[ai];
      int dirtmp = dir[ai];
      perm[ai] = perm[lmi];
      dir[ai] = dir[lmi];
      perm[lmi] = permtmp;
      dir[lmi] = dirtmp;
      
      // Remember, that we changed the largest mobile number's index.
      lmi = ai;
      
      // After each swapping, check if there’s any number, larger than the current 
      // largest mobile integer. If there’s one or more, change the direction 
      // of all of them. 
      for(int i = 0; i < n; i++) {
        if(perm[i] > perm[lmi])
          dir[i] = (dir[i] < 0) ? 1 : -1;
      }
    }

    @Override
    public void stop() {
      doStop = true;
    }
  }
  
  public static void main(String[] args) {
    int N = 8;
    List<int[]> perms = new LinkedList<int[]>();
    int[] perm = new int[N];
    int[] dir = new int[N];
    for(int i = 0; i < N; i++) {
      perm[i] = i + 1;
      dir[i] = -1;
    }
    
    // Expected reversed permutation at N!/2.
    int expected = 1;
    for(int i = 3; i <= N; i++) {
      expected *= i;
    }
    
    while(true) {
      // Save a reverse copy.
      int[] revperm = new int[N];
      for(int i = 0; i < N; i++)
        revperm[i] = perm[N - 1 - i];
      perms.add(revperm);
      
      EnumeratingPermuteAndReject.pjt(perm, dir);
      
      for(int[] revp : perms) {
        if(Arrays.equals(perm, revp)) {
          int actual = perms.size();
          System.out.println("Reversed array found at " + actual + ", expected at " + expected + ".");
          return;
        }
      }
    }
  }
}
