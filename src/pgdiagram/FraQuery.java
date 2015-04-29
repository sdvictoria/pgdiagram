/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class FraQuery extends JFrame {
    
    JTextField txtDatabaseURL = new JTextField();
    JTextArea txtInput = new JTextArea("select * from pg_aggregate");
    JTextArea txtOutput = new JTextArea();
    JScrollPane scrInput, scrOutput;
    
    public static void main(String[] arr) {
        FraQuery f = new FraQuery();        
        f.setVisible(true);
    }    
    
    public FraQuery() {        
        this.setBounds(100,100, 1000, 900);
        this.setLayout(null);
        JButton btnRun = new JButton("Run");        
        scrInput = new JScrollPane(txtInput);
        scrOutput = new JScrollPane(txtOutput);
        
        btnRun.setBounds(0,0, 300, 20);
        txtDatabaseURL.setBounds(300,0, 400, 20);          
        scrInput.setBounds(0,20, 800, 380);        
                     
        UtilLayout.allignBottom(scrInput, scrOutput, 800, 400);        
        
        UtilLayout.addToContainer(this.getContentPane(), btnRun, txtDatabaseURL, scrInput, scrOutput);
        
        txtInput.setBackground(Color.yellow);
        txtInput.setForeground(Color.blue);
        txtOutput.setBackground(Color.blue);
        txtOutput.setForeground(Color.yellow);

        
        btnRun.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {       
                runQuery();
            }
        });  
    }
    
    public void runQuery() {
        try {
            String sql = txtInput.getText().trim();
            if (sql.toLowerCase().contains("delete")) {
                txtOutput.setText("delete statements not supported");
                return;
            }
            if (sql.length()>0) {            
                Connection c = PgDiagram.getConnectionPostgres("postgres");
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery(sql);
                
                
                StringBuilder buffHeader = new StringBuilder();
                
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();
                String[] header = new String[columnCount];
                int[] max_widths = new int[columnCount];
                for (int i=0; i<columnCount; i++) {
                    header[i] = meta.getColumnName(i+1);
                    max_widths[i] = header[i].length();
                    buffHeader.append(header[i] + ";");
                }
                
                
                // Object[][] header = new String[columnCount];
                StringBuilder buffContent = new StringBuilder();
                int row = -1;
                while(rs.next()) {
                    row++;
                    for (int i=0; i<columnCount; i++) {
                        if (i>0) {
                            buffContent.append(";");
                        }
                        String str = rs.getString(i+1);
                        if (rs.wasNull()) {
                            str = "";
                        }
                        buffContent.append(str);
                        max_widths[i] = Math.max(max_widths[i], str.length());
                    }                    
                    buffContent.append("\n"); 
                }
                
                close(rs, st, c);        
                String result = buffHeader.toString() + "\n" + buffContent.toString();
                txtOutput.setText(result);
            }
        } catch (Exception e) {
            txtOutput.setText(e.toString());
        }
    }
    
    void close(AutoCloseable... arr) {
        for (AutoCloseable ac:arr) {
            try { if (ac!=null) { ac.close(); }} catch (Exception ex) {}
        }
    }
}
