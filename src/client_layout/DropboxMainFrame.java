package client_layout;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import client.DropboxClient;


@SuppressWarnings("serial")
public class DropboxMainFrame extends JFrame {
	  private JPanel _contentPanel;
	  private JLabel _statusBar;
	  private JButton _exitButton;
	  private LocalFileSystemPanel _lPanel;
	  
	  private DropboxClient _client;
	  /**Construct the frame*/
	  public DropboxMainFrame(DropboxClient client) {
		  enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		  try {
			  _client = client;
			  initComp();
		  }
		  catch(Exception e) {
			  e.printStackTrace();
		  }
	  }

	  /**Component initialization*/
	  private void initComp() throws Exception  {
		  assert _client != null;
		  _statusBar = new JLabel("Status: Ready");
		  _exitButton = new JButton("exit");
		  _lPanel = new LocalFileSystemPanel(_client);

		  _contentPanel = (JPanel) this.getContentPane();
		  _contentPanel.setLayout(new BoxLayout(_contentPanel,BoxLayout.PAGE_AXIS));
		  this.setSize(new Dimension(500, 600));

		  this.setTitle("my Dropbox");
		  _exitButton.addActionListener(new ActionListener()  {
			  public void actionPerformed(ActionEvent e) {
				  System.exit(0);
			  }
		  });

		  _contentPanel.add(_lPanel);
		  _contentPanel.add(_statusBar);
		  _contentPanel.add(_exitButton);
	  }
	  public void statusDispUpdate(String newStatus){
		  _statusBar.setText("Status: " + newStatus);
	  }
}
