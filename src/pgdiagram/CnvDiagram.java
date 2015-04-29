/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import pgdiagram.HitTestResult.HITTEST;

/**
 *
 * @author nospoon
 */
public class CnvDiagram extends JPanel {
    FraMain fraMain;
    //Model model;
    MoveDiagramMouseHandler moveModeMouseHandler;
    MouseHandlerLayout mouseHandlerLayout;
    MouseHandlerLayoutCG mouseHandlerLayoutCG;
    MouseHandlerQuery mouseHandlerQuery;    
    // 
    MouseHandlerCanvas mouseHandlerCanvas;
    Map<Object, Object> higlightItems = new HashMap();
    Map<Object, Object> selectedItems = new HashMap();    
    JTextArea txtDistinctValues = new JTextArea("12345rewq");
    List<CanvasGraphic> canvasGraphics = new ArrayList();
    List<CanvasGraphic> canvasGraphicsConnections = new ArrayList();
    boolean showFk = true;
    boolean initCanvasGraphicItems = true;
    ForeignKey tmp_fk = null;
    
    CnvDiagram(FraMain fra) {
        this.fraMain = fra;        
        moveModeMouseHandler = new MoveDiagramMouseHandler(this);
        mouseHandlerLayout = new MouseHandlerLayout(this);        
        mouseHandlerLayoutCG = new MouseHandlerLayoutCG(this);
        mouseHandlerQuery = new MouseHandlerQuery(this);        
        //special mouse handler for handling canvas movements withing scrollpane. Should always be registered
        mouseHandlerCanvas = new MouseHandlerCanvas(this);        
        registerMouseListener(mouseHandlerCanvas);
        
        Action escPressedAction = new EscPressedAction(this);
        
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "esc_pressed");
        this.getActionMap().put("esc_pressed", escPressedAction);        
//        this.addMouseMotionListener(moveModeMouseHandler);
        
