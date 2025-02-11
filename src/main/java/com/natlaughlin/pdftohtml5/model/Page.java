package com.natlaughlin.pdftohtml5.model;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Page extends Node
{

    private int id;
    private int number;
    private float width;
    private float height;
    private String base64;

    
    private ArrayList<Paragraph> paragraphs = new ArrayList<Paragraph>();
    
    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }
    public int getNumber()
    {
        return number;
    }
    public void setNumber(int number)
    {
        this.number = number;
    }
    public float getWidth()
    {
        return width;
    }
    public void setWidth(float width)
    {
        this.width = width;
    }
    public float getHeight()
    {
        return height;
    }
    public void setHeight(float height)
    {
        this.height = height;
    }
    public String getBase64()
    {
        return base64;
    }
    public void setBase64(String base64)
    {
        this.base64 = base64;
    }
    
    public ArrayList<Paragraph> getParagraphs()
    {
        return paragraphs;
    }
    
    public void addParagraph(Paragraph value)
    {
        paragraphs.add(value);
    }
    
    public void addParagraphs(List<Paragraph> value)
    {
        paragraphs.addAll(value);
    }
    
    public Rectangle2D toRectangle()
    {
        Rectangle2D rect = null;
        
        for(Paragraph p : getParagraphs())
        {
            Rectangle2D pRect = p.toRectangle();
            if(rect == null)
            {
                rect = pRect;
            }
            else
            {
                Rectangle2D.union(rect, pRect, rect);
            }
        }
        
        return rect;
    }
    
    @Override
    public ArrayList getSubNodes()
    {
        return getParagraphs();
    }
    

}
