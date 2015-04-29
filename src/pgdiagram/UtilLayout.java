/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import javax.swing.JComponent;

/**
 *
 * @author nospoon
 */
public class UtilLayout {
     public static void add(JComponent parent, JComponent... arr) {
        for (JComponent c:arr) {
            parent.add(c);
        }
    }
     
    public static void addToContainer(Container container, Component... comps) {
        for (Component c:comps) {
            container.add(c);
        }        
    }     
     
    public static void allignBottom(JComponent lead, JComponent target) {
        allignBottom(lead, target, -1, -1);
    }
    
    public static void allignBottom(JComponent lead, JComponent target, int width, int height) {
        Rectangle rLead = lead.getBounds();
        Rectangle rTarget = target.getBounds();
        rTarget.x = rLead.x;
        rTarget.y = rLead.y + rLead.height;
        if (width>0) {
            rTarget.width = width;
        }
        if (height>0) {
            rTarget.height = height;
        }
        target.setBounds(rTarget);
    }
         
    public static void setRight(Rectangle leading, Rectangle r) {
        r.x = leading.x + leading.width;
    }       
    
    public static void setBelow(Rectangle leading, Rectangle r) {
        r.y = leading.y + leading.height;
    } 

    public static void setRight(Section leading, Section section) {
        section.location.x = leading.location.x + leading.sectionDimension.width;
    }
        
    public static void setTop(Section leading, Section section) {
        section.location.y = leading.location.y;
    }        
    
    public static void setBelow(Section leading, Section section) {
        section.location.y = leading.location.y + leading.sectionDimension.height;
    }

    public static void allignWidth(Section leading, Section section) {        
        allignWidth(leading.sectionDimension.width, section);
    }

    public static void allignWidth(int w, Section section) {
        section.sectionDimension.width = Math.max(w, section.sectionDimension.width);
    }

}
