/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.Point;

/**
 *
 * @author nospoon
 */
public class ForeignKeyControlPoint {
    ForeignKey foreignKey;
    Point controlPoint = new Point();
    
    ForeignKeyControlPoint(ForeignKey foreignKey, Point controlPoint) {
        this.foreignKey = foreignKey;
        this.controlPoint = controlPoint;
    }
}
