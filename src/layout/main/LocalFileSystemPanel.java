package layout.main;

import java.awt.Component;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/*
 * local file system panel
 */
public class LocalFileSystemPanel extends JPanel{
	private String[] ColumnNames = {"name","size","last time modified","latest"};
	private Object[][] fileinfo = {};
	
	private JLabel filesystem = new JLabel("Local");
	private JMenuBar menubar1 = new JMenuBar();
	private JMenu viewmenu = new JMenu("view");
	private JMenuItem detailview = new JMenuItem("detail");
	private JMenuItem iconview = new JMenuItem("icon");
	private JLabel pathlabel = new JLabel("path:");
	private JTable myfiletable;// FileTable to be implemented
	
	
	public LocalFileSystemPanel(){
	    try {
	        jbInit();
	      }
	      catch(Exception ex) {
	        ex.printStackTrace();
	      }
	}
	
	void jbInit() throws Exception {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.add(filesystem);
        filesystem.setAlignmentX(Component.LEFT_ALIGNMENT);

        viewmenu.add(detailview);
        viewmenu.add(iconview);
        menubar1.add(viewmenu);
        
        FileTable tablemodel = new FileTable();
        myfiletable = new JTable(tablemodel);
        
        tablemodel.addColumn("name");
        tablemodel.addColumn("size");
        tablemodel.addColumn("last time modified");
        tablemodel.addColumn("latest");
        
//        String path = "C:/Users/xmnan/Desktop/dropbox";
        String path = System.getProperty("user.dir");
        File folder = new File(path);
        File[] filelist = folder.listFiles();
        for (int i = 0;i< filelist.length;i++){
        	if(filelist[i].isFile())
        		tablemodel.addRow(new Object[]{filelist[i].getName(),filelist[i].length() + " bytes",new SimpleDateFormat().format(new Date(filelist[i].lastModified())),"yes"});
        	else if(filelist[i].isDirectory())
        		tablemodel.addRow(new Object[]{filelist[i].getName(),"","","yes"});
        }
        this.add(menubar1);
        menubar1.setAlignmentX(Component.LEFT_ALIGNMENT);
//        this.add(myfiletable);
//        myfiletable.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.add(pathlabel);
        pathlabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JScrollPane scrollPane = new JScrollPane(myfiletable);
        
        //Add the scroll pane to this panel.
        this.add(scrollPane);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.setVisible(true);
	}
}
