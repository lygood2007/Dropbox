package layout.main;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class MainFrame extends JFrame {
	  private JPanel contentPane;

	  private JButton exitButton = new JButton("exit");
	  private FlowLayout myFlowLayout = new FlowLayout();
	  private JSplitPane split = new JSplitPane();
	  private LocalFileSystemPanel l_Panel = new LocalFileSystemPanel();
	  private DropboxFileSystemPanel d_Panel = new DropboxFileSystemPanel();
	  
	  /**Construct the frame*/
	  public MainFrame() {
	    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
	    try {
	      jbInit();
	    }
	    catch(Exception e) {
	      e.printStackTrace();
	    }
	  }

	  /**Component initialization*/
	  private void jbInit() throws Exception  {
	    contentPane = (JPanel) this.getContentPane();
	    contentPane.setLayout(myFlowLayout);
	    this.setSize(new Dimension(1000, 600));

	    this.setTitle("my Dropbox");
	    exitButton.addActionListener(new ActionListener()  {
	        public void actionPerformed(ActionEvent e) {
	          System.exit(0);
	        }
	      });

	      contentPane.add(l_Panel);
//	      contentPane.add(exitButton);
	      contentPane.add(d_Panel);
	  }
}
