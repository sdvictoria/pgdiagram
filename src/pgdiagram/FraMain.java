/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import sun.awt.AWTAccessor;

/**
 *
 * @author nospoon
 */
public class FraMain extends JFrame {
    public enum SelectionMode {OLD, LAYOUT, LAYOUT_CG, QUERY};
    public static SelectionMode selectionMode = SelectionMode.LAYOUT_CG;    
    
    public static Color backGround = Color.darkGray;
    public static Color colorColumnRowEven = Color.darkGray;
    public static Color colorColumnRowOdd = Color.darkGray.darker();
    public static Color colorColumnFg = Color.white;
    public static Color colorColumnFgHightlighted = Color.yellow.darker();
    public static Color colorColumnFgSelected = Color.yellow;    
    public static Color colorTableHeader = Color.black;
    public static Color colorTableName = Color.green;    
    public static Color colorTableNameHiglighted = Color.yellow.darker();    
    public static Color colorTableNameSelected = Color.yellow;    
    public static Color colorFont = Color.white;
    public static Color colorHighLightFg = Color.blue;
    public static Color colorHighLightBg = null;
    
    CnvDiagram canvas;
    TblTableList tblTables = new TblTableList();
    JTextField txtColor = new JTextField();
    JRadioButton radioSelectionModeSelect = new JRadioButton("Old mode");    
    JRadioButton radioLayoutMode = new JRadioButton("Layout mode");
    JRadioButton radioLayoutModeDG  = new JRadioButton("CG layout Mode");
    JRadioButton radioQueryMode  = new JRadioButton("CG query Mode");
    JCheckBox chkShowFk = new JCheckBox("Display FK cols");
    JScrollPane scrOutput;
    JTextArea txtOutput = new JTextArea();
    
    Model model;
    JScrollPane scrCanvas;
    
    PnlTableDetail2 pnlTableDetail;   
    
    
    
    public FraMain() {        
        this.setBounds(100,100, 1600, 1200);
        this.setLayout(null);
        
        this.addComponentListener(new ComponentAdapter() {  
            public void componentResized(ComponentEvent evt) {               
                resize();     
            }
        });
        
        WindowAdapter wa = new WindowAdapter() {
            public void windowStateChanged(WindowEvent e) {
                resize();   
            }
        };
        this.addWindowListener(wa);
        this.addWindowStateListener(wa);

        canvas = new CnvDiagram(this);
        scrCanvas = new JScrollPane(canvas);
        canvas.setBackground(backGround);
//         KeyAdapter keyListener = new KeyAdapter() {
//            @Override
//            public void keyPressed(KeyEvent e) {
//                int i = e.getKeyCode();
//                System.out.println("keyPressed :" + i);
//            }
//        };
//        this.addKeyListener(keyListener);
        
        canvas.setPreferredSize(new Dimension(4096, 4096));
        scrCanvas.setBounds(200,0, 1500, 990);
        scrCanvas.setBackground(backGround);
        
        
        
        MouseHandlerCanvas mouseHandlerCanvas = new MouseHandlerCanvas(canvas);
        
        scrCanvas.addMouseMotionListener(mouseHandlerCanvas);
        scrCanvas.addMouseListener(mouseHandlerCanvas);
        scrCanvas.addMouseWheelListener(mouseHandlerCanvas);
                        
        ButtonGroup gr0 = new ButtonGroup();
        gr0.add(radioSelectionModeSelect);
        gr0.add(radioLayoutMode);
        gr0.add(radioLayoutModeDG);
        gr0.add(radioQueryMode);
        
        radioSelectionModeSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (radioSelectionModeSelect.isSelected()) {
                    canvas.setSelectionMode(SelectionMode.OLD);
                    selectionMode = SelectionMode.OLD;
                }
            }                        
        });
        
        radioLayoutMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (radioLayoutMode.isSelected()) {
                    canvas.setSelectionMode(SelectionMode.LAYOUT);
                    selectionMode = SelectionMode.LAYOUT;
                }
            }                        
        });
        
        radioLayoutModeDG.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (radioLayoutModeDG.isSelected()) {
                    canvas.setSelectionMode(SelectionMode.LAYOUT_CG);
                    selectionMode = SelectionMode.LAYOUT_CG;
                }
            }                        
        });
        
        radioQueryMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (radioQueryMode.isSelected()) {
                    canvas.setSelectionMode(SelectionMode.QUERY);
                    selectionMode = SelectionMode.QUERY;
                }
            }                        
        });
        chkShowFk.setSelected(true);
        
        chkShowFk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.setShowFKColumns(chkShowFk.isSelected());                
            }                        
        });
        
        switch (selectionMode) {
            case OLD:
                radioSelectionModeSelect.setSelected(true);
                break;                                  
            case LAYOUT:
                radioLayoutMode.setSelected(true);                
                break;
            case LAYOUT_CG:
                radioLayoutModeDG.setSelected(true);
                break;                 
            case QUERY:
                radioQueryMode.setSelected(true);
                break;                                         
        }
        canvas.setSelectionMode(selectionMode);
        
        scrOutput = new JScrollPane(txtOutput);
        txtOutput.setBackground(Color.darkGray);
        txtOutput.setForeground(Color.yellow);
        scrOutput.setBackground(Color.darkGray);        
        this.getContentPane().setBackground(Color.darkGray);
                
