/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author nospoon
 */
public class TblTableListColumnModel extends DefaultTableColumnModel {
    TableColumn col = new TableColumn();

    public TblTableListColumnModel() {
        col.setPreferredWidth(300);
        col.setWidth(300);
    }

    @Override
    public int getColumnCount() {
        return 1;
    }
    
    @Override
    public TableColumn getColumn(int columnIndex) {
        // System.out.println("getColumn: " + columnIndex);
        return col;
        // return null; // return tableColumns.elementAt(columnIndex);
    }
    
}
