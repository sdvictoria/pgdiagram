/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author nospoon
 */
public class Table {
    String name;
    Schema schema;
    HashMap<String, Column> columns = new HashMap();
    HashMap<Integer, Column> columsByOrdinal = new HashMap();
    
    Column[] orderedColumns = null;
    List<String[]> primaryKeys = new ArrayList();
    List<String[]> uniqueKeys = new ArrayList();
    List<String[]> indices = new ArrayList();    
    long recordCount = -1;
    // display
    int x,y;
    Rectangle headerRect;    
    List<Rectangle> columnsRect = null;
    Rectangle totalRect = null;
    int headerNameHeight = 25;
    int columnNameHeight = 20;    

    public Table (String name, Schema schema)  {
        this.name = name;
        this.schema = schema;
    }
    
    Rectangle getHeaderRect(Table table) {
        if (headerRect==null) {
            headerRect = new Rectangle(0,0, 180, headerNameHeight);
        }
        return headerRect;
    }
  
   List<Rectangle> getColumnsRect(Table table, int offsetX, int offsetY) {
        if (columnsRect==null) {
            columnsRect = new ArrayList();
            Column[] cols = getColumnsInOrder();
            for (int i=0; i<cols.length; i++) {
                columnsRect.add(new Rectangle(offsetX,offsetY+i*20, 180, columnNameHeight));
            }
        }
        return columnsRect;
    }    

    Column getColumnRect(int x, int y) {        
        x -= this.x;
        y -= this.y;
        Column[] cols = getColumnsInOrder();        
        for (int idx=0; idx<cols.length; ++idx) {
            Rectangle rect = columnsRect.get(idx);            
            if (rect.contains(x,y)) {
                return cols[idx];
            }
        }        
        return null;
    }
   
   
    Rectangle getColumnRect(String colName) {
        Column[] cols = getColumnsInOrder();        
        for (int idx=0; idx<cols.length; ++idx) {
            Column col = cols[idx];
            if (col.name.equals(colName)) {
                return new Rectangle(columnsRect.get(idx));
            }
        }        
        return null;
    }
    
    void addTableOffsetTorect(Rectangle r) {
        r.x += this.x;
        r.y += this.y;
    }
    
    
    public String getColumnName(int idx) {
        Column[] cols = getColumnsInOrder();
        if (idx<cols.length) {
            return cols[idx].name;
        }
        return "";
    }
   
    Rectangle getTotalRect() {
        return getTotalRect(false);
    }
    
    Rectangle getTotalRect(boolean absolute) {
        if (totalRect==null) {
            totalRect = new Rectangle();
            if (headerRect!=null) {
                totalRect.add(headerRect);
            }
            if (columnsRect!=null) {
                for (Rectangle r:columnsRect) {        
                    totalRect.add(r);
                }        
            }
        }
         if (totalRect.width==0) {
             // mouse move handler can come here while there table dimensions are not available yet               
             totalRect = null;
             return new Rectangle();
        }
        if (absolute) {
            return new Rectangle(totalRect.x + this.x, totalRect.y + this.y, totalRect.width, totalRect.height);
        }
        return new Rectangle(totalRect);
    }
    
//    public Table(String name) {
//        this.name = name;
//    }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public String getName() {
        return this.name;
    }
    
    public Column addCol(String colName, String colType, long colTypeLen, int ordinal, boolean isNotNull) {
        Column c = new Column(this);
        c.name = colName;
        c.colType = colType;
        c.colTypeLen = colTypeLen;
        c.order = ordinal;
        c.isNotNull = isNotNull;
        // System.out.println("table: " + name + " col: " + colName + " order: " + c.order);
        columns.put(colName, c);
        columsByOrdinal.put(ordinal, c);
        return c;
    }
    
    public String[] getColumnNamesByOrdinal(int ordinals[]) {
        String arr[] = new String[ordinals.length];
        for (int idx =0; idx<ordinals.length; ++idx) {
            Column col = columsByOrdinal.get(ordinals[idx]-1);
            if (col!=null) {
                arr[idx] = col.name;
            }
        }
        return arr;
    }
    
    private Column[] reorderColumns(Column cols[]) {
        int emptyIdx = -1;
        for (int idx=0; idx<cols.length; idx++) {
            Column col = cols[idx];
            if (col==null) {
                if (emptyIdx==-1) {
                    emptyIdx = idx;
                }
            }
            else {
                if (emptyIdx>-1) {
                    cols[emptyIdx] = col;
                    cols[idx] = null;
                    emptyIdx++;
                }
            }
        }
        
        if (emptyIdx>-1) {
            Column cols2[] = Arrays.copyOf(cols, emptyIdx);
            return cols2;
        }
        return cols;
    }
    
    Column[] getColumnsInOrder() {
        if (orderedColumns==null) {
            //orderedColumns = new Column[columns.size()];
            orderedColumns = new Column[columns.size()+10];
            for (Column c:columns.values()) {
                orderedColumns[c.order]=c;
            }
            orderedColumns = reorderColumns(orderedColumns);
        }
        return orderedColumns;        
    }
    
    public void addPK(String colName) {
        String arr[] = new String[1];
        arr[0] = colName;
        addPK(arr);
    }
    
    public void addPK(String[] colNames) {
        primaryKeys.add(colNames);
    }

    public void addUniqueKey(String colName) {
        String arr[] = new String[1];
        arr[0] = colName;
        addUniqueKey(arr);        
    }

    public void addUniqueKey(String colName[]) {
        uniqueKeys.add(colName);        
    }
    
    public void addIndex(String colName) {
        String arr[] = new String[1];
        arr[0] = colName;
        addIndex(arr);        
    }

    public void addIndex(String colName[]) {
        indices.add(colName);        
    }

    public String toString() {
        return this.name;
    }
}
