/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgdiagram;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sun.java2d.loops.DrawRect;

/**
 *
 * @author nospoon
 */
public class CanvasGraphicTable implements CanvasGraphic {
    
    String SECTION_HEADER = "sectHeader";
    String SECTION_FOOTER = "sectFooter";    
    String SECTION_COLUMN = "sectColumn";
    String SECTION_INDEX = "index";
    
    public enum FooterItem {TYPE, PK, UQ, IDX, NOTNULL};
    public enum MetaData {TYPE, PK, UQ, IDX, NOTNULL};
    
    Section typeSection = null;
    List<Section> contraintSections_pk = null;
    List<Section> contraintSections_uq = null;
    List<Section> contraintSections_idx = null;
    
    Section header = null;
    Section columns = null;    
    Section contraintSections_notnull = null;
    boolean hasPrimaryKeys = false;
    boolean hasUniqueKeys = false;
    boolean hasIndexes = false;
    boolean hasNotNull = false;
    
    private boolean mouseOver;
    private boolean selected;    
    
    Object data;
//     Point location = new Point();
    Rectangle bounds = new Rectangle();
    Point shadowPosition = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);

    List<Section> sections = new ArrayList();
    
    List<CanvasGraphicTable> children = new ArrayList();
    HashMap<String, List<CanvasGraphicTable>> childrenMap = new HashMap();
    HashMap<String, Boolean> horAllignMap = new HashMap();
    
    public boolean showIndex = false;
    public boolean showNotNull = false;
    public boolean showPK = false;
    public boolean showType = false;
    public boolean showUnique = false; 
        
    boolean doLayout = true;
    boolean init = true;
    
    public CanvasGraphicTable(Object data) { 
        this.data = data;
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
    
    public void add(Section section) {
        sections.add(section);    
    }
    
    public void removeAllSections() {
        sections.clear();
    }

    public void layout(Graphics g) {
        for (Section section:sections) {
            if (section!=null) {
                section.layout(g);
            }
        }    
    }
    
    public void paint(Graphics g) {         
        if (init) {
            init = false;
            initCGTable(g);
        }
        
        if (doLayout) {
            doLayout = false;
            if (data instanceof Table) {
                updateCGTable(g);
            }
        }                
        
        if (shadowPosition.x!=Integer.MIN_VALUE && shadowPosition.y!=Integer.MIN_VALUE && (shadowPosition.x!=bounds.x || shadowPosition.y!=bounds.y)) {
        // if (shadowPosition.x!=Integer.MIN_VALUE && shadowPosition.y!=Integer.MIN_VALUE) {
            g.setColor(Color.gray);                
            g.fillRect(shadowPosition.x, shadowPosition.y, bounds.width, bounds.height);
            g.setColor(Color.darkGray);   
            g.drawLine(shadowPosition.x, shadowPosition.y, shadowPosition.x+bounds.width, shadowPosition.y+bounds.height);                         
            g.drawLine(shadowPosition.x+bounds.width, shadowPosition.y, shadowPosition.x, shadowPosition.y+bounds.height);
        }
                
        g.translate(bounds.x, bounds.y);
        for (Section section:sections) {
            section.paint(g);
        }                  
        g.translate(-bounds.x, -bounds.y);
        
        Color boundingColor = Color.yellow;                
        if (mouseOver) {
            boundingColor = Color.yellow.darker();
        }
        else if (selected) {
            boundingColor = Color.yellow;
        }
        if (boundingColor!=null) {    
            g.setColor(boundingColor);
            drawBoxCorners(bounds.x-3, bounds.y-3, bounds.width+6, bounds.height+6,g);
        }        
        drawBoxCorners(bounds.x-3, bounds.y-3, bounds.width+6, bounds.height+6,g);
        
        
    }
    
    void drawBoxCorners(int x, int y, int w, int h, Graphics g) {
        // g.drawRect(x, y, w, h);        
        g.drawLine(x, y, x+3, y);
        g.drawLine(x, y, x, y+3);
        
        g.drawLine(x+w, y, x+w-3, y);
        g.drawLine(x+w, y, x+w, y+3);
        
        g.drawLine(x, y+h, x+3, y+h);
        g.drawLine(x, y+h, x, y+h-3);
        
        g.drawLine(x+w, y+h, x+w-3, y+h);
        g.drawLine(x+w, y+h, x+w, y+h-3);
        
    }
    
    public List<SectionItem> hitTest(int x, int y, List<SectionItem> list) {
        x = x - bounds.x;
        y = y - bounds.y;
        for (Section section:sections) {
            section.hitTest(x,y, list);     
        }
        return list;
    }
        
    public void eventSelected(SectionItem item, boolean b) {
        // check if table layout needs to be redone
        if (item.data instanceof FooterItem) {
            FooterItem fi = (FooterItem) item.data;
            switch (fi) {
                case IDX:     showIndex = b; doLayout = true; break;
                case NOTNULL: showNotNull = b; doLayout = true; break;
                case PK:      showPK = b; doLayout = true; break;
                case TYPE:    showType = b; doLayout = true; break;
                case UQ:      showUnique = b; doLayout = true; break;
            }            
        }
    }    
    
    
  public void initCGTable(Graphics g) {
      Table table = (Table) data;
        header = new Section(SECTION_HEADER, this);
        header.add(new SectionItem("FT", SectionItem.ALLINGMENT.LEFT, 0, g));
        header.add(new SectionItem(table, SectionItem.ALLINGMENT.CENTER, 1, g));
        header.add(new SectionItem("RT", SectionItem.ALLINGMENT.RIGHT, 2, g));

        columns = new Section(SECTION_COLUMN, Section.ALLINGMENT.TOPTOBOTTOM, this);
        Column cols[] = table.getColumnsInOrder();
        for (int idx=0; idx<cols.length; idx++) {
            columns.add(new SectionItem(cols[idx], idx, g));
        }
           
        typeSection = new Section("type_constraint", Section.ALLINGMENT.TOPTOBOTTOM, this);   
        for (int idx=0; idx<cols.length; idx++) {   
            MetaDataStuff md = new MetaDataStuff(MetaData.TYPE, cols[idx].colType + (cols[idx].colTypeLen>0?"(" + cols[idx].colTypeLen + ")":""));
            typeSection.add(new SectionItem(md, idx, g));
        }                  

        
        if (table.primaryKeys.size()>0) {
            hasPrimaryKeys = true;
            contraintSections_pk = getConstraintSection(table.primaryKeys, "pk_constraint", cols, "pk", g);        
        }
        
        if (table.uniqueKeys.size()>0) {
            hasUniqueKeys = true;
            contraintSections_uq = getConstraintSection(table.uniqueKeys, "uq_constraint", cols, "uq", g);        
        }
        
        if (table.indices.size()>0) {
            hasIndexes = true;
            contraintSections_idx = getConstraintSection(table.indices, "idx_constraint", cols, "idx", g);
        }
        
        contraintSections_notnull = new Section("notnull_constraint", Section.ALLINGMENT.TOPTOBOTTOM, this);                
        for (int idx=0; idx<cols.length; idx++) {         
            if (cols[idx].isNotNull) {
                hasNotNull = true;
            }
            MetaDataStuff md = new MetaDataStuff(MetaData.PK, cols[idx].isNotNull?"not null":"");
            contraintSections_notnull.add(new SectionItem(md, idx, g));
        }          

        if (!hasNotNull) {
            contraintSections_notnull = null;
        }
  }    
  
  public void updateCGTable(Graphics g) {
        removeAllSections();
        Table table = (Table) data;
                    
        List<Section> constraintSections = new ArrayList();
        if (showType) { constraintSections.add(typeSection); }
        if (showPK) { constraintSections.addAll(contraintSections_pk); }
        if (showUnique) { constraintSections.addAll(contraintSections_uq); }
        if (showIndex) { constraintSections.addAll(contraintSections_idx); }
        if (showNotNull) { constraintSections.add(contraintSections_notnull); }
        
        Section footer = new Section(SECTION_FOOTER, this);        
        footer.add(new SectionItem(FooterItem.TYPE, SectionItem.ALLINGMENT.LEFT, -1, showType, g));
        if (hasPrimaryKeys) { footer.add(new SectionItem(FooterItem.PK, SectionItem.ALLINGMENT.LEFT, -1, showPK, g)); }
        if (hasUniqueKeys) { footer.add(new SectionItem(FooterItem.UQ, SectionItem.ALLINGMENT.LEFT, -1, showUnique, g)); }
        if (hasIndexes) { footer.add(new SectionItem(FooterItem.IDX, SectionItem.ALLINGMENT.LEFT, -1, showIndex, g)); }
        if (hasNotNull) { footer.add(new SectionItem(FooterItem.NOTNULL, SectionItem.ALLINGMENT.LEFT, -1, showNotNull, g)); }
        
        add(header);
        add(columns);
        // set(indexSections);
        for (Section section:constraintSections) {
            add(section);
        }
        add(footer);
        setupTableLayout(g, header, columns, constraintSections, footer);        
    }
  
   
    List<Section> getConstraintSection(List<String[]> contraints, String sectionName, Column cols[], String str, Graphics g) {        
        List<Section> sectionsList = new ArrayList();        
        for(String[] constrCols:contraints) {
            Section constrSection = new Section(sectionName, Section.ALLINGMENT.TOPTOBOTTOM, this);        
            for (int idx=0; idx<cols.length; idx++) {                        
                boolean flagAsConstraint = false;
                for (String colName:constrCols) {                
                    if (cols[idx].name.equals(colName)) {
                        flagAsConstraint = true;
                        break;
                    }
                }            
                MetaDataStuff md = new MetaDataStuff(MetaData.PK, flagAsConstraint?str:"");
                constrSection.add(new SectionItem(md, idx, g));
            }      
            sectionsList.add(constrSection);
        }    
        return sectionsList;
    }
    
    public void setupTableLayout(Graphics g, Section header, Section columns, List<Section> indexSections, Section footer) {
        columns.resetBounds(g);
        header.resetBounds(g);
        layout(g);
        UtilLayout.setBelow(header, columns);
        int w_section = 0;
        for (Section section:indexSections) {
            w_section += section.sectionDimension.width;
        }                      
        int minColumnWidth = columns.sectionDimension.width;
        int minConstraintsWidth = w_section;
        int minHeaderWidth = header.sectionDimension.width;
        
        int minWidth = Math.max(minColumnWidth+minConstraintsWidth, minHeaderWidth);
        
        UtilLayout.allignWidth(minWidth, header);
        UtilLayout.allignWidth(minWidth - minConstraintsWidth, columns);                    
        columns.resizeSectionItemsWidth(g);
        Section prevSection = columns;
        for (Section section:indexSections) {
            UtilLayout.setTop(prevSection, section);
            UtilLayout.setRight(prevSection, section);
            section.resizeSectionItemsWidth(g);
            prevSection = section;
        }   
        UtilLayout.setBelow(columns, footer);
        header.relayoutSectionItems(g);  
        
        // cg bounds
        Rectangle r = new Rectangle();
        // g.translate(bounds.x, bounds.y);
        for (Section section:sections) {
            Rectangle rs = new Rectangle(section.location.x, section.location.y, section.sectionDimension.width, section.sectionDimension.height);
            r.add(rs);
        }      
        bounds.width = r.width;
        bounds.height = r.height;
        
    }    

    class MetaDataStuff {
        MetaData md;
        String str;

        MetaDataStuff(MetaData md, String str) {
            this.md = md;            
            this.str = str;
        }
    }
}

