/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author nospoon
 */
public class Model {
    List<Database> databases = new ArrayList();
    
    Database getDatabase(String dbName) {
        for (Database db:databases) {
            if (db.name.equals(dbName)) {
                return db;
            }
        }    
        return null;
    }    
    
    Table getTable(String db_name, String table_schema, String table_name) {
        Database db = getDatabase(db_name);
        if (db!=null) {
            Schema schema = db.getSchema(table_schema);
            if (schema!=null) {
                return schema.getTable(table_name);
            }            
        }
        return null;
    }

    ForeignKey getForeignKey(String db_name, String table_schema, String constraint_name) {
        Database db = getDatabase(db_name);
        if (db!=null) {
            Schema schema = db.getSchema(table_schema);
            if (schema!=null) {
                return schema.getForeignKey(constraint_name);
            }            
        }
        return null;
    }
    
    
}
