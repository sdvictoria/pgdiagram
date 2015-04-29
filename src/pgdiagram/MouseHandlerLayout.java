/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import pgdiagram.HitTestResult.HITTEST;

/**
 *
 * @author nospoon
 */
public class MouseHandlerLayout extends MouseAdapter {
    CnvDiagram cnv;
    Point pointMousePressed = new Point();
    
    MouseHandlerLayout(CnvDiagram cnv) {
        this.cnv = cnv;
    }
    
    
//    int mousePressX, mousePressY;
//    int oldTableX, oldTableY;
//    Point oldControlPoint = new Point();
//    Point oldAnhorOffset = new Point();
//    Point lastMouseDragLocation = new Point();
//    Point currentMultiSelectEnd = new Point();
//    Point oldViewportPosition = new Point();
//    public CnvDiagram.HitTestResultOld hitTestResult = null;    
//    public boolean multiSelect;
//    public Rectangle currentSelectionRect = new Rectangle();
    public Map<Table, Point> selectedtables = new HashMap();
    public List<ForeignKey> selectedForeignKeys = new ArrayList();    
    public Map<ForeignKeyControlPoint, Point> selectedControlPoints = new HashMap();
    boolean boxSelect;
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e); //To change body of generated methods, choose Tools | Templates.    
    }
            
    @Override
    public void mouseDragged(MouseEvent e) {
        
        if (SwingUtilities.isMiddleMouseButton(e)) {
            return;
        }
        
        int ALLIGN_MARGIN = 7;
        boolean repaint = false;
        int diffX = e.getX() - pointMousePressed.x;
        int diffY = e.getY() - pointMousePressed.y;
        
        if (e.isControlDown()) {
            if (Math.abs(diffX)>Math.abs(diffY)) {
                diffY=0;
            }
            else {
               diffX=0;
            }
        }
        
        for (Map.Entry<Table, Point> entry:selectedtables.entrySet()) {
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
        for (Map.Entry<ForeignKeyControlPoint, Point> entry:selectedControlPoints.entrySet()) {
            ForeignKeyControlPoint fkcp = entry.getKey();
            Point cp = fkcp.controlPoint;
            Point oldPoint = entry.getValue();
            int newX = oldPoint.x + diffX;
            int newY = oldPoint.y + diffY;                
                        
            boolean gridX = true;
            boolean gridY = true;

            int idx = fkcp.foreignKey.linePoints.indexOf(fkcp);
            if (idx>=1) {
                Point cp0 = fkcp.foreignKey.linePoints.get(idx-1).controlPoint;                
                if (Math.abs(cp0.x-newX)<ALLIGN_MARGIN) { 
                    newX = cp0.x;
                    gridX = false;
                }
                if (Math.abs(cp0.y-newY)<ALLIGN_MARGIN) { 
                    newY = cp0.y;
                    gridY = false;
                }
            }
            
            if (idx<=fkcp.foreignKey.linePoints.size()-2) {
                Point cp1 = fkcp.foreignKey.linePoints.get(idx+1).controlPoint;
                if (gridX && Math.abs(cp1.x-newX)<ALLIGN_MARGIN) { 
                    newX = cp1.x;
                    gridX = false;
                }
                if (gridY && Math.abs(cp1.y-newY)<ALLIGN_MARGIN) { 
                    newY = cp1.y;
                    gridY = false;
                }
            }

            if (gridX) {
                newX =  (newX / 10) * 10;
            }
            if (gridY) {             
                newY =  (newY / 10) * 10;
            }
            
            cp.x = newX;
            cp.y = newY;
            repaint = true;
        }                
        if (repaint) {
            cnv.refresh();
        }               
                
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {

        ArrayList<HITTEST> mouseMoveTestCriteria = new ArrayList();
        mouseMoveTestCriteria.add(HITTEST.TABLE);
        mouseMoveTestCriteria.add(HITTEST.FOREIGNKEY);
        mouseMoveTestCriteria.add(HITTEST.FOREIGNKEY_CONTROLPOINT);
        
        cnv.removeItemsToHighlight();
        
        HitTestResult hit = cnv.hitTest(e.getX(), e.getY(), mouseMoveTestCriteria);
        
        // check if a controlPoint of an allready selected foreign key is selected. If so filter result such that only the selected fk control points remain
        filterForeignKeys(hit, selectedForeignKeys);        
        
        for (Table t:hit.tables) {
            cnv.addItemToHighlight(t);
        }
        for (Column c:hit.columns) {
            cnv.addItemToHighlight(c);
        }        
        
        for (ForeignKey fk:hit.foreignKeys) {
            cnv.addItemToHighlight(fk);
        }   
        
        for (ForeignKeyControlPoint fkcp:hit.foreignKeyControlPoints) {
            cnv.addItemToHighlight(fkcp);
        }    
        
        cnv.refresh();
    }
    
    private boolean filterForeignKeys(HitTestResult hit, List<ForeignKey> selectedForeignKeyList) {
        boolean filterForeignKeys = false;
        if (selectedForeignKeyList.size()>0) {
            for (ForeignKeyControlPoint fkcp:hit.foreignKeyControlPoints) {
                if (selectedForeignKeyList.contains(fkcp.foreignKey)) {
                    filterForeignKeys = true;
                    break;
                }
            }
            if (filterForeignKeys) {
                List<ForeignKey> tmp0 = new ArrayList();
                // filter foreign key references
                for (ForeignKey fk:hit.foreignKeys) {
                    if (!selectedForeignKeyList.contains(fk)) {
                        tmp0.add(fk);
                    }
                }
                hit.foreignKeys.removeAll(tmp0);

                List<ForeignKeyControlPoint> tmp1 = new ArrayList();
                // filter control points
                for (ForeignKeyControlPoint fkcp:hit.foreignKeyControlPoints) {
                    if (!selectedForeignKeyList.contains(fkcp.foreignKey)) {
                        tmp1.add(fkcp);
                    }
                }
                hit.foreignKeyControlPoints.removeAll(tmp1);
            }
        }
        return filterForeignKeys;
    }
    
    @Override
    public void mousePressed(MouseEvent e) {            
        if (SwingUtilities.isMiddleMouseButton(e)) {
            return;
        }        
        boolean shift_key = e.isShiftDown();
        pointMousePressed.x = e.getX();
        pointMousePressed.y = e.getY();
        
        ArrayList<HITTEST> mouseMoveTestCriteria = new ArrayList();
        mouseMoveTestCriteria.add(HITTEST.TABLE);
        mouseMoveTestCriteria.add(HITTEST.FOREIGNKEY);
        mouseMoveTestCriteria.add(HITTEST.FOREIGNKEY_CONTROLPOINT);
        
        HitTestResult hit = cnv.hitTest(e.getX(), e.getY(), mouseMoveTestCriteria);
        
        // check if a controlPoint of an allready selected foreign key is selected. If so filter result such that only the selected fk control points remain
        filterForeignKeys(hit, selectedForeignKeys);
        
        cnv.removeItemsToHighlight();      
        
        
        if (!shift_key) {
            
            // if something is selected that is included in the previous selection keep previous selection
            boolean selectedTablePress = false;
            boolean selectedForeigKeyPress = false;
            boolean selectedForeigKeyControlPointPress = false;
            for (Table t:hit.tables) {
                //System.out.println("-2a-");
                if (selectedtables.containsKey(t)) {
                    //System.out.println("-2b-");
                    selectedTablePress = true;
                    break;
                }
                if (!selectedTablePress) {
                    for (ForeignKey fk:hit.foreignKeys) {
                        if (selectedForeignKeys.contains(fk)) {
                            selectedForeigKeyPress = true;
                            break;
                        }
                    }                

                    if (!selectedForeigKeyPress) {
                        for (ForeignKeyControlPoint fkcp:hit.foreignKeyControlPoints) {
                            if (selectedControlPoints.containsKey(fkcp)) {
                                selectedForeigKeyControlPointPress = true;
                                break;
                            }
                        }                
                    }                
                }
            }

            if (selectedTablePress || selectedForeigKeyPress || selectedForeigKeyControlPointPress) {
                // keep current selection intact, overwrite old locations                
            }
            else {
                // if no existing selected item is selected, clear previous selection 
                selectedtables.clear();        
                selectedForeignKeys.clear();
                selectedControlPoints.clear();
                cnv.removeSelectedItems();

                if (hit.isNothingSelected()) {
                    // nothing is under the mouse, flag mousaction as start of box select
                    boxSelect = true;
                }
                else {
                    // something is under the mouse
                    for (Table t:hit.tables) {                            
                        selectedtables.put(t, new Point(t.x, t.y));
                        cnv.addSelectedItem(t);
                    }
                    for (ForeignKey fk:hit.foreignKeys) {    
                        selectedForeignKeys.add(fk);
                        cnv.addSelectedItem(fk);
                    }

                    for (ForeignKeyControlPoint fkcp:hit.foreignKeyControlPoints) {
                        selectedControlPoints.put(fkcp, new Point(fkcp.controlPoint));
                        cnv.addSelectedItem(fkcp);
                    }                                       
                }
            }
        }
        else {  // shift_key == true
            for (Table t:hit.tables) {            
                if (!selectedtables.containsKey(t)) {
                    selectedtables.put(t, new Point(t.x, t.y));
                    cnv.addSelectedItem(t);
                }
                else {
                    selectedtables.remove(t);
                    cnv.removeSelectedItem(t);                    
                }        
            }        
        
            for (ForeignKey fk:hit.foreignKeys) {
                if (!selectedForeignKeys.contains(fk)) {
                    selectedForeignKeys.add(fk);
                    cnv.addSelectedItem(fk);
                }
                else {
                    selectedForeignKeys.remove(fk);
                    cnv.removeSelectedItem(fk);                    
                }  
            }   

            for (ForeignKeyControlPoint fkcp:hit.foreignKeyControlPoints) {
                if (!selectedControlPoints.containsKey(fkcp)) {
                    selectedControlPoints.put(fkcp, new Point(fkcp.controlPoint));
                    cnv.addSelectedItem(fkcp);
                }
                else {
                    selectedControlPoints.remove(fkcp);
                    cnv.removeSelectedItem(fkcp);                    
                }  
            }       
        }
        
        // keep current selection intact, overwrite old locations      
        for (Table t:selectedtables.keySet()) {            
            selectedtables.put(t, new Point(t.x, t.y));                    
        }
        for (ForeignKeyControlPoint fkcp:selectedControlPoints.keySet()) {
            selectedControlPoints.put(fkcp, new Point(fkcp.controlPoint));
        }   

        // show table info if one table is selected
        if (hit.tables.size()==1) {
            this.cnv.fraMain.tableSelected(hit.tables.get(0));
        }

        cnv.refresh();
    }
    
    
    @Override
    public void mouseReleased(MouseEvent e) {
        boxSelect = true;
    }
    
//    @Override
//    public void mouseClicked(MouseEvent e) {
//        //System.out.println("mouseClicked");
//    }    
//    @Override
//    public void mouseEntered(MouseEvent e) {
//        //System.out.println("mouseEntered");
//    }
//    @Override
//    public void mouseExited(MouseEvent e) {
//        //System.out.println("mouseExited");
//    }         
}
