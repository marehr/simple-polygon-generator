package polygonsSWP.gui;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;

import javax.swing.*;

public class PolygonPointFrame extends JFrame{

	private static final long serialVersionUID = 1L;
	private JTextArea textArea;
	private JLabel label;
	private JScrollPane scrollPane;
	private JButton okbutton = new JButton("OK");
	private JButton button;
	private int x,y;
	private MainFrame main;
	private JOptionPane notification;
	
	public PolygonPointFrame(MainFrame frame)
	{
		main = frame;
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
				FileDialog fd = new FileDialog(new Frame(),"Choose File",FileDialog.LOAD);
				fd.setVisible(true);
				
				File f = new File(fd.getDirectory() + File.separator + fd.getFile());
				try {
					BufferedReader br = new BufferedReader(new FileReader(f));
					String line = null;
					while((line = br.readLine()) != null)
					{			
						if(line.equals(""))
						  continue;
					  x = Integer.valueOf(line.split(" ")[0]);
						y = Integer.valueOf(line.split(" ")[1]);
						addLine(x + " " + y);
					}
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
					ArrayList<polygonsSWP.geometry.Point> pointList = new ArrayList<polygonsSWP.geometry.Point>();
					String pointString = textArea.getText();
					String [] a = pointString.split("\n");
					for (int i = 0; i < a.length; i++) {
						x = Integer.valueOf(a[i].split(" ")[0]);
						y = Integer.valueOf(a[i].split(" ")[1]);
						pointList.add(new polygonsSWP.geometry.Point(x,y));
					}
					if(pointList.size() >= 3)
					{
						main.setPoints(pointList);
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
