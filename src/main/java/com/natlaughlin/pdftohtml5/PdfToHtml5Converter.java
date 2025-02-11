package com.natlaughlin.pdftohtml5;

import com.natlaughlin.pdftohtml5.model.Font;
import com.natlaughlin.pdftohtml5.model.*;
import com.natlaughlin.pdftohtml5.util.Task;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.fontbox.util.ResourceLoader;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptorDictionary;
import org.apache.pdfbox.pdmodel.text.PDTextState;
import org.apache.pdfbox.util.ImageIOUtil;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PdfToHtml5Converter extends PDFTextStripper
{
    public static int DPI = 300;

    private String inputPdfFileName;
    private String inputPdfPassword;
    private String outputBaseDir;

    public void setInputPdfFileName(String value)
    {
        inputPdfFileName = value;
    }

    public void setInputPdfPassword(String value)
    {
        inputPdfPassword = value;
    }

    public void setOutputBaseDir(String value)
    {
        outputBaseDir = value;
    }

    private List<Text> texts = new ArrayList<Text>();
    private List<Font> fonts = new ArrayList<Font>();
    private List<Color> colors = new ArrayList<Color>();
    private HtmlBuilder htmlBuilder;
    private File tempDir;

    private File pdfFile;
    private List<File> imgFiles = new ArrayList<File>();
    private byte[] encodedText;



    /*
     *
     *
     *
     * CONSTRUCTOR
     */

    public PdfToHtml5Converter(String encoding) throws IOException
    {
        super(ResourceLoader.loadProperties("PDFToHTML.properties"));
    }

    /*
     *
     *
     *
     * MAIN
     */

    public void convert() throws Exception
    {
        try
        {
            initialize();
            createPdfImages();
            extractPdfFonts();
            processPdfDocument();

        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            cleanup();
        }

    }

    private void createPdfImages() throws Exception
    {

        File pdfWithoutTextFile = createPdfWithoutText(pdfFile);
        imgFiles = convertPdfToImages(pdfWithoutTextFile, BufferedImage.TYPE_INT_RGB, "png", DPI);
        
    }

    private List<File> convertPdfToImages(File pdf, int imgType, String imgFormat, int resolution) throws Exception
    {
        ArrayList<File> images = new ArrayList<File>();

        PDDocument inputDocument = loadPdf(pdf);

        List allPages = inputDocument.getDocumentCatalog().getAllPages();
        for( int i=0; i<allPages.size(); i++ )
        {
            PDPage page = (PDPage)allPages.get( i );
            
            String imgFileName = String.format("%s-page%d",getFileNameWithoutExtension(pdf), i+1);
            File imgFile = new File(tempDir,imgFileName);
            
            
            BufferedImage image = page.convertToImage(imgType, resolution);
            FileOutputStream os = new FileOutputStream(imgFile);
            ImageIOUtil.writeImage(image, imgFormat, os, imgType, resolution);

            images.add(imgFile);
            
        }

        inputDocument.close();

      
        return images;
    }

    private File createPdfWithoutText(File pdf) throws Exception
    {
        PDDocument inputDocument = loadPdf(pdf);

        String pdfWithoutTextFileName = String.format("%s-withoutText.pdf",getFileNameWithoutExtension(pdf));
        File pdfWithoutTextFile = new File(tempDir,pdfWithoutTextFileName);
        String pdfWithoutTextAbsFileName = pdfWithoutTextFile.getAbsolutePath();

        List allPages = inputDocument.getDocumentCatalog().getAllPages();
        for( int i=0; i<allPages.size(); i++ )
        {
            PDPage page = (PDPage)allPages.get( i );
            PDFStreamParser parser = new PDFStreamParser(page.getContents());
            parser.parse();
            List tokens = parser.getTokens();
            List newTokens = new ArrayList();
            for( int j=0; j<tokens.size(); j++)
            {
                Object token = tokens.get( j );
                if( token instanceof PDFOperator )
                {
                    PDFOperator op = (PDFOperator)token;
                    if( op.getOperation().equals( "TJ") || op.getOperation().equals( "Tj" ))
                    {
                        //remove the one argument to this operator
                        newTokens.remove( newTokens.size() -1 );
                        continue;
                    }
                }
                newTokens.add( token );

            }
            PDStream newContents = new PDStream( inputDocument );
            ContentStreamWriter writer = new ContentStreamWriter( newContents.createOutputStream() );
            writer.writeTokens( newTokens );
            newContents.addCompression();
            page.setContents(newContents);
        }
        inputDocument.save( pdfWithoutTextAbsFileName );
        
        inputDocument.close();
         
        return pdfWithoutTextFile;
    }

    private void initialize() throws Exception
    {

        tempDir = File.createTempFile("temp", Long.toString(System.nanoTime()));
        tempDir.delete();
        tempDir.mkdir();



        File inputPdfFile = new File(inputPdfFileName);
        pdfFile = new File(tempDir, inputPdfFile.getName());
        FileUtils.copyFile(inputPdfFile, pdfFile);


        InputStream stream = ResourceLoader.loadResource("scripts/encode.pe");
        File convert = new File(tempDir, "encode.pe");
        FileUtils.copyInputStreamToFile(stream, convert);
        String convertFilename = convert.getAbsolutePath();

        String resource = "env/win32.zip";
        unzipResource(resource, tempDir);

    }

    private void cleanup() throws Exception
    {
        FileUtils.deleteDirectory(tempDir);
    }

    public PDDocument loadPdf(File pdfFile) throws Exception
    {
        PDDocument doc = PDDocument.load(pdfFile.getAbsolutePath(), true);

        if (doc.isEncrypted())
        {
            StandardDecryptionMaterial sdm = new StandardDecryptionMaterial(
                    inputPdfPassword);
            doc.openProtection(sdm);
            AccessPermission ap = doc.getCurrentAccessPermission();

            if (!ap.canExtractContent())
            {
                throw new IOException(
                        "You do not have permission to extract content");
            }
        }

        return doc;
    }

    public void processPdfDocument() throws Exception
    {

        resetEngine();

        document = loadPdf(pdfFile);
        List<COSObjectable> allpages = (List<COSObjectable>) document.getDocumentCatalog().getAllPages();

        List<COSObjectable> pages = new ArrayList<COSObjectable>();
        pages.add(allpages.get(0));





        String htmlFileName = getFileNameWithExtension(pdfFile, ".html");
        File htmlFile = new File(htmlFileName);

        File outputDir = new File(outputBaseDir);
        if(!outputDir.canWrite())
        {
            System.err.println(String.format("Can't write to directory: %s",outputBaseDir));
        }
        outputDir.mkdir();

        File outputHtmlFile = new File(outputBaseDir, htmlFile.getName());
        outputHtmlFile.createNewFile();
        FileWriter writer = new FileWriter(outputHtmlFile);


        htmlBuilder = new HtmlBuilder();
        htmlBuilder.setWriter(writer);


        for(Font font : fonts)
        {

            if(font.getFormat() == Font.Format.TTF && font.getConverted() == true)
            {
                htmlBuilder.addFontFace(font);
            }

            else if(font.getFormat() == Font.Format.OTF     && font.getConverted() == true)
            {
                htmlBuilder.addFontFace(font);
            }

            else{
                String hi = "hi";
            }
        }

        htmlBuilder.startHtml();


        processPages(allpages);


        htmlBuilder.endHtml();

    }



    @Override
    protected void processPage(PDPage page, COSStream content)
            throws IOException
    {
   


        float width = page.getMediaBox().getWidth();
        float height = page.getMediaBox().getHeight();

        Page p = new Page();
        p.setId(getCurrentPageNo());
        p.setNumber(getCurrentPageNo());
        p.setHeight(height);
        p.setWidth(width);

        File imgFile = imgFiles.get(getCurrentPageNo()-1);
        String base64 = base64EncodeFile(imgFile);
        p.setBase64(base64);

        texts = new ArrayList<Text>();
        processStream(page, page.findResources(), content);

        ArrayList<WordFragment> wordFragments = getWordFragments(texts);
        ArrayList<Word> words = getWords(wordFragments);
        ArrayList<Line> lines = getLines(words);
        ArrayList<Paragraph> paragraphs = getParagraphs(lines);
        
        p.addParagraphs(paragraphs);
        
        try
        {
        
            htmlBuilder.startPage(p);

            for(Paragraph paragraph : p.getParagraphs())
            {
                htmlBuilder.startParagraph(paragraph);

                for(Line line : paragraph.getLines())
                {

                    htmlBuilder.startLine(line);

                    for(Word word : line.getWords())
                    {
                        htmlBuilder.startWord(word);

                        for(WordFragment wf : word.getWordFragments())
                        {
                            htmlBuilder.startWordFragment(wf);

                            for(Text text : wf.getTexts())
                            {
                                htmlBuilder.startText(text);

                                htmlBuilder.endText();
                            }

                            htmlBuilder.endWordFragment(wf);
                        }

                        htmlBuilder.endWord();
                    }

                    htmlBuilder.endLine();

                }

                htmlBuilder.endParagraph();
            }

            htmlBuilder.endPage();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }








    }

    @Override
    protected void processTextPosition(TextPosition text)
    {
        int article = 0;
        int page = getCurrentPageNo();

        Composite composite = null;
        Color color = null;
        switch (this.getGraphicsState().getTextState().getRenderingMode())
        {
        case PDTextState.RENDERING_MODE_FILL_TEXT:
            composite = this.getGraphicsState().getNonStrokeJavaComposite();
            try
            {
                color = this.getGraphicsState().getNonStrokingColor().getJavaColor();
            }
            catch (Exception e)
            {
            }
            break;
        case PDTextState.RENDERING_MODE_STROKE_TEXT:
            composite = this.getGraphicsState().getStrokeJavaComposite();
            try
            {
                color = this.getGraphicsState().getStrokingColor()
                        .getJavaColor();
            } catch (Exception e)
            {
            }
            break;
        case PDTextState.RENDERING_MODE_NEITHER_FILL_NOR_STROKE_TEXT:
            // basic support for text rendering mode "invisible"
            Color nsc = null;
            try
            {
                nsc = getGraphicsState().getStrokingColor().getJavaColor();
            } catch (Exception e)
            {
            }
            float[] components =
                { Color.black.getRed(), Color.black.getGreen(),
                        Color.black.getBlue() };
            if (nsc != null)
                color = new Color(nsc.getColorSpace(), components, 0f);
            composite = this.getGraphicsState().getStrokeJavaComposite();
            break;
        default:
            break;
        }

        Text t = new Text();
        t.setEncoded(encodedText);
        t.setTextPosition(text);
        t.setColor(color);

        texts.add(t);

    }
    
    public ArrayList<WordFragment> getWordFragments(List<Text> inputTexts)
    {
        ArrayList<WordFragment> wordFragmentList = new ArrayList<WordFragment>();

        for(Text text : inputTexts)
        {
            WordFragment wordFragment = new WordFragment();
            wordFragment.addText(text);
            wordFragment.setDirection(text.getTextPosition().getDir());
            wordFragment.setLetterSpacing(0d);
            wordFragmentList.add(wordFragment);
        }

        if(wordFragmentList.size() < 2)
            return wordFragmentList;

        Queue<WordFragment> next = new ArrayDeque<WordFragment>();
        next.addAll(wordFragmentList);

        Iterator<WordFragment> it = next.iterator();
        WordFragment w1 = it.next();
        WordFragment w2;
        while(it.hasNext())
        {
            w2 = it.next();

            Text t1 = w1.getLastText();
            Text t2 = w2.getFirstText();

            Rectangle2D r1 = t1.toRectangle();
            Rectangle2D r2 = t2.toRectangle();

            if(r1.getY() != r2.getY())
            {
                w1 = w2;
                continue;
            }

            if(r1.getHeight() != r2.getHeight())
            {
                w1 = w2;
                continue;
            }

            double letterSpacing = r2.getX() - (r1.getX() + r1.getWidth());

            if(letterSpacing == 0)
            {
                WordFragment w3 = new WordFragment();
                w3.addTexts(w1.getTexts());
                w3.addTexts(w2.getTexts());
                w3.setLetterSpacing(letterSpacing);
                wordFragmentList.add(wordFragmentList.indexOf(w1), w3);
                wordFragmentList.remove(w1);
                wordFragmentList.remove(w2);

                w1 = w3;

            }
            else
            {
                w1 = w2;
            }

        }

        return wordFragmentList;
    }
    
    private ArrayList<Word> getWords(List<WordFragment> wordFragments)
    {
        ArrayList<Word> wordList = new ArrayList<Word>();

        for(WordFragment wordFragment : wordFragments)
        {
            Word word = new Word();
            word.addWordFragment(wordFragment);
            wordList.add(word);
        }

        if(wordList.size() < 2)
            return wordList;

        Queue<Word> next = new ArrayDeque<Word>();
        next.addAll(wordList);

        Iterator<Word> it = next.iterator();
        Word w1 = it.next();
        Word w2;

        while(it.hasNext())
        {
            w2 = it.next();

            WordFragment wf1 = w1.getLastWordFragment();
            WordFragment wf2 = w2.getFirstWordFragment();

            Text t1 = wf1.getLastText();
            Text t2 = wf2.getFirstText();

            Rectangle2D r1 = t1.toRectangle();
            Rectangle2D r2 = t2.toRectangle();



            boolean isSameHeight = false;

            double dHeight = r2.getY() - r1.getY();

            if(dHeight == 0)
                isSameHeight = true;

            boolean isSameWidth = false;

            double spacing = r2.getX() - (r1.getX() + r1.getWidth());
            double widthOfSpace = t1.getTextPosition().getWidthOfSpace();

            if(spacing == 0)
                isSameWidth = true;

            if(isSameHeight && isSameWidth)
            {
                Word w3 = new Word();
                w3.addWordFragments(w1.getWordFragments());
                w3.addWordFragments(w2.getWordFragments());
                wordList.add(wordList.indexOf(w1), w3);
                wordList.remove(w1);
                wordList.remove(w2);

                w1 = w3;

            }
            else
            {
                w1 = w2;
            }

        }


        return wordList;
    }
    
    public ArrayList<Line> getLines(List<Word> words)
    {
        ArrayList<Line> lineList = new ArrayList<Line>();

        for(Word word : words)
        {
            Line line = new Line();
            line.addWord(word);
            lineList.add(line);
        }

        if(lineList.size() < 2)
            return lineList;

        Queue<Line> next = new ArrayDeque<Line>();
        next.addAll(lineList);

        Iterator<Line> it = next.iterator();
        Line l1 = it.next();
        Line l2;

        while(it.hasNext())
        {
            l2 = it.next();

            Rectangle2D r1 = l1.toRectangle();
            Rectangle2D r2 = l2.toRectangle();

            double y1 = r1.getY();
            double y2 = r2.getY();

            Boolean isSameLine = false;
            double yMetric = Math.abs(y1-y2) ;
            double min = Math.min(r1.getHeight(), r2.getHeight());
            if( Math.abs(y1-y2) < min)
                isSameLine = true;

            if(isSameLine)
            {
                Line l3 = new Line();
                l3.addWords(l1.getWords());
                l3.addWords(l2.getWords());
                lineList.add(lineList.indexOf(l1), l3);
                lineList.remove(l1);
                lineList.remove(l2);

                l1 = l3;
                continue;
            }
            else
            {
                l1 = l2;
            }

        }

        return lineList;

    }
    
    
    public ArrayList<Paragraph> getParagraphs(List<Line> lines)
    {
        ArrayList<Paragraph> paragraphList = new ArrayList<Paragraph>();

        for(Line line : lines)
        {
            Paragraph paragraph = new Paragraph();
            paragraph.addLine(line);
            paragraphList.add(paragraph);
        }

        if(paragraphList.size() < 2)
            return paragraphList;

        Queue<Paragraph> next = new ArrayDeque<Paragraph>();
        next.addAll(paragraphList);

        Iterator<Paragraph> it = next.iterator();
        Paragraph p1 = it.next();
        Paragraph p2;

        while(it.hasNext())
        {
            p2 = it.next();

            Line l1 = p1.getLastLine();
            Line l2 = p2.getFirstLine();

            Word w1 = l1.getLastWord();
            Word w2 = l2.getFirstWord();

            WordFragment wf1 = w1.getLastWordFragment();
            WordFragment wf2 = w2.getFirstWordFragment();

            Text t1 = wf1.getLastText();
            Text t2 = wf2.getFirstText();

            Rectangle2D r1 = l1.toRectangle();
            Rectangle2D r2 = l2.toRectangle();
            double x1 = r1.getX();
            double x2 = r2.getX();
            double x3 = x1 + r1.getWidth();
            double x4 = x2 + r2.getWidth();

            Boolean isSameParagraph = false;
            if((x1-x2) == 0 || (x3-x4) == 0)
                isSameParagraph = true;

            if(isSameParagraph)
            {
                Paragraph p3 = new Paragraph();
                p3.addLines(p1.getLines());
                p3.addLines(p2.getLines());
                paragraphList.add(paragraphList.indexOf(p1), p3);
                paragraphList.remove(p1);
                paragraphList.remove(p2);

                p1 = p3;
                continue;
            }
            else
            {
                p1 = p2;
            }

        }

        return paragraphList;

    }
    
    private void extractPdfFonts() throws Exception
    {
        Map<String,File> rawfonts = extractPdfFontsFromPdf(pdfFile);

        fonts.clear();


//        Map<String,File> ttfs = convertFontsToTTF(rawfonts);
//
//        for(String fn : ttfs.keySet())
//        {
//            File ttf = ttfs.get(fn);
//            Font font = new Font();
//            font.setBase64(base64EncodeFile(ttf));
//            font.setConverted(true);
//            font.setFormat(Font.Format.TTF);
//            font.setName(fn);
//            fonts.add(font);
//        }


        Map<String,File> otfs = convertFontsToOTF(rawfonts);

        for(String fn : otfs.keySet())
        {
            File otf = otfs.get(fn);
            Font font = new Font();
            font.setBase64(base64EncodeFile(otf));
            font.setConverted(true);
            font.setFormat(Font.Format.OTF);
            font.setName(fn);
            fonts.add(font);
        }
    
    }
    
    private Map<String,File> extractPdfFontsFromPdf(File pdf) throws Exception
    {
        HashMap<String,File> fonts = new HashMap<String,File>();

        PDDocument inputDocument = loadPdf(pdf);

        Set<PDFont> pdfonts = new HashSet<PDFont>();

        List allPages = inputDocument.getDocumentCatalog().getAllPages();
        for( int i=0; i<allPages.size(); i++ )
        {
            PDPage page = (PDPage)allPages.get( i );
            if(page.getResources() != null)
            {
                Map<String,PDFont> fontmap = page.getResources().getFonts();
                pdfonts.addAll(fontmap.values());
            }
            
        }
        
        for(PDFont pdfont : pdfonts)
        {
            PDFontDescriptorDictionary dict = (PDFontDescriptorDictionary) pdfont.getFontDescriptor();

            if(dict == null)
            {
                continue;
            }

            String fn = dict.getFontName();
            String subtype = pdfont.getSubType();
            PDStream stream = null;
            String filename = null;
            String suffix = null;

            if(dict.getFontFile3() != null)
            {
                stream = dict.getFontFile3();
                if("Type1C".equals(subtype))
                {
                    suffix = ".cff";
                }
                else if("CIDFontType0C".equals(subtype))
                {
                    suffix = ".cid";
                }
                else{
                    suffix = ".ttf";
                }
            }
            else if(dict.getFontFile2() != null)
            {
                stream = dict.getFontFile2();
                suffix = ".ttf";
            }
            else if(dict.getFontFile() != null)
            {
                stream = dict.getFontFile();
                suffix = ".pfa";
            }
  

            if(suffix != null)
            {
                byte[] ba = stream.getByteArray();
                filename = pdf.getParent() + System.getProperty("file.separator") + fn + suffix;
                File file = new File(filename);
                FileUtils.copyInputStreamToFile(stream.createInputStream(), file);
          
                fonts.put(fn, file);
            }
        }

        inputDocument.close();

        return fonts;
    }
    
    public Map<String, File> convertFontsToTTF(Map<String, File> fonts) throws Exception
    {

        HashMap<String,File> ttfs = new HashMap<String,File>();

        for(String fn : fonts.keySet())
        {
            File file = fonts.get(fn);
            File ttf = convertFont(file, "ttf");
            ttfs.put(fn, ttf);
        }

        return ttfs;
    }
    
    public Map<String, File> convertFontsToOTF(Map<String, File> fonts) throws Exception
    {

        HashMap<String,File> ttfs = new HashMap<String,File>();

        for(String fn : fonts.keySet())
        {
            File file = fonts.get(fn);
            File ttf = convertFont(file, "otf");
            ttfs.put(fn, ttf);
        }

        return ttfs;
    }

    public File convertFont(File inFile, String ext) throws Exception
    {
        String inFilename = inFile.getAbsolutePath();

        String outFilename = String.format("%s.%s",getFileNameWithoutExtension(inFile), ext);

        File outFile = new File(tempDir, outFilename);

        outFilename = outFile.getAbsolutePath();

        String system = System.getProperty("os.name").toLowerCase();

        File fontforgeExec;
        String fontforge;
        if(system.contains("win"))
        {
            fontforgeExec =  new File(tempDir, "win32/fontforge/fontforge.bat");
            fontforge = fontforgeExec.getAbsolutePath();
        }
        else
        {
            fontforge = "/opt/homebrew/bin/fontforge";
        }

        File convertfile = new File(tempDir, "encode.pe");
        String convertFilename = convertfile.getAbsolutePath();

        String[] args = new String[]
            { fontforge, "-script", convertFilename, inFilename, outFilename };


        Task task = new Task(args, tempDir);
        task.execute();

        Task.printStreamGobbler(task.getOutput());

        return outFile;
    }

    public void unzipResource(String resource, File outPath) throws Exception
    {
        InputStream is = ResourceLoader.loadResource(resource);
        File resourceFile = new File(tempDir, resource);
        FileUtils.copyInputStreamToFile(is, resourceFile);
        ZipFile zipfile = new ZipFile(resourceFile);
        unzip(zipfile, outPath);
    }

    public void unzip(ZipFile zip, File outPath) throws Exception
    {
        Enumeration entries = zip.entries();
        while(entries.hasMoreElements())
        {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            File file = new File(outPath, entry.getName());
            if (entry.isDirectory())
            {
                FileUtils.forceMkdir(file);
            }
            else
            {
                FileUtils.copyInputStreamToFile(zip.getInputStream(entry), file);
            }
        }
    }




    public static String getFileNameWithExtension(File file, String ext)
    {
        String path = file.getAbsolutePath();
        int index = path.lastIndexOf('.');
        if (index > 0 && index <= path.length() - 2)
        {
            return path.substring(0, index) + ext;
        }
        return "";
    }

    public static String getFileNameWithoutExtension(File file)
    {
        int index = file.getName().lastIndexOf('.');
        if (index > 0 && index <= file.getName().length() - 2)
        {
            return file.getName().substring(0, index);
        }
        return "";
    }

    private static String base64EncodeFile(File file)
    {
        try
        {
            byte[] ba = getBytesFromFile(file);
            return Base64.encodeBase64String(ba);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getBytesFromFile(File file) throws IOException
    {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        if (length > Integer.MAX_VALUE)
        {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
        {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length)
        {
            throw new IOException("Could not completely read file "
                    + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    public static double pixelsToEm(double pixels)
    {
        /*
         * int decimalPlace = 3; BigDecimal bd = new BigDecimal(pixels/8); bd =
         * bd.setScale(decimalPlace,BigDecimal.ROUND_UP); return
         * bd.doubleValue();
         */

        return pixels / 8;

    }

    @Override
    public void processEncodedText(byte[] string) throws IOException
    {
        // pdfbox encodes things wrong, so keep track to the original bytes
        encodedText = string;
        super.processEncodedText(string);
    }
}
