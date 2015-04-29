/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.Color;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 *
 * @author nospoon
 */
public class PgDiagram {
    
    static String appDir;
    static String modelFileName;
    static boolean loadFromDB = true;
    static boolean edb = false;
    static String currentQueryID = "";

    public static void main(String[] args) {
        PgDiagram app = new PgDiagram();
        app.go();
    }
   
    void go() {
        
        if (false) {
            testConnectionAbort();
            return;
        }
        if (false) {
            testQueryOutput();
            return;
        }
        appDir = new File(".").getAbsolutePath();
        appDir = appDir.replace("\\", "/");
        if (appDir.endsWith("/.")) {
            appDir = appDir.substring(0, appDir.length()-2);
        }
        System.out.println("Current dir " + appDir);
        modelFileName = PgDiagram.appDir + "/model.pgd";
        
        
        Model model_db = null;
        Model model_file = null;
        if (loadFromDB) {          
            model_db = null;
            String dbName_ = "postgres";
            Connection conn = getConnectionPostgres(dbName_);            
            model_db = loadModelFromDB(conn);   
            if (true) {
                Date d1 = new Date();   
                for (Database db:model_db.databases) {
                    loadContraints(db, conn);
                    loadIndexes(db, conn);
                }                    
                Date d2 = new Date();
                System.out.println("fetch constraint list:" + (d2.getTime()-d1.getTime()) + " msec");                    
            }      
            
            for (Database db:model_db.databases) {
                for (Schema s:db.schemas) {
                    for (Table t:s.tables.values()) {
                        randomizeLocations(s.tables.values().toArray(new Table[0]));
                    }
                }
            }
            
            try {
                conn.close();
            } catch (Exception ex) {ex.printStackTrace();}
        }

        FraMain fra = new FraMain();
        model_file = fra.loadModel(modelFileName);     
        copyLocations(model_file, model_db);        
        fra.setModel(model_db);
    }
    
    public boolean compareModels(Model m0, Model m1) {
        boolean theSame = false;
        
//        List tableNames
//        for (m0.tables.
        
        return theSame;
    }

