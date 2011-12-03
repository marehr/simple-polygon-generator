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

    System.out.println("\n\n");
    System.out.println("result in generate");
    System.out.println("first: " + first);
    System.out.println("last: "+ last);
    System.out.println("left: " + leftPolygon.getPoints());
    System.out.println("right: " + rightPolygon.getPoints());
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
    // base size == 0
    if(points.size() == 0) {
      ArrayList<Point> list = new ArrayList<Point>();
      list.add(first);
      list.add(last);

      System.out.println("\n\n---size == 0---\npoints: " + points);
      System.out.println("first: " + first);
      System.out.println("last: "+ last);
      System.out.println("draw segment: " + first + " to " + last);
      System.out.println("------");
      return new OrderedListPolygon(list);
    }

    // base size == 1
    if(points.size() == 1) {
      ArrayList<Point> list = new ArrayList<Point>();
      list.add(first);
      list.add(points.get(0));
      list.add(last);

      System.out.println("\n\n---size == 1---\npoints: " + points);
      System.out.println("first: " + first);
      System.out.println("last: "+ last);
      System.out.println("draw segment: " + first + " to " + points.get(0) + " to " + last);
      System.out.println("------");
      return new OrderedListPolygon(list);
    }

    Point middle = GeneratorUtils.removeRandomPoint(points);

    ArrayList<Point> left = new ArrayList<Point>(points.size()),
                     right = new ArrayList<Point>(points.size());

    String output = partionateIn(left, right, points, first, middle);

    boolean onLeftSide = MathUtils.checkOrientation(first, last, middle) == -1;

    Polygon leftPolygon = spacePartitioning(onLeftSide ? left : right, first, middle),
            rightPolygon = spacePartitioning(onLeftSide ? right : left, middle, last);

    System.out.println("\n\n---general---\npoints: " + points + ", ordered: " + (onLeftSide ? "LEFT" : "RIGHT"));
    System.out.println("first: " + first);
    System.out.println("last: "+ last);
    System.out.println("middle: " + middle);
    System.out.println(output);

    System.out.println("left: " + leftPolygon.getPoints());
    System.out.println("right: " + rightPolygon.getPoints());
    Polygon merge = merge(leftPolygon, rightPolygon);
    System.out.println("merge: " + merge.getPoints());
    System.out.println("------");

    return merge;
  }

  public String toString(){
    return "SpacePartitioning";
  }
}
