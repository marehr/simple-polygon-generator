package polygonsSWP.gui;
import java.awt.*;

import javax.swing.*;

public class PolygonPointFrame extends JFrame{

	private static final long serialVersionUID = 1L;
	private JTextArea textArea;
	private JLabel label;
	private JScrollPane scrollPane;
	private JButton okbutton = new JButton("OK");
	
	public PolygonPointFrame(JFrame frame)
	{
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		label = new JLabel("Set Points: <x coordinate><SPACE><y coordinate><return>");
		setLayout(new BorderLayout(5,5));
		add(label,BorderLayout.NORTH);
		add(scrollPane,BorderLayout.CENTER);
		add(okbutton,BorderLayout.SOUTH);
		
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
