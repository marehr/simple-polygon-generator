package polygonsSWP.generators.rpa;

import polygonsSWP.geometry.Point;

public class RPAPoint
  extends Point
{
  
  public RPAPoint(Point point){
    super(point.x, point.y);
    visVa = false;
    visVb = false;
    visVaIns = false;
    visVbIns = false;
    state = State.NEW;
  }
  
  public enum State {NEW, IN, OUT, DEL, BOTH}
  
  
  
  public boolean visVa;
  public boolean visVb;
  public boolean visVaIns;
  public boolean visVbIns;
  
  public State state;

  public void setState() {
    if (!visVa || !visVb) {
      state = State.DEL;
    }
    else if(visVaIns && visVbIns) {
      state = State.IN;
    }
    else {
      state = State.OUT;
    }
  }
  
}
