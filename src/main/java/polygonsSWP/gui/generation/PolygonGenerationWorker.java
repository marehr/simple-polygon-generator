package polygonsSWP.gui.generation;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.geometry.Polygon;

class PolygonGenerationWorker
  implements Runnable
{
  private final PolygonGenerator pg;
  private final PolygonGenerationWorkerListener cb;
  private ImageIcon animation = new ImageIcon("load.gif");
  private JOptionPane opane;
  private JDialog dialog;
  
  PolygonGenerationWorker(PolygonGenerator generator, 
       PolygonGenerationWorkerListener callback) {
    pg = generator;
    cb = callback;
  }

  @Override
  public void run() {
	showDialog();
    Polygon p = pg.generate();
    if(p == null)
      cb.onCancelled();
    else
      cb.onFinished(p);
    dialog.setVisible(false);
  }
  
  private void showDialog()
  {
	    opane = new JOptionPane("",JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE,animation,new Object[] {},null);
	    dialog = new JDialog();   
	    dialog.addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	            pg.stop();
	        }
	    });
	    dialog.setTitle("Generating ...");
	    dialog.setBackground(Color.white);
	    dialog.setSize(200,200);
	    dialog.setLocationRelativeTo(null);
	    dialog.add(opane);
	    dialog.setVisible(true);
  }

  public void stop() {
    pg.stop();
  }
}
