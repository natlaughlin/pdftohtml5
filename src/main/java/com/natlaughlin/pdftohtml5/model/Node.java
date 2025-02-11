package com.natlaughlin.pdftohtml5.model;

import org.apache.pdfbox.pdmodel.font.PDFont;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
public class Node
{
    
    
    public ArrayList<Node> getSubNodes()
    {
        return null;
    }
    
    
    public PDFont getDominantFont()
    {
        ArrayList<PDFont> fonts = new ArrayList<PDFont>();
        for(Node n : getSubNodes())
        {
            fonts.add(n.getDominantFont());
        }
        if(fonts.size() == 0){
            return null;
        }
        PDFont f = (PDFont) getDominantProperty(fonts);
        return f;
    }
    
    public Float getDominantFontSize()
    {
        ArrayList<Float> fontSizes = new ArrayList<Float>();
        for(Node n : getSubNodes())
        {
            fontSizes.add(n.getDominantFontSize());
        }
        if(fontSizes.size() == 0){
            return null;
        }
        Float fs = (Float) getDominantProperty(fontSizes);
        return fs;
    }
    
    public Color getDominantColor()
    {
        ArrayList<Color> colors = new ArrayList<Color>();
        for(Node n : getSubNodes())
        {
            colors.add(n.getDominantColor());
        }
        if(colors.size() == 0){
            return new Color(0,0,0);
        }
        Color c = (Color) getDominantProperty(colors);
        return c;
    }
    
    public static Object getDominantProperty(ArrayList list)
    {
        HashMap<Object,Integer> map = new HashMap<Object,Integer>();
        
        for(Object o : list)
        {
            if(!map.containsKey(o))
            {
                map.put(o, 0);
            }
            else
            {
                map.put(o, map.get(o) + 1);
            }
        }
        
        Integer max = Collections.max(map.values());
        for(Object o : map.keySet())
        {
            if(map.get(o) == max)
                return o;
        }
        
        return null;
    }
    
}
