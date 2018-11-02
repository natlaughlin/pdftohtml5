package com.natlaughlin.pdftohtml5;


public class PdfToHtml5
{

    private static final String OUTPUT = "-output";

    /*
     * debug flag
     */
    private boolean debug = false;

    /**
     * @param args Command line arguments, should be one and a reference to a file.
     *
     * @throws Exception If there is an error parsing the document.
     */
    public static void main( String[] args ) throws Exception
    {
    	PdfToHtml5 o = new PdfToHtml5();
    	o.run(args);
    }

    public void run( String[] args ) throws Exception
    {
        String password = "";
        String encoding = "UTF-8";
        String pdfFile = null;
        String outputPath = "out";

        for( int i=0; i<args.length; i++ )
        {
           if( args[i].equals( OUTPUT ) )
    		{
            	i++;
                if( i >= args.length )
                {
                    usage();
                }
            	outputPath = args[i];
    		}
            else
            {
                if( pdfFile == null )
                {
                    pdfFile = args[i];
                }
            }
        }

        if( pdfFile == null )
        {
            usage();
        }
        else
        {
            PdfToHtml5Converter converter = new PdfToHtml5Converter(encoding);
           
            converter.setInputPdfFileName(pdfFile);
            converter.setInputPdfPassword(password);
            converter.setForceParsing(true);
            converter.setOutputBaseDir(outputPath);
            converter.convert();
               
        }
    }

    /**
     * This will print the usage requirements and exit.
     */
    private static void usage()
    {
        System.err.println(
                        "Usage: java -jar pdftohtml5.jar [OPTIONS] <PDF file>\n" +
                        "  -output <dir>	The output directory, defaults to 'out' \n" +
                        "  <PDF file>       The PDF document to use\n"
            );
        System.exit( 1 );
    }
}
