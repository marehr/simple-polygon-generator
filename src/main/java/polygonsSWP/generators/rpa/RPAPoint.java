package polygonsSWP.generators.rpa;

import polygonsSWP.geometry.Point;

public class RPAPoint
  extends Point
{
  public RPAPoint(double _x, double _y) {
    super(_x, _y);
    state = State.NEW;
  }
  
  public RPAPoint(Point point){
    super(point.x, point.y);
    state = State.OLD;
  }

  public enum State {OLD, NEW, DEL}
  
  public State state;
  
}
