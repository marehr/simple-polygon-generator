package polygonsSWP.gui.visualisation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JToolBar;

import polygonsSWP.data.History;
import polygonsSWP.data.PolygonHistory;
import polygonsSWP.data.Scene;
import polygonsSWP.data.listener.HistoryListener;

/**
 * Controls the step-by-step visualisation of the generator run. Provides play,
 * pause, step forward/backward controls.
 * 
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 */
class VisualisationControl implements HistoryListener
{
  private final List<VisualisationControlListener> observers;

  /* Buttons. */
  private JButton b_first;
  private JButton b_prev;
//  private JButton b_playpause;
  private JButton b_next;
  private JButton b_last;

  private boolean autoPlay = true;

  final private Thread newSceneEmitterThread = new Thread(new Runnable() {

    @Override
    public void run() {
      while(true){
        try {
          synchronized (newSceneEmitterThread) {
            newSceneEmitterThread.wait();
          }
        } catch (InterruptedException e) {
        }

        synchronized (this) {
          if(autoPlay) last();
          else changeStates(true);
        }
      }
    }
  });

  @Override
  public void onHistorySave(History history, Scene scene) {
    synchronized (newSceneEmitterThread) {
      newSceneEmitterThread.notify();
    }
  }

  /* The history */
  private PolygonHistory history;
  private int sceneIdx;
  
  VisualisationControl(JToolBar toolbar) {
    observers = new LinkedList<VisualisationControlListener>();
    
    b_first = new JButton("First");
    b_first.setEnabled(false);
    toolbar.add(b_first);
    
    b_prev = new JButton("Previous");
    b_prev.setEnabled(false);
    toolbar.add(b_prev);
    
    b_next = new JButton("Next");
    b_next.setEnabled(false);
    toolbar.add(b_next);
    
    b_last = new JButton("Last");
    b_last.setEnabled(false);
    toolbar.add(b_last);
    
    registerListeners();
    newSceneEmitterThread.start();
  }
  
  private void registerListeners() {
    b_first.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        first();
      }
    });
    
    b_prev.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        previous();
      }
    });
    
    b_next.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        next();
      }
    });
    
    b_last.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        last();
      }
    });
  }

  /* API. */
  
  void addVisualisationControlListener(VisualisationControlListener listener) {
    observers.add(listener);
  }

  /**
   * Set the history.
   * 
   * @param ph the polygon history object. May be null, in which case the
   *          controls should be disabled.
   */
  synchronized void setHistory(PolygonHistory ph) {
    history = ph;
    if(history == null || history.getScenes().size() < 1) {
      // Disable anything.
      changeStates(false);
      
      // Remove last active scene from PaintPanel.
      emitNoScene();
      
    } else {
      // Initially show last scene.
      last();
      
      // Enable anything.
      changeStates(true);
    }
  }
  
  /**
   * @return the current scene
   */
  public Scene getCurrentScene() {
    return (history == null) ? null : history.getScenes().get(sceneIdx);
  }
  
  /* Helpers. */
  synchronized private void changeStates(boolean enable) {
    boolean first, prev, next, last;
    
    if(enable) {
      if(sceneIdx > 0) {
        first = true;
        prev = true;
      } else {
        first = false;
        prev = false;
      }
      
      if(sceneIdx < history.getScenes().size() - 1) {
        next = true;
        last = true;
      } else {
        next = false;
        last = false;
      }
      
    } else {
      first = false;
      prev = false;
      next = false;
      last = false;
    }
    
    b_first.setEnabled(first);
    b_prev.setEnabled(prev);
    b_next.setEnabled(next);
    b_last.setEnabled(last);
  }
  
  private void emitNoScene() {
    for(VisualisationControlListener vcl : observers)
      vcl.onNewScene(null);
  }
  
  private void emitCurrentScene() {
    for(VisualisationControlListener vcl : observers)
      vcl.onNewScene(history.getScenes().get(sceneIdx));
  }
  
  synchronized private void first() {
    sceneIdx = 0;
    autoPlay = false;
    changeStates(true);
    emitCurrentScene();
  }
  
  synchronized private void previous() {
    sceneIdx--;
    autoPlay = false;
    changeStates(true);
    emitCurrentScene();
  }
  
  synchronized private void next() {
    sceneIdx++;
    autoPlay = false;
    changeStates(true);
    emitCurrentScene();
  }
  
  synchronized private void last() {
    sceneIdx = history.getScenes().size() - 1;
    autoPlay = true;
    changeStates(true);
    emitCurrentScene();
  }

}
