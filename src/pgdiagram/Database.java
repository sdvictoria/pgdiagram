/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.util.ArrayList;
import java.util.List;
import static pgdiagram.PgDiagram.toIntegerArray;

/**
 *
 * @author nospoon
 */
public class Database {
    String name = "";
    List<Schema> schemas = new ArrayList();
    
    public Database(String name) {
        this.name = name;
    }
    
    Schema getSchema(String schemaName) {
        for (Schema schema:schemas) {
            if (schema.name.equals(schemaName)) {
                return schema;
            }
        }    
        return null;
    }    
    
    Table getTable(String schemaName, String tableName) {
        Schema schema = getSchema(schemaName);
        if (schema!=null) {
            return schema.getTable(tableName);
        }
        return null;
    }
}
