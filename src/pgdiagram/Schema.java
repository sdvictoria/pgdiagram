/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.Color;
import java.awt.Point;
import java.util.*;

/**
 *
 * @author nospoon
 */
public class Schema {
    String name = "";
    Database db = null;
    Map<String, Table> tables = new HashMap();
    Map<String, List<ForeignKey>> foreignKeys = new HashMap();
    
    public Schema(String name, Database db) {
        this.name = name;
        this.db = db;
    }    
    
    public void add(Table t) {
        tables.put(t.getName(), t);
    }
    
    public Table getTable(String tableName) {
        return tables.get(tableName);
    } 

    public ForeignKey getForeignKey(String constraintName) {
        
        for (List<ForeignKey> fks:foreignKeys.values()) {
            for (ForeignKey fk:fks) {
                if (fk.constraint_name.equals(constraintName)) {
                    return fk;
                }
            }
        }
        return null;
    } 
    
    public ForeignKey addFK(String constraint_name, String match_option, String update_rule, String delete_rule, Table fromTable, String[] fromCols, Table toTable, String[] toCols, String fromCardinal, String toCardinal, int[] positions_in_other_constraint, List<Point> linePoints, Color color) {
        String key = fromTable.name;
        
        List<ForeignKey> list = foreignKeys.get(key);
        if (list==null) {
            list = new ArrayList();
            foreignKeys.put(key, list);
        }        
                
        ForeignKey tl = new ForeignKey(constraint_name, match_option, update_rule, delete_rule, fromTable, fromCols, toTable, toCols, fromCardinal, toCardinal, positions_in_other_constraint, list.size());
        if (linePoints==null) {
            linePoints = new ArrayList();            
        }
        while (linePoints.size()<2) {
            linePoints.add(new Point());
        }
        
        tl.linePoints.clear();
        for (Point p:linePoints) {            
            tl.linePoints.add(new ForeignKeyControlPoint(tl, p));
        }
        // tl.linePoints = linePoints;
        tl.color = color;
        list.add(tl);   
        return tl;
    }
    
    public List<ForeignKey> getKFListFromTable(String tableName) {
        List<ForeignKey> list = foreignKeys.get(tableName);
        if (list==null) {
            list =  new ArrayList();
        }
        return list;
    }

     public List<ForeignKey> getKFListToTable(String tableName) {
        List<ForeignKey> list = new ArrayList();
        for (List<ForeignKey> fkList:foreignKeys.values()) {
            for (ForeignKey fk:fkList) {
                if (fk.toTable.name.equals(tableName)) {
                    list.add(fk);
                }
            }
        }
        return list;
    }

    public List<String> getOrderedTableNames() {
        List<String> orderedTableNames = new ArrayList();
        for (Table t : tables.values()) {
            orderedTableNames.add(t.getName());
        }
        Collections.sort(orderedTableNames);
        return orderedTableNames;
    }
}
