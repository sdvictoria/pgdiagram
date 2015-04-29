/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class TblTableList extends JTable{
    
    TableListModel currentModel;
    TblTableListColumnModel currentColumnModel;
    TableCellRenderer rend;

    public TblTableList() {
        // super(new DefaultTableModel(), new TblTableListColumnModel());
        setAutoCreateColumnsFromModel(false);        
        setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
        currentColumnModel = new TblTableListColumnModel();
        currentModel = new TableListModel();                
        rend = new TblTableListCellRenderer();
        
        setColumnModel(columnModel);
        setModel(currentModel);
        setDefaultRenderer(Object.class, rend);
        
    }
    
    public TableColumnModel getColumnModel() {
        if (currentModel==null) {
            return super.getColumnModel();
        }
        else {
            return currentColumnModel;
        }
    }
    
    void setData(List<Table> tables) {                
        currentModel.setData(tables);
        currentModel.fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return currentModel.getRowCount();
    }
    
    @Override
    public int getColumnCount() {
        return currentModel.getColumnCount();
    }    

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
    
}
