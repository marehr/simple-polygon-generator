package polygonsSWP.gui.generation;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;
import polygonsSWP.geometry.Polygon;


/**
 * Panel which controls the polygon generation.
 * 
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <sebastianthobe@googlemail.com>
 */
public class PolygonGenerationPanel
  extends JPanel
  implements PolygonGenerationWorkerListener
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

  /* Internals. */

  protected void emitPolygonGenerationStarted() {
    for (PolygonGenerationPanelListener pgl : observers)
      pgl.onPolygonGenerationStarted();
  }

  protected void emitPolygonGenerationCanceled() {
    for (PolygonGenerationPanelListener pgl : observers)
      pgl.onPolygonGenerationCancelled();
  }
  
  protected void emitPolygonGenerated(Polygon p) {
    for (PolygonGenerationPanelListener pgl : observers)
      pgl.onPolygonGenerated(p, null);
  }


  /**
   * Called by polygon generation worker upon successful 
   * polygon generation. :)
   */
  @Override
  public void onFinished(Polygon polygon) {
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
  
  /**
   * Checks parameters and starts a worker thread to generate
   * the polygon.
   */
  protected void runGenerator() {
    PolygonGeneratorFactory pgf = p_generator_config.getGeneratorFactory();
    Map<Parameters, Object> params = p_generator_config.getParameters();
    if(pgf == null || params == null)
      return;

    PolygonGenerator pg = null;
    try {
      pg = pgf.createInstance(params, null);
    }
    catch (IllegalParameterizationException e) {
      JOptionPane.showMessageDialog(null,
          "Could not create PolygonGenerator.\n" +
          "Error was: " + e.getMessage() + "\n" + 
          "For parameter: " + e.getIllegalParameter(),
          "Parameterization error",
          JOptionPane.ERROR_MESSAGE);  
      return;
    }
    
    worker = new PolygonGenerationWorker(pg, this);
    t = new Thread(worker);
    b_generate_polygon.setText("Cancel");
    
    // TODO think about order
    emitPolygonGenerationStarted();
    t.start();
  }
  
  /**
   * Stops the worker thread.
   */
  protected void stopGenerator() {
    worker.stop();
  }
}