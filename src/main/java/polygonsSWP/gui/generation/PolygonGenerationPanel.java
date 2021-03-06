package polygonsSWP.gui.generation;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;

import polygonsSWP.data.History;
import polygonsSWP.data.LastHistory;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.gui.GUIModeListener;
import polygonsSWP.gui.generation.HistorySceneChooser.HistorySceneMode;


/**
 * Panel which controls the polygon generation.
 * 
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class PolygonGenerationPanel
  extends JPanel
  implements PolygonGenerationWorkerListener, GUIModeListener,
  HistorySceneModeListener
{
  private static final long serialVersionUID = 1L;

  /* Controls */
  private final PolygonGenerationConfiguration p_generator_config;
  private final JButton b_generate_polygon;

  /* Observers */
  private final List<PolygonGenerationPanelListener> observers;

  /* Generation worker thread. */
  private Thread t;
  private PolygonGenerationWorker worker;
  private History steps;
  private PolygonStatistics stats;
  private Map<Parameters, Object> params;

  private HistorySceneMode historySceneMode = HistorySceneMode.standard();

  public PolygonGenerationPanel(final PolygonGeneratorFactory[] polygon_algorithm_list) {
    observers = new LinkedList<PolygonGenerationPanelListener>();

    // Initialize generator configuration panel.
    p_generator_config = new PolygonGenerationConfiguration(polygon_algorithm_list);
    
    // Initialize generate button.
    b_generate_polygon = new JButton("Generate");
    b_generate_polygon.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(t == null)
          runGenerator();
        else
          stopGenerator();
      }
    });

    // Layout controls
    setLayout(new BorderLayout());
    add(p_generator_config, BorderLayout.CENTER);
    add(b_generate_polygon, BorderLayout.PAGE_END);

    addHistorySceneModeListener(this);
  }

  /* API */

  /**
   * Register an PolygonGenerationPanel observer.
   * 
   * @param listener Class that's interested in polygon generation related events
   */
  public void addPolygonGenerationPanelListener(
      PolygonGenerationPanelListener listener) {
    observers.add(listener);
  }
  
  /**
   * Register an PointGenerationModeListener.
   * 
   * @param listener Class that would like to be notified when the point generation
   *                 mode has changed.
   */
  public void addPointGenerationModeListener(PointGenerationModeListener listener) {
    // Redirect to generator configuration panel.
    p_generator_config.addPointGenerationModeListener(listener);
  }

  /**
   * Register an HistorySceneModeListener.
   * 
   * @param listener Class that would like to be notified when the history scene
   *                 mode has changed.
   */
  public void addHistorySceneModeListener(HistorySceneModeListener listener) {
    // Redirect to generator configuration panel.
    p_generator_config.addHistorySceneModeListener(listener);
  }

  /* Internals. */

  protected void emitPolygonGenerationStarted() {
    for (PolygonGenerationPanelListener pgl : observers)
      pgl.onPolygonGenerationStarted(stats, steps, params);
  }

  protected void emitPolygonGenerationCanceled() {
    for (PolygonGenerationPanelListener pgl : observers)
      pgl.onPolygonGenerationCancelled();
  }
  
  protected void emitPolygonGenerated(Polygon p) {
    for (PolygonGenerationPanelListener pgl : observers)
      pgl.onPolygonGenerated(p, stats, steps, params);
  }

  @Override
  public void onHistorySceneModeSwitched(HistorySceneMode mode) {
    historySceneMode = mode;
  }

  /**
   * Called by polygon generation worker upon successful 
   * polygon generation. :)
   */
  @Override
  public void onFinished(Polygon polygon) {
//    stats.stop();

    b_generate_polygon.setText("Generate");
    t = null;
    emitPolygonGenerated(polygon);
  }
  
  @Override
  public void onCancelled() {
    b_generate_polygon.setText("Generate");
    t = null;
    emitPolygonGenerationCanceled();
  }
  
  @Override
  public void onGUIModeChanged(boolean generatorMode) {
    if(generatorMode)
      p_generator_config.onGeneratorMode();
  }
  
  /**
   * Creates a generator and starts a worker thread to generate
   * the polygon.
   */
  protected void runGenerator() {
    int boundingBox = p_generator_config.getBoundingBoxSize();
    steps = null;

    if(historySceneMode.shouldHistoryBeCreated())
      steps = new History(boundingBox);

    if(historySceneMode.onlyShowLastScene())
      steps = new LastHistory(boundingBox);

    params = new HashMap<Parameters, Object>();
    stats = new PolygonStatistics();
    PolygonGenerator pg = p_generator_config.createGenerator(stats, steps, params);

    if(pg == null){
      steps = null;
      params = null;
      stats = null;
      return;
    }

    worker = new PolygonGenerationWorker(pg, this);
    t = new Thread(worker);
    b_generate_polygon.setText("Cancel");

    // TODO think about order
    emitPolygonGenerationStarted();

//    stats.start();
    t.start();
  }
  
  /**
   * Stops the worker thread.
   */
  protected void stopGenerator() {
    worker.stop();
  }
}
