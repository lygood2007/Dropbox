package client_layout;

import javax.swing.table.DefaultTableModel;

public class FileTable extends DefaultTableModel{
	public boolean isCellEditable(int row,int column){
		return false;
	}
}
