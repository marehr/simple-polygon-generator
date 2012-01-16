package polygonsSWP.gui.visualisation;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import polygonsSWP.data.History;
import polygonsSWP.data.Scene;
import polygonsSWP.geometry.Point;


public class SVGPanel
  extends PaintPanel
{

  /**
   * 
   */
  private static final long serialVersionUID = -5371996989682220436L;
  private History polygonHistory = null;
  private Scene currentScene = null;

  public SVGPanel() {
    super();
    this.drawMode = false;
  }

  /* API */

  /*
   * Its not possible to draw in it, yet!
   * @see polygonsSWP.gui.visualisation.PaintPanel#setDrawMode(boolean,
   * java.util.List)
   */
  @Override
  void setDrawMode(boolean d, List<Point> p) {
    repaint();
  }

  /**
   * This methods sets the History Object you want to display.
   * 
   * @param history
   */
  void setHistory(History history) {
    polygonHistory = history;
  }

  /**
   * Display next scene.
   */
  void nextScene() {
    if (currentScene == null) currentScene = polygonHistory.getScenes().get(0);
    else {
      currentScene =
          polygonHistory.getScenes().get(
              (polygonHistory.getScenes().indexOf(currentScene) + 1) %
                  polygonHistory.getScenes().size());
    }
  }

  /**
   * Display previous scene.
   */
  void previousScene() {
    if (currentScene == null) currentScene =
        polygonHistory.getScenes().get(polygonHistory.getScenes().size() - 1);
    else {
      currentScene =
          polygonHistory.getScenes().get(
              (polygonHistory.getScenes().indexOf(currentScene) - 1) %
                  polygonHistory.getScenes().size());
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * polygonsSWP.gui.visualisation.PaintPanel#paintComponent(java.awt.Graphics)
   */
  @Override
  public void paintComponent(Graphics g) {
    // First of all initialize panel
    initPanel(g);
    // SVG class needs a Graphics2D but according to sun it is always safe to
    // cast Graphics to Graphics2D since Java 1.2+
    Graphics2D g2d = (Graphics2D) g;
    currentScene.paint(g2d, zoom, offsetX, offsetY);
  }
}
