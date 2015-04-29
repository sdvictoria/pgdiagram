/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.Color;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class PnlTableDetail2 extends JPanel {
    JLabel lblTableName = new JLabel("Table name");
    // JTable tblReferences = new JTable();
    JTextArea txtSummary = new JTextArea();
    JScrollPane srcSummary = new JScrollPane();
    boolean showFKColumns = true;
    
    public PnlTableDetail2(FraMain fra) {
        super();
        init();
    }
    
    void init() {        
        lblTableName.setForeground(Color.green);
        lblTableName.setBackground(Color.darkGray);
        lblTableName.setOpaque(true);
        txtSummary.setBackground(Color.darkGray);
        txtSummary.setForeground(Color.green);
        srcSummary.setBackground(Color.darkGray);
        //tblReferences.setBackground(Color.yellow);        
        lblTableName.setBounds(0,0,200, 20);
        srcSummary = new JScrollPane(txtSummary);
        this.setBackground(Color.darkGray);       
        
               
        //JScrollPane scrTableReferences = new JScrollPane(tblReferences);
        // scrTableList.setBounds(0,0, 200, 700);
        // scrTableList.setBounds(0,0, 100, 200);
        // tblTables.setBackground(backGround);
//        scrTableList.getViewport().setBackground(backGround);        
        
        UtilLayout.allignBottom(lblTableName, srcSummary, 200, 250);
        
        this.setLayout(null);
        UtilLayout.add(this, lblTableName, srcSummary);        
    }
        
    public void setTable(Table table) {
        lblTableName.setText(table.getName());
        String s = "";
        s += "pk: " + concatStringList(table.primaryKeys) + "\n";
        s += "unique: " + concatStringList(table.uniqueKeys) + "\n";
        s += "index: " + concatStringList(table.indices) + "\n";
        
        String fkListStr = "";
        List<ForeignKey> fkList = table.schema.getKFListFromTable(table.name);
        for (ForeignKey fk:fkList) {
            // fkListStr += "- (" + fk.constraint_name + ")\n";
            fkListStr += "- " + concatStringList(fk.fromCols) + " -> " + fk.toTable.name + "." + concatStringList(fk.toCols) + "\n";
            if (!fk.fromCardinal.isEmpty() || !fk.toCardinal.isEmpty()) {
                String cardinal_string = "cardinal: " + (fk.fromCardinal.isEmpty()?"?":fk.fromCardinal) + " -> " + (fk.toCardinal.isEmpty()?"?":fk.toCardinal) + "\n";
                fkListStr += "  "+ cardinal_string;
            }            
            fkListStr += "  update: " + fk.update_rule.toLowerCase() + "; delete:" + fk.delete_rule.toLowerCase() + "\n";
        }        

        s += "references: " + (fkListStr.length()==0 ? "none\n" : "\n" + fkListStr);
        
        fkListStr = "";
        fkList = table.schema.getKFListToTable(table.name);
        for (ForeignKey fk:fkList) {
            // fkListStr += "- (" + fk.constraint_name + ")\n";
            fkListStr += "- " + fk.fromTable.name + "." + concatStringList(fk.fromCols) + " -> " + concatStringList(fk.toCols) + "\n";
            if (!fk.fromCardinal.isEmpty() || !fk.toCardinal.isEmpty()) {
                String cardinal_string = "cardinal: " + (fk.fromCardinal.isEmpty()?"?":fk.fromCardinal) + " -> " + (fk.toCardinal.isEmpty()?"?":fk.toCardinal) + "\n";
                fkListStr += " "+ cardinal_string;
            }
            fkListStr += "  update: " + fk.update_rule.toLowerCase() + "; delete:" + fk.delete_rule.toLowerCase() + "\n";
        }                

        s += "referenced by: " + (fkListStr.length()==0 ? "none\n" : "\n" + fkListStr);
        s += "schema: " + table.schema.name + "\n";
        s += "database: " + table.schema.db.name + "\n";        

        txtSummary.setText(s);        
    }
    
    String concatStringList(List<String[]> list) {
        String ret = "";                
        for (String arr[]:list) {
            String s = "";
            if (arr.length==1) {
                s += arr[0];
            }
            else {
                s += "{"+arr[0];
                for (int i=1; i< arr.length; ++i) {
                    s += " " + arr[i];
                }
                s += "}";
            }
            if (ret.length()>0) {
                ret += ", ";
            }
            ret += s;
        }
        return ret;
    }
    
   String concatStringList(String[] arr) {
        String ret = "";                
        String s = "";
        if (arr.length==1) {
            s += arr[0];
        }
        else {
            s += "{"+arr[0];
            for (int i=1; i< arr.length; ++i) {
                s += " " + arr[i];
            }
            s += "}";
        }
        ret += s;
        return ret;
    }    
}
