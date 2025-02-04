package com.natlaughlin.pdftohtml5;


import org.junit.Test;

import java.io.IOException;


public class TestConvertPDF
{
	
	@Test
	public void testConvert()
	{
		PdfToHtml5Converter converter = null;
		try{
			converter = new PdfToHtml5Converter("utf-8");
		}
		catch(IOException e){

		}
		converter.setInputPdfFileName("src/test/resources/p01sep02.pdf");
		converter.setInputPdfPassword("");
		converter.setForceParsing(true);
		converter.setOutputBaseDir("out");
		try{
			converter.convert();
		}
		catch(Exception e){
			
		}
	}
}
