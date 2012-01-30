package polygonsSWP.gui.visualisation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import polygonsSWP.data.History;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.data.Scene;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.Trapezoid;
import polygonsSWP.gui.GUIModeListener;
import polygonsSWP.gui.generation.PointGenerationModeListener;
import polygonsSWP.gui.generation.PolygonGenerationPanelListener;


/**
 * TODO: describe me
 * 
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 */
public class PolygonView
  extends JPanel
  implements PolygonGenerationPanelListener, PointGenerationModeListener,
  GUIModeListener
{
  private static final long serialVersionUID = 1L;

  private final PaintPanel pp;
  private final JToolBar tb;
  private final JButton saveButton;
  private final JButton centerViewButton;
  private final JToggleButton trapezoidButton;
  private final VisualisationControl visControl;
  private PaintPanelStatusBar ppsb;

  private Polygon polygon;

  public PolygonView() {
    ppsb = new PaintPanelStatusBar();
    pp = new PaintPanel(ppsb);
    tb = new JToolBar();

    centerViewButton = new JButton("Center View");
    centerViewButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        pp.resetView();
      }
    });
    tb.add(centerViewButton);

    saveButton = new JButton("Save Polygon As...");
    saveButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        savePolygon();
      }
    });
    saveButton.setEnabled(false);
    tb.add(saveButton);

    trapezoidButton = new JToggleButton("Trapezoidation");
    trapezoidButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        trapezoidatePolygon();
      }
    });
    trapezoidButton.setEnabled(false);
    tb.add(trapezoidButton);

    visControl = new VisualisationControl(tb);
    visControl.addVisualisationControlListener(pp);

    layoutControls();
  }

  /* Layout helpers. */

  final private void layoutControls() {
    setLayout(new BorderLayout());

    add(tb, BorderLayout.PAGE_START);
    add(pp, BorderLayout.CENTER);
    add(ppsb, BorderLayout.PAGE_END);
  }

  /* PolygonGenerationPanelListener methods. */

  @Override
  public void
      onPolygonGenerationStarted(PolygonStatistics stats, History steps) {
    saveButton.setEnabled(false);
    trapezoidButton.setEnabled(true);
    trapezoidButton.setSelected(false);
    polygon = null;
    visControl.setHistory(steps);

    // add observer
    if (steps != null) steps.setHistoryListener(visControl);
  }

  @Override
  public void onPolygonGenerationCancelled() {
  }

  @Override
  public void onPolygonGenerated(Polygon newPolygon, PolygonStatistics stats,
      History history) {
    pp.setCurrentPolygon(newPolygon);
    visControl.setHistory(history);
    polygon = newPolygon;
    saveButton.setEnabled(true);
  }

  /* PointGenerationModeListener methods. */

  @Override
  public void onPointGenerationModeSwitched(boolean randomPoints,
      List<Point> points) {
    pp.setDrawMode(!randomPoints, points);
  }

  /* GUIModeListener methods. */

  @Override
  public void onGUIModeChanged(boolean generatorMode) {
    pp.setGUIinGenerationMode(generatorMode);
  }

  /**
   * Calls trapezoidation and displays trapezoids. this quick and dirty and
   * needs to be adapted to gui but dont remove it!
   */

  protected void trapezoidatePolygon() {
    assert (polygon != null);
    if (trapezoidButton.isSelected()) {
      List<Trapezoid> trapezoids = ((OrderedListPolygon) polygon).sweepLine();
      History hist = new History();
      Scene scene = hist.newScene().setBoundingBox(600, 600);
      for (Trapezoid item : trapezoids)
        scene.addPolygon(item, Color.DARK_GRAY);
      pp.onNewScene(scene);
    }
    else {

    }
  }

  /* Internals. */

  /**
   * Takes care of user interaction through JFileChooser and writes the polygon
   * to a file.
   */
  protected void savePolygon() {
    assert (polygon != null);

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

    if (jfc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

    File f = jfc.getSelectedFile();
    FileOutputStream fos = null;
    OutputStreamWriter osw = null;

    if (!f.exists()) {
      try {
        f.createNewFile();
      }
      catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Could not create \"" +
            f.getName() + "\".", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
    else {
      int retval =
          JOptionPane.showConfirmDialog(this, "\"" + f.getName() +
              "\" exists. Overwrite?", "Overwrite?", JOptionPane.YES_NO_OPTION);
      if (retval == JOptionPane.NO_OPTION) return;
    }

    try {
      fos = new FileOutputStream(f);
    }
    catch (FileNotFoundException e) {
      // Should not happen, as file was created above.
      assert (false);
    }

    String data;
    if (jfc.getFileFilter().equals(svgFilter)) data =
        visControl.getCurrentScene().toSvg();
    else data = polygon.toString();

    osw = new OutputStreamWriter(fos);

    try {
      osw.write(data);
    }
    catch (IOException e) {
      // Write error, most likely due to insufficient access rights.
      JOptionPane.showMessageDialog(this, "Could not write to \"" +
          f.getName() + "\".", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      osw.close();
    }
    catch (IOException e) {
      // Could also be a write error.
      JOptionPane.showMessageDialog(this, "Could not write to \"" +
          f.getName() + "\".", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }
}
