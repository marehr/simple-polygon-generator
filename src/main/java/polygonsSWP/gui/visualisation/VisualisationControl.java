package polygonsSWP.gui.visualisation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

import polygonsSWP.data.History;
import polygonsSWP.data.Scene;
import polygonsSWP.data.listener.HistoryListener;
import polygonsSWP.gui.generation.HistorySceneChooser.HistorySceneMode;
import polygonsSWP.gui.generation.HistorySceneModeListener;

/**
 * Controls the step-by-step visualisation of the generator run. Provides play,
 * pause, step forward/backward controls.
 * 
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 */
class VisualisationControl implements HistoryListener, HistorySceneModeListener
{
  private final List<VisualisationControlListener> observers;

  /* Buttons. */
  private JButton b_first;
  private JButton b_prev;
  private JButton b_playpause;
  private JButton b_next;
  private JButton b_last;

  private boolean autoPlay = true;
  private boolean inPlayMode = false;
  private int framesFrecuency = 1000/25;
  private HistorySceneMode historySceneMode = HistorySceneMode.standard();

  final private Thread newSceneEmitterThread = new Thread(new Runnable() {

    @Override
    public void run() {
      while(true){
        long waitTime = inPlayMode ? framesFrecuency: 0;
        try {
          synchronized (newSceneEmitterThread) {
            newSceneEmitterThread.wait(waitTime);
          }
        } catch (InterruptedException e) {
        }

        synchronized (this) {
          if(inPlayMode){
            next();
          } else {
            if(autoPlay) last();
            else changeStates(true);
          }
        }
      }
    }
  });

  /**
   * observer methods
   */

  @Override
  public void onHistorySave(History history, Scene scene) {
    if(!autoPlay) return;

    synchronized (newSceneEmitterThread) {
      newSceneEmitterThread.notify();
    }
  }

  @Override
  synchronized public void onHistorySceneModeSwitched(HistorySceneMode mode) {
    historySceneMode = mode;
    setAutoPlay(autoPlay);
  }

  /* The history */
  private History history;
  private int sceneIdx;
  
  VisualisationControl(JToolBar toolbar) {
    observers = new LinkedList<VisualisationControlListener>();
    
    toolbar.add(new JSeparator(JSeparator.VERTICAL));
    
    b_playpause = new JButton("Play");
    b_playpause.setEnabled(false);
    toolbar.add(b_playpause);
    
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
    
    b_playpause.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        playOrPause();
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
  synchronized void setHistory(History ph) {
    if ( ph != null && ph == history ) {

      /*
       * if auto play is enabled, switch to last history scene
       * but if auto play is not enabled, the user is currently navigating
       * through the history scenes and therefore don't want to change the scene
       */
      if(autoPlay) last();

      /*
       * if we just create the history and don't show the results immediately
       * then this callback means, that an polygon was generated and we
       * should jump to the last scene, to show the completed polygon
       */
      if(historySceneMode.inCreateOnlyMode())
        last();

      return;
    }

    history = ph;
    if(history == null || history.getScenes().size() < 1) {
      // Disable anything.
      changeStates(false);
      
      // Remove last active scene from PaintPanel.
      emitNoScene();
      
    } else {
      // Initially show last scene.
      last();
    }
  }
  
  /**
   * @return the current scene
   */
  synchronized public Scene getCurrentScene() {
    return (history == null) ? null : history.getScenes().get(sceneIdx);
  }
  
  /* Helpers. */
  synchronized private void changeStates(boolean enable) {
    boolean first = false, prev = false, next = false, last = false;

    if(enable) {
      if(sceneIdx > 0) {
        first = true;
        prev = true;
      }

      if(sceneIdx < history.getScenes().size() - 1) {
        next = true;
        last = true;
      } else {
        inPlayMode = false;
      }

    } else {
      sceneIdx = 0;
      setAutoPlay(true);
      inPlayMode = false;
    }

    b_first.setEnabled(first);
    b_prev.setEnabled(prev);
    b_next.setEnabled(next);
    b_last.setEnabled(last);
    b_playpause.setEnabled(first || last);
    b_playpause.setText(inPlayMode ? "Pause" : "Play");
  }
  
  private void emitNoScene() {
    for(VisualisationControlListener vcl : observers)
      vcl.onNewScene(null);
  }
  
  synchronized private void emitCurrentScene() {
    for(VisualisationControlListener vcl : observers)
      vcl.onNewScene(history.getScenes().get(sceneIdx));
  }
  
  synchronized private void first() {
    sceneIdx = 0;
    setAutoPlay(false);
    changeStates(true);
    emitCurrentScene();
  }
  
  synchronized private void previous() {
    if(sceneIdx <= 0) return;

    sceneIdx--;
    setAutoPlay(false);
    changeStates(true);
    emitCurrentScene();
  }
  
  synchronized private void next() {
    if(sceneIdx >= history.getScenes().size() - 1) return;

    sceneIdx++;
    setAutoPlay(false);
    changeStates(true);
    emitCurrentScene();
  }
  
  synchronized private void last() {
    sceneIdx = history.getScenes().size() - 1;
    setAutoPlay(true);

    changeStates(true);
    emitCurrentScene();
  }
  
  synchronized private void playOrPause() {
    inPlayMode = !inPlayMode;

    changeStates(true);
    synchronized (newSceneEmitterThread) {
      newSceneEmitterThread.notify();
    }
  }

  private void setAutoPlay(boolean enable){
    autoPlay = enable && historySceneMode.inCreateAndShowMode();
  }
}
