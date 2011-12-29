package polygonsSWP.gui.generation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
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

import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;
import polygonsSWP.generators.PolygonGeneratorFactory;
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
  private final JSpinner sp_edges, sp_size;
  private final JButton b_load_points;
  private final JLabel lbl_size;
  
  /* the list of user-selected points */
  private List<Point> points = null;
  
  PolygonGenerationConfiguration(PolygonGeneratorFactory[] polygon_algorithm_list) {
    observers = new LinkedList<PointGenerationModeListener>();
    
    // init combobox
    cb_polygon_algorithm_chooser = new GeneratorChooser(polygon_algorithm_list, true);

    // init spinners
    sp_edges = new JSpinner(new SpinnerNumberModel(5, 3, 1000, 1));
    sp_size = new JSpinner(new SpinnerNumberModel(600, 1, 10000, 20));
    
    // init buttons
    b_load_points = new JButton("Load Points");
    b_load_points.setEnabled(false);
   
    // init labels
    lbl_size = new JLabel("Bounding box size");
    
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
    gbc.gridwidth = 2;
    add(cb_polygon_algorithm_chooser, gbc);
  }
  
  final private void registerListeners() {
    
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
    for (PointGenerationModeListener pgml : observers)
      pgml.onPointGenerationModeSwitched(randomPoints, points);
  }
  
  /* API */
  
  void addPointGenerationModeListener(PointGenerationModeListener listener) {
    observers.add(listener);
  }

  PolygonGeneratorFactory getGeneratorFactory() {
    PolygonGeneratorFactory pgf =
      (PolygonGeneratorFactory) cb_polygon_algorithm_chooser.getSelectedItem();
    return pgf;
  }

  Map<Parameters, Object> getParameters() {
    // TODO: check parameters again, but in a better way.
    // I disabled incapable algorithms in GeneratorChooser, but
    // if for example RPA was selected when "Set Points" was clicked,
    // RPA still stays selected.
    Map<Parameters, Object> params = new HashMap<Parameters, Object>();

    Integer size = (Integer) sp_size.getValue();
    params.put(Parameters.size, size);

    if (rb_polygonByGenerator.isSelected()) {
      Integer edges = (Integer) sp_edges.getValue();
      params.put(Parameters.n, edges);
      
      // Sanity check: number of points should be far less than
      // area of bounding box.
      if((size * size) < (edges * 100)) {
        JOptionPane.showMessageDialog(null,
            "You have specified a too small bounding box. Please increase size.", "Error",
            JOptionPane.ERROR_MESSAGE);
        return null;
      }
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
