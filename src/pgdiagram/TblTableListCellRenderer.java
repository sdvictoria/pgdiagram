/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.Color;
import java.awt.Component;
import java.io.Serializable;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author nospoon
 */
public class TblTableListCellRenderer extends JLabel implements TableCellRenderer, Serializable {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Color fg = Color.blue;
        Color bg = Color.yellow;
        setOpaque(true);

        if (isSelected) {
            super.setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());            
        } else {
            bg = row%2==0?FraMain.colorColumnRowEven:FraMain.colorColumnRowOdd;
                        
            fg = FraMain.colorTableName;

            setBackground(bg);
            setForeground(fg);
        }

        setFont(table.getFont());

//        if (hasFocus) {
//            Border border = null;
//            if (isSelected) {
//                border = DefaultLookup.getBorder(this, ui, "Table.focusSelectedCellHighlightBorder");
//            }
//            if (border == null) {
//                border = DefaultLookup.getBorder(this, ui, "Table.focusCellHighlightBorder");
//            }
//            setBorder(border);
//
//            if (!isSelected && table.isCellEditable(row, column)) {
//                Color col;
//                col = DefaultLookup.getColor(this, ui, "Table.focusCellForeground");
//                if (col != null) {
//                    super.setForeground(col);
//                }
//                col = DefaultLookup.getColor(this, ui, "Table.focusCellBackground");
//                if (col != null) {
//                    super.setBackground(col);
//                }
//            }
//        } else {
//            setBorder(getNoFocusBorder());
//        }
        setText(value.toString());
        return this;
    }        
}