//        DiagramGlassPane glassPane = new DiagramGlassPane(this);        
//        this.setGlassPane(glassPane);
//        glassPane.setVisible(true);
        pnlTableDetail = new PnlTableDetail2(this);
        
        JButton btnLoad = new JButton("Load");
        JButton btnSave = new JButton("Save");   
        JButton btnRecordCount = new JButton("Record count");   
        JButton btnCardinals = new JButton("Calculate cardinals");   
                                      
        JButton fkAllignAtStart   = new JButton("s-ali");    
        JButton fkAllignAtEnd   = new JButton("e-ali");    
                                
        int xStep = 5;
        int yStep = 1;
       
//        fkAllignAtStart.addActionListener(new AllignFkAncherAction(this, true));
//        fkAllignAtEnd.addActionListener(new AllignFkAncherAction(this, false));
        
         btnLoad.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {       
                model = loadModel(PgDiagram.modelFileName);
                setModel(model);
            }
        });     
        
        btnSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {                     
                if (model!=null) {
                    PgDiagram.toFile(model, PgDiagram.appDir + "/model.pgd");
                }
            }
        });     
        
        btnRecordCount.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {                     
                if (model!=null) {
                    PgDiagram.recordCount(model);
                    canvas.refresh();
                }
            }
        });  
        
        btnCardinals.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {                     
                if (model!=null) {
                    PgDiagram.getCardinals(model);
                    canvas.refresh();
                }
            }
        });          
        
        txtColor.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {                
                Color color = null;
                String colorCode = txtColor.getText();
                colorCode = colorCode.toUpperCase();
                colorCode = colorCode.trim();
                colorCode = colorCode.replaceAll("0X", "");
                if (colorCode.length()==8) {
                    colorCode = colorCode.substring(0, 6);
                }
                if (colorCode.length()==6) {
                    try {
                        color  = new Color(Integer.parseInt(colorCode, 16));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (color!=null) {
//                    if (canvas.hitTestResult!=null && canvas.hitTestResult.hitForeignKey!=null) {
//                        canvas.hitTestResult.hitForeignKey.color = color;
//                    }
//                    canvas.refresh();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        pnlTableDetail.setBackground(Color.gray);
        
        JScrollPane scrTableList = new JScrollPane(tblTables);
        scrTableList.setBounds(0,0, 200, 500);
        // scrTableList.setBounds(0,0, 100, 200);
        tblTables.setBackground(backGround);
        scrTableList.getViewport().setBackground(backGround);
        // tblTables.setPreferredScrollableViewportSize(new Dimension(1500, 1500));
                
        UtilLayout.allignBottom(scrTableList, pnlTableDetail, 200, 300);        
        UtilLayout.allignBottom(pnlTableDetail, btnLoad, 200, 20);
        UtilLayout.allignBottom(btnLoad, btnSave, 200, 20);
        UtilLayout.allignBottom(btnSave, btnRecordCount, 200, 20);
        UtilLayout.allignBottom(btnRecordCount, btnCardinals, 200, 20);                
        UtilLayout.allignBottom(btnCardinals, radioSelectionModeSelect, 200, 20);        
        UtilLayout.allignBottom(radioSelectionModeSelect, radioLayoutMode, 200, 20);
        UtilLayout.allignBottom(radioLayoutMode, radioLayoutModeDG, 200, 20);  
        UtilLayout.allignBottom(radioLayoutModeDG, radioQueryMode, 200, 20);          
        UtilLayout.allignBottom(radioQueryMode, chkShowFk, 200, 20);                
        
        UtilLayout.allignBottom(scrCanvas,scrOutput, scrCanvas.getWidth(), 300);
        
        txtColor.setBounds(200,1000, 90, 20);
        fkAllignAtStart.setBounds(300, 1020, 90, 20);        
        fkAllignAtEnd.setBounds(600, 1020, 90, 20);
        
//        radioSelectionModeLayout.setBackground(Color.blue);
//radioSelectionModeSelect.setBounds(100, 1000, 190, 120);
        
//        btnLoad.setBounds(0,1000, 90, 20);
//        btnSave.setBounds(100,1000, 90, 20);

        addToContentPane(scrTableList, pnlTableDetail, scrCanvas, scrOutput);    
        addToContentPane(btnLoad, btnSave, btnRecordCount, btnCardinals);               
        addToContentPane(radioSelectionModeSelect, radioLayoutMode, radioLayoutModeDG, radioQueryMode, chkShowFk);               
        // addToContentPane(txtColor, fkAllignAtStart, fkAllignAtEnd);                
        
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
    }
    
    
    public void resize() {
        Rectangle rect = scrCanvas.getBounds();
        rect.width = this.getWidth()-216;
        rect.height = this.getHeight()-300;
        scrCanvas.setBounds(rect);
        
        rect = scrOutput.getBounds();
        rect.width = this.getWidth()-216;
        rect.height = 260;
        scrOutput.setBounds(rect);
        
        UtilLayout.allignBottom(scrCanvas,scrOutput);
    }
    
    public void setModel(Model m) {
        this.model = m;
        List<Table> tableList = new ArrayList();
        for (Database db:model.databases) {
            for(Schema schema:db.schemas) {
                // table list                
                String[] arr = schema.tables.keySet().toArray(new String[0]);
                Arrays.sort(arr);
                for (String tableName:arr) {
                    Table table = schema.tables.get(tableName);
                    tableList.add(table);
                }
            }
        }
        tblTables.setData(tableList);        
        canvas.refresh();
    }
    
    public void tableSelected(Table table) {
        pnlTableDetail.setTable(table);
    }
    
    public Model loadModel(String modelFileName) {
        System.out.println("Reading from " + modelFileName);  
        Model mdl = PgDiagram.toModel(modelFileName);
        return mdl;
    }
    private void addToContentPane(Component... comps) {
        for (Component c:comps) {
            this.getContentPane().add(c);
        }        
    }
    
    public void createStatementBasedOnSelection() {
        Map<Table, String> aliasMap = new HashMap();
        char current_alias = 'A';
        List<Table> tables = new ArrayList<Table>();
        List<Column> cols = new ArrayList<Column>();
        for (Object obj:canvas.selectedItems.keySet()) {
            if (obj instanceof Column) {                
                Column col = (Column) obj;
                cols.add(col);
                if (!aliasMap.containsKey(col.table)) {
                    aliasMap.put(col.table, ""+current_alias++);                    
                    tables.add(col.table);
                }
            }            
        }
        for (Object obj:canvas.selectedItems.keySet()) {
            if (obj instanceof Table) {                
                Table table = (Table) obj;
                if (!aliasMap.containsKey(table)) {
                    aliasMap.put(table, ""+current_alias++);                    
                    tables.add(table);
                }
            }            
        }
        if (aliasMap.size()>1) {
            toGraphs(tables);
        }
        
        List<Node> roots = toGraphs(tables);        
        

        String sql = "";
        
        if (aliasMap.size()>0) {        
            boolean useAlias = aliasMap.size()>1;
            if (cols.size()==0) {
                sql = "select * from ";
            }
            else {
               sql = "select ";
               for (int i=0; i<cols.size(); ++i) {
                Column col = cols.get(i);
                   if (i>0) {
                       sql += ", ";
                   }
                   if (useAlias) {
                       sql += aliasMap.get(col.table) + ".";
                   }               
                   sql += col.name;
               }
               sql += " from ";                       
            }        
        
            for (Node joinRoot:roots) {
                Table table = ((Table) joinRoot.obj);
                String table0Alias = aliasMap.get(table);
                sql += PgDiagram.fullyQualify(table) + " " + (useAlias?table0Alias + " ":"");
                if (joinRoot.children.size()>0) {
                    for (Node child:joinRoot.children) {
                        Table refTable = (Table)child.obj;
                        String table1Alias = aliasMap.get(refTable);
                        sql += "\n";
                        sql += " " + createJoinStatement(table, refTable, table0Alias, table1Alias);                        
                    }
                }
            }
        }
        txtOutput.setText(sql);
    }
        
    String createJoinStatement(Table table0, Table table1, String table0Alias, String table1Alias) {
        String str = "";
        Schema schema = table0.schema;
        boolean joinOnForeigTable = true;
        String fromAlias = table0Alias; 
        String toAlias = table1Alias;   

        List<ForeignKey> fks = schema.getKFListFromTable(table0.name);
        for (ForeignKey fk:fks) {
            if (fk.toTable.name.equals(table1.name)) {
                str += " " +PgDiagram.getJoinStatement(fk, joinOnForeigTable, fromAlias, toAlias) + "\n"; 
            }
        }

        joinOnForeigTable = false;
        fromAlias = table1Alias; 
        toAlias = table0Alias;                                    

        fks = schema.getKFListToTable(table0.name);
        for (ForeignKey fk:fks) {
            if (fk.fromTable.name.equals(table1.name)) {
                str += " " +PgDiagram.getJoinStatement(fk, joinOnForeigTable, fromAlias, toAlias) + "\n"; 
            }
        }
        return str;
    }
    
    public List<Node> toGraphs(List<Table> tables) {
        List<Node> graphs = new ArrayList();
        
        Map<Table, Node> tableMap = new HashMap();
        
        for (Table table:tables) {
            tableMap.put(table, new Node(table));
        }
        // construct map
        for (int i=0; i<tables.size(); ++i) {
            Table table0 = tables.get(i);
            Node node0 = tableMap.get(table0);
            for (int j=i+1; j<tables.size(); ++j) {
                Table table1 = tables.get(j);
                if (isLinked(table0, table1)) {
                    Node node1 = tableMap.get(table1);
                    node1.children.add(node0);
                    node0.parents.add(node1);
                }
            }
        }
        
        // get parents (root of (sub)graphs)
        for (int i=0; i<tables.size(); ++i) {
            Table table = tables.get(i);
            Node node = tableMap.get(table);
            List<Node> roots = new ArrayList();
            getRoots(node, roots);
            for (Node root:roots) {
                if (!graphs.contains(root)) {
                    graphs.add(root);
                }
            }
        }        
        return graphs;
    }
    
    void getRoots(Node node, List<Node> roots) {
        if (node.parents.size()==0) {
            roots.add(node);
            return;
        }
        for (Node parent:node.parents) {
            getRoots(parent, roots);
        }
    }
    
    boolean isLinked(Table t0, Table t1) {
        Schema schema = t0.schema;
        List<ForeignKey> fks = schema.getKFListFromTable(t0.name);
        for (ForeignKey fk:fks) {
            if (fk.toTable.name.equals(t1.name)) {
                return true;
            }
        }
        fks = schema.getKFListToTable(t0.name);
        for (ForeignKey fk:fks) {
            if (fk.fromTable.name.equals(t1.name)) {
                return true;
            }
        }
        return false;
    }
    
      
//    class AllignFkAncherAction implements ActionListener {
//        FraMain fra;
//        boolean start;
//        int xStep;
//        int yStep;
//        AllignFkAncherAction(FraMain fra, boolean start) {            
//            this.fra = fra;
//            this.start = start;
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            if (fra.canvas.hitTestResult!=null) {
//                ForeignKey fk = fra.canvas.hitTestResult.hitForeignKey;
//                if (fk!=null) {                    
//                    if (this.start) {
//                        fk.allignYAtStart();
//                    }
//                    else {
//                        fk.allignYAtEnd();
//                    }                        
//                    fra.canvas.refresh();
//                }
//            }
//        }
//    }    
}

class Node {
    Object obj;
    
    List<Node> parents = new ArrayList();            
    List<Node> children = new ArrayList();
    
    public Node(Object obj) {
        this.obj = obj;        
    }
}
