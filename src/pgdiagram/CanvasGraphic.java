/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.Graphics;
import java.util.List;

/**
 *
 * @author nospoon
 */
public interface CanvasGraphic {
    public void paint(Graphics g);
    public void setMouseOver(boolean b);
    public void setSelected(boolean b);
    public boolean isMouseOver();
    public boolean isSelected();
    public List<SectionItem> hitTest(int x, int y, List<SectionItem> list);
}
