/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 *
 * @author nospoon
 */
public class SectionItem {    
    public enum ALLINGMENT {LEFT, CENTER, RIGHT};
    Object data;    
    Rectangle bounds;    
    ALLINGMENT allignment = ALLINGMENT.LEFT;
    int order = -1;
    Section section;
    boolean mouseOver = false;
    private boolean selected = false;
    
    public SectionItem(Object obj, int order, Graphics g) {
        this(obj, ALLINGMENT.LEFT, order, g);
    }
    
    public SectionItem(Object obj, ALLINGMENT allignment, int order, Graphics g) {
        this(obj, allignment, order, false, g);
    }
    
    public SectionItem(Object obj, ALLINGMENT allignment, int order, boolean selected, Graphics g) {
        this.data = obj;
        this.allignment = allignment;
        this.order = order;
        this.selected = selected;
        resetBounds(g);
    }    
    
    public void resetBounds(Graphics g) {
        FontMetrics metrics = g.getFontMetrics();
        Rectangle r = metrics.getStringBounds(getVisualStuff(this.data).str, g).getBounds();            
        this.bounds = new Rectangle(0, 0, r.width, r.height);
    }
    
    VisualStuff getVisualStuff(Object obj) {
        VisualStuff vs = new VisualStuff();
        if (obj instanceof String) {
            vs.str = (String)obj;
        }
        else if (obj instanceof Table)                       { setVisualStuffTable(((Table)obj), vs); }
        else if (obj instanceof Column)                     { setVisualStuffColumn(((Column)obj), vs); }
        else if (obj instanceof CanvasGraphicTable.FooterItem)    { setVisualStuffFooter(((CanvasGraphicTable.FooterItem)obj), vs); }        
        else if (obj instanceof CanvasGraphicTable.MetaDataStuff) { setVisualStuffMeta(((CanvasGraphicTable.MetaDataStuff)obj), vs); }
        else if (obj !=null) {
            vs.str = obj.toString();
        }
        
        // add some padding
        if (vs.str.length()>0) {
            vs.str = " " + vs.str + " ";
        }
        return vs;
    }
    
    private void setVisualStuffFooter(CanvasGraphicTable.FooterItem footerItem, VisualStuff vs) {        
        vs.str = footerItem.toString().toLowerCase();        
        if (!mouseOver && !selected) {
            vs.bg = Color.black;
            vs.fg = Color.gray;
        }        
        else if (mouseOver && !selected) {
            vs.bg = Color.black;
            vs.fg = Color.yellow.darker();
        }
        else if (!mouseOver && selected) {
            vs.bg = Color.lightGray;
            vs.fg = Color.black;
        }            
        else {
            vs.bg = Color.lightGray;
            vs.fg = Color.darkGray;
        }            
    }
    
    private void setVisualStuffTable(Table table, VisualStuff vs) {        
        vs.str = table.name + (table.recordCount>-1?" (" + table.recordCount + ")":"");
        // vs.bg = FraMain.colorTableHeader;                    
        vs.bg = new Color((int)(Math.random()*255), (int)(Math.random()*255),(int)(Math.random()*255));
        if (!mouseOver && !selected) {            
            vs.fg = FraMain.colorTableName;            
        }
        else if (mouseOver && !selected) {
            vs.fg = FraMain.colorTableNameHiglighted;  
        }
        else if (!mouseOver && selected) {
            vs.fg = FraMain.colorTableNameSelected;  
        }            
        else {
            vs.fg = FraMain.colorTableNameSelected;
        }            
    }
    
    private void setVisualStuffColumn(Column col, VisualStuff vs) {        
        vs.str = col.name;
        vs.bg = order%2==0?FraMain.colorColumnRowEven:FraMain.colorColumnRowOdd;
        if (!mouseOver && !selected) {            
            vs.fg = FraMain.colorColumnFg;            
        }
        else if (mouseOver && !selected) {
            vs.fg = FraMain.colorColumnFgHightlighted;                                
        }
        else if (!mouseOver && selected) {
            vs.fg = FraMain.colorColumnFgSelected;
        }            
        else {
            vs.fg = FraMain.colorColumnFgSelected;
        }            
    }    
    
    private void setVisualStuffMeta(CanvasGraphicTable.MetaDataStuff md, VisualStuff vs) {        
        vs.str = md.str;
        vs.bg = order%2==0?FraMain.colorColumnRowEven:FraMain.colorColumnRowOdd;
        vs.fg = Color.lightGray;  
    }    
    
    public void paint(Graphics g) {
        g.setColor(Color.red);
        VisualStuff vs = getVisualStuff(data);
        if (vs.bg!=null) {
            g.setColor(vs.bg);
        }
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        int ascent = g.getFontMetrics().getAscent();
        int x = bounds.x;
        
        if (vs.fg!=null) {
            g.setColor(vs.fg);
        }
        if (allignment.equals(ALLINGMENT.CENTER)) {
            FontMetrics metrics = g.getFontMetrics();
            Rectangle r = metrics.getStringBounds(vs.str, g).getBounds();      
            int str_w = r.width;
            x = bounds.x + (bounds.width - str_w)/2;
        }
        g.drawString(vs.str, x, bounds.y+ascent);
    }
    
    public void setSelected(boolean b) {
        this.selected = b;
        this.section.cg.eventSelected(this, b);
    }
    
    public void toggleSelected() {
        setSelected(!this.selected);
    }    
        
    public String toString() {
        return getVisualStuff(data).str;
    }
    
    class VisualStuff {        
        Color fg = Color.black;
        Color bg = Color.white;
        String str = "";
        // Font font = Font.PLAIN;
    }
    
    public Section getSection() {
        return section;
    }
    
    public CanvasGraphicTable getCG() {
        return section.cg;
    }
    
}
