package com.natlaughlin.pdftohtml5.model;


import java.io.StringWriter;
import java.util.HashMap;

import javax.swing.text.html.CSS.Attribute;

public class Style
{
    private String selector = null;
    private HashMap<Attribute,String> properties = new HashMap<Attribute,String>();
    
    public String getSelector() throws Exception
    {
        return selector;
    }
    public void setSelector(String selector) throws Exception
    {
        this.selector = selector;
    }
    public HashMap<Attribute, String> getProperties() throws Exception
    {
        return properties;
    }
    public void setProperties(HashMap<Attribute, String> properties) throws Exception
    {
        this.properties = properties;
    }
    
    
    public void addProperty(Attribute css, String value) throws Exception
    {
        properties.put(css, value);
    }
    
    public String getProperty(Attribute css) throws Exception
    {
        return properties.get(css);
    }
    
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        try
        {
            String selector = String.format(".%s",getSelector());
            sb.append(selector);
            sb.append("{");
            for(Attribute property : getProperties().keySet())
            {
                String c = String.format("%s:%s;",property, getProperties().get(property));
                sb.append(c);
            }
            sb.append("}");
            
        }
        catch(Exception e)
        {
            return "error formatting style";
        }
        
        return sb.toString();
        
    }
    
}
