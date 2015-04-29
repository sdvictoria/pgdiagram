/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nospoon
 */
public class Section {
    public enum ALLINGMENT {LEFTTORIGHT, TOPTOBOTTOM};
    String name = "";
    ALLINGMENT allingment = ALLINGMENT.LEFTTORIGHT;
    List<SectionItem> items = new ArrayList();
    Point location = new Point();
    Dimension sectionDimension = new Dimension();
    CanvasGraphicTable cg;
    
    public Section(String name, CanvasGraphicTable cg) {
        this(name, ALLINGMENT.LEFTTORIGHT, cg);        
    }
    
    public Section(String name, ALLINGMENT allingment, CanvasGraphicTable cg) {
        this.name = name;
        this.allingment = allingment;
        this.cg = cg;
    }
    
    List<Object> children = new ArrayList();
    
    public void add(SectionItem item) {
        item.section = this;
        items.add(item);
    }
        
    public void paint(Graphics g) {        
        g.translate(location.x, location.y);
        for (SectionItem item:items) {
            item.paint(g);
        }
        g.translate(-location.x, -location.y);
        g.setColor(Color.green);
        g.drawRect(location.x, location.y, sectionDimension.width, sectionDimension.height);
    }
    
    public void hitTest(int x, int y, List<SectionItem> itemList) {
        x -= location.x;
        y -= location.y;
        for (SectionItem item:items) {
            if (item.bounds.contains(x,y)) {
                itemList.add(item);
                // System.out.println("cg hitTest: "+ item.toString() + " " +  item.data.getClass().getSimpleName());
            }
        }       
    }
    
     public void layout(Graphics g) {
        SectionItem prevItem = null;
        Rectangle bb = null;
        for (SectionItem item:items) {
            if (prevItem==null) {
                item.bounds.x=0;
                item.bounds.y=0;
            } 
            else {
                if (allingment.equals(ALLINGMENT.LEFTTORIGHT)) {
                    UtilLayout.setRight(prevItem.bounds, item.bounds);   
                }
                else {
                    UtilLayout.setBelow(prevItem.bounds, item.bounds);   
                }     
            }
            if (bb==null) {
                bb = new Rectangle(item.bounds);            
            }
            else {
                bb.add(item.bounds);
            }
            prevItem = item;
        }
        if (bb!=null) {
            this.sectionDimension.width=bb.width;
            this.sectionDimension.height=bb.height;
        }
    }
     
    public void resizeSectionItemsWidth(Graphics g) {
         // resize single centered item
         for (SectionItem item:items) {
             item.bounds.width = this.sectionDimension.width;
         }  
    }
     
    public void resetBounds(Graphics g) {
        for (SectionItem item:items) {
            item.resetBounds(g);
        }
    }
    
    public void relayoutSectionItems(Graphics g) {
         // resize single centered item
         SectionItem centeredItem = null;
         int wleft = 0;
         int wright = 0;
         for (SectionItem item:items) {
             switch(item.allignment) {
                 case LEFT: wleft += item.bounds.width; break;
                 case RIGHT: wright += item.bounds.width; break;
                 case CENTER: centeredItem = item; break;
             }
         }
         
         if (centeredItem!=null) {
            int wcenter = this.sectionDimension.width - wleft - wright;
            centeredItem.bounds.width = wcenter;
            layout(g);      
        }                   
    }
}
