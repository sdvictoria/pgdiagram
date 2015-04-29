/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nospoon
 */
public class ForeignKey {
    String constraint_name = "";
    String match_option = ""; 
    String update_rule = "";
    String delete_rule = "";
            
    Table fromTable, toTable;
    String fromCols[], toCols[];
    //int positions_in_other_constraint[];
    String toCardinal = "";     // 1 or N
    String fromCardinal = "";   // 1 or N
    
    int order;
    // --- gui
    public List<ForeignKeyControlPoint> linePoints = new ArrayList();
    public Color color = Color.black;
    public Anchor startAnchorOffset = new Anchor(true);
    public Anchor endAnchorOffset = new Anchor(false);    
    
    public ForeignKey(String constraint_name, String match_option, String update_rule, String delete_rule, Table fromTable, String[] fromCols, Table toTable, String[] toCols, String fromCardinal, String toCardinal, int[] positions_in_other_constraint, int order) {        
        this.constraint_name = constraint_name;
        this.match_option = match_option; 
        this.update_rule = update_rule;
        this.delete_rule = delete_rule;
        this.fromTable = fromTable;
        this.fromCols = fromCols;
        this.toTable = toTable;
        this.toCols = toCols;
        this.fromCardinal = fromCardinal;
        this.toCardinal = toCardinal;
        //this.positions_in_other_constraint = positions_in_other_constraint;
        this.order = order;
        
        linePoints.add(new ForeignKeyControlPoint(this, new Point()));
        linePoints.add(new ForeignKeyControlPoint(this, new Point()));        
        color = new Color(0xFF00FF);
    }
    
    public void setStartPoint(Point p) {
//        if (linePoints.size()==0) {
//            linePoints.add(new Point());
//        }
        linePoints.set(0, new ForeignKeyControlPoint(this, p));
    }

    public void setEndPoint(Point p) {        
        linePoints.set(linePoints.size()-1, new ForeignKeyControlPoint(this, p));
    }    
    
    public void allignYAtStart() {
        if (linePoints.size()>2) {
            linePoints.get(1).controlPoint.y = linePoints.get(0).controlPoint.y;
        }
    }
    
    public void allignYAtEnd() {
        if (linePoints.size()>2) {
            linePoints.get(linePoints.size()-2).controlPoint.y = linePoints.get(linePoints.size()-1).controlPoint.y;
        }        
    }
    
    public void addControlPoint(int lineSectionIdx, Point p) {
        linePoints.add(lineSectionIdx, new ForeignKeyControlPoint(this, p));
    }
    
    public void removeControlPoint(Point p) {
        if (linePoints.size()>2) {
            if (linePoints.get(0).controlPoint==p || linePoints.get(linePoints.size()-1).controlPoint==p) {
                
            }
            else {
                linePoints.remove(p);
            }
        }
    }        
    
//    public String[] getToColumnsInAlternativeOrder() {
//        String ret[] = new String[toCols.length];        
//        for (int i=0; i<positions_in_other_constraint.length; ++i) {
//            ret[i] = toCols[positions_in_other_constraint[i]-1];
//        }        
//        return ret;
//    }
}
