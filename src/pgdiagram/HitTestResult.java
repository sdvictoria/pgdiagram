package pgdiagram;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HitTestResult {
    public enum HITTEST { TABLE, COLUMN, FOREIGNKEY, FOREIGNKEY_CONTROLPOINT }
    public List<Table> tables = new ArrayList();
    public List<Column> columns = new ArrayList();    
    public List<ForeignKey> foreignKeys = new ArrayList();
    public List<ForeignKeyControlPoint> foreignKeyControlPoints = new ArrayList();
    
    public boolean isNothingSelected() {
        return tables.isEmpty() && columns.isEmpty() && foreignKeys.isEmpty() && foreignKeyControlPoints.isEmpty();
    }
    
    
//    Map<Table, Point> tableLocations = new HashMap();               
//        
//        Column column;
//        ForeignKey hitForeignKey = null;
//        int foreignKeyLineSection = -1; 
//        Point controlPoint;
//        Anchor anchor;
//        
//        public boolean isNothingSelected() {
//            if (table==null && column==null && hitForeignKey==null) {
//                return true;
//            }
//            return false;
//        }


//    public void saveLocations() {
//        tableLocations.clear();
//        for (Table t:tables) {
//            tableLocations.put(t, new Point(t.x, t.y));
//        }        
//    }
}    
