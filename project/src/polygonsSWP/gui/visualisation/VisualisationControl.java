package polygonsSWP.gui.visualisation;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JToolBar;

import polygonsSWP.data.PolygonHistory;


/**
 * Controls the step-by-step visualisation of the generator run. Provides play,
 * pause, step forward/backward controls.
 * 
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 */
class VisualisationControl
{
  private final List<VisualisationControlListener> observers;

  VisualisationControl(JToolBar toolbar) {
    observers = new LinkedList<VisualisationControlListener>();

    // TODO add buttons to toolbar, register actionlisteners...
  }

  void addVisualisationControlListener(VisualisationControlListener listener) {
    observers.add(listener);
  }

  /**
   * Set the history.
   * 
   * @param ph the polygon history object. May be null, in which case the
   *          controls should be disabled.
   */
  void setHistory(PolygonHistory ph) {
    // TODO
  }
}
