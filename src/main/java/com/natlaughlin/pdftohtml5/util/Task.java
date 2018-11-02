package com.natlaughlin.pdftohtml5.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

public class Task 
{
	private Logger logger;
	
	private String[] args;
	private File dir;
	private Process process;
	private StreamGobbler error;
	private StreamGobbler output;
	private Integer exitCode;
	
	public Task(String[] args, File dir)
	{
		this.args = args;
		this.dir = dir;
		
		logger = Logger.getLogger(getClass().getName());
	}
	
	
	public String[] getArgs()
	{
		return args;
	}
	
	public StreamGobbler getError()
	{
		return error;
	}
	
	public StreamGobbler getOutput()
	{
		return output;
	}
	
	public Integer getExitCode()
	{
		return exitCode;
	}
	
	public void execute() throws Exception
	{
		String osName = System.getProperty("os.name");
    	Integer exitCode = null;
    	
    	long startTime = startProcessing("Executing: " + Arrays.toString(args));

    	ArrayList<String> argsp = new ArrayList<String>();
    
    	File pwd = dir;
    	
    	Collections.addAll(argsp, args);
  
    	
    	String[] a = new String[argsp.size()];
    	argsp.toArray(a);

    	Process p = Runtime.getRuntime().exec(a, null, pwd);
        
    	error = new StreamGobbler(p.getErrorStream());
	    output = new StreamGobbler(p.getInputStream());

	    exitCode = p.waitFor();

    	if(exitCode == 0)
    	{
    		logger.info("Finished.");
    	}
    	else
    	{
    		logger.severe("Error!");
    		printStreamGobbler(error);
    	}
        
        stopProcessing("Time for execution: ", startTime);
	}
	
	public static void printStreamGobbler(StreamGobbler gobbler) throws Exception
    {
    	InputStreamReader isr = new InputStreamReader(gobbler);
    	BufferedReader br = new BufferedReader(isr);
    	
    	String line = null;
    	while((line = br.readLine()) != null) 
    	{
    		System.out.println(line);
    	}
    }
	
	private long startProcessing(String message) 
    {
        logger.info(message);
        return System.currentTimeMillis();
    }
    
    private void stopProcessing(String message, long startTime) 
    {
        
    	long stopTime = System.currentTimeMillis();
        float elapsedTime = ((float)(stopTime - startTime))/1000;
        logger.info(message + elapsedTime + " seconds");
   
    }

	
}
