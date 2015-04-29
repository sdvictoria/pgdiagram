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

public class MouseHandlerLayoutCG extends MouseAdapter {
    CnvDiagram cnv;
    
    int gridX = 25;
    int gridY = 25;
    Point pointMousePressed = new Point();
    Point pointMouseDragged = new Point();
    // Point deltaMouseDragged = new Point();
    boolean dragRight = true;
    boolean dragDown = true;
    
    MouseHandlerLayoutCG(CnvDiagram cnv) {
        this.cnv = cnv;
    }
    
    List<SectionItem> cglistHightlighted = new ArrayList();
    List<SectionItem> cglistSelected = new ArrayList();
    List<CanvasGraphicTable> canvasGraphicsHighLighted = new ArrayList();
    
    
    public List<Table> selectedtables = new ArrayList();
    public List<Column> selectedColumns = new ArrayList();    
    // ----
    Map<CanvasGraphicTable, Point> cg_previousLocations = new HashMap();
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e); //To change body of generated methods, choose Tools | Templates.    
    }
    
    private void setMouseOverStateCG(List<CanvasGraphicTable> items, boolean b) {
        for (CanvasGraphicTable cg:items) {
            cg.setMouseOver(b);
        } 
    }
    
    private boolean compare2waysCG(List<CanvasGraphicTable> l0, List<CanvasGraphicTable> l1) {
        boolean b0 = l0.containsAll(l1);
        boolean b1 = l1.containsAll(l0);   
        return b0&&b1;
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

        boolean repaint = false;
        ArrayList<HitTestResult.HITTEST> mouseMoveTestCriteria = new ArrayList();
        boolean same =false;
        List<SectionItem> oldCglistHightlighted = cglistHightlighted;                
        cglistHightlighted = cnv.hitTestCanvasGraphics(e.getX(), e.getY(), mouseMoveTestCriteria);
        
        same = compare2ways(oldCglistHightlighted, cglistHightlighted);
        if (!same) {            
            List<CanvasGraphicTable> oldCanvasGraphicslist = canvasGraphicsHighLighted;
            canvasGraphicsHighLighted = getCanvasGraphics(cglistHightlighted);
            
            same = compare2waysCG(oldCanvasGraphicslist, canvasGraphicsHighLighted);
            if (!same) {
                setMouseOverStateCG(oldCanvasGraphicslist, false);        
                setMouseOverStateCG(canvasGraphicsHighLighted, true);
                oldCanvasGraphicslist = canvasGraphicsHighLighted;            
                repaint = true;
            }
        }
                       
        if (repaint) {
            cnv.refresh();
        }
    }
    
    List<CanvasGraphicTable> getCanvasGraphics(List<SectionItem> items) {
        List<CanvasGraphicTable> cgList = new ArrayList();
        for (SectionItem item:items) {
            CanvasGraphicTable cg = item.getCG();
            if (!cgList.contains(cg)) {
                cgList.add(cg);
            }
        }
        return cgList;
    }
   
 
    @Override
    public void mousePressed(MouseEvent e) {            
        if (SwingUtilities.isMiddleMouseButton(e)) {
            return;
        }        
        boolean shift_key = e.isShiftDown();
        pointMousePressed.x = e.getX();
        pointMousePressed.y = e.getY();                
        
        ArrayList<HitTestResult.HITTEST> mouseMoveTestCriteria = new ArrayList();
        mouseMoveTestCriteria.add(HitTestResult.HITTEST.TABLE);                    
        mouseMoveTestCriteria.add(HitTestResult.HITTEST.FOREIGNKEY);                    
        mouseMoveTestCriteria.add(HitTestResult.HITTEST.FOREIGNKEY_CONTROLPOINT);                    
        
        List<SectionItem> list = cnv.hitTestCanvasGraphics(e.getX(), e.getY(), mouseMoveTestCriteria);
                        
        CanvasGraphicTable cg_selectedTable = null;
        for (SectionItem item:list) {
            if (item.data instanceof CanvasGraphicTable.FooterItem) {
                item.toggleSelected();
                return;
            }            
            else if (item.section.cg.data instanceof Table) {
                cg_selectedTable = item.section.cg; // (Table)item.data;
            }            
        }    
        
        if (cg_selectedTable!=null) {
            cg_previousLocations = copyLocations(cg_selectedTable);
        }

        // show table info if one table is selected
        if (cg_selectedTable!=null) {
            this.cnv.fraMain.tableSelected((Table)cg_selectedTable.data);
        }
        cnv.refresh();
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {        
        if (cg_previousLocations.size()==0) {
            return;
        }
        
        int diffX = e.getX() - pointMousePressed.x;
        int diffY = e.getY() - pointMousePressed.y;
                        
        for (Map.Entry<CanvasGraphicTable, Point> entry:cg_previousLocations.entrySet()) {
            CanvasGraphicTable cg = entry.getKey();
            Point prevPoint = entry.getValue();
            
            int diffX_drag = e.getX()-pointMouseDragged.x;
            int diffY_drag = e.getY()-pointMouseDragged.y;
            
            if      (diffX_drag>0) { dragRight = true;  }
            else if (diffX_drag<0) { dragRight = false; }
            if      (diffY_drag>0) { dragDown = true;  }
            else if (diffY_drag<0) { dragDown = false; }
            
            cg_move(cg, prevPoint, diffX, diffY, gridX, gridY, dragRight, dragDown);            
        }        
        pointMouseDragged.x = e.getX();
        pointMouseDragged.y = e.getY();
        cnv.refresh();
    }   
    
    private void cg_move(CanvasGraphicTable cg, Point orgPoint, int dX, int dY, int gridX, int gridY, boolean rightDrag, boolean downDrag) {
        int newX = orgPoint.x + dX;
        int newY = orgPoint.y + dY;                
        int newXgrid = newX;
        int newYgrid = newY;  
        
        if (gridX>1) {
            newXgrid = ((newX/gridX)+(rightDrag?1:0)) * gridX;
        }
        if (gridY>1) {
            newYgrid = ((newY/gridY)+(downDrag?1:0)) * gridY;
        }
                
        cg.shadowPosition.x = newXgrid;
        cg.shadowPosition.y = newYgrid;
        
        cg_move(cg, newX, newY);
    }
    
    private void cg_move(CanvasGraphicTable cg, int x, int y) {
        cg.bounds.x = x;
        cg.bounds.y = y;
    }
    
    private Map<CanvasGraphicTable, Point> copyLocations(CanvasGraphicTable cg) {
        Map<CanvasGraphicTable, Point> map = new HashMap();
        Point p = new Point(cg.bounds.x, cg.bounds.y);
        map.put(cg, p);        
        return map;
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        for (Map.Entry<CanvasGraphicTable, Point> entry:cg_previousLocations.entrySet()) {
            CanvasGraphicTable cg = entry.getKey();
            if (cg.shadowPosition.x != Integer.MIN_VALUE) {
                // drag occured, move cg to same location as shadow
                cg_move(cg, cg.shadowPosition.x, cg.shadowPosition.y);            
            }            
        }
        cnv.refresh();
        
        cg_previousLocations.clear();
        dragRight = true;
        dragDown = true;        
    }
    
    public void revertDrag() {        
        for (Map.Entry<CanvasGraphicTable, Point> entry:cg_previousLocations.entrySet()) {
            CanvasGraphicTable cg = entry.getKey();
            Point prevPoint = entry.getValue();        
            cg_move(cg, prevPoint.x, prevPoint.y);            
            cg.shadowPosition.x = Integer.MIN_VALUE;
            cg.shadowPosition.y = Integer.MIN_VALUE;
            // System.out.println("revertDrag()");
        }
        cnv.refresh();
        cg_previousLocations.clear();
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

    