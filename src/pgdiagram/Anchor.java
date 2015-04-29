package pgdiagram;

import java.awt.Point;

public class Anchor {
    public static enum AnchorOrientation { LEFT, RIGHT };
    public AnchorOrientation orientation = AnchorOrientation.LEFT;    
    public Point offset = new Point();   
    public boolean startAnchor;
    
    public Anchor(boolean startAnchor) {
        this.startAnchor = startAnchor;
        offset.x = -10;
    }
    
    public void flip() {
        orientation = orientation==AnchorOrientation.LEFT?AnchorOrientation.RIGHT:AnchorOrientation.LEFT;
        offset.x = -offset.x;
    }
    
}
