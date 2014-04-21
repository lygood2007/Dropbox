package client_layout;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import client.DropboxClient;

public class DropboxLayout {
	private boolean _packFrame = false;
	
	public DropboxLayout(DropboxClient client) {
		DropboxMainFrame frame = new DropboxMainFrame(client);
		//Validate frames that have preset sizes
		//Pack frames that have useful preferred size info, e.g. from their layout
		if (_packFrame) {
			frame.pack();
		}
		else {
			frame.validate();
		}
		
		//Center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);
	}
}
