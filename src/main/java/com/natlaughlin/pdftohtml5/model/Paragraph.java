package com.natlaughlin.pdftohtml5.model;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Paragraph extends Node
{

	private ArrayList<Line> lines = new ArrayList<Line>();
	
	public ArrayList<Line> getLines()
	{
		return lines;
	}
	
	public void addLine(Line value)
	{
		lines.add(value);
	}
	
	public void addLines(List<Line> value)
	{
		lines.addAll(value);
	}
	
	public Line getFirstLine()
	{
		if(getLines().size() > 0)
			return getLines().get(0);
		
		return null;
	}
	
	public Line getLastLine()
	{
		if(getLines().size() > 0)
			return getLines().get(getLines().size()-1);
		
		return null;
	}
	
	public Rectangle2D toRectangle()
	{
		Rectangle2D rect = null;
    	
    	for(Line l : getLines())
    	{
    		Rectangle2D lRect = l.toRectangle();
    		if(rect == null)
    		{
    			rect = lRect;
    		}
    		else
    		{
    			Rectangle2D.union(rect, lRect, rect);
    		}
    	}
    	
    	return rect;
	}
	
	@Override
	public String toString()
	{
		String out = "";
		
		for(Line line : getLines())
			out += line.toString();
		
		return out;
	}
	
	@Override
	public ArrayList getSubNodes()
	{
		return getLines();
	}

}
