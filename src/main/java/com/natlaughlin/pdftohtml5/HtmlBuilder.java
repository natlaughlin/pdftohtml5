
package com.natlaughlin.pdftohtml5;

import com.natlaughlin.pdftohtml5.model.Font;
import com.natlaughlin.pdftohtml5.model.*;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;

public class HtmlBuilder
{
	private Writer writer;
	
	private List<Style> fontNames;

	private StringTemplateGroup stringTemplateGroup;

	private StringTemplate stCss;

	
	private HashMap<Font,Style> fontStyles;
	private HashMap<Color,Style> colorStyles;

	public HtmlBuilder() throws Exception
	{
		initialize();
	}
	
	public void initialize() throws Exception
	{

		stringTemplateGroup = new StringTemplateGroup("htmlbuilder");
		stCss = stringTemplateGroup.getInstanceOf("templates/css");

		fontStyles = new HashMap<Font,Style>();
		colorStyles = new HashMap<Color,Style>();
	}
	
	
	public void addFontFace(Font font) throws Exception
	{
		StringTemplate stFontFace = stringTemplateGroup.getInstanceOf("templates/fontface");
		stFontFace.setAttribute("family", font.getName());
		stFontFace.setAttribute("base64", font.getBase64());
		
		stCss.setAttribute("fontfaces", stFontFace);
	}
	
	public void startHtml() throws Exception
	{
		StringTemplate stHtmlStart = stringTemplateGroup.getInstanceOf("templates/htmlStart");
		stHtmlStart.setAttribute("css", stCss.toString());
		writer.append(stHtmlStart.toString());
	}
	
	public void endHtml() throws Exception
	{
		StringTemplate stHtmlEnd = stringTemplateGroup.getInstanceOf("templates/htmlEnd");
		writer.append(stHtmlEnd.toString());
	}

	private Page page = null;
	private Color pageColor = null;
	private PDFont pageFont = null;
	private Float pageFontSize = null;
	public void startPage(Page page) throws Exception
	{
		
		StringTemplate stPageStart = stringTemplateGroup.getInstanceOf("templates/pageStart");
		stPageStart.setAttribute("id", page.getId());
		stPageStart.setAttribute("width", page.getWidth());
		stPageStart.setAttribute("height", page.getHeight());
		stPageStart.setAttribute("base64", page.getBase64());
		
		
		pageColor = page.getDominantColor();
		pageFont = page.getDominantFont();
		pageFontSize = page.getDominantFontSize();
		if(pageFont != null){
			stPageStart.setAttribute("font", pageFont.getBaseFont());
		}
		if(pageFontSize != null){
			stPageStart.setAttribute("fontSize", pageFontSize);
		}
		stPageStart.setAttribute("r", pageColor.getRed());
		stPageStart.setAttribute("g", pageColor.getGreen());
		stPageStart.setAttribute("b", pageColor.getBlue());

		this.page = page;
		
		writer.append(stPageStart.toString());
	}
	
	public void endPage() throws Exception
	{
		StringTemplate stPageEnd = stringTemplateGroup.getInstanceOf("templates/pageEnd");
		writer.append(stPageEnd.toString());
		
		writer.flush();
	}
	
	private double xParagraph = 0;
	private double yParagraph = 0;
	private Paragraph paragraph = null;
	private Color paragraphColor = null;
	private PDFont paragraphFont = null;
	private Float paragraphFontSize = null;
	public void startParagraph(Paragraph paragraph) throws Exception
	{
		Rectangle2D rect = paragraph.toRectangle();
		float top = (float) rect.getY();
		float left = (float) rect.getX();
		
		StringTemplate stParagraphStart = stringTemplateGroup.getInstanceOf("templates/paragraphStart");
		
		paragraphFont = paragraph.getDominantFont();
		paragraphFontSize = paragraph.getDominantFontSize();
		paragraphColor = paragraph.getDominantColor();
		
		xParagraph = left;
		yParagraph = top;
		
		top -= paragraphFontSize * 0.9;
		
		if(top != 0)
			stParagraphStart.setAttribute("top", top);
		if(left != 0)
			stParagraphStart.setAttribute("left", left);
		
		if(paragraphFont != pageFont)
			stParagraphStart.setAttribute("font", paragraphFont.getBaseFont());
		if(Float.compare(paragraphFontSize,pageFontSize) != 0)
			stParagraphStart.setAttribute("fontSize", paragraphFontSize);
//		if(paragraphColor != pageColor)
//		{
//			stParagraphStart.setAttribute("r", paragraphColor.getRed());
//			stParagraphStart.setAttribute("g", paragraphColor.getGreen());
//			stParagraphStart.setAttribute("b", paragraphColor.getBlue());
//		}
		
		this.paragraph = paragraph;
		
		writer.append(stParagraphStart.toString());
		
	}
	
