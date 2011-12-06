package polygonsSWP.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.generators.*;
import polygonsSWP.generators.PolygonGenerator.Parameters;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;


// TODO: rewrite.
public class MainFrame
  extends JFrame
{

  private static final long serialVersionUID = 313119639927682997L;

  private final int DEFAULTSIZE = 600;

  // main components
  private PaintPanel _canvas = new PaintPanel();
  private InfoFrame infoframe;

  // menu components
  private GeneratorChooser cb_polygon_algorithm_chooser;

  private PolygonGenerator[] polygon_algorithm_list = { new PermuteAndReject(),
      new TwoOptMoves(), new RandomPolygonAlgorithm(), new SpacePartitioning(),
      new IncrementalConstructionAndBacktracking(), new ConvexHullGenerator() };

  private JButton b_load_points, b_generate_polygon, b_calc_shortest_path,
      b_save_polygon;
  private JSpinner sl_edges;

  private ButtonGroup bg_shortest_path, polygon_menu;
  private JRadioButton rb_set_points, rb_generate_points, rb_polygonByUser,
      rb_polygonByGenerator;

  // panels
  private JPanel p_polygon_generation, p_shortest_path, p_menu;

  // class variables

  public static void main(String[] args) {
    JFrame frame = new MainFrame();
    frame.setTitle("PolygonGen");
    frame.setSize(1000, 650);
    frame.setBackground(Color.white);
    frame.setLocationRelativeTo(null); // center window
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    frame.setVisible(true);
  }

  public MainFrame() {
    // init combobox

    cb_polygon_algorithm_chooser =
        new GeneratorChooser(polygon_algorithm_list, true);

    // init slider

    sl_edges = new JSpinner(new SpinnerNumberModel(5, 3, 1000, 1));

    // init buttons

    b_calc_shortest_path = new JButton("Calculate Shortest Path");
    b_load_points = new JButton("Load Points");
    b_load_points.setEnabled(false);
    b_generate_polygon = new JButton("Generate Polygon");
    b_save_polygon = new JButton("Save Polygon");

    // init RadioButtons and Groups
    polygon_menu = new ButtonGroup();
    rb_polygonByGenerator = new JRadioButton("Generate");
    rb_polygonByGenerator.setSelected(true);
    rb_polygonByUser = new JRadioButton("Set Points");
    polygon_menu.add(rb_polygonByGenerator);
    polygon_menu.add(rb_polygonByUser);

    bg_shortest_path = new ButtonGroup();
    rb_generate_points = new JRadioButton("Generate Points");
    rb_generate_points.setSelected(true);
    rb_set_points = new JRadioButton("Set Points");
    bg_shortest_path.add(rb_generate_points);
    bg_shortest_path.add(rb_set_points);

    // building interface

    // First part: polygon generation

    p_polygon_generation = new JPanel();
    p_polygon_generation.setLayout(new GridBagLayout());
    p_polygon_generation.setBorder(BorderFactory.createTitledBorder("Polygon Generation"));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;

    gbc.gridx = 0;
    gbc.gridy = 0;
    p_polygon_generation.add(rb_polygonByGenerator, gbc);

    gbc.gridx = 1;
    gbc.gridy = 0;
    p_polygon_generation.add(sl_edges, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    p_polygon_generation.add(rb_polygonByUser, gbc);

    gbc.gridx = 1;
    gbc.gridy = 1;
    p_polygon_generation.add(b_load_points, gbc);

    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 2;
    p_polygon_generation.add(cb_polygon_algorithm_chooser, gbc);

    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.gridwidth = 1;
    p_polygon_generation.add(b_generate_polygon, gbc);

    gbc.gridx = 1;
    gbc.gridy = 3;
    p_polygon_generation.add(b_save_polygon, gbc);

    // Second part: Shortest path configuration

    p_shortest_path = new JPanel();
    p_shortest_path.setLayout(new GridBagLayout());
    p_shortest_path.setBorder(BorderFactory.createTitledBorder("Shortest Path"));

    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    p_shortest_path.add(rb_generate_points, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    p_shortest_path.add(rb_set_points, gbc);

    gbc.gridx = 0;
    gbc.gridy = 2;
    p_shortest_path.add(b_calc_shortest_path, gbc);

    p_menu = new JPanel();
    p_menu.setLayout(new GridLayout(2, 1));
    p_menu.add(p_polygon_generation);
    p_menu.add(p_shortest_path);

    // adding all panels to main window

    setLayout(new BorderLayout(5, 5));
    add(p_menu, BorderLayout.WEST);
    add(_canvas, BorderLayout.CENTER);

    // action listener

    // Combobox

    cb_polygon_algorithm_chooser.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // TODO react on PolygonGenerator accepted parameters
        // remember deactivateNonSupportedParamComponents
      }

    });

    // Buttons

    b_calc_shortest_path.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // method stubs
      }
    });

    b_generate_polygon.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (checkParamCombination()) {
          runGenerator();
        }
      }
    });

    b_load_points.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cb_polygon_algorithm_chooser.getSelectedItem();
        JFrame f = new PolygonPointFrame(MainFrame.this);
        f.setTitle("Set Polygon Points");
        f.setSize(400, 300);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
      }
    });

    b_save_polygon.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (_canvas.getPolygon() != null) {
          FileDialog fd =
              new FileDialog(new Frame(), "Save Polygon To", FileDialog.SAVE);
          fd.setVisible(true);
          if (fd.getDirectory() != null)
            savePolygonToFile(fd.getDirectory() + File.separator + fd.getFile());
        }
        else {
          JOptionPane.showMessageDialog(null, "There is no polygon to save",
              "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    });

    // RadioButtons

    rb_polygonByGenerator.addMouseListener(new MouseListener() {
      public void mouseReleased(MouseEvent arg0) {
      }

      public void mousePressed(MouseEvent arg0) {
      }

      public void mouseExited(MouseEvent arg0) {
      }

      public void mouseEntered(MouseEvent arg0) {
      }

      public void mouseClicked(MouseEvent arg0) {
        _canvas.setDrawMode(false);
        b_load_points.setEnabled(false);
        sl_edges.setEnabled(true);
        b_generate_polygon.setEnabled(true);
        cb_polygon_algorithm_chooser.switchPointGenerationMode(true);
      }
    });

    rb_polygonByUser.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent arg0) {
        _canvas.setPoints(new ArrayList<Point>());
        _canvas.setDrawMode(true);
        b_load_points.setEnabled(true);
        sl_edges.setEnabled(false);
        b_generate_polygon.setEnabled(true);
        cb_polygon_algorithm_chooser.switchPointGenerationMode(false);
      }
    });
  }

  // we may dont need this method
  protected boolean checkParamCombination() {
    return true;
  }

  public void setPoints(List<Point> points) {
    _canvas.setPoints(points);
  }

  private void runGenerator() {
    PolygonGenerator pg =
        (PolygonGenerator) cb_polygon_algorithm_chooser.getSelectedItem();
    
    assert(pg != null);
    
    Map<Parameters, Object> params = new HashMap<Parameters, Object>();

    // TODO: remove this hard code
    params.put(Parameters.size, DEFAULTSIZE);

    // TODO: check parameters again, but in a better way.
    // I disabled incapable algorithms in GeneratorChooser, but
    // if for example RPA was selected when "Set Points" was clicked,
    // RPA still stays selected.
    
    if (rb_polygonByGenerator.isSelected()) {
      params.put(Parameters.n, sl_edges.getValue());
      Polygon p = pg.generate(params, null);
      _canvas.setPolygon(p);
    }
    else if (rb_polygonByUser.isSelected()) {
      List<Point> points = _canvas.getPoints();
      if (points.size() >= 3) {
        params.put(Parameters.points, points);
        Polygon p = pg.generate(params, null);
        _canvas.setPolygon(p);
      }
      else {
        JOptionPane.showMessageDialog(null,
            "You have to specify at least three points.", "Error",
            JOptionPane.ERROR_MESSAGE);
      }
    }   
  }

  private void savePolygonToFile(String filePath) {
    File f = new File(filePath);
    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter(f));
      List<Point> plist = _canvas.getPolygon().getPoints();
      for (int i = 0; i < plist.size(); i++) {
        Point p = plist.get(i);
        bw.write(p.x + " " + p.y + "\n");
      }
      bw.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void generateInfo() {
    infoframe.clearText();
    infoframe.setVisible(true);
    infoframe.addLine("Polygon Edges:");
    infoframe.addLine("-------------");
    infoframe.addLine("|    X     |     Y    |");
    infoframe.addLine("-------------");

    infoframe.addLine("TEST");

  }
}