        txtDistinctValues.setBounds(400, 400, 200, 300);
        txtDistinctValues.setVisible(false);
        // btnTest.setBackground(Color.red);
        this.setLayout(null);
        UtilLayout.add(this, txtDistinctValues);    
    }
    
    public void removeRegisteredMouseListeners() {
        this.removeMouseListener(moveModeMouseHandler);
        this.removeMouseMotionListener(mouseHandlerLayout);
        this.removeMouseWheelListener(mouseHandlerLayout);        
        this.removeMouseListener(mouseHandlerLayout);
        this.removeMouseMotionListener(mouseHandlerLayout);
        this.removeMouseWheelListener(mouseHandlerLayout);                
        this.removeMouseListener(mouseHandlerLayoutCG);
        this.removeMouseMotionListener(mouseHandlerLayoutCG);
        this.removeMouseWheelListener(mouseHandlerLayoutCG);                       
        this.removeMouseListener(mouseHandlerQuery);
        this.removeMouseMotionListener(mouseHandlerQuery);
        this.removeMouseWheelListener(mouseHandlerQuery);               
    }
    
    private void registerMouseListener(MouseAdapter ma) {
        this.addMouseListener(ma);
        this.addMouseMotionListener(ma);
        this.addMouseWheelListener(ma);
    }
    
    public void setShowFKColumns(boolean state) {
        this.showFk = state;
        refresh();
    }
    
    public void setSelectionMode(FraMain.SelectionMode selectionMode) {
        removeRegisteredMouseListeners();
        switch (selectionMode) {
            case OLD:
                registerMouseListener(moveModeMouseHandler);                
                break;            
            case LAYOUT:                
                registerMouseListener(mouseHandlerLayout);                
                break;
            case LAYOUT_CG:                
                registerMouseListener(mouseHandlerLayoutCG);                
                break;                
            case QUERY:
                registerMouseListener(mouseHandlerQuery);                
                break;
        }        
        refresh();
    }     
    
    @Override
    public void paint(Graphics g_) {
        super.paint(g_);    
        Graphics2D g = (Graphics2D) g_;        
        g.setColor(this.getBackground());        
        g.fillRect(this.getX(), this.getY(), this.getWidth(), this.getHeight());

        
        Model model = fraMain.model;
        if (model!=null && initCanvasGraphicItems) {
            initCanvasGraphicItems = false;
            int x = 30;
            int y = 30;
            for (Database db:model.databases) {       
                for (Schema schema:db.schemas) {    
                    for (Table table:schema.tables.values()) {
                        CanvasGraphicTable cgt = new CanvasGraphicTable(table);
                        cgt.bounds.x=x;
                        cgt.bounds.y=y;
                        canvasGraphics.add(cgt);
                        x+=200;
                        if (x>900) {
                            x = 30;
                            y+= 200;
                        }
                    }
                    Table from_table = model.getTable("postgres", "public", "testarrays");
                    String from_cols[] = {from_table.getColumnName(0)};
                    Table to_table = model.getTable("postgres", "public", "testa");
                    String to_cols[] = {to_table.getColumnName(0)};
                    int iarr[] = {0};
                    List<Point> lineControlPoints = new ArrayList();
                    lineControlPoints.add(new Point(800, 300));
                    lineControlPoints.add(new Point(900, 400));
                    lineControlPoints.add(new Point(950, 600));
                    tmp_fk = schema.addFK("test_contstraint123456", "NO MATCH", "CASC_UPDATE", "CASC_DEL", from_table, from_cols, to_table, to_cols, "1", "N", iarr, lineControlPoints, Color.red);
                    CanvasGraphicForeignKey cgfk = new CanvasGraphicForeignKey(tmp_fk);           
                    canvasGraphicsConnections.add(cgfk);
                }
            }
        }                      
        
        CanvasGraphic cg_from = null;
        CanvasGraphic cg_to = null;
        
        for (CanvasGraphic cg:canvasGraphics) {            
            cg.paint(g);
            if (cg instanceof CanvasGraphicTable) {
                CanvasGraphicTable cgt = (CanvasGraphicTable) cg;
                Table table = (Table) cgt.data;
                if (table==tmp_fk.fromTable) {
                    cg_from = cgt;
                }
                else if (table==tmp_fk.toTable) {
                    cg_to = cgt;
                }
            }
        }
        
        for (CanvasGraphic cg:canvasGraphicsConnections) {  
            if (cg instanceof CanvasGraphicForeignKey) {
                CanvasGraphicForeignKey cgfk = (CanvasGraphicForeignKey)cg;
                cgfk.cg_from = cg_from;
                cgfk.cg_to = cg_to;
            }
            cg.paint(g);
        }

        if (moveModeMouseHandler.multiSelect) {
            Rectangle r = moveModeMouseHandler.currentSelectionRect;
            g.setColor(Color.blue);
            g.drawRect(r.x, r.y, r.width, r.height);
        }
        
        if (model!=null) {
            for (Database db:model.databases) {       
                for (Schema schema:db.schemas) {    
                    for (Table table:schema.tables.values()) {
                        boolean highLighted = false;
                        boolean selected = !selectedItems.containsKey(table);
                        if (selected) {
                            highLighted = higlightItems.containsKey(table);
                        }
                        drawTable(table, highLighted, selected, fraMain.selectionMode, g);
                    }

                    for (List<ForeignKey> fkList:schema.foreignKeys.values()) {
                        // draw unhighlighted links first
                        for (ForeignKey fk:fkList) {
                            boolean highLighted = false;
                            boolean selected = !selectedItems.containsKey(fk);
                            if (selected) {
                                highLighted = higlightItems.containsKey(fk);
                            }
                        
                            if (!highLighted && !selected) {
                                drawForeignKey(fk, false, false, g);
                                for (ForeignKeyControlPoint fkcp:fk.linePoints) {                                    
                                    if (FraMain.selectionMode.equals(FraMain.SelectionMode.LAYOUT) || FraMain.selectionMode.equals(FraMain.SelectionMode.OLD)) {
                                        drawControlPoint(fkcp.controlPoint, Color.green, Color.green, g);
                                    } 
                                    else {
                                        drawControlPoint(fkcp.controlPoint, fk.color, null, g);
                                    }                                    
                                }
                            }
                        }
                    }
                    for (List<ForeignKey> fkList:schema.foreignKeys.values()) {
                        // draw highlighted links on top
                        for (ForeignKey fk:fkList) {
                            boolean highLighted = false;
                            boolean selected = !selectedItems.containsKey(fk);
                            if (selected) {
                                highLighted = higlightItems.containsKey(fk);
                            }
                        
                            if (highLighted || selected) {                      
                                drawForeignKey(fk, highLighted, selected , g);
                                for (ForeignKeyControlPoint fkcp:fk.linePoints) {
                                    if (FraMain.selectionMode.equals(FraMain.SelectionMode.LAYOUT) || FraMain.selectionMode.equals(FraMain.SelectionMode.OLD)) {
                                        drawControlPoint(fkcp.controlPoint, highLighted?Color.black:Color.white, Color.white, g);
                                    } 
                                    else {
                                        // drawControlPoint(fkcp.controlPoint, fk.color, null, g);
                                    }
                                }
                            }
                        }                
                    }
                }
            }
        }
        testMasking(g);
        super.paintComponents(g_);    
    }
    
    void drawForeignKey(ForeignKey fk, boolean highLighted, boolean selected, Graphics2D g) {   
        //int widthFactor = 2;       
        if (fk.constraint_name.equals("test_contstraint123456")) {
            return;
        }
        int lineWidth = fk.fromCols.length;
        lineWidth = (Math.min(lineWidth, 5));            
        
            Table fromTable = fk.fromTable;
            Table toTable = fk.toTable;
            if (toTable==null) {
                toTable = fk.toTable;
            }

            Point cpFrom = new Point();
            Point cpTo = new Point();                    
            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;
            
            Color dashedColor0 = null;
            Color dashedColor1 = null;
            Stroke dashed0 = null;
            Stroke dashed1 = null;
            if (highLighted) {                
                dashedColor0 = Color.white;
                dashedColor1 = Color.black;
                dashed0 = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10, new float[] {10, 20}, 0);
                dashed1 = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10, new float[] {10, 20}, 10);
            }
            if (selected) {                
                dashedColor0 = Color.white;
                dashedColor1 = Color.black;                
                lineWidth++;
                dashed0 = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10, new float[] {10, 20}, 0);
                dashed1 = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10, new float[] {10, 20}, 10);
            }            
            
            Stroke stroke = new BasicStroke(lineWidth);
            Color lineColor = fk.color;
            g.setColor(lineColor);
            
            for (String from:fk.fromCols) {
                Rectangle fromRect = fromTable.getColumnRect(from);
                if (fromRect==null) {
                    fromRect = fromTable.getColumnRect(from);
                }
                if (fromRect==null) {
                    int a = 99;
                }

                fromTable.addTableOffsetTorect(fromRect);
                if (fromRect!=null) {                            
                    int x1 = fromRect.x + (fk.startAnchorOffset.orientation==Anchor.AnchorOrientation.LEFT?-1:fromRect.width+1);
                    int y1 = fromRect.y + fromRect.height/2 + fk.startAnchorOffset.offset.y;
                    int x2 = x1 + fk.startAnchorOffset.offset.x;
                    int y2 = y1;

                    g.drawLine(x1, y1, x2, y2);
                    cpFrom.x += x2;
                    cpFrom.y += y2;

                    minY = Math.min(minY, y1);
                    minY = Math.min(minY, y2);
                    maxY = Math.max(maxY, y1);
                    minY = Math.min(minY, y2);
                }
            }

            cpFrom.x /= fk.fromCols.length;
            cpFrom.y /= fk.fromCols.length;

            g.fillRect(cpFrom.x-(lineWidth)/2, minY, lineWidth, Math.abs(maxY-minY)+1);

            minY = Integer.MAX_VALUE;
            maxY = Integer.MIN_VALUE;

            for (String to:fk.toCols) {
                Rectangle toRect = toTable.getColumnRect(to);
                toTable.addTableOffsetTorect(toRect);
                g.setColor(lineColor);               
                if (toRect!=null) {
                    int x1 = toRect.x + (fk.endAnchorOffset.orientation==Anchor.AnchorOrientation.LEFT?-1:toRect.width+1);
                    int y1 = toRect.y + toRect.height/2 + fk.endAnchorOffset.offset.y;
                    int x2 = x1 + fk.endAnchorOffset.offset.x;
                    int y2 = y1;                            

                    g.drawLine(x1, y1, x2, y2);
                    // arrow
                    int arrow_sz = 5;
                    int x3 = x1+ (fk.endAnchorOffset.orientation==Anchor.AnchorOrientation.LEFT?-arrow_sz:+arrow_sz);
                    int x4 = x1+ (fk.endAnchorOffset.orientation==Anchor.AnchorOrientation.LEFT?-arrow_sz:+arrow_sz);
                    int y3 = y2-arrow_sz;
                    int y4 = y2+arrow_sz;

                    int x[] = new int[4];
                    int y[] = new int[4];

                    x[0] = x1;
                    x[1] = x3;
                    x[2] = x4;

                    y[0] = y2;
                    y[1] = y3;
                    y[2] = y4;

                    g.fillPolygon(x,y, 3);

                    cpTo.x += x2;
                    cpTo.y += y2;

                    minY = Math.min(minY, y1);
                    minY = Math.min(minY, y2);
                    maxY = Math.max(maxY, y1);
                    minY = Math.min(minY, y2);                            

                }                    
            }

            cpTo.x /= fk.toCols.length;
            cpTo.y /= fk.toCols.length;

            // System.out.println("22:" + 1+(fk.fromCols.length-1)*widthFactor);
            g.fillRect(cpTo.x-(lineWidth/2), minY, lineWidth, Math.abs(maxY-minY)+1);
            // g.drawLine(cpTo.x, minY, cpTo.x, maxY);  
            Stroke orgStroke = g.getStroke();
            
