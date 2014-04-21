package layout;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
	  private JPanel _contentPanel;
	  private JLabel _statusBar;
	  private JButton _exitButton;
	  private LocalFileSystemPanel _lPanel;
	  
	  /**Construct the frame*/
	  public MainFrame() {
	    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
	    try {
	      init();
	    }
	    catch(Exception e) {
	      e.printStackTrace();
	    }
	  }

	  /**Component initialization*/
	  private void init() throws Exception  {
		_statusBar = new JLabel("Status: Ready");
		_exitButton = new JButton("exit");
		_lPanel = new LocalFileSystemPanel();
		  
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
