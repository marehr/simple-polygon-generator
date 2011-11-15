package polygonsSWP.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import java.util.List;

/**
 * GraphPolygon is another Polygon implementation which uses
 * an undirected graph to represent the polygon. This is necessary
 * for some algorithms such as 2-Opt moves which work on undirected 
 * edges instead of a point chain.  
 * 
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 */
public class GraphPolygon implements Polygon
{
  /*
   * Data model:
   * 
   * vertices: unordered list of vertices in the polygon
   * edge: unordered list of edges connecting the vertices
   * 
   * Undirected graph means, that Edge(a, b) == Edge(b, a) are not allowed both
   * in edges at the same time. Some potions of this code will need 
   * to verify this invariant (eg. addEdge()). The same is valid for the
   * degree of vertices. Some generic, graph-centered functions may be
   * indifferent about degrees, whereas all polygon-related functions will
   * need to make sure that all vertices are adjacent to exactly two other
   * vertices (eg. getPoints() which essentially transforms the graph back
   * into a polygon, an ordered list of points).
   * 
   * One could argue that class inheritance is wrong here (some graphs are
   * polygons, not all polygons can be substituted with a graph), still this
   * may be very useful for 2-Opt moves and may provide better edge handling for
   * other algorithms as well.
   * 
   * TODO Think about this. Maybe a full graph implementation is better.
   */
  private List<Point> vertices;
  private List<Edge> edges;
   
  {
    vertices = new ArrayList<Point>();
    edges = new ArrayList<Edge>();
  }
  
  private void roundtripEdges() {
    edges = new ArrayList<Edge>();
    for(int i = 0; i < vertices.size(); i++)
      edges.add(new Edge(vertices.get(i), vertices.get((i + 1) % vertices.size())));
  }
  
  public GraphPolygon(List<Point> coords) {
    vertices = coords;
    roundtripEdges();
  }
  
  /**
   * Permutes the vertices and constructs a new polygon from the new
   * list.
   */
  public void permute() {
    Collections.shuffle(vertices);
    roundtripEdges();
  }
  
  /**
   * Returns a polygon.
   * 
   * TODO This does not check whether the graph actually is a polygon.
   * 
   * @return the polygon as a (implicitly cyclically linked) list of points
   */
  public List<Point> getPoints() {    
    List<Point> retval = new ArrayList<Point>(vertices.size());
    List<Edge> tmp = new ArrayList<Edge>(edges);
    
    // Loop through edges until we're back at start again.
    Point start = vertices.get(0);
    Point curr = start;
    do {
      retval.add(curr);
      
      // Find successor and mark edge as visited.
      for(Edge e : tmp) {
        if(e.a.equals(curr)) {
          curr = e.b;
          tmp.remove(e);
          break;
        } else if(e.b.equals(curr)) {
          curr = e.a;
          tmp.remove(e);
          break;
        }
      }
        
    } while(!curr.equals(start));
   
    return retval;
  }
  
  /**
   * @return a reference to the internal edges list
   */
  public List<Edge> getEdges() {
    return edges;
  }

  /**
   * Finds a random intersection in the polygon.
   * 
   * @return array of the two intersecting edges
   */
  public Edge[] findRandomIntersection() {
    List<Edge[]> le = new ArrayList<Edge[]>();
    
    // Find all intersections
    for(int i = 0; i < edges.size(); i++) {
      Edge a = edges.get(i);
      for(int j = i + 1; j < edges.size(); j++) {
        Edge b = edges.get(j);
        if(a.isIntersecting(b))
          le.add(new Edge[] {a, b});
      }
    }
    
    // Return a random one
    if(le.isEmpty())
      return null;
    else {
      Random r = new Random(System.currentTimeMillis());
      return le.get(r.nextInt(le.size()));
    }
  }

  /**
   * Removes an edge from the graph.
   */
  public void removeEdge(Edge e) {
    edges.remove(e);
  }

  /**
   * Adds an edge to the graph if does not contain the edge already.
   */
  public void addEdge(Edge e) {
    if(!edges.contains(e))
      edges.add(e);
  }
  
  
}
