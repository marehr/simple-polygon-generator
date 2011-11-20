package polygonsSWP.gui;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
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
				FileDialog fd = new FileDialog(new Frame(),"Choose File",FileDialog.LOAD);
				fd.show();
				
				// TODO: OS dependent file reading
				//FileReader fr = new FileReader(new File(fd.getDirectory() + fd.getFile()));
			}
		});
		
		okbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<polygonsSWP.data.Point> pointList = new ArrayList<polygonsSWP.data.Point>();
				String pointString = textArea.getText();
				String [] a = pointString.split("\n");
				for (int i = 0; i < a.length; i++) {
					x = Integer.valueOf(a[i].split(" ")[0]);
					y = Integer.valueOf(a[i].split(" ")[1]);
					pointList.add(new polygonsSWP.data.Point(x,y));
				}
				main.setPoints(pointList);
				
			}
		});
		
	}
	
	public void addLine(String text)
	{
		String tmp = textArea.getText();
		textArea.setText(tmp +"\n"+text);
	}
	
	public void clearText()
	{
		textArea.setText("");
	}

}
