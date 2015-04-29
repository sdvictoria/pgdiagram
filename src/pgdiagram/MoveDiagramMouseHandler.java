/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

/**
 *
 * @author nospoon
 */
public class MoveDiagramMouseHandler extends MouseAdapter {


    
    CnvDiagram cnv;
    
    MoveDiagramMouseHandler(CnvDiagram cnv) {
        this.cnv = cnv;
    }
    
    int mousePressX, mousePressY;
    int oldTableX, oldTableY;
    Point oldControlPoint = new Point();
    Point oldAnhorOffset = new Point();
    Point lastMouseDragLocation = new Point();
    Point currentMultiSelectEnd = new Point();
    Point oldViewportPosition = new Point();
    public CnvDiagram.HitTestResultOld_ hitTestResult = null;    
    public boolean multiSelect;
    public Rectangle currentSelectionRect = new Rectangle();
    public Map<Table, Point> multiSelectedtables = new HashMap();
    public Map<Point, Point> selectedControlPoints = new HashMap();
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e); //To change body of generated methods, choose Tools | Templates.    
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        boolean repaint = false;
        mousePressX = e.getX();
        mousePressY = e.getY();        
        JViewport vp = cnv.fraMain.scrCanvas.getViewport();         
        oldViewportPosition.x = vp.getX();
        oldViewportPosition.y = vp.getY();
        
        
        lastMouseDragLocation.x = e.getX();        
        lastMouseDragLocation.y = e.getY();
        
        hitTestResult = cnv.hitTestOld(e.getX(), e.getY());
        
        // reset initial positions
        for (Map.Entry<Table, Point> entry:multiSelectedtables.entrySet()) {
            Table table = entry.getKey();
            Point oldPoint = entry.getValue();
            oldPoint.x = table.x;
            oldPoint.y = table.y;
        }

        for (Map.Entry<Point, Point> entry:selectedControlPoints.entrySet()) {
            Point cp = entry.getKey();
            Point oldPoint = entry.getValue();
            oldPoint.x = cp.x;
            oldPoint.y = cp.y;
        }        
        
        if (hitTestResult.isNothingSelected()) {
            if (!SwingUtilities.isMiddleMouseButton(e)) {
                multiSelect = true;
                multiSelectedtables.clear();
                selectedControlPoints.clear();
                return;
            }
        }
        multiSelect = false;
        
        if (hitTestResult.table!=null) {
            oldTableX = hitTestResult.table.x;
            oldTableY = hitTestResult.table.y;
            
            if (hitTestResult.column!=null) {
                // PgDiagram.fetchDistinctValues(hitTestResult.column);
                StringBuilder buff = new StringBuilder();
                for (String s:hitTestResult.column.distinctValues) {
                    if (buff.length()>0) {
                        buff.append("\n");
                    }
                    buff.append(s);
                }
                if (buff.length()>0) {
                    this.cnv.txtDistinctValues.setVisible(true);
                    this.cnv.txtDistinctValues.setText(buff.toString());
                    this.cnv.txtDistinctValues.setLocation(e.getX(), e.getY());
                    
                }
                else {
                    this.cnv.txtDistinctValues.setVisible(false);
                }
            }    
            else {
                this.cnv.txtDistinctValues.setVisible(false);
            }
        }        
        
        if (hitTestResult.hitForeignKey!=null) {
            String r = Integer.toHexString(hitTestResult.hitForeignKey.color.getRed());            
            String g = Integer.toHexString(hitTestResult.hitForeignKey.color.getGreen());
            String b = Integer.toHexString(hitTestResult.hitForeignKey.color.getBlue());
            if (r.length()==1) { r = "0" + r; }
            if (g.length()==1) { g = "0" + g; }
            if (b.length()==1) { b = "0" + b; }    
            String colorCode = "0x"+r+g+b;
            colorCode = colorCode.toUpperCase();
            cnv.fraMain.txtColor.setText(colorCode);
            if (e.isControlDown() && !e.isShiftDown()) {
                // hitTestResult.hitForeignKey.addControlPoint(hitTestResult.foreignKeyLineSection-1, e.getPoint());
                Point np = new Point(e.getPoint());
                System.out.println("hitTestResult.foreignKeyLineSection: " + hitTestResult.foreignKeyLineSection);
                hitTestResult.hitForeignKey.addControlPoint(hitTestResult.foreignKeyLineSection+1, np);
                hitTestResult.controlPoint = np;
                repaint = true;
            }            
        }
        
        if (hitTestResult.controlPoint!=null) {
            if (e.isControlDown() && e.isShiftDown()) {                
                hitTestResult.hitForeignKey.removeControlPoint(hitTestResult.controlPoint);
                hitTestResult.controlPoint = null;
                repaint = true;
            }
            else {
                oldControlPoint.x = hitTestResult.controlPoint.x;
                oldControlPoint.y = hitTestResult.controlPoint.y;                
            }
        }
 
        if (hitTestResult.anchor != null) {
            oldAnhorOffset.x = hitTestResult.anchor.offset.x;
            oldAnhorOffset.y = hitTestResult.anchor.offset.y;
        }
        
        // show table info if one table is selected
        if (hitTestResult.table!=null) {
            this.cnv.fraMain.tableSelected(hitTestResult.table);
        }
        
        if (repaint) {
            cnv.refresh();
        }        
    }
        
    @Override
    public void mouseDragged(MouseEvent e) {
        
        if (hitTestResult==null) {
            return;
        }
        
        boolean repaint = false;
        int diffX = e.getX() - mousePressX;
        int diffY = e.getY() - mousePressY;
        
        if (e.isControlDown()) {
            if (Math.abs(diffX)>Math.abs(diffY)) {
                diffY=0;
            }
            else {
               diffX=0;
            }
        }
        
       
        if (multiSelect) {        
            int x = diffX>0?mousePressX:e.getX();
            int y = diffY>0?mousePressY:e.getY();                               
            currentSelectionRect.x = mousePressX;
            currentSelectionRect.y = mousePressY;
            currentSelectionRect.width = Math.abs(diffX);
            currentSelectionRect.height = Math.abs(diffY);
            repaint = true;
        
        } else if (multiSelectedtables.size()>0 || selectedControlPoints.size()>0) {
            for (Map.Entry<Table, Point> entry:multiSelectedtables.entrySet()) {
                Table table = entry.getKey();
                Point oldPoint = entry.getValue();
                int newX = oldPoint.x + diffX;
                int newY = oldPoint.y + diffY;                
                boolean grid = true;
                if (grid) {
                    newX =  (newX / 10) * 10;
                    newY =  (newY / 10) * 10;
                }
                table.x = newX;
                table.y = newY;
                repaint = true;
            }        
            for (Map.Entry<Point, Point> entry:selectedControlPoints.entrySet()) {
                Point cp = entry.getKey();
                Point oldPoint = entry.getValue();
                int newX = oldPoint.x + diffX;
                int newY = oldPoint.y + diffY;                
                boolean grid = true;
                if (grid) {
                    newX =  (newX / 10) * 10;
                    newY =  (newY / 10) * 10;
                }
                cp.x = newX;
                cp.y = newY;
                repaint = true;
            }              
        } else if (hitTestResult.table!=null) {
            int newX = oldTableX + diffX;
            int newY = oldTableY + diffY;
            boolean grid = true;
            if (grid) {
                newX =  (newX / 10) * 10;
                newY =  (newY / 10) * 10;
            }
            hitTestResult.table.x = newX;
            hitTestResult.table.y = newY;  
            repaint = true;
        } else if (hitTestResult.anchor!=null) {
            int anchorX = diffX + oldAnhorOffset.x;
            if (hitTestResult.anchor.orientation==Anchor.AnchorOrientation.RIGHT) {
                anchorX = diffX + oldAnhorOffset.x;
            }
            int anchorY = oldAnhorOffset.y + diffY;
            
            Table table = hitTestResult.anchor.startAnchor?hitTestResult.hitForeignKey.fromTable:hitTestResult.hitForeignKey.toTable;
            if (table!=null) {
                if (hitTestResult.anchor.orientation==Anchor.AnchorOrientation.RIGHT && e.getX() < table.x) {
                    hitTestResult.anchor.flip();
                    mousePressX -= table.totalRect.width;
                    mouseDragged(e);
                    return;
                }
                if (hitTestResult.anchor.orientation==Anchor.AnchorOrientation.LEFT && e.getX() > table.x + table.totalRect.width) {
                    hitTestResult.anchor.flip();
                    mousePressX += table.totalRect.width;
                    mouseDragged(e);
                    return;                        
                }
            }            
            
            boolean grid = true;
            if (grid) {
                if (Math.abs(anchorX) < 20) {
                    anchorX = (anchorX / 2) * 2;
                }
                else {
                    anchorX = (anchorX / 10) * 10;
                }
                anchorY = (anchorY / 2) * 2;                
//                if (hitTestResult.anchor.orientation==Anchor.AnchorOrientation.RIGHT) {                
//                    anchorX = Math.max(anchorX, 0);                
//                }
//                else {
//                    anchorX = Math.min(anchorX, 0);
//                }
                anchorY = Math.max(anchorY, -10);
                anchorY = Math.min(anchorY, 10);
            }
            hitTestResult.anchor.offset.x = anchorX;
            hitTestResult.anchor.offset.y = anchorY;
            repaint = true;
        } else if (hitTestResult.controlPoint!=null) {
            hitTestResult.controlPoint.x = oldControlPoint.x + diffX;
            hitTestResult.controlPoint.y = oldControlPoint.y + diffY;                       
            
            boolean gridX = true;
            boolean gridY = true;
            Point cp = hitTestResult.controlPoint;
            int idx = hitTestResult.hitForeignKey.linePoints.indexOf(cp);
            if (idx>0) {
                Point p = hitTestResult.hitForeignKey.linePoints.get(0).controlPoint;
                int allignMargin = 6;
                if (Math.abs(p.x-cp.x)<allignMargin) { 
                    hitTestResult.controlPoint.x = p.x;
                    gridX = false;
                }
                if (Math.abs(p.y-cp.y)<allignMargin) { 
                    hitTestResult.controlPoint.y = p.y;
                    gridY = false;
                }
            }
            
            if (idx<hitTestResult.hitForeignKey.linePoints.size()-1) {
                Point p = hitTestResult.hitForeignKey.linePoints.get(idx+1).controlPoint;
                int allignMargin = 6;
                if (Math.abs(p.x-cp.x)<allignMargin) { 
                    hitTestResult.controlPoint.x = p.x;
                    gridX = false;
                }
                if (Math.abs(p.y-cp.y)<allignMargin) { 
                    hitTestResult.controlPoint.y = p.y;
                    gridY = false;
                }
            }
            
            if (gridX) {
                hitTestResult.controlPoint.x =  (hitTestResult.controlPoint.x / 10) * 10;
            }
            if (gridY) {
                hitTestResult.controlPoint.y =  (hitTestResult.controlPoint.y / 10) * 10;
            }
            repaint = true;
        }
        if (repaint) {
            cnv.refresh();
        }    
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
//        //System.out.println("mouseMoved");
//        CnvDiagram.HitTestResultOld tmpHit = cnv.hitTestOld(e.getX(), e.getY());
//        if (tmpHit.controlPoint!=null) {
//            highLightMouseOver(tmpHit.controlPoint);
//            return;
//        } else if (tmpHit.table!=null) {
//            if (tmpHit.column==null) {
//                highLightMouseOver(tmpHit.table);
//            } 
//            else {
//                highLightMouseOver(tmpHit.column);
//            }            
//            return;
//        }
//        highLightMouseOver(null);        
    }
    
    public void highLightMouseOver(Object obj) {
        cnv.removeItemsToHighlight();      
        if (obj!=null) {
            cnv.addItemToHighlight(obj);
        }
//        else {
//            cnv.removeAllItemsToHighlight();            
//        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        //System.out.println("mouseReleased");
        int diffX = e.getX() - mousePressX;
        int diffY = e.getY() - mousePressY;
        
        int x = diffX>0?mousePressX:e.getX();
        int y = diffY>0?mousePressY:e.getY();       
        // 
        if (multiSelect) {
            
            for (Database db:cnv.fraMain.model.databases) {
                for (Schema schema:db.schemas) {            
            
                    Rectangle r = new Rectangle(mousePressX, mousePressY, Math.abs(diffX), Math.abs(diffY));
                    for (Table table:schema.tables.values()) {
                        //if (table.name.equals("actor")) {
                            if (r.intersects(table.getTotalRect(true))) {
                                multiSelectedtables.put(table, new Point(table.x, table.y));
                            }
                        //}
                    }

                    for (List<ForeignKey> fklist:schema.foreignKeys.values()) {
                        for (ForeignKey fk:fklist) {
                            int idx=-1;
                            for (ForeignKeyControlPoint fkcp:fk.linePoints) {
                                idx++;
                                if (idx>0 && idx<fk.linePoints.size()-1) {
                                    if (r.contains(fkcp.controlPoint)) {
                                        selectedControlPoints.put(fkcp.controlPoint, new Point(fkcp.controlPoint));
                                        System.out.println("Added point " + fkcp.controlPoint.toString());
                                    }
                                }
                            }
                        }
                    }            
                }
            }
        }        
        multiSelect = false;
        hitTestResult = null;
        cnv.refresh();
    }
    
//    @Override
//    public void mouseClicked(MouseEvent e) {
//        //System.out.println("mouseClicked");
//    }    
//

//
//    @Override
//    public void mouseEntered(MouseEvent e) {
//        //System.out.println("mouseEntered");
//    }
//
//    @Override
//    public void mouseExited(MouseEvent e) {
//        //System.out.println("mouseExited");
//    }      
    
    
}
