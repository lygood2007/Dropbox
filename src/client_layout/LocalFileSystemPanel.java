package client_layout;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import client.DropboxClient;
/*
 * local file system panel
 */
@SuppressWarnings("serial")
public class LocalFileSystemPanel extends JPanel{
	private String _currentpath;
	private String _rootpath;
	private ArrayList<File> _files = new ArrayList<File>();
	private ArrayList<File> _dirs = new ArrayList<File>();
	
	private JLabel _filesystem = new JLabel("Local File System");
	private JMenuBar _menubar = new JMenuBar();
	private JMenu _viewmenu = new JMenu("view");
	private JMenu _settingmenu = new JMenu("setting");
	private JMenu _helpmenu = new JMenu("help");
	private JMenuItem _detailMenuItem = new JMenuItem("detail");
	private JMenuItem _iconMenuItem = new JMenuItem("icon");
	private JMenuItem _loginMenuItem = new JMenuItem("login");
	private JMenuItem _helpMenuItem = new JMenuItem("help");
	private JMenuItem _aboutMenuItem = new JMenuItem("about");
	private JLabel _pathlabel = new JLabel("path:");
	private JTable _filetable;// FileTable to be implemented
	private FileTable _tablemodel = new FileTable();
	private DropboxClient _client;
	
	public LocalFileSystemPanel(DropboxClient client){
	    try {
	    	_client = client;
	        init();
	      }
	      catch(Exception ex) {
	        ex.printStackTrace();
	      }
	}
	
	void init() throws Exception {
		assert _client != null;
		_currentpath = _client.getClientRoot();
		_rootpath = "/"; // HOW TO DEAL WITH THIS in different platform!!
		
		_files = new ArrayList<File>();
		_dirs = new ArrayList<File>();
		
		_filesystem = new JLabel("Local File System");
		_menubar = new JMenuBar();
		_viewmenu = new JMenu("view");
		_settingmenu = new JMenu("setting");
		_helpmenu = new JMenu("help");
		_detailMenuItem = new JMenuItem("detail");
		_iconMenuItem = new JMenuItem("icon");
		_loginMenuItem = new JMenuItem("login");
		_helpMenuItem = new JMenuItem("help");
		_aboutMenuItem = new JMenuItem("about");
		_pathlabel = new JLabel("path:");
		_tablemodel = new FileTable();
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.add(_filesystem);
        _filesystem.setAlignmentX(Component.LEFT_ALIGNMENT);
        // generate menu items
        _viewmenu.add(_detailMenuItem);
        _viewmenu.add(_iconMenuItem);
        _settingmenu.add(_loginMenuItem);
        _helpmenu.add(_helpMenuItem);
        _helpmenu.add(_aboutMenuItem);
        
        _menubar.add(_viewmenu);
        _menubar.add(_settingmenu);
        _menubar.add(Box.createHorizontalGlue());
        _menubar.add(_helpmenu);
        
        // menu item functions
        _loginMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JFrame login = new LoginView();
                login.setLocationRelativeTo(null);
        	    login.setResizable(false);
        	    login.setVisible(true);
            }
        });
        _aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JFrame about = new AboutView();
                about.setLocationRelativeTo(null);
        	    //about.setResizable(false);
        	    about.setVisible(true);
            }
        });
        
        // generate file table
        _filetable = new JTable(_tablemodel);
        
        _tablemodel.addColumn("name");
        _tablemodel.addColumn("size");
        _tablemodel.addColumn("last modified");
        _tablemodel.addColumn("latest");
        
        File folder = new File(_currentpath);
        File[] filelist = folder.listFiles();
        if (!filelist.equals(null)){
        	for (File currentfile : filelist){
        		if(currentfile.isFile())
        			_files.add(currentfile);
        		else if(currentfile.isDirectory())
        			_dirs.add(currentfile);
        	}
        	if(!_currentpath.equals(_rootpath))
        		_tablemodel.addRow(new Object[]{"..","","","yes"});
        	for(int i = 0;i<_dirs.size();i++){
        		_tablemodel.addRow(new Object[]{_dirs.get(i).getName(),"",new SimpleDateFormat().format(new Date(_dirs.get(i).lastModified())),"yes"});
        	}
        	for(int i = 0;i<_files.size();i++){
        		_tablemodel.addRow(new Object[]{_files.get(i).getName(),_files.get(i).length() + " bytes",new SimpleDateFormat().format(new Date(_files.get(i).lastModified())),"yes"});
        	}
        }
        else{
        	if(!_currentpath.equals(_rootpath))
        		_tablemodel.addRow(new Object[]{"..","","","yes"});        	
        }
        _filetable.addMouseListener(new TableListener());
        
        this.add(_menubar);
        _menubar.setAlignmentX(Component.LEFT_ALIGNMENT);
        _pathlabel.setText("path:" + _currentpath);
        this.add(_pathlabel);
        _pathlabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JScrollPane scrollPane = new JScrollPane(_filetable);
        
        //Add the scroll pane to this panel.
        this.add(scrollPane);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.setVisible(true);
	}
	private void updateTable(){
        _files = new ArrayList<File>();
        _dirs = new ArrayList<File>();
        
        int rowCount=_tablemodel.getRowCount();
        for (int i = rowCount - 1;i >= 0;i--) {
            _tablemodel.removeRow(i);
        }
        
        File folder = new File(_currentpath);
        File[] filelist = folder.listFiles();
        if (!filelist.equals(null)){
        	for (File currentfile : filelist){
        		if(currentfile.isFile())
        			_files.add(currentfile);
        		else if(currentfile.isDirectory())
        			_dirs.add(currentfile);
        	}
        	if(!_currentpath.equals(_rootpath))
        		_tablemodel.addRow(new Object[]{"..","","","yes"});
        	for(int i = 0;i<_dirs.size();i++){
        		_tablemodel.addRow(new Object[]{_dirs.get(i).getName(),"",new SimpleDateFormat().format(new Date(_dirs.get(i).lastModified())),"yes"});
        	}
        	for(int i = 0;i<_files.size();i++){
        		_tablemodel.addRow(new Object[]{_files.get(i).getName(),_files.get(i).length() + " bytes",new SimpleDateFormat().format(new Date(_files.get(i).lastModified())),"yes"});
        	}
        	_pathlabel.setText("path:" + _currentpath);
        }
        else{
        	if(!_currentpath.equals(_rootpath))
        		_tablemodel.addRow(new Object[]{"..","","","yes"});        	
        }
	}
	
	private void setCurrentPath(String newpath){
		_currentpath = newpath;
	}
	

	class TableListener extends MouseAdapter{
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				JTable target = (JTable)e.getSource();
				int row = target.getSelectedRow();
				String currentfile = target.getModel().getValueAt(row, 0).toString();
				File c_file = new File(_currentpath);
				if (currentfile.equals("..")){
					setCurrentPath(c_file.getParent());
					updateTable();
					return;
				}
				String targetabsolutepath = _currentpath + System.getProperty("file.separator") + currentfile;
				File t_file = new File(targetabsolutepath);
				if (t_file.isDirectory()){
					setCurrentPath(targetabsolutepath);
					updateTable();
					return;
				}
				else{
					//the user choose a file, open with default program
					if (Desktop.isDesktopSupported()) {
						try{
							Desktop.getDesktop().open(t_file);
						}
						catch(IOException ex){
							ex.printStackTrace();
						}
					}
				}
			}
		}
	}
}


