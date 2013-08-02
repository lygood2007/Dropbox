package layout.main;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.UIManager;

public class Dropbox_main {
		private boolean packFrame = false;
	  /**Construct the application*/
	  public Dropbox_main() {
	    MainFrame frame = new MainFrame();
	    //Validate frames that have preset sizes
	    //Pack frames that have useful preferred size info, e.g. from their layout
	    if (packFrame) {
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
	    frame.setVisible(true);
	  }
	public static void main(String[] argc){
		
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e){
			e.printStackTrace();
		}
	    new Dropbox_main();
	    EventQueue.invokeLater(new Runnable(){
	    	public void run() {
	    	}
	    });
	}
	
}
