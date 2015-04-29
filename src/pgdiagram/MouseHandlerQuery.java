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
public class MouseHandlerQuery extends MouseAdapter {
    CnvDiagram cnv;
    Point pointMousePressed = new Point();
    
    MouseHandlerQuery(CnvDiagram cnv) {
        this.cnv = cnv;
    }
    
    List<SectionItem> cglistHightlighted = new ArrayList();
    List<SectionItem> cglistSelected = new ArrayList();

    
    public List<Table> selectedtables = new ArrayList();
    public List<Column> selectedColumns = new ArrayList();    
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e); //To change body of generated methods, choose Tools | Templates.    
    }
            
    @Override
    public void mouseDragged(MouseEvent e) {
                
    }
    
    private void setMouseOverState(List<SectionItem> items, boolean b) {
        for (SectionItem item:items) {
            item.mouseOver = b;
        } 
    }
    
    private boolean compare2ways (List<SectionItem> l0, List<SectionItem> l1) {
        boolean b0 = compare (l0, l1);
        boolean b1 = compare (l1, l0);        
        return b0&&b1;
    }
    
    private boolean compare (List<SectionItem> l0, List<SectionItem> l1) {
        for (SectionItem item0:l0) {
            if (!l1.contains(item0)) {
                return false;
            }
        }
        return true;
    }    
    
    @Override
    public void mouseMoved(MouseEvent e) {

        ArrayList<HITTEST> mouseMoveTestCriteria = new ArrayList();
        mouseMoveTestCriteria.add(HITTEST.TABLE);
        mouseMoveTestCriteria.add(HITTEST.COLUMN);
                
        List<SectionItem> oldCglistHightlighted = cglistHightlighted;                
        cglistHightlighted = cnv.hitTestCanvasGraphics(e.getX(), e.getY(), mouseMoveTestCriteria);
        
        boolean repaint = false;
        
        boolean same = compare2ways(oldCglistHightlighted, cglistHightlighted);
        if (!same) {
            setMouseOverState(oldCglistHightlighted, false);        
            setMouseOverState(cglistHightlighted, true);
            oldCglistHightlighted = cglistHightlighted;
            repaint = true;
        }
                       
//        HitTestResult hit = cnv.hitTest(e.getX(), e.getY(), mouseMoveTestCriteria);
//        cnv.removeItemsToHighlight();
//        
//        if (hit.columns.size()>0) {
//            for (Column c:hit.columns) {
//                cnv.addItemToHighlight(c);        
//            }   
//        }
//        else {
//            for (Table t:hit.tables) {
//                cnv.addItemToHighlight(t);
//            }
//        }             
        if (repaint) {
            cnv.refresh();
        }
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
        mouseMoveTestCriteria.add(HITTEST.COLUMN);               
        
        List<SectionItem> list = cnv.hitTestCanvasGraphics(e.getX(), e.getY(), mouseMoveTestCriteria);
                        
        for (SectionItem item:list) {
            if (item.data instanceof CanvasGraphicTable.FooterItem) {
                item.toggleSelected();
            }            
        }    
                
        HitTestResult hit = cnv.hitTest(e.getX(), e.getY(), mouseMoveTestCriteria);
                
        cnv.removeItemsToHighlight();      
                
        if (!shift_key) {            
            // if something is selected that is included in the previous selection keep previous selection
            boolean selectedTablePress = false;
            boolean selectedColumnPress = false;
            for (Table t:hit.tables) {
                //System.out.println("-2a-");
                if (selectedtables.contains(t)) {
                    //System.out.println("-2b-");
                    selectedTablePress = true;
                    break;
                }
            }

            if (selectedTablePress || selectedColumnPress) {
                // keep current selection intact, overwrite old locations                
            }

            if (hit.columns.size()>0) {
                for (Column col:hit.columns) {    
                    if (selectedColumns.contains(col)) {
                        selectedColumns.remove(col);
                        cnv.removeSelectedItem(col);
                        cnv.addItemToHighlight(col);
                    }
                    else {
                        selectedColumns.add(col);
                        cnv.addSelectedItem(col);
                    }
                }
            }
            else {            
                for (Table t:hit.tables) {                            
                    if (selectedtables.contains(t)) {
                        selectedtables.remove(t);
                        cnv.removeSelectedItem(t);
                        cnv.addItemToHighlight(t);
                    }
                    else {
                        selectedtables.add(t);
                        cnv.addSelectedItem(t);
                    }
                }
            }
        }

        // show table info if one table is selected
        if (hit.tables.size()==1) {
            this.cnv.fraMain.tableSelected(hit.tables.get(0));
        }

        cnv.refresh();
        cnv.fraMain.createStatementBasedOnSelection();
    }
    
//    @Override
//    public void mouseReleased(MouseEvent e) {}
//    
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
