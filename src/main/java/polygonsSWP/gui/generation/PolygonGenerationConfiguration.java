package polygonsSWP.gui.generation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import polygonsSWP.data.History;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;
import polygonsSWP.geometry.Point;
import polygonsSWP.gui.generation.HistorySceneChooser.HistorySceneMode;
import polygonsSWP.util.GeneratorUtils;


class PolygonGenerationConfiguration
  extends JPanel
{
  private static final long serialVersionUID = 1L;

  /* Observers. */
  private final List<PointGenerationModeListener> pointGenerationModeListener;
  private final List<HistorySceneModeListener> historySceneModeListener;
  
  /* Controls. */
  private final JRadioButton rb_polygonByUser, rb_polygonByGenerator;
  private final GeneratorChooser cb_polygon_algorithm_chooser;
  private final HistorySceneChooser cb_historySceneChooser;
  private final JSpinner sp_edges, sp_size, sp_runs, sp_radius, sp_velocity;
  private final JButton b_load_points;
  private final JLabel lbl_size, lbl_runs, lbl_radius, lbl_velocity, lbl_historyScenes;
  
  /* the list of user-selected points */
  private List<Point> points = null;
  
  PolygonGenerationConfiguration(PolygonGeneratorFactory[] polygon_algorithm_list) {
    pointGenerationModeListener = new LinkedList<PointGenerationModeListener>();
    historySceneModeListener = new LinkedList<HistorySceneModeListener>();
    
    // init combobox
    cb_polygon_algorithm_chooser = new GeneratorChooser(polygon_algorithm_list, true);

    // init spinners
    sp_edges = new JSpinner(new SpinnerNumberModel(100, 3, 10000, 1));
    sp_size = new JSpinner(new SpinnerNumberModel(600, 1, 10000, 20));
    sp_runs = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 1));
    sp_runs.setEnabled(false);
    sp_radius = new JSpinner(new SpinnerNumberModel(150, 1, 10000, 10));
    sp_radius.setEnabled(false);
    sp_velocity = new JSpinner(new SpinnerNumberModel(15, 1, 10000, 1));
    sp_velocity.setEnabled(false);
    
    // init buttons
    b_load_points = new JButton("Load Points");
    b_load_points.setEnabled(false);
   
    // init labels
    lbl_size = new JLabel("Bounding box size");
    lbl_runs = new JLabel("Iterations");
    lbl_radius = new JLabel("Initial radius");
    lbl_velocity = new JLabel("Max. Velocity");
    
    // init RadioButtons and Groups
    ButtonGroup polygon_menu = new ButtonGroup();
    rb_polygonByGenerator = new JRadioButton("Generate Points");
    rb_polygonByGenerator.setSelected(true);
    rb_polygonByUser = new JRadioButton("Set Points");
    polygon_menu.add(rb_polygonByGenerator);
    polygon_menu.add(rb_polygonByUser);
    
    lbl_historyScenes = new JLabel("History Scenes");
    cb_historySceneChooser = new HistorySceneChooser();
    
    // Layout controls on panel.
    layoutControls();
    
    // Register ActionListeners with buttons.
    registerListeners();
  }
  
  /* Internals. */

  final private void layoutControls() {
    setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;

    gbc.gridx = 0;
    gbc.gridy = 0;
    add(rb_polygonByGenerator, gbc);

    gbc.gridx = 1;
    gbc.gridy = 0;
    add(sp_edges, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    add(rb_polygonByUser, gbc);

    gbc.gridx = 1;
    gbc.gridy = 1;
    add(b_load_points, gbc);
    
    gbc.gridx = 0;
    gbc.gridy = 2;
    add(lbl_size, gbc);
    
    gbc.gridx = 1;
    gbc.gridy = 2;
    add(sp_size, gbc);

    gbc.gridx = 0;
    gbc.gridy = 3;
    add(lbl_runs, gbc);
    
    gbc.gridx = 1;
    gbc.gridy = 3;
    add(sp_runs, gbc);
    
    gbc.gridx = 0;
    gbc.gridy = 4;
    add(lbl_radius, gbc);
    
    gbc.gridx = 1;
    gbc.gridy = 4;
    add(sp_radius, gbc);
    
    gbc.gridx = 0;
    gbc.gridy = 5;
    add(lbl_velocity, gbc);
    
    gbc.gridx = 1;
    gbc.gridy = 5;
    add(sp_velocity, gbc);
    
    gbc.gridx = 0;
    gbc.gridy = 6;
    gbc.gridwidth = 2;
    add(cb_polygon_algorithm_chooser, gbc);

    gbc.gridx = 0;
    gbc.gridy = 7;
    add(lbl_historyScenes, gbc);

    gbc.gridx = 1;
    gbc.gridy = 7;
    add(cb_historySceneChooser, gbc);
  }
  
  final private void registerListeners() {
    
    // Generator Combobox
    cb_polygon_algorithm_chooser.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        PolygonGeneratorFactory pgf = cb_polygon_algorithm_chooser.getSelectedItem();

        // Enable/Disable controls based on Generator parameterization.
        List<Parameters> addparams = pgf.getAdditionalParameters();
        sp_runs.setEnabled(addparams.contains(Parameters.runs));
        sp_radius.setEnabled(addparams.contains(Parameters.radius));
        sp_velocity.setEnabled(addparams.contains(Parameters.velocity));
        rb_polygonByUser.setEnabled(pgf.acceptsUserSuppliedPoints());
      }
      
    });

    // History Scene combobox
    cb_historySceneChooser.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        HistorySceneMode mode = cb_historySceneChooser.getSelectedItem();
        emitHistorySceneModeSwitched(mode);
      }
    });

    // Load points button
    b_load_points.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        PolygonPointFrame f = new PolygonPointFrame(true);
        List<Point> p;
        if((p = f.getPoints()) != null) {
          points = p;
          emitPointGenerationModeSwitched(false, points);
        }
      }
    });

    // RadioButtons
    rb_polygonByGenerator.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent arg0) {
        b_load_points.setEnabled(false);
        sp_edges.setEnabled(true);
        cb_polygon_algorithm_chooser.switchPointGenerationMode(true);
        points = null;
        emitPointGenerationModeSwitched(true, null);
      }
    });

    rb_polygonByUser.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent arg0) {
        b_load_points.setEnabled(true);
        sp_edges.setEnabled(false);
        cb_polygon_algorithm_chooser.switchPointGenerationMode(false);
        points = new LinkedList<Point>();
        emitPointGenerationModeSwitched(false, points);
      }
    });
  }  
  
  protected void emitPointGenerationModeSwitched(boolean randomPoints,
      List<Point> points) {
    for (PointGenerationModeListener pgml : pointGenerationModeListener)
      pgml.onPointGenerationModeSwitched(randomPoints, points);
  }

  protected void emitHistorySceneModeSwitched(HistorySceneMode mode) {
    for (HistorySceneModeListener pgml : historySceneModeListener)
      pgml.onHistorySceneModeSwitched(mode);
  }
  
  /* API */
  
  void onGeneratorMode() {
    if(rb_polygonByGenerator.isSelected()) {
      emitPointGenerationModeSwitched(true, null);
    } else {
      assert(points != null);
      emitPointGenerationModeSwitched(false, points);
    }
  }
  
  void addPointGenerationModeListener(PointGenerationModeListener listener) {
    pointGenerationModeListener.add(listener);
  }

  void addHistorySceneModeListener(HistorySceneModeListener listener) {
    historySceneModeListener.add(listener);
  }

  public int getBoundingBoxSize(){
    return (Integer) sp_size.getValue();
  }

  public PolygonGenerator createGenerator(PolygonStatistics stats,
      History steps,  Map<Parameters, Object> params) {

    PolygonGeneratorFactory pgf = (PolygonGeneratorFactory) cb_polygon_algorithm_chooser.getSelectedItem();
    
    int size = getBoundingBoxSize();
    params.put(Parameters.size, size);
    
    // Random points?
    if (rb_polygonByGenerator.isSelected()) {
      Integer edges = (Integer) sp_edges.getValue();

      // Sanity check: number of points should be far less than
      // area of bounding box.
      if((size * size) < (edges * 100)) {
        JOptionPane.showMessageDialog(null,
            "You have specified a too small bounding box. Please increase size.", "Error",
            JOptionPane.ERROR_MESSAGE);
        return null;
      }
      
      // If the algorithm also accepts user-defined set of points,
      // we generate that here in order to display it first.
      // This way, convexHull can display all points + the polygon.
      if(pgf.acceptsUserSuppliedPoints()) {
        
        try {
          // Temporarily add number of points parameter.
          params.put(Parameters.n, edges);

          // Create points.
          points = GeneratorUtils.createOrUsePoints(params, true);
          
          // Display points.
          emitPointGenerationModeSwitched(false, points);
          
          // Update params.
          params.remove(Parameters.n);
          params.put(Parameters.points, points);
          
        } catch (IllegalParameterizationException e) {
          // Can not happen.
          throw new RuntimeException(e);
        }
      } else {
        // clear the points in the gui
        points = null;
        emitPointGenerationModeSwitched(true, points);

        // If not, let the generator do the work (-> RPA, Velocity).
        params.put(Parameters.n, edges);
        
      }
      
    // User-supplied points?
    } else if (rb_polygonByUser.isSelected()) {
      assert (points != null);

      if (points.size() >= 3) {
        params.put(Parameters.points, points);
      }
      else {
        JOptionPane.showMessageDialog(null,
            "You have to specify at least three points.", "Error",
            JOptionPane.ERROR_MESSAGE);
        return null;
      }
    }
    
    if(sp_runs.isEnabled())
      params.put(Parameters.runs, (Integer) sp_runs.getValue());
    
    if(sp_radius.isEnabled())
      params.put(Parameters.radius, (Long) ((Number) sp_radius.getValue()).longValue());

    if(sp_velocity.isEnabled())
      params.put(Parameters.velocity, (Integer) sp_velocity.getValue());
    
    // Create generator instance.
    try {
      return pgf.createInstance(params, stats, steps);
    }
    catch (IllegalParameterizationException e) {
      JOptionPane.showMessageDialog(null,
          "Could not create PolygonGenerator.\n" +
          "Error was: " + e.getMessage() + "\n" + 
          "For parameter: " + e.getIllegalParameter(),
          "Parameterization error",
          JOptionPane.ERROR_MESSAGE);
      return null;
    }
  }
}
