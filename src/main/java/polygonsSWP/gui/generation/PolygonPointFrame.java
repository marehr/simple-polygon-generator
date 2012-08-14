package polygonsSWP.gui.generation;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import polygonsSWP.geometry.Point;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class PolygonPointFrame extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTextArea textArea;
	private JLabel label;
	private JScrollPane scrollPane;
	private JButton okbutton = new JButton("OK");
	private JButton button;
	private double x,y;
  protected List<Point> pointList;
	
	public PolygonPointFrame(boolean modal)
	{	  
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		label = new JLabel("Set Points: x <SPACE> y");
		button = new JButton("Open File");
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1,2));
		panel.add(label);
		panel.add(button);
			
		setLayout(new BorderLayout(5,5));
		add(panel,BorderLayout.NORTH);
		add(scrollPane,BorderLayout.CENTER);
		add(okbutton,BorderLayout.SOUTH);
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearText();
				FileDialog fd = new FileDialog(new Frame(), "Choose File",FileDialog.LOAD);
				fd.setVisible(true);
				
				File f = new File(fd.getDirectory() + File.separator + fd.getFile());
				try {
					BufferedReader br = new BufferedReader(new FileReader(f));
					String line = null;
					StringBuilder pointString = new StringBuilder("");
					while((line = br.readLine()) != null)
					{			
						if(line.equals(""))
						  continue;
						pointString.append(line + "\n");
					}
					addLine(pointString.toString());
				} catch (Exception e1) {
					JOptionPane.showMessageDialog (null, "An error occured while reading the file.\nYou may should check the point format.", "Error", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
		});
		
		okbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try
				{
					pointList = new ArrayList<Point>();
					String pointString = textArea.getText();
					String [] a = pointString.split("\n");
					for (int i = 0; i < a.length; i++) {
						x = Double.valueOf(a[i].split(" ")[0]);
						y = Double.valueOf(a[i].split(" ")[1]);
						pointList.add(new polygonsSWP.geometry.Point(x,y));
					}
					if(pointList.size() >= 3)
					{
						JOptionPane.showMessageDialog (null, "Points have been set successfully", "Notification", JOptionPane.PLAIN_MESSAGE);
						hide_self();
					}
					else
					{
						//TODO: dirty
						throw new Exception();
					}
				}catch(Exception e1)
				{
					e1.printStackTrace();
					JOptionPane.showMessageDialog (null, "An error occured while parsing the points.\nYou may should check the point format.", "Error", JOptionPane.ERROR_MESSAGE);
				}				
			}
		});
		
    setTitle("Load polygon points");
    setSize(400, 300);
    setLocationRelativeTo(null);
    setModal(modal);
    setVisible(true);
	}
	
	public List<Point> getPoints() {
	  return pointList;
	}
	
	private void hide_self() {
		this.setVisible(false);
	}
	
	public void addLine(String text)
	{
		String tmp = textArea.getText();
		textArea.setText(tmp + text + "\n");
	}
		
	public void clearText()
	{
		textArea.setText("");
	}

}
