/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author nospoon
 */
public class TableListModel extends DefaultTableModel {
    
    public List<Table> tables = null;
    
    void setData(List<Table> tables) {
        this.tables = tables;
    }
    
    public Object getValueAt(int row, int col) {
        if (tables==null) {
            return null;
        }
        if (row>=tables.size()) {
            return null;
        }    
        return tables.get(row);        
    }
    
    @Override
    public int getRowCount() {
         if (tables==null) {
            return 0;
        }
        return tables.size();
    }
    
    @Override
    public int getColumnCount() {
        return 1;
    }
}