	public void endParagraph() throws Exception
	{
		
		StringTemplate stParagraphEnd = stringTemplateGroup.getInstanceOf("templates/paragraphEnd");
		writer.append(stParagraphEnd.toString());

		xParagraph = 0;
		yParagraph = 0;
		
	}
	
	double xLine = 0;
	double yLine = 0;
	private Line line = null;
	private Color lineColor = null;
	private PDFont lineFont = null;
	private Float lineFontSize = null;
	Double wordSpacing = null;
	public void startLine(Line line) throws Exception
	{
		Rectangle2D r = line.toRectangle();

		float top = (float) r.getY();
		float left = (float) r.getX();

		xLine = left;
		yLine = top;

		top -= yParagraph;
		left -= xParagraph;
		
		StringTemplate stLineStart = stringTemplateGroup.getInstanceOf("templates/lineStart");
		
		if(top != 0)
			stLineStart.setAttribute("top", top);
		if(left != 0)
			stLineStart.setAttribute("left", left);
		
		lineFont = line.getDominantFont();
		lineFontSize = line.getDominantFontSize();
		lineColor = line.getDominantColor();
		
		//wordSpacing = Double.valueOf(Float.valueOf(lineFontSize * lineFont.getStringWidth(" ")/1000));
		//stLineStart.setAttribute("wordSpacing",wordSpacing);
		
		if(lineFont != paragraphFont)
			stLineStart.setAttribute("font", lineFont.getBaseFont());
		if(Float.compare(lineFontSize, paragraphFontSize) != 0)
			stLineStart.setAttribute("fontSize", lineFontSize);
		if(lineColor != paragraphColor)
		{
			stLineStart.setAttribute("r", lineColor.getRed());
			stLineStart.setAttribute("g", lineColor.getGreen());
			stLineStart.setAttribute("b", lineColor.getBlue());
		}
		
		this.line = line;
		
		
		writer.append(stLineStart.toString());
		
	}
	public void endLine() throws Exception
	{
		StringTemplate stLineEnd = stringTemplateGroup.getInstanceOf("templates/lineEnd");
		writer.append(stLineEnd.toString());
		
		xLine = 0;
		yLine = 0;
	}
	
	double xWord = 0;
	double yWord = 0;
	private Word word = null;
	private Color wordColor = null;
	private PDFont wordFont = null;
	private Float wordFontSize = null;
	private int numSpaces = 0;
	public void startWord(Word word) throws Exception
	{
		Rectangle2D r1 = word.toRectangle();

		double top = r1.getY();
		double left = r1.getX();
		
		xWord = left;
		yWord = top;
		
		left -= xLine;
		top -= yLine;
		
		/*
		float spaceWidth = word.getLastWordFragment().getLastText().getTextPosition().getWidthOfSpace();
		numSpaces = 0;
		if(left >= spaceWidth)
		{
			BigDecimal spaces = new BigDecimal(left / spaceWidth);
			numSpaces = spaces.setScale(0, RoundingMode.DOWN).toBigInteger().intValue();
		}
		*/
		
		
		StringTemplate stWordStart = stringTemplateGroup.getInstanceOf("templates/wordStart");
		
		if(top != 0)
			stWordStart.setAttribute("top", top);
		if(left != 0)
			stWordStart.setAttribute("left", left);
		
		wordFont = word.getDominantFont();
		wordFontSize = word.getDominantFontSize();
		wordColor = word.getDominantColor();
		
		if(wordFont != lineFont)
			stWordStart.setAttribute("font", wordFont.getBaseFont());
		if(Float.compare(wordFontSize,lineFontSize) != 0)
			stWordStart.setAttribute("fontSize", wordFontSize);
		if(wordColor != lineColor)
		{
			stWordStart.setAttribute("r", wordColor.getRed());
			stWordStart.setAttribute("g", wordColor.getGreen());
			stWordStart.setAttribute("b", wordColor.getBlue());
		}
		
		this.word = word;
		
		writer.append(stWordStart.toString());
		
		xLine += word.toRectangle().getWidth();
		
	}
	
