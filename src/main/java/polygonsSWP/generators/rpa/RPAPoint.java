package polygonsSWP.generators.rpa;

import polygonsSWP.geometry.Point;

public class RPAPoint
  extends Point
{
  
  public RPAPoint(Point point){
    super(point.x, point.y);
    visVa = false;
    visVb = false;
    visInOutVa = VisInOut.NONE;
    visInOutVb = VisInOut.NONE;
    state = State.NN;
  }
  
  public enum VisInOut {FROMINSIDE, FROMOUTSIDE, BOTH, NONE}
  public enum State {NN, IN, OUT, DEL, BOTH}
  
  
  
  public boolean visVa;
  public boolean visVb;
  public VisInOut visInOutVa;
  public VisInOut visInOutVb;
  
  public State state;

  public void setState() {
    if (!visVa || !visVb) {
      state = State.DEL;
    }
    else if (visInOutVa == VisInOut.BOTH && visInOutVb == VisInOut.BOTH){
      state = State.BOTH;
    }
    else if ((visInOutVa == VisInOut.FROMINSIDE || visInOutVa == VisInOut.BOTH) && 
             (visInOutVb == VisInOut.FROMINSIDE || visInOutVb == VisInOut.BOTH)){
      state = State.IN;
    }
    else {
      state = State.OUT;
    }
  }
  
  public boolean fromVa(){
    if (visInOutVa == VisInOut.FROMINSIDE || visInOutVa == VisInOut.BOTH){
      return true;
    }
    else return false;
  }
  
  public boolean fromVb(){
    if (visInOutVb == VisInOut.FROMINSIDE || visInOutVb == VisInOut.BOTH){
      return true;
    }
    else return false;
  }
  
}
