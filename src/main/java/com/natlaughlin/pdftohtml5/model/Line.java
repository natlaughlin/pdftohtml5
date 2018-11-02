package com.natlaughlin.pdftohtml5.model;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.util.TextPosition;

public class Line extends Node
{
	private ArrayList<Word> words = new ArrayList<Word>();
	
	public ArrayList<Word> getWords()
	{
		return words;
	}
	
	public void addWord(Word value)
	{
		words.add(value);
	}
	
	public void addWords(List<Word> value)
	{
		words.addAll(value);
	}
	
	public Word getFirstWord()
	{
		if(getWords().size() > 0)
			return getWords().get(0);
		
		return null;
	}
	
	public Word getLastWord()
	{
		if(getWords().size() > 0)
			return getWords().get(getWords().size()-1);
		
		return null;
	}
	
	public Rectangle2D toRectangle()
	{
		Rectangle2D rect = null;
    	
    	for(Word w : getWords())
    	{
    		Rectangle2D wRect = w.toRectangle();
    		if(rect == null)
    		{
    			rect = wRect;
    		}
    		else
    		{
    			Rectangle2D.union(rect, wRect, rect);
    		}
    	}
    	
    	return rect;
    	
	}
	
	@Override
	public String toString()
	{
		String out = "";
		
		for(Word word : getWords())
			out += word.toString() + " ";
		
		return out;
	}
	
	@Override 
	public ArrayList getSubNodes()
	{
		return getWords();
	}
}
