package polygonsSWP.generators.rpa;

import polygonsSWP.geometry.Point;

public class RPAPoint
  extends Point
{
  
  public RPAPoint(Point point){
    super(point.x, point.y);
    state = State.NN;
  }
  
  public enum VisInOut {FROMINSIDE, FROMOUTSIDE, BOTH, NONE}
  public enum State {NN, IN, OUT, DEL, BOTH}
  
  
  
  public boolean visVa;
  public boolean visVb;
  public VisInOut visInOutVa;
  public VisInOut visInOutVb;
  
  public State state;
  
}
