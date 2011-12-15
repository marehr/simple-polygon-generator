package polygonsSWP.gui.generation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGenerator.Parameters;
import polygonsSWP.geometry.Point;


class PolygonGenerationConfiguration
  extends JPanel
{
  private static final long serialVersionUID = 1L;

  /* Observers. */
  private final List<PointGenerationModeListener> observers;
  
  /* Controls. */
  private final JRadioButton rb_polygonByUser, rb_polygonByGenerator;
  private final GeneratorChooser cb_polygon_algorithm_chooser;
  private final JSpinner sl_edges;
  private final JButton b_load_points;
  
  /* the list of user-selected points */
  private List<Point> points = null;
  
  PolygonGenerationConfiguration(PolygonGenerator[] generators) {
    observers = new LinkedList<PointGenerationModeListener>();
    
    // init combobox
    cb_polygon_algorithm_chooser = new GeneratorChooser(generators, true);

    // init slider
    sl_edges = new JSpinner(new SpinnerNumberModel(5, 3, 1000, 1));
    
    // init buttons
    b_load_points = new JButton("Load Points");
    b_load_points.setEnabled(false);
   
    // init RadioButtons and Groups
    ButtonGroup polygon_menu = new ButtonGroup();
    rb_polygonByGenerator = new JRadioButton("Generate Points");
    rb_polygonByGenerator.setSelected(true);
    rb_polygonByUser = new JRadioButton("Set Points");
    polygon_menu.add(rb_polygonByGenerator);
    polygon_menu.add(rb_polygonByUser);
    
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
    add(sl_edges, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    add(rb_polygonByUser, gbc);

    gbc.gridx = 1;
    gbc.gridy = 1;
    add(b_load_points, gbc);

    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 2;
    add(cb_polygon_algorithm_chooser, gbc);
  }
  
  final private void registerListeners() {
    /*
     * b_load_points.addActionListener(new ActionListener() { public void
     * actionPerformed(ActionEvent e) { JFrame f = new PolygonPointFrame(this);
     * f.setTitle("Set Polygon Points"); f.setSize(400, 300);
     * f.setLocationRelativeTo(null); f.setVisible(true); } });
     */

    // RadioButtons
    rb_polygonByGenerator.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent arg0) {
        b_load_points.setEnabled(false);
        sl_edges.setEnabled(true);
        cb_polygon_algorithm_chooser.switchPointGenerationMode(true);
        points = null;
        emitPointGenerationModeSwitched(true, null);
      }
    });

    rb_polygonByUser.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent arg0) {
        b_load_points.setEnabled(true);
        sl_edges.setEnabled(false);
        cb_polygon_algorithm_chooser.switchPointGenerationMode(false);
        points = new LinkedList<Point>();
        emitPointGenerationModeSwitched(false, points);
      }
    });
  }  
  
  protected void emitPointGenerationModeSwitched(boolean randomPoints,
      List<Point> points) {
    for (PointGenerationModeListener pgml : observers)
      pgml.onPointGenerationModeSwitched(randomPoints, points);
  }
  
  /* API */
  
  void addPointGenerationModeListener(PointGenerationModeListener listener) {
    observers.add(listener);
  }

  PolygonGenerator getGenerator() {
    PolygonGenerator pg =
      (PolygonGenerator) cb_polygon_algorithm_chooser.getSelectedItem();
    return pg;
  }

  Map<Parameters, Object> getParameters() {
    // TODO: check parameters again, but in a better way.
    // I disabled incapable algorithms in GeneratorChooser, but
    // if for example RPA was selected when "Set Points" was clicked,
    // RPA still stays selected.
    Map<Parameters, Object> params = new HashMap<Parameters, Object>();

    // TODO: remove this hard code
    params.put(Parameters.size, 600);

    if (rb_polygonByGenerator.isSelected()) {
      params.put(Parameters.n, sl_edges.getValue());
    }
    else if (rb_polygonByUser.isSelected()) {
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
    
    return params;
  }
}
