package polygonsSWP.gui;
import java.awt.*;

import javax.swing.*;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class InfoFrame extends JFrame{

	private static final long serialVersionUID = 1L;
	private JTextArea textArea;
	private JLabel label;
	private JScrollPane scrollPane;
	private JButton button;
	
	public InfoFrame()
	{
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		label = new JLabel("Info:");
		button = new JButton("Open File");
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1,2));
		panel.add(label);
		panel.add(button);
		
		setLayout(new BorderLayout(5,5));
		add(panel,BorderLayout.NORTH);
		add(scrollPane,BorderLayout.CENTER);
		
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
