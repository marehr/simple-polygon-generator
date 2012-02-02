package polygonsSWP.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.Point;


public class EdgeList
{

  private class EdgeComparator
    implements Comparator<LineSegment>
  {

    /**
     * The comparator now compares the endpoint and the starting point of isec1
     * with isec2 based on a orientation test.
     */
    @Override
    public int compare(LineSegment isec1, LineSegment isec2) {
      if (isec1.equals(isec2)) { return 0; }
      // Choose right endpoint (the one with the smaller Y-coordinate)
      Point endP = null;
      Point infP = null;
      if (isec1._a.y < isec1._b.y) {
        endP = isec1._a;
        infP = isec1._b;
      }
      else {
        endP = isec1._b;
        infP = isec1._a;
      }
      Point begin, end;
      if (isec2._a.y > isec2._b.y) {
        begin = isec2._a;
        end = isec2._b;
      }
      else {
        begin = isec2._b;
        end = isec2._a;
      }
      int fstOrient = MathUtils.checkOrientation(begin, end, endP);
      int sndOrient = MathUtils.checkOrientation(begin, end, infP);
      if ((sndOrient == 1 || fstOrient == 1)) return 1;
      if (fstOrient == 0 && sndOrient == 0) return 0;
      return -1;
    }

  }

  private TreeMap<Point, LineSegment[]> isecStore =
      new TreeMap<Point, LineSegment[]>() {
        /**
 * 
 */
        private static final long serialVersionUID = 1L;

        @Override
        public String toString() {
          String reValue = "";
          reValue += "{";
          for (Entry<Point, LineSegment[]> item : this.entrySet()) {
            reValue += "(" + item.getKey() + ")=[";
            for (LineSegment p : item.getValue())
              reValue += p + ",";
            reValue += "]\n";
          }
          return reValue;
        }
      };
  private TreeMap<Point, LineSegment[]> endStore =
      new TreeMap<Point, LineSegment[]>() {
        /**
     * 
     */
        private static final long serialVersionUID = 1L;

        @Override
        public String toString() {
          String reValue = "";
          reValue += "{";
          for (Entry<Point, LineSegment[]> item : this.entrySet()) {
            reValue += "(" + item.getKey() + ")=[";
            for (LineSegment p : item.getValue())
              reValue += p + ",";
            reValue += "]\n";
          }
          return reValue;
        }
      };
  private TreeSet<LineSegment> orderedEdges = new TreeSet<LineSegment>(
      new EdgeComparator());
  private List<Point> markedEdges = new ArrayList<Point>();

  public EdgeList() {
  }

  /**
   * This methods adds the line to the orderedStore.
   * 
   * @param isec
   * @param endPoint
   */
  private void insertInOrderedStore(Point isec, Point endPoint) {
    orderedEdges.add(new LineSegment(endPoint, isec));
  }

  /**
   * This methods adds the edge to the endstore, but if it already contains the
   * endpoint adds it to the array.
   * 
   * @param isec
   * @param endPoint
   */
  private void insertInEndStore(Point isec, Point endPoint) {
    if (endStore.containsKey(endPoint)) {
      LineSegment[] tmpArray = endStore.remove(endPoint);
      if (tmpArray[0] == null) tmpArray[0] = new LineSegment(endPoint, isec);
      else if (tmpArray[1] == null)
        tmpArray[1] = new LineSegment(endPoint, isec);
      endStore.put(endPoint, tmpArray);
    }
    else {
      LineSegment[] isecs = { new LineSegment(endPoint, isec), null };
      endStore.put(endPoint, isecs);
    }
  }

  /**
   * This methods adds the edge to the isecStore, but if it already contains the
   * isec adds it to the array.
   * 
   * @param isec
   * @param endPoint
   */
  private void insertInIsecStore(Point isec, Point endPoint) {
    // If the isecStore doesn't contain the isec, just add it
    System.out.println("IsecStoreInsert:");
    System.out.println("Before: " + isecStore);
    if (!isecStore.containsKey(isec)) {
      System.out.println("ContainsKey!");
      LineSegment[] tmpArray = { new LineSegment(endPoint, isec), null };
      isecStore.put(isec, tmpArray);
    }
    // If the isecStore contains the isec, check which one of the
    // edges is null and replace it with the new one
    else {
      System.out.println("Doesn't contain Key");
      LineSegment[] tmpArray = isecStore.remove(isec);
      if (tmpArray[0] == null) tmpArray[0] = new LineSegment(endPoint, isec);
      else if (tmpArray[1] == null)
        tmpArray[1] = new LineSegment(endPoint, isec);
      isecStore.put(isec, tmpArray);
    }
    System.out.println("After: " + isecStore);
  }

