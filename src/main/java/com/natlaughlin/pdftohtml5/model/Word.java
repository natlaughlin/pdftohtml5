package com.natlaughlin.pdftohtml5.model;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.font.PDFont;

public class Word extends Node
{
    private ArrayList<WordFragment> wordFragments = new ArrayList<WordFragment>();
    
    public ArrayList<WordFragment> getWordFragments()
    {
        return wordFragments;
    }
    
    public void addWordFragment(WordFragment value)
    {
        wordFragments.add(value);
    }
    
    public void addWordFragments(List<WordFragment> values)
    {
        wordFragments.addAll(values);
    }
    

    @Override
    public String toString()
    {
        String out = "";
        
        for(WordFragment wordFragment : getWordFragments())
            out += wordFragment.toString();
        
        return out;
    }
    
    public WordFragment getFirstWordFragment()
    {
        if(getWordFragments().size() > 0)
            return getWordFragments().get(0);
        
        return null;
    }
    
    public WordFragment getLastWordFragment()
    {
        if(getWordFragments().size() > 0)
            return getWordFragments().get(getWordFragments().size()-1);
        
        return null;
    }
    
    public Rectangle2D toRectangle()
    {
        Rectangle2D rect = null;
        
        for(WordFragment p : getWordFragments())
        {
            Rectangle2D tpRect = p.toRectangle();
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
    
    @Override
    public ArrayList getSubNodes()
    {
        return getWordFragments();
    }
    

}
