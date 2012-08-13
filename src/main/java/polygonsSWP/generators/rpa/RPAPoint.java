package polygonsSWP.generators.rpa;

import polygonsSWP.geometry.Point;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
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