  /**
   * Add the edge to all the stores according to the assumptions.
   * 
   * @param isec
   * @param endPoint
   */
  public void insertEdge(Point isec, Point endPoint) {
    System.out.println("Add to Store: " + endPoint + " " + isec);
    insertInEndStore(isec, endPoint);
    insertInIsecStore(isec, endPoint);
    insertInOrderedStore(isec, endPoint);
  }

  private void updateIsecStore(Point endPoint, Point oldIsec, Point newIsec) {
    assert (isecStore.containsKey(oldIsec));
    System.out.println("IsecStore: ");
    System.out.println(isecStore);
    LineSegment[] tmpArray = isecStore.remove(oldIsec);
    if (tmpArray[0] != null &&
        tmpArray[0].equals(new LineSegment(endPoint, oldIsec))) {
      if (tmpArray[1] == null) {
        tmpArray[0] = new LineSegment(endPoint, newIsec);
        isecStore.put(newIsec, tmpArray);
      }
      else {
        LineSegment[] oldTmp = { tmpArray[1], null };
        LineSegment[] newTmp = { new LineSegment(endPoint, newIsec), null };
        isecStore.put(oldIsec, oldTmp);
        isecStore.put(newIsec, newTmp);
      }
    }
    else if (tmpArray[1] != null &&
        tmpArray[1].equals(new LineSegment(endPoint, oldIsec))) {
      if (tmpArray[0] == null) {
        LineSegment[] newTmp = { new LineSegment(endPoint, newIsec), null };
        isecStore.put(newIsec, newTmp);
      }
      else {
        LineSegment[] oldTmp = { tmpArray[0], null };
        LineSegment[] newTmp = { new LineSegment(endPoint, newIsec), null };
        isecStore.put(oldIsec, oldTmp);
        isecStore.put(newIsec, newTmp);
      }
    }
    System.out.println(isecStore);
  }

  private void updateOrderedStore(Point endPoint, Point oldIsec, Point newIsec) {
    removeOrderedStore(new LineSegment(endPoint, oldIsec));
    orderedEdges.add(new LineSegment(endPoint, newIsec));
  }

  private void updateEndStore(Point endPoint, Point oldIsec, Point newIsec) {
    assert (endStore.containsKey(endPoint));
    System.out.println("ENDSTORE:--------------");
    System.out.println(endStore);
    LineSegment[] isecs = endStore.remove(endPoint);
    if (isecs[0] != null && isecs[0].equals(new LineSegment(endPoint, oldIsec))) {
      if (isecs[1] == null) {
        LineSegment[] newTmp = { new LineSegment(endPoint, newIsec), null };
        endStore.put(endPoint, newTmp);
      }
      else {
        LineSegment[] oldTmp = { new LineSegment(endPoint, newIsec), isecs[1] };
        endStore.put(endPoint, oldTmp);
      }
    }
    else if (isecs[1] != null &&
        isecs[1].equals(new LineSegment(endPoint, oldIsec))) {
      if (isecs[0] == null) {
        LineSegment[] newTmp = { new LineSegment(endPoint, newIsec), null };
        endStore.put(endPoint, newTmp);
      }
      else {
        LineSegment[] newTmp = { isecs[0], new LineSegment(endPoint, newIsec) };
        endStore.put(endPoint, newTmp);
      }
    }
    System.out.println(endStore);
  }

  /**
   * This methods updates intersection points of edges in the datastructure. So,
   * we have to change the specified edge in each datastructure.
   * 
   * @param endPoint
   * @param oldIsec
   * @param newIsec
   */
  public void updateIntersection(Point endPoint, Point oldIsec, Point newIsec) {
    System.out.println("Upate store with :" + oldIsec + " " + endPoint + " " +
        newIsec);
    updateEndStore(endPoint, oldIsec, newIsec);
    updateIsecStore(endPoint, oldIsec, newIsec);
    updateOrderedStore(endPoint, oldIsec, newIsec);
  }

  /**
   * Mark edges, specified by endPoint for deletion. So, delete every edge with
   * this endpoint from datastructures.
   * 
   * @param endPoint
   */
  public void markEdge(Point endPoint) {
    System.out.println("MARK: " + endPoint);
    markedEdges.add(endPoint);
  }

