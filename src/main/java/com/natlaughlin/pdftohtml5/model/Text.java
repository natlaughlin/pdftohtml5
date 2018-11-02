package com.natlaughlin.pdftohtml5.model;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.util.TextPosition;

import java.awt.*;
import java.awt.geom.Rectangle2D;


public class Text extends Node
{
	private int page;
	private int article;

	private TextPosition textPosition;
	private byte[] encoded;
	private Color color;
	
	public Rectangle2D toRectangle()
	{
		Rectangle2D rect = new Rectangle2D.Float();
		rect.setRect(textPosition.getX(), textPosition.getY(), textPosition.getWidth(), textPosition.getHeight());
		return rect;
	}

	public int getArticle()
	{
		return article;
	}

	public void setArticle(int article)
	{
		this.article = article;
	}

	public TextPosition getTextPosition()
	{
		return textPosition;
	}

	public void setTextPosition(TextPosition textPosition)
	{
		this.textPosition = textPosition;
	}

	public byte[] getEncoded(){ return encoded; }
	public void setEncoded(byte[] value) { encoded = value; }
	
	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}
	
	@Override
	public String toString()
	{
		return textPosition.getCharacter();
	}
	
	@Override
	public Color getDominantColor()
	{
		return getColor();
	}
	
	@Override 
	public PDFont getDominantFont()
	{
		return getTextPosition().getFont();
	}
	
	@Override 
	public Float getDominantFontSize()
	{
		return getTextPosition().getTextPos().getValue(0, 0);
	}
	
	
}
