
package com.natlaughlin.pdftohtml5.model;

import java.io.Serializable;

import org.antlr.stringtemplate.StringTemplate;

public class Font implements Serializable
{
    
    public static class Format
    {
        public static final int PFB = 0;
        public static final int CFF = 1;
        public static final int TTF = 2;
        public static final int OTF = 3;
        public static final int WOFF = 4;
    }
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private Integer format;
    private String base64;
    private Boolean converted;
    

    public Long getId()
    {
        return id;
    }
    public void setId(Long id)
    {
        this.id = id;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public Integer getFormat()
    {
        return format;
    }
    public void setFormat(Integer format)
    {
        this.format = format;
    }
    public String getBase64()
    {
        return base64;
    }
    public void setBase64(String base64)
    {
        this.base64 = base64;
    }
    public Boolean getConverted()
    {
        return converted;
    }
    public void setConverted(Boolean converted)
    {
        this.converted = converted;
    }

    

}
