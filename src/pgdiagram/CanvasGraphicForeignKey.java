/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.List;

/**
 *
 * @author nospoon
 */
public class CanvasGraphicForeignKey implements CanvasGraphic {
    ForeignKey fk;
    boolean mouseOver;
    boolean selected;
    
    public CanvasGraphic cg_from;
    public CanvasGraphic cg_to;
        
    public CanvasGraphicForeignKey(ForeignKey fk) {
        this.fk = fk;
    }
    
    public void setMouseOver(boolean b) {
        this.mouseOver = b;
    }
    
    public void setSelected(boolean b) {
        this.selected = b;
    }
    
    public boolean isMouseOver() {
        return this.mouseOver;
    }
    
    public boolean isSelected() {
        return this.selected;
    }    
    
    public List<SectionItem> hitTest(int x, int y, List<SectionItem> list) {
        return list;
    }    
    
    public void paint(Graphics g_) {             
        Graphics2D g = (Graphics2D) g_;
        int lineWidth = fk.fromCols.length;
        lineWidth = (Math.min(lineWidth, 5));            
        
        CanvasGraphicTable cgt_from = (CanvasGraphicTable) cg_from;
        CanvasGraphicTable cgt_to = (CanvasGraphicTable) cg_to;

        Point cpFrom = new Point();
        Point cpTo = new Point();                    
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        cpFrom.x = cgt_from.bounds.x + cgt_from.bounds.width/2;
        cpFrom.y = cgt_from.bounds.y + cgt_from.bounds.height/2;

        cpTo.x = cgt_to.bounds.x + cgt_to.bounds.width/2;
        cpTo.y = cgt_to.bounds.y + cgt_to.bounds.height/2;


        Point arr[] = { cpFrom, cpTo }; 

        CnvDiagram.drawLineSegments(arr, Color.yellow, Color.blue, 100, g);
        // arrow
    }
}