  private LineSegment getNearestLeftLineSegment(Point currP) {
    LineSegment[] edges = endStore.get(currP);
    if (edges == null) {
      edges = isecStore.get(currP);
    }
    if (edges[1] != null) {
      if (edges[0]._b.x < edges[1]._b.x) {
        LineSegment edge = orderedEdges.lower(edges[0]);
        return edge;
      }
      else {
        LineSegment edge = orderedEdges.lower(edges[1]);
        return edge;
      }
    }
    else {
      LineSegment edge = orderedEdges.lower(edges[0]);
      return edge;
    }
  }
  
  public LineSegment getLeftEdge(Point currP, Point endPoint) {
    LineSegment edge = new LineSegment(endPoint, currP);
    return orderedEdges.lower(edge);
  }
  
  public LineSegment getRightEdge(Point currP, Point endPoint) {
    LineSegment edge = new LineSegment(endPoint, currP);
    return orderedEdges.higher(edge);
  }

  private LineSegment getNearestRightLineSegment(Point currP) {
    LineSegment[] edges = endStore.get(currP);
    if (edges == null) {
      edges = isecStore.get(currP);
    }
    if (edges[1] != null) {
      if (edges[0]._b.x < edges[1]._b.x) {
        LineSegment edge = orderedEdges.higher(edges[1]);
        return edge;
      }
      else {
        LineSegment edge = orderedEdges.higher(edges[0]);
        return edge;
      }
    }
    else {
      LineSegment edge = orderedEdges.higher(edges[0]);
      return edge;
    }
  }

  /**
   * @param x
   * @param direct
   * @return
   */
  public LineSegment[] searchIntersectingEdges(Point currP,
      PointType.Direction direct) {
    if (direct == PointType.Direction.LEFT) {
      LineSegment[] tmp = { this.getNearestLeftLineSegment(currP), null };
      return tmp;
    }
    else if (direct == PointType.Direction.RIGHT) {
      LineSegment[] tmp = { this.getNearestRightLineSegment(currP), null };
      return tmp;
    }
    else {
      LineSegment[] tmp =
          { this.getNearestLeftLineSegment(currP),
              this.getNearestRightLineSegment(currP) };
      return tmp;
    }
  }

  /**
   * @param endPoint
   * @return This Method returns all edges ending with 'endPoint'. This can
   *         either be one for every PointClass except MIN, which would return
   *         two edges.
   */
  public Point[] getIntersectionByEndPoint(Point endPoint) {
    LineSegment[] tmpArray = endStore.get(endPoint);
    Point[] retArray = new Point[2];
    retArray[0] = tmpArray[0]._b;
    if (tmpArray[1] != null) retArray[1] = tmpArray[1]._b;
    return retArray;
  }

  private void removeIsec(Point isec, Point endPoint) {
    assert (isecStore.containsKey(isec));
    LineSegment[] tmpEdges = isecStore.remove(isec);
    if (tmpEdges[0] != null &&
        tmpEdges[0].equals(new LineSegment(endPoint, isec))) {
      if (tmpEdges[1] == null) return;
      else {
        LineSegment[] tmpArray = { tmpEdges[1], null };
        isecStore.put(isec, tmpArray);
      }
    }
    else if (tmpEdges[1] != null &&
        tmpEdges[1].equals(new LineSegment(endPoint, isec))) {
      if (tmpEdges[0] == null) return;
      else {
        LineSegment[] tmpArray = { tmpEdges[0], null };
        isecStore.put(isec, tmpArray);
      }
    }
  }

  private void removeOrderedStore(LineSegment edge) {
    Object[] tmp = orderedEdges.toArray();
    orderedEdges.clear();
    for (Object item : tmp) {
      if (!item.equals(edge)) orderedEdges.add((LineSegment) item);
    }
  }

  /**
   * Removes all marked edges from data structure. TODO: fix this crapy remove,
   * but there for the comparator needs to be valid!
   */
  public void removeMarkedEdges() {
    System.out.println("Remove all marked edges.");
    for (Point endPoint : markedEdges) {
      LineSegment[] edges = endStore.remove(endPoint);
      for (LineSegment edge : edges) {
        if (edge != null) {
          removeIsec(edge._b, endPoint);
          removeOrderedStore(edge);
        }
      }
    }
    markedEdges.clear();
  }
}
