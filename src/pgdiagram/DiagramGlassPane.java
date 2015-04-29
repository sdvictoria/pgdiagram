/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;

public class DiagramGlassPane extends JComponent {
    
    FraMain fra;
    
    
    
    public DiagramGlassPane(FraMain fra) {
      this.fra = fra;
    }
    
    public void paint(Graphics g) {
        g.setColor(Color.red);
        Container root = fra.getContentPane();
       // g.setColor(new Color(100,100,100,100));
        //rPaint(root,g);
    }
    
//    private void rPaint(Container cont, Graphics g) {
//        for(int i=0; i<cont.getComponentCount(); i++) {
//            Component comp = cont.getComponent(i);
//            if(!(comp instanceof JPanel)) {
//                int x = comp.getX();
//                int y = comp.getY();
//                int w = comp.getWidth();
//                int h = comp.getHeight();
//                g.drawRect(x+4,y+4,w-8,h-8);
//                g.drawString(comp.getClass().getName(),x+10,y+20);
//            }
//            if(comp instanceof Container) {
//                rPaint((Container)comp,g);
//            }
//        }
//    }
    
    
    
   
}    