	public void endWord() throws Exception
	{
		
		StringTemplate stWordEnd = stringTemplateGroup.getInstanceOf("templates/wordEnd");
		writer.append(stWordEnd.toString());
		

	}
	
	public void startWordFragment(WordFragment wf) throws Exception
	{

	}
	
	public void endWordFragment(WordFragment wf) throws Exception
	{

	}
	
	
	double xText = 0;
	double yText = 0;
	private Text text = null;
	private Color textColor = null;
	private PDFont textFont = null;
	private Float textFontSize = null;
	public void startText(Text text) throws Exception
	{
		String character = text.getTextPosition().getCharacter();
		
		PDFont f = text.getTextPosition().getFont();

		String baseFont = text.getDominantFont().getBaseFont();

		if(text.getEncoded().length == 1)
		{
			int code = text.getEncoded()[0];
			if(code < 32)
			{
				int i = 0xe000 + code;
				String hex = Integer.toHexString(i);
				character = "&#x"+hex+";";
			}
			else
			{
				// undo pdfbox "smart" encoding
				String c = Character.toString((char)code);
				if(!character.equals(c))
				{
					character = c;
				}
			}
		}
		else
		{
			character = StringEscapeUtils.escapeHtml4(character);
		}
		
		Rectangle2D r1 = text.toRectangle();

		double top = r1.getY();
		double left = r1.getX();
		
		xText = left;
		yText = top;
		
		left -= xWord;
		top -= yWord;

		textFont = text.getDominantFont();
		textFontSize = text.getDominantFontSize();
		textColor = text.getDominantColor();
		
		StringTemplate stTextStart = stringTemplateGroup.getInstanceOf("templates/textStart");
		
		if(top != 0)
		{
			stTextStart.setAttribute("top", top);
		}

		if(textFont != null && textFont != wordFont)
		{
			stTextStart.setAttribute("font", textFont.getBaseFont());
		}
		if(Float.compare(textFontSize,wordFontSize) != 0)
		{
			stTextStart.setAttribute("fontSize", textFontSize);
		}
		if(textColor != null && textColor != wordColor)
		{
			stTextStart.setAttribute("r", textColor.getRed());
			stTextStart.setAttribute("g", textColor.getGreen());
			stTextStart.setAttribute("b", textColor.getBlue());
		}
		
		if(stTextStart.getAttributes() != null && stTextStart.getAttributes().size() > 0)
		{
			stTextStart.setAttribute("writeTag", true);
		}
		
		stTextStart.setAttribute("text", character);
		
		this.text = text;
		
		writer.append(stTextStart.toString());
	}
	
	public void endText() throws Exception
	{
		
	}


	public void setWriter(Writer value) throws Exception
	{
		writer = value;
	}
	
	


	private static String escape(String chars)
    {
        StringBuilder builder = new StringBuilder(chars.length());
        for (int i = 0; i < chars.length(); i++)
        {
            char c = chars.charAt(i);
            // write non-ASCII as named entities
            if ((c < 32) || (c > 127))
            {
            	int charAsInt = c;
            	if(c < 32)
            		charAsInt = c + 0xe000;

            	String hex = Integer.toHexString(charAsInt);

                builder.append("&#x").append(hex).append(";");
            }
            else
            {
                switch (c)
                {
                case 34:
                    builder.append("&quot;");
                    break;
                case 38:
                    builder.append("&amp;");
                    break;
                case 60:
                    builder.append("&lt;");
                    break;
                case 62:
                    builder.append("&gt;");
                    break;
                default:
                    builder.append(String.valueOf(c));
                }
            }
        }
        return builder.toString();
    }

}
