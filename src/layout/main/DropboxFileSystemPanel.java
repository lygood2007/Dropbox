package layout.main;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import layout.main.LocalFileSystemPanel.TableListener;

/*
 * Dropbox file system
 */
public class DropboxFileSystemPanel extends JPanel{
	private String[] ColumnNames = {"name","size","last time modified","latest"};
	private Object[][] fileinfo = {};
	private String rootpath = "C:\\";
	private String currentpath = rootpath;
	private ArrayList<File> files = new ArrayList<File>();
	private ArrayList<File> dirs = new ArrayList<File>();
	
	private JLabel filesystem = new JLabel("Local");
	private JMenuBar menubar1 = new JMenuBar();
	private JMenu viewmenu = new JMenu("view");
	private JMenuItem detailview = new JMenuItem("detail");
	private JMenuItem iconview = new JMenuItem("icon");
	private JLabel pathlabel = new JLabel("path:");
	private JTable myfiletable;// FileTable to be implemented
	private FileTable tablemodel = new FileTable();
	
	
	public DropboxFileSystemPanel(){
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
        
        myfiletable = new JTable(tablemodel);
        
        tablemodel.addColumn("name");
        tablemodel.addColumn("size");
        tablemodel.addColumn("last modified");
        tablemodel.addColumn("latest");
        
//        String path = "C:/Users/xmnan/Desktop/dropbox";
        File folder = new File(currentpath);
        File[] filelist = folder.listFiles();
        if (!filelist.equals(null)){
        	for (File currentfile : filelist){
        		if(currentfile.isFile())
        			files.add(currentfile);
        		else if(currentfile.isDirectory())
        			dirs.add(currentfile);
        	}
        	if(!currentpath.equals(rootpath))
        		tablemodel.addRow(new Object[]{"..","","","yes"});
        	for(int i = 0;i<dirs.size();i++){
        		tablemodel.addRow(new Object[]{dirs.get(i).getName(),"",new SimpleDateFormat().format(new Date(dirs.get(i).lastModified())),"yes"});
        	}
        	for(int i = 0;i<files.size();i++){
        		tablemodel.addRow(new Object[]{files.get(i).getName(),files.get(i).length() + " bytes",new SimpleDateFormat().format(new Date(files.get(i).lastModified())),"yes"});
        	}
        }
        else{
        	if(!currentpath.equals(rootpath))
        		tablemodel.addRow(new Object[]{"..","","","yes"});        	
        }
        myfiletable.addMouseListener(new TableListener());
        
        this.add(menubar1);
        menubar1.setAlignmentX(Component.LEFT_ALIGNMENT);
//        this.add(myfiletable);
//        myfiletable.setAlignmentX(Component.LEFT_ALIGNMENT);
        pathlabel.setText("path:" + currentpath);
        this.add(pathlabel);
        pathlabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JScrollPane scrollPane = new JScrollPane(myfiletable);
        
        //Add the scroll pane to this panel.
        this.add(scrollPane);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.setVisible(true);
	}
	private void updateTable(){
//        FileTable tablemodel = new FileTable();
//        myfiletable = new JTable(tablemodel);
        files = new ArrayList<File>();
        dirs = new ArrayList<File>();
        
        int rowCount=tablemodel.getRowCount();
        for (int i = rowCount - 1;i >= 0;i--) {
            tablemodel.removeRow(i);
        }
        
        File folder = new File(currentpath);
        File[] filelist = folder.listFiles();
        if (!filelist.equals(null)){
        	for (File currentfile : filelist){
        		if(currentfile.isFile())
        			files.add(currentfile);
        		else if(currentfile.isDirectory())
        			dirs.add(currentfile);
        	}
        	if(!currentpath.equals(rootpath))
        		tablemodel.addRow(new Object[]{"..","","","yes"});
        	for(int i = 0;i<dirs.size();i++){
        		tablemodel.addRow(new Object[]{dirs.get(i).getName(),"",new SimpleDateFormat().format(new Date(dirs.get(i).lastModified())),"yes"});
        	}
        	for(int i = 0;i<files.size();i++){
        		tablemodel.addRow(new Object[]{files.get(i).getName(),files.get(i).length() + " bytes",new SimpleDateFormat().format(new Date(files.get(i).lastModified())),"yes"});
        	}
        	pathlabel.setText("path:" + currentpath);
        }
        else{
        	if(!currentpath.equals(rootpath))
        		tablemodel.addRow(new Object[]{"..","","","yes"});        	
        }
	}
	
	private void setCurrentPath(String newpath){
		currentpath = newpath;
	}
	
	class MenuBarListener implements ActionListener{
		public void actionPerformed(ActionEvent ev){
			
		}
	}
	class TableListener extends MouseAdapter{
		   public void mouseClicked(MouseEvent e) {
			      if (e.getClickCount() == 2) {
			         JTable target = (JTable)e.getSource();
			         int row = target.getSelectedRow();
			         String currentfile = target.getModel().getValueAt(row, 0).toString();
			         File c_file = new File(currentpath);
			         if (currentfile.equals("..")){
			        	 setCurrentPath(c_file.getParent());
			        	 updateTable();
			        	 return;
			         }
			         String targetabsolutepath = currentpath + "\\" + currentfile;
			         File t_file = new File(targetabsolutepath);
			         if (t_file.isDirectory()){
			        	 setCurrentPath(targetabsolutepath);
			        	 updateTable();
			        	 return;
			         }
			         else{
			        	 //the user choose a file, do nothing
			         }
			   }
		   }
	}
}