    public static Model toModel(String filename) {
        Model model = new Model();
        try {            
            FileReader reader = new FileReader(filename);            
            Properties p = new Properties();
            p.load(reader);
            
            for (int db_idx=0; ;db_idx++) {
                String dbKey = "db_" + db_idx + ".";
                String dbName = p.getProperty(dbKey + "name");
                if (dbName==null) {
                    break;
                }
                Database db = new Database(dbName); 
                model.databases.add(db);
                for (int schema_idx=0; ;schema_idx++) {
                    String schemaKey = dbKey + "schema_" + schema_idx + ".";
                    String schemaName = p.getProperty(schemaKey + "name");
                    if (schemaName==null) {
                        break;
                    }
                    Schema schema = new Schema(schemaName, db);   
                    db.schemas.add(schema);
                    for (int table_idx=0; ;table_idx++) {
                        String tableKey = schemaKey + "table_" + table_idx + ".";
                        String tableName = p.getProperty(tableKey + "name");
                        if (tableName==null) {
                            break;
                        }
                        Table table = new Table(tableName, schema);
                        schema.add(table);
                        String recordCountStr = p.getProperty(tableKey + "recordCount", "-1");                        
                        try {
                            table.recordCount = Integer.parseInt(recordCountStr);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                            
                        for (int column_idx=0; ;column_idx++) {
                            String columnKey = tableKey + "column_" + column_idx + ".";
                            String columnName = p.getProperty(columnKey + "name");
                            if (columnName==null) {
                                break;
                            }
                            String columnType = p.getProperty(columnKey + "type", "");
                            long columnTypeLen = Long.parseLong(p.getProperty(columnKey + "typelen", "-1"));
                            String strIsNotNull = p.getProperty(columnKey + "isNotNull", "f");
                            boolean isNotNull = strIsNotNull.equalsIgnoreCase("t");                            
                            Column c = table.addCol(columnName, columnType, columnTypeLen, column_idx, isNotNull);
                            String actualDistinctCount_str = p.getProperty(columnKey + "actualDistinctCount");
                            if (actualDistinctCount_str!=null) {
                                try {
                                    c.actualDistinctCount = Long.parseLong(actualDistinctCount_str);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                            if (c.actualDistinctCount>0) {                        
                                c.distinctValues = readDistinctValuesFromFile(appDir + "/" + getColumnFileName(c));
                            }                    
                        }

                        for (int pk_idx=0; ;pk_idx++) {               
                            String pks = p.getProperty(tableKey + "pk_" + pk_idx);
                            if (pks==null) {
                                break;
                            }
                            String[] arr = toStringArray(pks, ",");
                            table.addPK(arr);
                        }     

                        for (int uk_idx=0; ;uk_idx++) {               
                            String uks = p.getProperty(tableKey + "unique_" + uk_idx);
                            if (uks==null) {
                                break;
                            }
                            String[] arr = toStringArray(uks, ",");
                            table.addUniqueKey(arr);
                        }    

                        for (int ik_idx=0; ;ik_idx++) {               
                            String iks = p.getProperty(tableKey + "index_" + ik_idx);
                            if (iks==null) {
                                break;
                            }
                            String[] arr = toStringArray(iks, ",");
                            table.addIndex(arr);
                        }     
                        
                        String val = p.getProperty(tableKey + "location");
                        Point point = stringToPoint(val);
                        if (point!=null) {
                            table.x = point.x;
                            table.y = point.y;
                        }     
                    }

                    for (int table_idx=0; ;table_idx++) {
                        String tableKey = schemaKey + "table_" + table_idx + ".";
                        String tableName = p.getProperty(tableKey + "name");
                        if (tableName==null) {
                            break;
                        }
                        
                        for (int fk_idx=0; ;fk_idx++) { 
                            String fkKey = tableKey + "fk_" + fk_idx + ".";
                            String fkFromTable = p.getProperty(fkKey + "fromTable");
                            if (fkFromTable==null) {
                                break;
                            }
                            String constraint_name = p.getProperty(fkKey + "constraint_name", "");
                            String match_option = p.getProperty(fkKey + "match_option", "");
                            String update_rule = p.getProperty(fkKey + "update_rule", "");
                            String delete_rule = p.getProperty(fkKey + "delete_rule", "");
                            String fkFromCols[] = toStringArray(p.getProperty(fkKey + "fromCols"), ",");
                            int fkToConstraintCols[] = toIntegerArray(p.getProperty(fkKey + "toConstraintCols"), ",");
                            String fkToTable = p.getProperty(fkKey + "toTable");
                            String fkToCols[] = toStringArray(p.getProperty(fkKey + "toCols"), ",");    
                            
                            String fromCardinal = p.getProperty(fkKey + "fromCardinal", "");
                            String toCardinal = p.getProperty(fkKey + "toCardinal", "");                            

                            List<Point> linePoints = toPointList(p.getProperty(fkKey + "linePoints"));  
                            String colorCode = p.getProperty(fkKey + "lineColor", "");
                            Color lineColor = Color.black;
                            try {
                                colorCode = colorCode.trim();
                                colorCode = colorCode.toUpperCase();
                                colorCode = colorCode.replaceAll("0X", "");
                                if (colorCode.length()==8) {
                                    colorCode = colorCode.substring(0, 6);
                                }
                                if (colorCode.length()==6) {
                                    lineColor = new Color(Integer.parseInt(colorCode, 16));
                                }
                            } catch ( Exception ex) { ex.printStackTrace(); }
                            Table fromTable = schema.getTable(fkFromTable);
                            Table toTable = schema.getTable(fkToTable);

                            if (toTable==null) {
                                toTable = toTable;
                            }
                            ForeignKey fk = schema.addFK(constraint_name, match_option, update_rule, delete_rule, fromTable, fkFromCols, toTable, fkToCols, fromCardinal, toCardinal, fkToConstraintCols, linePoints, lineColor);
                            String point_str = p.getProperty(fkKey + "startAnchor.Offset");
                            Point point = stringToPoint(point_str);
                            if (point!=null) {
                                fk.startAnchorOffset.offset = point;
                            }
                            point_str = p.getProperty(fkKey + "endAnchor.Offset");
                            point = stringToPoint(point_str);
                            if (point!=null) {
                                fk.endAnchorOffset.offset = point;
                            }

                            String enum_str = p.getProperty(fkKey + "startAnchor.Orientation");
                            if ("RIGHT".equalsIgnoreCase(enum_str)) {
                                fk.startAnchorOffset.orientation = Anchor.AnchorOrientation.RIGHT;
                            }

                            enum_str = p.getProperty(fkKey + "endAnchor.Orientation");
                            if ("RIGHT".equalsIgnoreCase(enum_str)) {
                                fk.endAnchorOffset.orientation = Anchor.AnchorOrientation.RIGHT;
                            }
                        }
                    }
                }                        
            reader.close();
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return model;
    }
    
    public static Point stringToPoint(String s) {
        Point p = null;
        try {
            String arr[] = toStringArray(s, ",");
            if (arr.length==2) {
                p = new Point(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return p;
    }   
    
    public static void toFileDistincValues(Column col, String filename) {
        try {
            if (col!=null && col.distinctValues!=null && col.distinctValues.size()>0) {
                FileWriter fw = new FileWriter(filename);        
                for (String s:col.distinctValues) {
                    fw.write(s + "\n");
                }
                 fw.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static List<String> readDistinctValuesFromFile(String filename) {
        List<String> list = new ArrayList();
        try {
            File f = new File(filename);
            if (f.isFile() && f.canRead()) {
                BufferedReader reader = new BufferedReader(new FileReader(f));       
                String line = reader.readLine();
                while (line!=null) {
                    list.add(line);                
                    line = reader.readLine();
                }
                reader.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }
    
    public static void toFile(Model model, String filename) {
        
        // table.recordcount
        
        try {
            FileWriter fw = new FileWriter(filename);        

            int db_idx = -1;
            for (Database db:model.databases) {                
                db_idx++;
                String dbKey = "db_" + db_idx + ".";
                fw.write(dbKey + "name=" + db.name + "\n");
                int schema_idx = -1;
                for (Schema schema:db.schemas) {
                    schema_idx++;
                    String schemaKey = dbKey + "schema_" + schema_idx + ".";
                    fw.write(schemaKey + "name=" + schema.name + "\n");
                    Collection<String> coll = schema.tables.keySet();
                    String arr[] = coll.toArray(new String[0]);
                    Arrays.sort(arr);

                    int table_idx = -1;
                    for (String tableName:arr) {
                        table_idx++;
                        Table table = schema.tables.get(tableName);
                        String tableKey = schemaKey + "table_" + table_idx + ".";
                        fw.write(tableKey + "name=" + table.name + "\n");
                        if (table.recordCount>-1) {
                            fw.write(tableKey + "recordCount=" + table.recordCount + "\n");
                        }                        
                        Column[] cols = table.getColumnsInOrder();
                        int column_idx = -1;
                        for (Column c:cols) {
                            column_idx++;
                            String columnKey = tableKey + "column_" + column_idx + ".";
                            if (c==null) {
                                fw.write(columnKey + "name=null\n");                                
                            }                           
                            else {
                                fw.write(columnKey + "name=" + c.name + "\n");
                                fw.write(columnKey + "type=" + c.colType + "\n");
                                if (c.colTypeLen>0) {
                                    fw.write(columnKey + "typelen=" + c.colTypeLen + "\n");                                    
                                }                                
                                if (c.isNotNull) {
                                    fw.write(columnKey + "isNotNull=" + (c.isNotNull?"t":"f") + "\n");
                                }
                                if (c.actualDistinctCount>0) {
                                    fw.write(columnKey + "actualDistinctCount=" + c.actualDistinctCount + "\n");
                                    toFileDistincValues(c, appDir + "/" + getColumnFileName(c));
                                }                                                        
                            }
                        }

                        int pk_idx=-1;
                        for (String[] pkCols:table.primaryKeys) {
                            pk_idx++;
                            fw.write(tableKey + "pk_" + pk_idx + "=" + toCommaSeperatedString(pkCols) + "\n");
                        }
                        int uk_idx=-1;
                        for (String[] ukCols:table.uniqueKeys) {
                            uk_idx++;
                            fw.write(tableKey + "unique_" + uk_idx + "=" + toCommaSeperatedString(ukCols) + "\n"); 
                        }            
                        int ik_idx=-1;
                        for (String[] idx:table.indices) {
                            ik_idx++;
                            fw.write(tableKey + "index_" + ik_idx + "=" + toCommaSeperatedString(idx) + "\n"); 
                        }            

                        List<ForeignKey> tableFKs = schema.getKFListFromTable(tableName);
                        int fk_idx=-1;
                        for (ForeignKey fk:tableFKs) {
                            fk_idx++;
                            String fkKey = tableKey + "fk_" + fk_idx + ".";
                            fw.write(fkKey + "constraint_name=" + fk.constraint_name + "\n");
                            fw.write(fkKey + "match_option=" + fk.match_option + "\n");
                            fw.write(fkKey + "update_rule=" + fk.update_rule + "\n");
                            fw.write(fkKey + "delete_rule=" + fk.delete_rule + "\n");                            
                            fw.write(fkKey + "fromTable=" + fk.fromTable.name + "\n");
                            fw.write(fkKey + "fromCols=" + toCommaSeperatedString(fk.fromCols) + "\n");
                            //fw.write(fkKey + "toConstraintCols=" + toCommaSeperatedString(fk.positions_in_other_constraint) + "\n");
                            fw.write(fkKey + "toTable=" + fk.toTable.name + "\n");
                            fw.write(fkKey + "toCols=" + toCommaSeperatedString(fk.toCols) + "\n");              
                            fw.write(fkKey + "fromCardinal=" + fk.fromCardinal + "\n");              
                            fw.write(fkKey + "toCardinal=" + fk.toCardinal + "\n");              
                            // control points
                            fw.write(fkKey + "linePoints=" + toCommaSeperatedString(fk.linePoints) + "\n");  
                            // line color
                            String r = Integer.toHexString(fk.color.getRed());            
                            String g = Integer.toHexString(fk.color.getGreen());
                            String b = Integer.toHexString(fk.color.getBlue());
                            if (r.length()==1) { r = "0" + r; }
                            if (g.length()==1) { g = "0" + g; }
                            if (b.length()==1) { b = "0" + b; }    
                            String colorCode = r+g+b;
                            colorCode = colorCode.toUpperCase();
                            colorCode = "0x"+colorCode;
                            fw.write(fkKey + "lineColor=" + colorCode + "\n");     
                            fw.write(fkKey + "startAnchor.Offset=" + fk.startAnchorOffset.offset.x + "," + fk.startAnchorOffset.offset.y + "\n");     
                            fw.write(fkKey + "startAnchor.Orientation=" + fk.startAnchorOffset.orientation + "\n");     
                            fw.write(fkKey + "endAnchor.Offset=" + fk.endAnchorOffset.offset.x + "," + fk.endAnchorOffset.offset.y + "\n");     
                            fw.write(fkKey + "endAnchor.Orientation=" + fk.endAnchorOffset.orientation + "\n");                         
                        }
                        fw.write(tableKey + "location=" + table.x + "," + table.y + "\n"); 
                    }
                }                        
            }
            fw.close();
        } catch (Exception ex) {ex.printStackTrace();}            
    }
    static String[] toStringArray(String s, String seperator) {
        if (s==null) {
            return new String[0];
        }
        String arr[] = s.split(seperator);        
        for (int i=0; i<arr.length; i++) {
            arr[i] = arr[i].trim();
        }
        return arr;
    }

    static int[] toIntegerArray(String s, String seperator) {
        if (s==null) {
            return new int[0];
        }
        String arr[] = s.split(seperator);        
        int[] iarr = new int[arr.length];
        for (int i=0; i<arr.length; i++) {
            try {
                iarr[i] = Integer.parseInt(arr[i].trim());
            } catch (Exception ex) {
                System.err.println("toIntegerArray(): Can not parse " +  s);
                ex.printStackTrace();
            }
        }
        return iarr;
    }
    
    static String getColumnFileName(Column c) {
        return c.table.schema.db.name + "_" + c.table.schema.name + "_" + c.table.name + "_" + c.name;
    }
    
    static String toCommaSeperatedString(List<ForeignKeyControlPoint> points) {
        StringBuilder buff = new StringBuilder();
        if (points!=null) {
            for (ForeignKeyControlPoint fkcp:points) {
                if (buff.length()>0) {
                    buff.append(";");
                }
                buff.append(fkcp.controlPoint.x + "," + fkcp.controlPoint.y);
            }
        }
        return buff.toString();
    }
    
    static List<Point> toPointList(String str) {       
        List<Point> list = new ArrayList();
        if (str!=null) {
            String arr[] = str.split(";");   
            for (String s:arr) {
                String tmp[] = s.split(",");
                if (tmp.length==2) {
                    try {
                        Point p = new Point();
                        p.x = Integer.parseInt(tmp[0]);
                        p.y = Integer.parseInt(tmp[1]);            
                        list.add(p);
                    } catch (Exception ex) {ex.printStackTrace();}
                }
            }
        }
        return list;
    }
    
    static String toCommaSeperatedString(String arr[]) {
        StringBuilder buff = new StringBuilder();
        if (arr!=null) {
            for (String s:arr) {
                if (buff.length()>0) {
                    buff.append(", ");  
                }
                buff.append(s);  
            }
        }
        return buff.toString();
    }
    
    static String toCommaSeperatedString(int arr[]) {
        StringBuilder buff = new StringBuilder();
        if (arr!=null) {
            for (int i:arr) {
                if (buff.length()>0) {
                    buff.append(", ");  
                }
                buff.append(i);  
            }
        }
        return buff.toString();
    }    

    
    static String toSeperatedString(String arr[], String sep) {
        return toSeperatedString(arr, sep, null);
    }
    
    static String toSeperatedString(String arr[], String sep, String prefix) {
        StringBuilder buff = new StringBuilder();
        if (arr!=null) {
            for (String s:arr) {
                if (buff.length()>0) {
                    buff.append(sep);  
                }
                buff.append((prefix!=null?prefix:"")+ s);  
            }
        }
        return buff.toString();
    }
    
    static void fetchDistinctValues(Column c) {
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
//        try {            
//            conn = getConnectionPostgres(dbName);            
//            System.out.println("fetching distinct values for " + c.table.name + "." + c.name);
//            st = conn.createStatement();
//            st.setQueryTimeout(100);
//            String sql = "select count(distinct("+ c.name +")) from " +schema+ "." + c.table.name;                
//            rs = st.executeQuery(sql);                            
//            while (rs.next()) {            
//                c.actualDistinctCount = rs.getLong(1);
//            }
//
//            if (c.actualDistinctCount<100) {
//                List<String> values = new ArrayList();
//                sql = "select distinct("+ c.name +") from " +schema+ "." + c.table.name + " order by " + c.name;
//                rs = st.executeQuery(sql);                
//                while (rs.next()) {            
//                    values.add(rs.getString(1));
//                }
//                c.distinctValues = values;
//            }                
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        finally {
//            try {
//                if (rs!=null) { rs.close(); }
//                if (st!=null) { st.close(); }
//                if (conn!=null) {conn.close(); }            
//            } catch (Exception ex1) {}
//        }
    }
    
    List<String> getStringList(String sql, Connection c) {
        List<String> list = new ArrayList();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = c.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                list.add(rs.getString(1));
            }            
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            close(rs, st);
        }
        return list;
    }
    
    static long getNumber(String sql, Connection c) {
        long l = 0;
        Statement st = null;
        ResultSet rs = null;
        try {
            st = c.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                l = rs.getLong(1);
            }            
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            close(rs, st);
        }
        return l;
    }
    
    static void close(AutoCloseable... arr) {
        for (AutoCloseable ac:arr) {
            try { if (ac!=null) { ac.close(); }} catch (Exception ex) {}
        }
    }
    
    Table getTableAndColumns(Model model, Statement st, String dbName, String schemaName, String constraint_name, List<String> columns, List<Integer> positions_in_other_constraint) {
        try {
            String sql = "select table_catalog, table_schema, table_name from information_schema.key_column_usage\n"
                + " where constraint_catalog = '" + dbName + "' and constraint_schema = '" + schemaName + "'"
                + " and constraint_name = '" + constraint_name + "'"
                + " group by table_catalog, table_schema, table_name";
                       
            String table_catalog = null;
            String table_schema = null;
            String table_name = null;

            ResultSet rs1 = st.executeQuery(sql);
            while (rs1.next()) { 
                table_catalog = rs1.getString("table_catalog");
                table_schema = rs1.getString("table_schema");
                table_name = rs1.getString("table_name");                                   
                break;
            }
            close(rs1);

            sql = "select column_name, position_in_unique_constraint from information_schema.key_column_usage\n"
                + " where constraint_catalog = '" + dbName + "' and constraint_schema = '" + schemaName + "'"                                        
                + " and constraint_name = '" + constraint_name + "'"
                + "and table_catalog = '"+table_catalog+"' and table_schema='"+table_schema+"' and table_name ='"+table_name+"' "
                + " order by ordinal_position";
            rs1 = st.executeQuery(sql);                                
            while (rs1.next()) { 
                columns.add(rs1.getString("column_name"));
                if (positions_in_other_constraint!=null) {
                    Integer pos = rs1.getInt("position_in_unique_constraint");
                    if (rs1.wasNull()) {
                        pos = null;
                    }
                    positions_in_other_constraint.add(pos);
                }
            }
            close(rs1);

            Table tbl = model.getTable(table_catalog, table_schema, table_name);
            return tbl;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    int[] toPrimitiveArr(Integer[] arr) {
        int[] ret = new int[arr.length];
        for (int i=0; i<arr.length; ++i) {
            ret[i] = arr[i];
        }
        return ret;
    }
    
  Model loadModelFromDB(Connection conn) {
        Model model = new Model();    
        try {            
            Statement st = conn.createStatement();
            
            String sql = "select distinct(catalog_name) from information_schema.schemata order by catalog_name";
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {            
                String dbName = rs.getString("catalog_name");              
                model.databases.add(new Database(dbName));
            }
            rs.close();
            
            for (Database db:model.databases) {
                sql = "select distinct(schema_name) from information_schema.schemata where catalog_name = '" + db.name + "' ";                        
                        sql += "  and schema_name != 'information_schema' and schema_name not like 'pg_%'";
                        if (edb) {                        
                            sql += "  and schema_name != 'dbo' and schema_name != 'public' and schema_name != 'sys'";
                        }
                        sql += "  order by schema_name";
                rs = st.executeQuery(sql);
                while (rs.next()) {            
                    db.schemas.add(new Schema(rs.getString("schema_name"), db));
                }
                rs.close();
                
                for (Schema schema:db.schemas) {
                    // not null constraint: pg_attribute
                    sql = "select c2.relname, attr.attname, attr.attnum, attr.attnotnull, coltyp.typname, coltyp.typlen from pg_attribute attr\n" +
                            "join pg_class c2 on c2.oid = attr.attrelid\n" +
                            "join pg_namespace n2 on n2.oid = c2.relnamespace\n" +
                            "join pg_authid a2 on a2.oid = n2.nspowner\n" +
                            "join pg_type coltyp on coltyp.oid = attr.atttypid\n" + 
                            "where \n" +
                            "2=2\n" +
                            "and a2.rolname = '" + db.name + "' and n2.nspname = '" + schema.name + "' \n" +
                            "and attr.attnum>0 and c2.relkind='r' \n" +
                            "and attr.attisdropped = 'f'\n" +
                            "order by c2.relname, attr.attnum";
                    Table table = null;
                    rs = st.executeQuery(sql);
                    while (rs.next()) {   
                        String tableName = rs.getString(1);
                        if (table==null || !table.name.equals(tableName)) {
                            table = new Table(tableName, schema);
                            schema.add(table);
                        }
                        String columnName = rs.getString(2);
                        Integer columnPosition = rs.getInt(3);
                        String strIsNotNull = rs.getString(4);
                        String columnType = rs.getString(5);
                        long columnTypeLen = rs.getLong(6);
                        boolean isNotNull = strIsNotNull.equalsIgnoreCase("t");                                                
                        table.addCol(columnName, columnType, columnTypeLen, columnPosition-1, isNotNull);
                    }  
                    rs.close();
                }                    
            }
            
            st.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return model;
    }    
    
    void randomizeLocations(Table[] tables) {
        float xMax = 600;
        float yMax = 600;
        for (Table t:tables) {
            int x = (int) (Math.random()*xMax);
            int y = (int) (Math.random()*yMax);
            t.setPosition(x, y);
        }
    }
  
    void loadContraints(Database db, Connection conn) {
        Statement st = null;
        ResultSet rs = null;
          try {
            String sql = "select authcon.rolname, nscon.nspname, con.conname, con.contype, con.confupdtype, con.confdeltype, con.confmatchtype, nstab.nspname, tab.relname, conkey, nsftab.nspname, ftab.relname, confkey\n" +
                        "  from pg_constraint con\n" +
                        "  join pg_class tab on tab.oid = con.conrelid\n" +
                        "  left join pg_class ftab on ftab.oid = con.confrelid\n" +
                        "  join pg_namespace nscon on nscon.oid = con.connamespace\n" +
                        "  join pg_namespace nstab on nstab.oid = tab.relnamespace\n" +
                        "  left join pg_namespace nsftab on nsftab.oid = ftab.relnamespace\n" +
                        "  join pg_authid authcon on authcon.oid = nscon.nspowner\n";
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {                                
                String owner_name = rs.getString(1);
                String constraint_schema = rs.getString(2);
                String constraint_name = rs.getString(3);
                String constraint_type = rs.getString(4);  // c = check constraint, f = foreign key constraint, p = primary key constraint, u = unique constraint
                String update_rule = rs.getString(5);      // set for foreign key
                String delete_rule = rs.getString(6);      // set for foreign key
                String match_type = rs.getString(7);
                String table_schema_name = rs.getString(8);
                String table_name = rs.getString(9);
                String table_columns = rs.getString(10);
                String ftable_schema_name = rs.getString(11);  // set for foreign key
                String ftable_name = rs.getString(12);         // set for foreign key
                String ftable_columns = rs.getString(13);      // set for foreign key

                // get table on which constraint applies to
                Table table = db.getTable(table_schema_name, table_name);
                if (table!=null) {
                    table_columns = table_columns.replaceAll("\\{", "");
                    table_columns = table_columns.replaceAll("}", "");
                    int iarr[] = toIntegerArray(table_columns, ",");
                    String[] sarr = table.getColumnNamesByOrdinal(iarr);
//                    for (String s:sarr) {
//                        System.out.print(s+ " ");
//                    }
//                    System.out.println();                        

                    if (constraint_type.equals("p")) {
                        table.addPK(sarr);
                        //System.out.print("pk: " + table.name + " ");
                    } else if (constraint_type.equals("u")) {
                        table.addUniqueKey(sarr);
                        //System.out.print("uq: " + table.name + " ");                        
                    }  else if (constraint_type.equals("f")) {                                 
                        // get foreign table
                        Table ftable = db.getTable(ftable_schema_name, ftable_name);
                        if (ftable!=null) {
                            System.out.print("fk: " + table.name + " -> " + ftable.getName() + ": ");               
                            ftable_columns = ftable_columns.replaceAll("\\{", "");
                            ftable_columns = ftable_columns.replaceAll("}", "");
                            int iarrf[] = toIntegerArray(ftable_columns, ",");
                            String[] sarrf = ftable.getColumnNamesByOrdinal(iarrf);                                
//                            for (String s:sarrf) {
//                                System.out.print(s+ " ");
//                            }
//                            System.out.println();                        

                            Schema schema = db.getSchema(constraint_schema);                                
                            schema.addFK(constraint_name, match_type, update_rule, delete_rule, table, sarr, ftable, sarrf, "", "", iarrf, null, Color.black);
                        }
                    }
                }
            }            
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            close(rs, st);
        }                 
    }
    void loadIndexes(Database db, Connection conn) {
        try { 
            String sql = "select auth.rolname, nsidx.nspname, cidx.relname, nstab.nspname, ctab.relname, idx.indkey from pg_index idx\n" +
                            "join pg_class cidx on cidx.oid = idx.indexrelid\n" +
                            "join pg_class ctab on ctab.oid = idx.indrelid\n" +
                            "join pg_namespace nsidx on nsidx.oid = cidx.relnamespace\n" +
                            "join pg_namespace nstab on nstab.oid = ctab.relnamespace\n" +
                            "join pg_authid auth on auth.oid = nsidx.nspowner\n" +
                            "where idx.indisunique = 'f' and idx.indisprimary = 'f'";

            Statement st = conn.createStatement();     
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {            
                String indexOwner = rs.getString(1);              
                String indexSchema = rs.getString(2);              
                String indexName = rs.getString(3);              
                String tableSchema = rs.getString(4);   
                String tableName = rs.getString(5);                       
                String table_columns = rs.getString(6);
                table_columns = table_columns.replaceAll("\\{", "");
                table_columns = table_columns.replaceAll("}", "");
                int iarr[] = toIntegerArray(table_columns, " ");

                Table table = db.getTable(tableSchema, tableName);
                if (table!=null) {
                    String[] sarr = table.getColumnNamesByOrdinal(iarr);
                    table.indices.add(sarr);                        
                }                
            }
            close(st, rs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
       
    List<String> getConstaints(Connection conn, String table, String schema, String contype) {
        // c = check constraint, f = foreign key constraint, p = primary key constraint, u = unique constraint
        // other constraints
        String sql = "select c.conname from \n" +
              " pg_authid a\n" +
              " join pg_namespace n on n.nspowner = a.oid\n" +
              " join pg_constraint c on c.connamespace = n.oid\n" +
              " where a.rolname = '"+table+"' and  n.nspname= '"+schema+"' and contype = '"+contype+"'";
        
        return getStringList(sql, conn);        
    }
      
    public static Connection getConnectionPostgres(String dbName) {

        String url = null;
        String user = null;
        String pwd = null;

        if (edb) {
        }
        else {
            // local
            url = "jdbc:postgresql://localhost:5432";
            // url = "jdbc:postgresql://127.0.0.1:5432";
            
            user = "superuser";
            pwd = "super";                            
//            user = "postgres";
//            pwd = "postgres";                            
            
            // dbName = "postgres";
            dbName = "postgres";
             
        }

        Connection conn = null;
        try {
                Class.forName("org.postgresql.Driver");                        
                conn = DriverManager.getConnection(url + "/" + dbName, user, pwd);						
        } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }				
        return conn;
    }    
    
    public static void recordCount(Model mdl) {        
        Connection c = PgDiagram.getConnectionPostgres("postgres");
        for (Database db:mdl.databases) {
            for(Schema schema:db.schemas) {
                for(Table table:schema.tables.values()) {
                    System.out.println("recordCount: " + table.name);
                    String sql = "select count(*) from " + db.name + "." + schema.name + "." + table.name;
                    // String sql = "select count(*) from " + schema.name + "." + table.name;
                    table.recordCount = getNumber(sql, c);
                }
            }
        }
        close(c);
    }
    
    static String fullyQualify(Object obj) {
        if (obj instanceof Table) {
            Table table = (Table) obj;            
            // return table.schema.db.name + "." + table.schema.name + "." + table.name;
            return table.schema.name + "." + table.name;
        }
        return "";
    }
    
    public static String getJoinStatement(ForeignKey fk, boolean joinOnForeigTable, String fromAlias, String toAlias) {
        String fromTable = fullyQualify(fk.fromTable);
        String toTable = fullyQualify(fk.toTable);

        String sql = "join " + (joinOnForeigTable?toTable + " " + toAlias:fromTable + " " + fromAlias) + " on ";        
        String[] toColsArr = fk.toCols;
        for (int i=0; i<fk.fromCols.length; ++i) {
            if (i>0) {
                sql += " and ";
            }
            if (joinOnForeigTable) {
                sql += toAlias + "." + toColsArr[i] + " = " + fromAlias + "." + fk.fromCols[i];   
            }
            else {
                sql += fromAlias + "." + fk.fromCols[i] + " = " + toAlias + "." + toColsArr[i];
            }
        }
        return sql;
    }
    
    public static void getCardinals(Model mdl) {
        Connection c = PgDiagram.getConnectionPostgres("postgres");
        for (Database db:mdl.databases) {
            for(Schema schema:db.schemas) {
                for(List<ForeignKey> fkList:schema.foreignKeys.values()) {
                    for(ForeignKey fk:fkList) {
                    System.out.println("getCardinals: " + fk.constraint_name);                    
                    
                    String[] toColsArr = fk.toCols;
                    
                    String toAlias = "A";
                    String fromAlias = "B";
                    
                    String toColsStr = toSeperatedString(toColsArr, "|| '#' ||", toAlias + ".");
                    // String fromCols = toSeperatedString(fk.fromCols, "|| '#' ||", toAlias + ".");
                    String join = getJoinStatement(fk, false, fromAlias, toAlias);
                    String sql = "select max(count) from (\n"
                               + "  select count("+toColsStr+") as count from " + fullyQualify(fk.toTable) + " " + toAlias + "\n"
                               + "   " + join + "\n"
                               + "   group by " + toColsStr + "\n"
                               + ") as foo";
                    
                    System.out.println("sql=" + sql);
                    long l = getNumber(sql, c);  
                    if (l==1) {
                        fk.toCardinal = "1";
                    }
                    else if (l>1) {
                        fk.toCardinal = "N";
                    }
                    System.out.println("getCardinals() " + fk.constraint_name + " cardinal " + fk.toTable + ":" +l);
/*
select count(*) from (
select count(postgres.public.sdv_whitelist.id) from postgres.public.sdv_whitelist join postgres.public.sdv_whitelist2ip on postgres.public.sdv_whitelist.id = postgres.public.sdv_whitelist2ip.whitelist
group by postgres.public.sdv_whitelist.id
having count(postgres.public.sdv_whitelist.id) > 1
) as foo                    
                    
                    */
                    }
                }
            }
        }
        close(c);
    }
    
    boolean updateModel(Model org, Model update) {
        
        compareModel(org, update);
        
        return false;
    }
    
    void compareModel(Model m0, Model m1) {
        List<Object> diff = new ArrayList();
        for (Database db0:m0.databases) {
            Database db1 = m1.getDatabase(db0.name);
            if (db1==null) {
                diff.add(db0);
                continue;
            }
            for (Schema schema0:db0.schemas) {
                Schema schema1 = db1.getSchema(schema0.name);
                if (schema1==null) {
                    diff.add(schema0);
                    continue;
                }
                
                for (Table table0:schema0.tables.values()) {                    
                    Table table1 = m1.getTable(db0.name, schema0.name, table0.name);
                    if (table1==null) {
                        diff.add(schema0);
                        continue;
                    }                    
                    // compareTable(table0, table1);
                }
                
//                for (ForeignKey fk0:schema0.foreignKeys) {                    
//                    ForeignKey fk1 = m1.getForeignKey(db0.name, schema0.name, fk0.constraint_name);
//                    if (fk1==null) {
//                        diff.add(fk0);
//                        continue;
//                    }                    
//                    // compareForeignKey();
//                }
            }
        }
    }
    
    void copyLocations(Model model_from, Model model_to) {
        for (Database db:model_to.databases) {
            for (Schema schema_to:db.schemas) {
                for (Table table_to:schema_to.tables.values()) {
                    Table table_from = model_from.getTable(db.name, schema_to.name, table_to.name);
                    if (table_from!=null) {
                        table_to.x = table_from.x;
                        table_to.y = table_from.y;
                    }
                }
            }
        }    
    }
    
    void testConnectionAbort() {
        Connection conn1 = getConnectionPostgres("postgres");
        ExecuteQueryRunnable r = new ExecuteQueryRunnable("select pg_sleep(40000)", conn1);
        Thread t = new Thread(r);
        t.start();
        
        try {
            Thread.currentThread().sleep(1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        try {
            Connection conn2 = getConnectionPostgres("postgres");
            stopCurrentQuery(r, conn2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    void stopCurrentQuery(ExecuteQueryRunnable r, Connection c) throws Exception {        
        String sql = "select procpid from pg_stat_activity where current_query like '"+r.currentQueryID+"%'";
        long procId = getNumber(sql, c);
        Statement st = c.createStatement();
        sql = "select pg_cancel_backend("+procId+")";
        st.execute(sql);                 
    }
    
    class ExecuteQueryRunnable implements Runnable {
        
        String sql;
        Connection conn;
        Statement st;
        ResultSet rs;
        String currentQueryID;
        SQLException ex;
        
        ExecuteQueryRunnable(String sql, Connection conn) {
            this.sql = sql;
            this.conn = conn;
            UUID uuid = UUID.randomUUID();
            currentQueryID = "-- pgDiagram query " + uuid.toString();
        }
        
        public void run() {
            sql =  currentQueryID + "\n" + sql;
            try {
                Statement st = conn.createStatement();
                rs = st.executeQuery(sql);        
            } catch (SQLException e) {
                this.ex = e;
                ex.printStackTrace();
            }                
        }
    }
    
    void testQueryOutput() {
        Connection conn = getConnectionPostgres("postgres");
        String sql1 = "select * from TestTypeNumeric";
        String sql2 = "select * from TestComposite";
        String sql3 = "select * from TestArrays";
        getQueryOutput(sql1, conn);
        getQueryOutput(sql2, conn);
        getQueryOutput(sql3, conn);
        
    }
    void getQueryOutput(String sql, Connection conn) {
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);        

            ResultSetMetaData md = rs.getMetaData();
            int colcount = md.getColumnCount();
            String[] columnNames = new String[colcount];
            for (int i=1;i<=colcount; ++i) {
                //System.out.println(md.getColumnLabel(i) + " " + md.getColumnType(i) + " " + md.getColumnClassName(i));
                columnNames[i-1] = md.getColumnLabel(i);
            }

            List<Object[]> rows = new ArrayList();
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                Object[] row = new Object[colcount];
                for (int i=1;i<=colcount; ++i) {
                    row[i-1] = rs.getObject(i);
                }
                rows.add(row);                
            }
            
            Object arr[][] = new Object[rowCount][colcount];
            for (int i=0; i<rowCount; ++i ) {
                Object[] row = rows.get(i);
                for (int j=0; j<colcount; ++j) {
                    arr[i] = row;
                }                
            }
            
            toTextTable(columnNames, arr);
        } catch (SQLException ex) {            
            ex.printStackTrace();
        }
    }
    
    public static String toTextTable(String[] columns, Object[][] data) {
        
        int colCount = columns.length;
        int colSz[] = new int[colCount];
        
        // determine colum size of header
        for (int i=0; i<colCount; ++i) {
            colSz[i] = columns[i].length();
        }
        
        // determine max col size of data per column
        for (int i=0; i<data.length; ++i) {
            for (int j=0; j<colCount; ++j) {
                int sz = 0;
                if (data[i][j]!=null) {
                    sz = data[i][j].toString().length();
                }            
                colSz[j] = Math.max(colSz[j], sz);
            }
        }

        StringBuilder buff = new StringBuilder();

        String sep = "+";
        for (int j=0; j<colCount; ++j) {
            sep += String.format(" %1$-" + (colSz[j]) + "s ", "");            
            sep += "+";                
        }
        sep = sep.replace(' ', '-');            
        buff.append(sep);
        buff.append("\n");
        // output column        
        buff.append("|");
        for (int i=0; i<colCount; ++i) {            
            buff.append(String.format(" %1$-" + (colSz[i]) + "s ", columns[i]));
            buff.append("|");
        }
        buff.append("\n");
        buff.append(sep);  
        buff.append("\n");
        // data              
        for (int i=0; i<data.length; ++i) {
            buff.append("|");  
            for (int j=0; j<colCount; ++j) {
                buff.append(String.format(" %1$-" + (colSz[j]) + "s ", (data[i][j]==null?"":data[i][j].toString())));
                buff.append("|");                
            }
            buff.append("\n");
        }
        buff.append(sep);
        buff.append("\n");
        System.out.print(buff.toString());                
        
        return buff.toString();
    }
}
