package com.natlaughlin.pdftohtml5.model;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.pdfbox.pdmodel.font.PDFont;

public class WordFragment extends Node
{
    private ArrayList<Text> texts = new ArrayList<Text>();
    
    public ArrayList<Text> getTexts()
    {
        return texts;
    }
    
    public void addText(Text value)
    {
        texts.add(value);
    }
    
    public void addTexts(List<Text> values)
    {
        texts.addAll(values);
    }
    
    
    private Float direction = null;
    public void setDirection(Float value)
    {
        direction = value;
    }
    public Float getDirection()
    {
        return direction;
    }
    
    
    private Double letterSpacing = null;
    public void setLetterSpacing(Double value)
    {
        letterSpacing = value;
    }
    public Double getLetterSpacing()
    {
        return letterSpacing;
    }
    
    
    @Override
    public String toString()
    {
        String out = "";
        
        for(Text text : getTexts())
            out += text.toString();
        
        return out;
    }
    
    public Text getFirstText()
    {
        if(getTexts().size() > 0)
            return getTexts().get(0);
        
        return null;
    }
    
    public Text getLastText()
    {
        if(getTexts().size() > 0)
            return getTexts().get(getTexts().size()-1);
        
        return null;
    }
    
    public Rectangle2D toRectangle()
    {
        Rectangle2D rect = null;
        
        for(Text p : getTexts())
        {
            Rectangle2D tpRect = textPositionToRectangle(p);
            if(rect == null)
            {
                rect = tpRect;
            }
            else
            {
                Rectangle2D.union(rect, tpRect, rect);
            }
        }
        
        return rect;
    }
    
    
    public static Rectangle2D textPositionToRectangle(Text text)
    {
        float positionX = text.getTextPosition().getX();
        float positionY = text.getTextPosition().getY();
        float positionWidth = text.getTextPosition().getWidth();
        float positionHeight = text.getTextPosition().getHeight();
        
        Rectangle2D rect = new Rectangle2D.Float();
        rect.setRect(positionX, positionY, positionWidth, positionHeight);
        return rect;
    }
    
    @Override
    public ArrayList getSubNodes()
    {
        return getTexts();
    }

    

}
