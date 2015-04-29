/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

public class MouseHandlerCanvas extends MouseAdapter {

    // Point lastMouseDragLocation = new Point();
    Point lastPanelLocation = new Point();
    Point lastMousePostionOnScreen = new Point();
    
    @Override
    public void mouseMoved(MouseEvent e) {
        
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isMiddleMouseButton(e)) {
            Point p = MouseInfo.getPointerInfo().getLocation();
            int diffX = p.x - lastMousePostionOnScreen.x;
            int diffY = p.y - lastMousePostionOnScreen.y;
            JViewport viewport = (JViewport) cnv.getParent();
            Rectangle rect = new Rectangle(-lastPanelLocation.x - diffX, -lastPanelLocation.y - diffY, viewport.getWidth(), viewport.getHeight());
            cnv.scrollRectToVisible(rect);  
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        
    }

    @Override
    public void mouseExited(MouseEvent e) {
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        lastMousePostionOnScreen.x = 0;
        lastMousePostionOnScreen.y = 0;
        lastPanelLocation.x = 0;
        lastPanelLocation.y = 0;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = MouseInfo.getPointerInfo().getLocation();
        lastMousePostionOnScreen.x = p.x;
        lastMousePostionOnScreen.y = p.y;
        lastPanelLocation.x = cnv.getX();
        lastPanelLocation.y = cnv.getY();
        super.mousePressed(e); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e); //To change body of generated methods, choose Tools | Templates.
    }
    CnvDiagram cnv;
    Point pointMousePressed = new Point();
    
    MouseHandlerCanvas(CnvDiagram cnv) {
        this.cnv = cnv;
    }
}