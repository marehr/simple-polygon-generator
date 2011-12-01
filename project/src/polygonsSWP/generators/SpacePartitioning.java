package polygonsSWP.generators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.data.PolygonHistory;
import polygonsSWP.util.GeneratorUtils;
import polygonsSWP.util.MathUtils;

/**
 * TODO:
 * - filter duplicated points from edges
 * - understand, why one specialcase can't be handled
 */
public class SpacePartitioning implements PolygonGenerator {

  @Override
  public String[] getAcceptedParameters() {
    return new String[] { "n", "size", "points" };
  }

  @Override
  public Polygon generate(Map<String, Object> params, PolygonHistory steps) {
    System.out.println("<------------------------- NEW GENERATE ------------------------->");

    List<Point> points = GeneratorUtils.createOrUsePoints(params);
    System.out.println("points: " + points);

    Point first = GeneratorUtils.removeRandomPoint(points),
          last = GeneratorUtils.removeRandomPoint(points);
    System.out.println("first: " + first + ", last: "+ last);

    List<Point> left = new ArrayList<Point>(points.size()),
                right = new ArrayList<Point>(points.size());

    partionateIn(left, right, points, first, last);

    Polygon leftPolygon = spacePartitioning(left, first, last),
            rightPolygon = spacePartitioning(right, last, first);

    System.out.println("left: " + leftPolygon.getPoints());
    System.out.println("right: " + leftPolygon.getPoints());
    OrderedListPolygon merge =(OrderedListPolygon) merge(leftPolygon, rightPolygon);
    System.out.println("merge: " + merge.getPoints());

    removeDuplicates(merge);

    System.out.println("polygon: " + merge.getPoints());

    System.err.println(merge.isSimple()? "simple" : "not simple");

    return merge;
  }

  private void removeDuplicates(Polygon result){
    List<Point> list = result.getPoints();
    list.remove(0);

    Iterator<Point> it = list.iterator();
    Point next = null, prev = null;
    while(it.hasNext()){
      next = it.next();

      if(prev == next)
        it.remove();

      prev = next;
    }
  }

  private String partionateIn(List<Point> left, List<Point> right,
      List<Point> points, Point first, Point last) {

    assert right != null && left != null && points != null;
    assert first != null && last != null;

    String output = "";
    for(Point point: points){

      int orients = MathUtils.checkOrientation(first, last, point);
      output += "orients: [" + first + "," + last + "," + point + "]" + 
          (orients < 0 ? "LEFT" : (orients == 0 ? "ONSEGMENT" : "RIGHT")) + "\n";
      if(orients < 0){ // on left-side
        left.add(point);
      } else {
        right.add(point);
      }

    }

    return output;
  }

  private Polygon merge(Polygon left, Polygon right) {
    left.getPoints().addAll(right.getPoints());
    return left;
  }

  private Polygon spacePartitioning(List<Point> points, Point first, Point last) {
    boolean intialside = true;
    //System.out.println("\npoints: " + points + ", ordered: " + (intialside ? "RIGHT" :"LEFT"));
    //System.out.println("first: " + first + ", last: "+ last);

    if(points.size() == 1) {
      //Point a = first.x < last.x ? first : last,
      //      b = a == first? last : first;
      Point a = intialside ? first : last, b =  intialside ? last : first;

      ArrayList<Point> list = new ArrayList<Point>();
      list.add(a);
      list.add(points.get(0));
      list.add(b);

      System.out.println("\n\n\npoints: " + points + ", ordered: " + (intialside ? "RIGHT" :"LEFT"));
      System.out.println("first: " + first + ", last: "+ last);
      System.out.println("draw segment: " + a + " to " + points.get(0) + " to " + b);
      return new OrderedListPolygon(list);
    }

    // base
    if(points.size() == 0) {
      //Point a = first.x < last.x ? first : last,
      //      b = a == first? last : first;
      Point a = intialside ? first : last, b =  intialside ? last : first;

      ArrayList<Point> list = new ArrayList<Point>();
      list.add(a);
      list.add(b);

      System.out.println("\n\n\npoints: " + points + ", ordered: " + (intialside ? "RIGHT" :"LEFT"));
      System.out.println("first: " + first + ", last: "+ last);
      System.out.println("draw segment: " + a + " to " + b);
      return new OrderedListPolygon(list);
    }

    Point middle = GeneratorUtils.removeRandomPoint(points);

    ArrayList<Point> left = new ArrayList<Point>(points.size()),
                     right = new ArrayList<Point>(points.size());

    String output = partionateIn(left, right, points, first, middle);

    Polygon leftPolygon = spacePartitioning(left, first, middle),
            rightPolygon = spacePartitioning(right, middle, last),
            a = intialside ? leftPolygon : rightPolygon,
            b = intialside ? rightPolygon : leftPolygon;

    System.out.println("\n\n\npoints: " + points + ", ordered: " + (intialside ? "RIGHT" :"LEFT"));
    System.out.println("first: " + first + ", last: "+ last);
    System.out.println("middle: " + middle);
    System.out.println(output);

    System.out.println("left: " + leftPolygon.getPoints());
    System.out.println("right: " + rightPolygon.getPoints());
    Polygon merge = merge(a, b);
    System.out.println("merge: " + merge.getPoints());

    return merge;
  }

  public String toString(){
    return "SpacePartitioning";
  }
}
