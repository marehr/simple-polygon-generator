package polygonsSWP.gui.generation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileFilter;

import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGenerator.Parameters;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.gui.controls.GeneratorChooser;


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

  /* controls */
  private final GeneratorChooser cb_polygon_algorithm_chooser;
  private final JSpinner sl_edges;
  private final JButton b_load_points, b_generate_polygon, b_save_polygon;
  private final JRadioButton rb_polygonByUser, rb_polygonByGenerator;

  /* observers */
  private final List<PolygonGenerationPanelListener> observers;

  /* the current polygon! */
  private Polygon polygon = null;

  /* the list of user-selected points */
  private List<Point> points = null;

  public PolygonGenerationPanel(final PolygonGenerator[] generators) {
    observers = new LinkedList<PolygonGenerationPanelListener>();

    // init combobox
    cb_polygon_algorithm_chooser = new GeneratorChooser(generators, true);

    // init slider
    sl_edges = new JSpinner(new SpinnerNumberModel(5, 3, 1000, 1));

    // init buttons
    b_load_points = new JButton("Load Points");
    b_load_points.setEnabled(false);

    b_generate_polygon = new JButton("Generate");

    b_save_polygon = new JButton("Save");
    b_save_polygon.setEnabled(false);

    // init RadioButtons and Groups
    ButtonGroup polygon_menu = new ButtonGroup();
    rb_polygonByGenerator = new JRadioButton("Generate Points");
    rb_polygonByGenerator.setSelected(true);
    rb_polygonByUser = new JRadioButton("Set Points");
    polygon_menu.add(rb_polygonByGenerator);
    polygon_menu.add(rb_polygonByUser);

    // Layout controls on panel.
    layoutControls();

    // Register action listeners with buttons.
    registerListeners();
  }

  /* API */

  /**
   * Register an observer.
   * 
   * @param listener Class thats interested in polygon generation related events
   */
  public void addPolygonGenerationPanelListener(
      PolygonGenerationPanelListener listener) {
    observers.add(listener);
  }

  /**
   * Retrieve list of points used to generate the next polygon of. May be
   * manipulated by foreign classes (e.g. PaintPanel). TODO: Remove if not used.
   * 
   * @return List of initial points for next run or null if point generation
   *         mode is set to random.
   */
  public List<Point> getPoints() {
    if (rb_polygonByUser.isSelected()) return points;
    else return null;
  }

  /**
   * Retrieve generated polygon. TODO: Remove if not used.
   * 
   * @return the generated polygon or null if no polygon has been created.
   */
  public Polygon getPolygon() {
    return polygon;
  }

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

    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.gridwidth = 1;
    add(b_generate_polygon, gbc);

    gbc.gridx = 1;
    gbc.gridy = 3;
    add(b_save_polygon, gbc);
  }

  final private void registerListeners() {
    b_generate_polygon.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        runGenerator();
      }
    });

    /*
     * b_load_points.addActionListener(new ActionListener() { public void
     * actionPerformed(ActionEvent e) { JFrame f = new PolygonPointFrame(this);
     * f.setTitle("Set Polygon Points"); f.setSize(400, 300);
     * f.setLocationRelativeTo(null); f.setVisible(true); } });
     */

    b_save_polygon.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        savePolygon();
      }
    });

    // RadioButtons
    rb_polygonByGenerator.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent arg0) {
        b_load_points.setEnabled(false);
        sl_edges.setEnabled(true);
        b_generate_polygon.setEnabled(true);
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
        b_generate_polygon.setEnabled(true);
        cb_polygon_algorithm_chooser.switchPointGenerationMode(false);
        points = new LinkedList<Point>();
        emitPointGenerationModeSwitched(false, points);
      }
    });
  }

  protected void emitPolygonGenerationStarted() {
    for (PolygonGenerationPanelListener pgl : observers)
      pgl.onPolygonGenerationStarted();
  }

  protected void emitPolygonGenerated(Polygon p) {
    for (PolygonGenerationPanelListener pgl : observers)
      pgl.onPolygonGenerated(p);
  }

  protected void emitPointGenerationModeSwitched(boolean randomPoints,
      List<Point> points) {
    for (PolygonGenerationPanelListener pgl : observers)
      pgl.onPointGenerationModeSwitched(randomPoints, points);
  }

  /**
   * Called by polygon generation worker upon successful 
   * polygon generation. :)
   */
  @Override
  public void onFinished(Polygon polygon) {
    // TODO enable buttons
    b_save_polygon.setEnabled(true);

    this.polygon = polygon;
    emitPolygonGenerated(polygon);
  }
  
  /**
   * Checks parameters and starts a worker thread to generate
   * the polygon.
   */
  protected void runGenerator() {
    // TODO: check parameters again, but in a better way.
    // I disabled incapable algorithms in GeneratorChooser, but
    // if for example RPA was selected when "Set Points" was clicked,
    // RPA still stays selected.

    PolygonGenerator pg =
        (PolygonGenerator) cb_polygon_algorithm_chooser.getSelectedItem();

    assert (pg != null);

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
        return;
      }
    }

    // TODO think about order and disable buttons
    Thread t = new Thread(new PolygonGenerationWorker(pg, params, this));
    emitPolygonGenerationStarted();
    t.start();
  }
  
  /**
   * Takes care of user interaction through JFileChooser and writes
   * the polygon to a file.
   */
  protected void savePolygon() {
    assert(polygon != null);

    FileFilter svgFilter = new FileFilter() {
      @Override
      public boolean accept(File f) {
        return true;
      }
      @Override
      public String getDescription() {
        return ".svg (Scalable Vector Graphics)";
      }
    };
    FileFilter polygonFilter = new FileFilter() {
      @Override
      public boolean accept(File arg0) {
        return true;
      }
      @Override
      public String getDescription() {
        return ".polygon (Our own custom polygon format)";
      }
    };
    
    JFileChooser jfc = new JFileChooser();
    jfc.setDialogTitle("Save polygon as");
    jfc.setAcceptAllFileFilterUsed(false);
    jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    jfc.setMultiSelectionEnabled(false);
    jfc.addChoosableFileFilter(polygonFilter);
    jfc.addChoosableFileFilter(svgFilter);
    
    if(jfc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
      return;
       
    File f = jfc.getSelectedFile();
    FileOutputStream fos = null;
    OutputStreamWriter osw = null;
    
    if(!f.exists()) { 
      try {
        f.createNewFile();
      } catch (IOException e) {
        JOptionPane.showMessageDialog(this,
          "Could not create \"" + f.getName() + "\".", "Error",
          JOptionPane.ERROR_MESSAGE);
        return;
      }
    } else {
      int retval = JOptionPane.showConfirmDialog(this,
          "\"" + f.getName() + "\" exists. Overwrite?", "Overwrite?",
          JOptionPane.YES_NO_OPTION);
      if(retval == JOptionPane.NO_OPTION)
        return;
    }
    
    try {
      fos = new FileOutputStream(f);
    }
    catch (FileNotFoundException e) {
      // Should not happen, as file was created above.
      assert(false);
    }
    
    String data;
    if(jfc.getFileFilter().equals(svgFilter))
      data = polygon.toSVG();
    else
      data = polygon.toString();
    
    osw = new OutputStreamWriter(fos);
    
    try {
      osw.write(data);
    }
    catch (IOException e) {
      // Write error, most likely due to insufficient access rights.
      JOptionPane.showMessageDialog(this,
          "Could not write to \"" + f.getName() + "\".", "Error",
          JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    try {
      osw.close();
    }
    catch (IOException e) {
      // Could also be a write error.
      JOptionPane.showMessageDialog(this,
          "Could not write to \"" + f.getName() + "\".", "Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }
}