//            g.setStroke(stroke);
//
//            // g.drawLine(cpFrom.x, cpFrom.y, cpTo.x, cpTo.y);
//            g.setStroke(orgStroke);

            fk.setStartPoint(cpFrom);
            fk.setEndPoint(cpTo);
            Point lastPoint = null;
            for (ForeignKeyControlPoint fkcp:fk.linePoints) {
                if (lastPoint!=null) {
                    g.setStroke(stroke);
                    g.setColor(lineColor);
                    g.drawLine(fkcp.controlPoint.x, fkcp.controlPoint.y, lastPoint.x, lastPoint.y);
                    if (dashed0!=null) {
                        g.setStroke(dashed0);
                        g.setColor(dashedColor0);
                        g.drawLine(fkcp.controlPoint.x, fkcp.controlPoint.y, lastPoint.x, lastPoint.y);
                        g.setStroke(dashed1);
                        g.setColor(dashedColor1);
                        g.drawLine(fkcp.controlPoint.x, fkcp.controlPoint.y, lastPoint.x, lastPoint.y);                        
                    }                    
                }
                
                g.setStroke(orgStroke);
                g.setColor(Color.green);
//                if (moveModeMouseHandler.hitTestResult!=null && moveModeMouseHandler.hitTestResult.controlPoint==fkcp.controlPoint) {
//                   g.setColor(FraMain.colorHighLightFg);
//                }                
                lastPoint = fkcp.controlPoint;
            }
            
