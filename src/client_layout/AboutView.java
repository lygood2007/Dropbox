package client_layout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class AboutView extends JFrame{
	private JPanel _panel;
	private JLabel _aboutLabel;
	
	public AboutView(){
	    try {
	    	init();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void init() throws Exception{
		this.setSize(500, 300);
		this.setTitle("About");
		
		_panel = new JPanel();
		this.add(_panel);
		_panel.setLayout(null);

		_aboutLabel = new JLabel("TO BE IMPLEMENTED");
		_aboutLabel.setBounds(0, 0, 300, 100);
		_panel.add(_aboutLabel);
	}
}