//            for (ForeignKeyControlPoint fkcp:fk.linePoints) {
//                drawControlPoint(fkcp.controlPoint, Color.green, Color.green, g);
//            }
            g.setStroke(orgStroke);
    }
    
    
    void testMasking(Graphics2D g) {
        
     //   AlphaComposite ac = AlphaComposite.getInstance(WIDTH);
        
//        GradientPaint gp = new GradientPaint(0,0, Color.red, 400,400, Color.green);
//        g.setPaint(gp);
        // g.fillRect(0,0, 400, 400);
//        g.drawRect(0,0, 400, 400);
//        g.drawLine(0,0, 400, 400);
        
        Color colorStart = Color.yellow;
        Color colorEnd = Color.blue;
        int distance = 1300;
        Point[] arr = new Point[4];
        arr[0] = new Point(20,20);
        arr[1] = new Point(400,20);
        arr[2] = new Point(400,400);
        arr[3] = new Point(100,400);
        
        drawLineSegments(arr, colorStart, colorEnd, distance, g);
        
        colorStart = Color.green;
        colorEnd = new Color(1,1,1,0);
        distance = 50;
        arr = new Point[2];
        arr[0] = new Point(400,400);
        arr[1] = new Point(100,400);        
        drawLineSegments(arr, colorStart, colorEnd, distance, g);
        arr = new Point[2];
        arr[0] = new Point(400,400);
        arr[1] = new Point(400,20);     
        drawLineSegments(arr, colorStart, colorEnd, distance, g);        
    }
    
    public static void drawLineSegments(Point[] arr, Color colorStart, Color colorEnd, int distance, Graphics2D g) {
        
        //int distance_pow = distance^2;
        Paint c[] = new Paint[3];
        
        double curr_len = 0;
        for (int idx=0; idx<arr.length-1; ++idx) {
            Point p0 = arr[idx];
            Point p1 = arr[idx+1];
            
            double deltax = p1.x - p0.x;
            double deltay = p1.y - p0.y;
            
            double len = Math.sqrt(Math.pow(deltax,2) + Math.pow(deltay,2));                    
            
            double deltaxn = deltax / len;
            double deltayn = deltay / len;
            
            if (curr_len<distance) {
                double g0x = p0.x + curr_len * -deltaxn;
                double g0y = p0.y + curr_len * -deltayn;
                double dist_left = distance-curr_len;
                if (dist_left<0) {
                    dist_left =0;
                }
                double g1x = p0.x + (dist_left) * deltaxn;
                double g1y = p0.y + (dist_left) * deltayn;

                c[idx] = new GradientPaint((int)g0x, (int)g0y, colorStart, (int)g1x, (int)g1y, colorEnd);      
                // System.out.println((int)g0x + "," + (int)g0y + " - " + (int)g1x + "," + (int)g1y);
            }
            else {
                c[idx]  = colorEnd;
            }
            
            curr_len += len;
            
        }
        drawPolyLine(arr, c, g);
    }
        
    public static void drawPolyLine(Point points[], Paint gradients[], Graphics2D g) {    
        
        for (int idx=0; idx<points.length-1; ++idx) {
            
            Point p0 = points[idx];
            Point p1 = points[idx+1];
            g.setPaint(gradients[idx]);
            g.drawLine(p0.x, p0.y, p1.x, p1.y);
        }
    }
    
    void drawLineSegment(int x0, int y0, int x1, int y1) {
        
    }
    
    void drawControlPoint(Point p, Color innerColor, Color outerColor, Graphics g) {
        int innerOffset = 1;
        int outerOffset = 3;         
        if (innerColor!=null) {
            g.setColor(innerColor);
            g.fillRect(p.x-innerOffset, p.y-innerOffset, innerOffset*2, innerOffset*2);
        }
        if (outerColor!=null) {
            g.setColor(outerColor);
            g.drawRect(p.x-outerOffset, p.y-outerOffset, outerOffset*2-1, outerOffset*2-1);
        }
    }
    
    void drawTable(Table table, boolean highLighted, boolean selected, FraMain.SelectionMode selectionMode, Graphics2D g) {           
        int ascent = g.getFontMetrics().getAscent();
        
        Rectangle headerRect = table.getHeaderRect(table);
        int offsetX = 0;
        int offsetY = headerRect.y + headerRect.height;
        List<Rectangle> columnsRect = table.getColumnsRect(table, offsetX, offsetY);
        Column[] orderedColumns = table.getColumnsInOrder();
        
        Rectangle totalRect = table.getTotalRect();
        // draw
        int xOffset = table.x;
        int yOffset = table.y;
        g.translate(xOffset, yOffset);
        g.setColor(FraMain.colorTableHeader);
        if (moveModeMouseHandler.multiSelectedtables.get(table)!=null) {
            g.setColor(Color.yellow);
        }
        
        g.fillRect(headerRect.x, headerRect.y, headerRect.width, headerRect.height);
        g.setColor(FraMain.colorTableName);
        if (higlightItems.containsKey(table)) {
            switch (selectionMode) {                                
                case OLD:
                case LAYOUT:
                    break;
                case QUERY:
                    g.setColor(FraMain.colorTableNameHiglighted);                                              
            }
        }
        if (selectedItems.containsKey(table)) {
            switch (selectionMode) {                                
                case OLD:
                case LAYOUT:
                    break;
                case QUERY:
                    g.setColor(FraMain.colorTableNameSelected);
            }
        }
        
        g.drawString(table.name + (table.recordCount>-1?" (" + table.recordCount + ")":""), headerRect.x, headerRect.y+ascent);
                
        int idx = -1;
        for (Rectangle r:columnsRect) {        
            idx++;
            Column col = orderedColumns[idx];
            if (col!=null) {                 
                g.setColor(idx%2==0?FraMain.colorColumnRowEven:FraMain.colorColumnRowOdd);
                g.fillRect(r.x, r.y, r.width, r.height);
                if (col.name!=null) {
                    g.setColor(FraMain.colorColumnFg);
                    
                    if (!showFk) {
                        if (isColFk(col)) {
                            g.setColor(Color.black);
                        }
                    }
                    // if (moveModeMouseHandler.hitTestResult!=null && moveModeMouseHandler.hitTestResult.column==col)  {
                    if (higlightItems.containsKey(col)) {
                        switch (selectionMode) {                                
                            case OLD:
                            case LAYOUT:
                                g.setColor(FraMain.colorHighLightFg);
                                break;
                            case QUERY:
                                g.setColor(FraMain.colorColumnFgHightlighted);
                        }                                                
                    }                    
                    if (selectedItems.containsKey(col)) {                        
                        switch (selectionMode) {                                
                            case OLD:
                            case LAYOUT:
                                g.setColor(FraMain.colorHighLightFg);
                                break;
                            case QUERY:
                                g.setColor(FraMain.colorColumnFgSelected);
                        }                          
                    }  
                    g.drawString(col.name, r.x, r.y+ascent);
                    int distinctCount = 0;
                    if (col.distinctValues!=null) {
                        distinctCount = col.distinctValues.size();
                    }
                    g.drawString(col.name + (distinctCount==0?"":" {" + distinctCount + "}") , r.x, r.y+ascent);                    
                }
            }
        }        
        
        g.setColor(Color.lightGray);
        // constraints
        int constraint_x_offset = 150;
        int constraint_x_width = 20;
        boolean move_constraint = false;
        for(String[] cols:table.primaryKeys) {
            if (move_constraint) {
                constraint_x_offset += constraint_x_width;
                move_constraint = false;
            }
            for (String colName:cols) {
                Rectangle r=table.getColumnRect(colName);
                if (r!=null) {
                     g.drawString("PK", r.x+constraint_x_offset, r.y+ascent);    
                     move_constraint = true;
                }
            }
        }
               
        for(String[] cols:table.uniqueKeys) {
            if (move_constraint) {
                constraint_x_offset += constraint_x_width;
            }
            for (String colName:cols) {
                Rectangle r=table.getColumnRect(colName);
                if (r!=null) {
                     g.drawString("UQ", r.x+constraint_x_offset, r.y+ascent);    
                     move_constraint = true;
                }
            }
        }            

        for(String[] cols:table.indices) {
            if (move_constraint) {
                constraint_x_offset += constraint_x_width;
            }
            for (String colName:cols) {
                Rectangle r=table.getColumnRect(colName);
                if (r!=null) {
                     g.drawString("IDX", r.x+constraint_x_offset, r.y+ascent);    
                     move_constraint = true;
                }
            }
        }       
        // null allowed
        if (move_constraint) {
            constraint_x_offset += constraint_x_width;
        }
        for (Column col:table.columns.values()) {           
            Rectangle r=table.getColumnRect(col.name);
            if (r!=null) {
                if (col.isNotNull) {
                    g.drawString("NOT NULL", r.x+constraint_x_offset, r.y+ascent);    
                    move_constraint = true;
                }
            }
        }          
        
        g.setColor(Color.green);
        g.drawRect(totalRect.x, totalRect.y, totalRect.width, totalRect.height);
        

        // table border
        Color c_hl = null;
        if (selected) {
            switch (selectionMode) {                                
                case OLD:
                case LAYOUT:
                    c_hl = Color.cyan; // FraMain.colorHighLightFg.brighter();            
                    break;
                case QUERY:
                    c_hl = Color.gray;
            }            
        }
        else if (highLighted) {
            switch (selectionMode) {                                
                case OLD:
                case LAYOUT:
                    c_hl = FraMain.colorHighLightFg;
                    break;
                case QUERY:
                    c_hl = Color.gray;
            }            
        }
        else {
            switch (selectionMode) {                                
                case OLD:
                case LAYOUT:
                    c_hl = Color.green;
                    break;
                case QUERY:
                    c_hl = Color.gray;
            }                          
        }
        
        if (c_hl!=null) {
            g.setColor(c_hl);            
            g.drawRect(totalRect.x, totalRect.y, totalRect.width, totalRect.height);
        }
        
        g.translate(-xOffset, -yOffset);
    }

    boolean isColFk(Column col) {        
        Table t = col.table;
        List<ForeignKey> fkList = t.schema.getKFListFromTable(t.getName());
        for (ForeignKey fk:fkList) {
            for (String colName:fk.fromCols) {
                if (colName.equals(col.name)) {
                    return true;
                }
            }            
        }
        return false;
    }
    
    HitTestResultOld_ hitTestOld(int x, int y) {
        
        HitTestResultOld_ hit = new HitTestResultOld_();
        Model model = fraMain.model;
        if (model==null) {
            return hit;
        }        
        
        for (Database db:model.databases) {       
            for (Schema schema:db.schemas) {    
                for (Table table:schema.tables.values()) {
                    Rectangle rect = table.getTotalRect();
                    rect.x += table.x;
                    rect.y += table.y;

                    if (rect.contains(x, y)) {
                        // System.out.println("HitTest: table " + table.getName());
                        hit.table = table;

                        Column col = table.getColumnRect(x, y);
                        if (col!=null) {
                            hit.column = col;
                            System.out.println("HitTest: column " + col.name);
                        }
                    }
                }        

                double min_dist = 5;
                Point hitPoint = new Point(x,y);
                for (List<ForeignKey> fkList:schema.foreignKeys.values()) {
                    for(ForeignKey fk:fkList) {
                        Point lastPoint = null;
                        int lineSection = -2;
                        for (ForeignKeyControlPoint fkcp:fk.linePoints) {
                            lineSection++;
                            if (lastPoint!=null) {
                                double dist = distanceToLine(lastPoint, fkcp.controlPoint, hitPoint);
        //                        if (fk.toTableName.equals("actor")) {
        //                            if (fk.fromTableName.equals("po_related_transaction")) {
        //                                // dist = distanceToLine(new Point(2,0), new Point(4,2), new Point (10,0));
        //                            }
        //                        }
                                if (dist<min_dist) {
                                    min_dist = dist;
                                    hit.hitForeignKey = fk;
                                    hit.foreignKeyLineSection = lineSection;
                                    //System.out.println("HitTest: link 2: dist to line: " + dist + " " + fk.fromTableName + " -> " + fk.toTableName + " section:" + hit.foreignKeyLineSection);
                                    int current_point_idx = -1;
                                    int point_idx = -1;
                                    for (ForeignKeyControlPoint fkcp1:fk.linePoints) {
                                        current_point_idx++;
                                        dist = distanceToPoint(fkcp1.controlPoint, hitPoint);                                
                                        //System.out.println("HitTest: link 2a: dist to point: " + dist);
                                        if (dist<5) {
                                            point_idx = current_point_idx;
                                            min_dist = dist;
                                            hit.controlPoint = fkcp1.controlPoint;
                                            //System.out.println("HitTest: link 3: " + fk.fromTableName + " -> " + fk.toTableName + " controlPoint:" + fkcp1.controlPoint.toString());
                                        }
                                    }
                                    if (point_idx==0) {
                                        hit.anchor = fk.startAnchorOffset;
        //                                oldAnhorOffset.x = fk.startAnchorOffset.offset.x;
        //                                oldAnhorOffset.y = fk.startAnchorOffset.offset.y;
                                    }
                                    else {
                                        if (point_idx==fk.linePoints.size()-1) {
                                            hit.anchor = fk.endAnchorOffset;
        //                                    oldAnhorOffset.x = fk.endAnchorOffset.offset.x;
        //                                    oldAnhorOffset.y = fk.endAnchorOffset.offset.y;
                                        }
                                    }
                                }
                            }
                            lastPoint = fkcp.controlPoint;                            
                        }
                    }
                }
            }
        }
        return hit;    
    }    
    
    List<SectionItem> hitTestCanvasGraphics(int x, int y, List<HITTEST> test) {
        List<SectionItem> list = new ArrayList();
        for (CanvasGraphic cg:canvasGraphics) {
            cg.hitTest(x, y, list);
        }        
        return list;
    }
    
    HitTestResult hitTest(int x, int y, List<HITTEST> test) {        
        HitTestResult hit = new HitTestResult();     
        Model model = fraMain.model;
        if (model==null) { 
            return hit;
        }
        
        for (Database db:model.databases) {       
            for (Schema schema:db.schemas) {           
                if (test.contains(HITTEST.TABLE) || test.contains(HITTEST.COLUMN) ) {
                    for (Table table:schema.tables.values()) {
                        Rectangle rect = table.getTotalRect();
                        rect.x += table.x;
                        rect.y += table.y;

                        if (rect.contains(x, y)) {                    
                            if (test.contains(HITTEST.TABLE)) {
                                hit.tables.add(table);
                            }
                            if (test.contains(HITTEST.COLUMN)) {
                                Column col = table.getColumnRect(x, y);
                                if (col!=null) {
                                    hit.columns.add(col);
                                    // System.out.println("HitTest: column " + col.name);
                                }
                            }
                        }
                    }       
                }
                if (test.contains(HITTEST.FOREIGNKEY)) {

                    int MIN_DISTANCE_TO_LINE = 5;
                    int MIN_DISTANCE_TO_POINT = 5;
                    Point hitPoint = new Point(x,y);
                    for (List<ForeignKey> fkList:schema.foreignKeys.values()) {
                        for(ForeignKey fk:fkList) {
                            Point lastPoint = null;
                            int lineSection = -2;
                            for (ForeignKeyControlPoint fkcp:fk.linePoints) {
                                lineSection++;
                                if (lastPoint!=null) {
                                    double dist = distanceToLine(lastPoint, fkcp.controlPoint, hitPoint);
                                    if (dist<MIN_DISTANCE_TO_LINE) {
                                        // min_dist = dist;
                                        hit.foreignKeys.add(fk);
                                        System.out.println("HitTest: link 2: dist to line: " + dist + " " + fk.fromTable.name + " -> " + fk.toTable.name + " section:" + lineSection);
                                        if (test.contains(HITTEST.FOREIGNKEY_CONTROLPOINT)) {
                                        // hit.foreignKeyLineSection = lineSection;

                                            int current_point_idx = -1;
                                            int point_idx = -1;
                                            for (ForeignKeyControlPoint fkcp1:fk.linePoints) {
                                                current_point_idx++;
                                                dist = distanceToPoint(fkcp1.controlPoint, hitPoint);                                
                                                System.out.println("HitTest: link 2a: dist to point: " + dist);
                                                if (dist<MIN_DISTANCE_TO_POINT) {
                                                    point_idx = current_point_idx;
                                                    // min_dist = dist;
                                                    hit.foreignKeyControlPoints.add(fkcp1);
                                                    System.out.println("HitTest: link 3: " + fk.fromTable.name + " -> " + fk.toTable.name + " controlPoint:" + fkcp1.controlPoint.toString());
                                                }
                                            }
        //                                    if (point_idx==0) {
        //                                        hit.anchor = fk.startAnchorOffset;
        //        //                                oldAnhorOffset.x = fk.startAnchorOffset.offset.x;
        //        //                                oldAnhorOffset.y = fk.startAnchorOffset.offset.y;
        //                                    }
        //                                    else {
        //                                        if (point_idx==fk.linePoints.size()-1) {
        //                                            hit.anchor = fk.endAnchorOffset;
        //        //                                    oldAnhorOffset.x = fk.endAnchorOffset.offset.x;
        //        //                                    oldAnhorOffset.y = fk.endAnchorOffset.offset.y;
        //                                        }
        //                                    }
                                        }
                                        continue;
                                    }
                                }
                                lastPoint = fkcp.controlPoint;                            
                            }
                        }
                    }
                }
            }
        }
        return hit;    
    }        
    
    double distanceToPoint(Point p0, Point p1) {
        double vx = p1.x-p0.x;
        double vy = p1.y-p0.y;        
        double len = Math.sqrt(Math.pow(vx,2) + Math.pow(vy,2));
        return len;        
    }
    
    double distanceToLine(Point p0, Point p1, Point p) {       
        
//        p0 = new Point(2,0);
//        p1 = new Point(4,2);
//        p = new Point (20,0);
        
        double vx = p1.x-p0.x;
        double vy = p1.y-p0.y;
      
        double nvx = vx;
        double nvy = vy;
        double vlen = Math.sqrt(Math.pow(vx,2) + Math.pow(vy,2));
        
        if (vlen==0) {
            // points have same position
        }
        else {
            nvx /= vlen;
            nvy /= vlen;
        }
        
        double vpx = p.x - p0.x;
        double vpy = p.y - p0.y;
        
        double plen = Math.sqrt(Math.pow(vpx,2) + Math.pow(vpy,2));
        
        double nvpx = vpx / plen;
        double nvpy = vpy / plen;
        
        
        // dot
        double dotP = nvpx*nvx + nvpy*nvy;
        //float projRatio = dotP / length(v);
        double projx = p0.x + (dotP*nvx)*plen;
        double projy = p0.y + (dotP*nvy)*plen;
        
        // 'cap' projected point
        double p0pjDistance = Math.sqrt(Math.pow(p0.x-projx,2) + Math.pow(p0.y-projy,2));
        double p1pjDistance = Math.sqrt(Math.pow(p1.x-projx,2) + Math.pow(p1.y-projy,2));
        
        if (p0pjDistance>vlen || p1pjDistance>vlen) {
            if (p0pjDistance<p1pjDistance) {
                projx = p0.x;
                projy = p0.y;
            }
            else {
                projx = p1.x;
                projy = p1.y;
            }
        }
        // distance calculation
        double distance = Math.sqrt(Math.pow(p.x-projx,2) + Math.pow(p.y-projy,2));
        return distance;
    }
    
    public void refresh() {
        this.invalidate();
        this.revalidate();
        this.repaint();
    }
    
    class HitTestResultOld_ {
        Table table;
        Column column;
        ForeignKey hitForeignKey = null;
        int foreignKeyLineSection = -1; 
        Point controlPoint;
        Anchor anchor;
        
        public boolean isNothingSelected() {
            if (table==null && column==null && hitForeignKey==null) {
                return true;
            }
            return false;
        }
    }

    public void addItemToSelected(Object obj) {
        higlightItems.put(obj, obj);
        refresh();
        
//        if (!higlightItems.containsKey(obj)) {
//            higlightItems.put(obj, hs);
//            HighLightAnimationRunnable r = new HighLightAnimationRunnable(obj, this, hs);
//            Thread t = new Thread(r);
//            t.start();
//        }
        
    }
    
   public void addItemToHighlight(Object obj) {
        higlightItems.put(obj, obj);        
    }

    public void removeItemsToHighlight() {
       higlightItems.clear();        
    }    

    public void addSelectedItem(Object obj) {
        selectedItems.put(obj, obj);       
    }        
    
    public void removeSelectedItem(Object obj) {        
        selectedItems.remove(obj);       
    }         
    
    
    public void removeSelectedItems() {
        selectedItems.clear();        
    }                
}

class EscPressedAction extends AbstractAction   {
    
    CnvDiagram cnv;   
    public EscPressedAction(CnvDiagram cnv) {
        this.cnv = cnv;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        e.getActionCommand();
        // int i = e.getKeyCode();
        int i = 0;
        System.out.println("keyPressed :" + i);
        cnv.mouseHandlerLayoutCG.revertDrag();
    }

}
