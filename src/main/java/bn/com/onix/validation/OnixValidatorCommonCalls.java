package bn.com.onix.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.*;
import org.apache.commons.io.filefilter.*;
import org.slf4j.Logger;

public class OnixValidatorCommonCalls {
	
	public static boolean copyFile(String psSourceFile, String psTargetDir, String psTargetFile) 
			throws IOException {
		
		boolean bSuccess = true;
		
		File oSourceFile = new File(psSourceFile);
		File oTargetDir  = new File(psTargetDir);
		File oTargetFile = new File(psTargetFile);
		
		if (oSourceFile.exists()) {
			
			oTargetDir.mkdirs();

			FileUtils.copyFile(oSourceFile, oTargetFile);
		}
		else {
			OnixValidator.logError("Cannot copy file since the source file(" + psSourceFile + ") does not exist!");
			bSuccess = false;
	    }
		
		return bSuccess;
	}
	
	public static boolean copyFilesToDirectory(String psSourceDirectory, String psTargetDirectory) 
			throws IOException {
		
		boolean bSuccess = true;
		
		File oSourceDir = new File(psSourceDirectory);
		File oTargetDir = new File(psTargetDirectory);
		
		if (oSourceDir.exists() && oSourceDir.isDirectory()) {
			
			if (!oTargetDir.exists()) {
				oTargetDir.mkdirs();
		    }
			
			if (oTargetDir.exists() && oTargetDir.isDirectory()) {
				
				File[] aSourceFiles = oSourceDir.listFiles();
				
				for (File oTmpSourceFile : aSourceFiles) {
					
					FileUtils.copyFileToDirectory(oTmpSourceFile, oTargetDir);
			    }
			}
			else {
				OnixValidator.logError("Cannot copy files since the target directory(" + psTargetDirectory + ") does not exist!");
				bSuccess = false;				
		    }		     
		}
		else {
			OnixValidator.logError("Cannot copy files since the source directory(" + psSourceDirectory + ") does not exist!");
			bSuccess = false;
	    }
		
		return bSuccess;
    }

    public static PrintStream createErrorLoggingProxy(final PrintStream poPrintStream, final Logger poLogger) {
    	
        return new PrintStream(poPrintStream) {
        	
            public void print(final String psLogMsg) {
            	poPrintStream.print(psLogMsg);
            	poLogger.error(psLogMsg);
            }
        };
    }	
	
    public static PrintStream createInfoLoggingProxy(final PrintStream poPrintStream, final Logger poLogger) {
    	
        return new PrintStream(poPrintStream) {
        	
            public void print(final String psLogMsg) {
            	poPrintStream.print(psLogMsg);
            	poLogger.info(psLogMsg);
            }
        };
    }
    
	public static boolean deleteDirectories(String psTargetDirectory, double dNumOfDaysThreshold) {

		boolean bSuccess = true;
		long    nCutoff  = System.currentTimeMillis() - Math.round(dNumOfDaysThreshold * 24 * 60 * 60 * 1000);

		File oTargetDir = new File(psTargetDirectory);
		
		if (oTargetDir.exists() && oTargetDir.isDirectory()) {

			Collection<File> oTargetFiles = 
					FileUtils.listFilesAndDirs(oTargetDir, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
			
			for (File oTempFile : oTargetFiles) {

				if (oTempFile.exists() && (oTempFile.getAbsolutePath() != psTargetDirectory)) {
					
					if (oTempFile.isDirectory() && FileUtils.isFileOlder(oTempFile, nCutoff)) {
						OnixValidator.logInfo("\tDeleting the directory(" + oTempFile.getAbsolutePath() + ").");
					    FileUtils.deleteQuietly(oTempFile);
					}
				}
		    }
		}
		else {
			OnixValidator.logError("Cannot delete directories since the target directory(" + psTargetDirectory + ") does not exist!");
			bSuccess = false;				
	    }		     
		
		return bSuccess;		
	}    
	
	public static boolean deleteFiles(String psTargetDirectory, double dNumOfDaysThreshold) {

		boolean bSuccess = true;
		long    nCutoff  = System.currentTimeMillis() - Math.round(dNumOfDaysThreshold * 24 * 60 * 60 * 1000);

		File oTargetDir = new File(psTargetDirectory);
		
		if (oTargetDir.exists() && oTargetDir.isDirectory()) {

			Collection<File> oTargetFiles = 
					FileUtils.listFilesAndDirs(oTargetDir, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
			
			for (File oTempFile : oTargetFiles) {
				
				if (oTempFile.exists() && (oTempFile.getAbsolutePath() != psTargetDirectory)) {
					
					if (FileUtils.isFileOlder(oTempFile, nCutoff)) {
						OnixValidator.logInfo("\tDeleting the file/directory(" + oTempFile.getAbsolutePath() + ").");
					    FileUtils.deleteQuietly(oTempFile);
					}
				}
		    }			
		}
		else {
			OnixValidator.logError("Cannot delete files since the target directory(" + psTargetDirectory + ") does not exist!");
			bSuccess = false;				
	    }		     
		
		return bSuccess;		
	}
		
	public static long getAvailableMemoryMB() {
		
		long    nFreeMB  = 0;
		Runtime oRuntime = Runtime.getRuntime();
		
		nFreeMB = oRuntime.freeMemory() / 1024 / 1024;
	    
	    return nFreeMB;
	}	
	
	public static String getStackTrace(final Throwable throwable) {
		
	     final StringWriter sw = new StringWriter();
	     final PrintWriter  pw = new PrintWriter(sw, true);
	     
	     throwable.printStackTrace(pw);
	     
	     return sw.getBuffer().toString();
	}
	
	public static long getUsedMemoryMB() {
		
		long    nUsedMB  = 0;
		Runtime oRuntime = Runtime.getRuntime();
		
	    nUsedMB = (oRuntime.totalMemory() - oRuntime.freeMemory()) / 1024 / 1024;
	    
	    return nUsedMB;
	}	
	
	public static void gzipIt(String psInputFilepath) {
		 
	     byte[] buffer = new byte[1024];
	     
	     try{
	 
	    	GZIPOutputStream gzos = 
	    		new GZIPOutputStream(new FileOutputStream(psInputFilepath + ".gz"));
	 
	        FileInputStream in = 
	            new FileInputStream(psInputFilepath);
	 
	        int len;
	        while ((len = in.read(buffer)) > 0) {
	        	gzos.write(buffer, 0, len);
	        }
	 
	        in.close();
	 
	    	gzos.finish();
	    	gzos.close();
	    	
	    	OnixValidator.logDebug("Complete!  Done with running GZIP on (" + psInputFilepath + ").");
	 
	    }catch(IOException ex){
	    	OnixValidator.logError(OnixValidator.getStackTrace(ex));
	    }
    }
	
	public static boolean moveDirectory(String psSourceDirectory, String psNewParentDirectory) 
	    throws IOException {
		
		boolean bSuccess = true;
		
		File oSourceDir    = new File(psSourceDirectory);
		File oNewParentDir = new File(psNewParentDirectory);
		
		if (oSourceDir.exists() && oSourceDir.isDirectory()) {
			
			if (!oNewParentDir.exists())
				oNewParentDir.mkdirs();
			
			FileUtils.moveDirectoryToDirectory(oSourceDir, oNewParentDir, true);
		}
		else {
			OnixValidator.logError("Cannot move directory since the source directory(" + psSourceDirectory + ") does not exist!");
			bSuccess = false;
	    }
		
		return bSuccess;		
	}
		
	public static void removeFiles(String psDirectory, String psFileExtension) {
		
		final String sFileExtension = psFileExtension;
		
		File oXmlFilesDir = new File(psDirectory);
		if (oXmlFilesDir.exists() && oXmlFilesDir.isDirectory()) {
			
			File[] xmlFiles = oXmlFilesDir.listFiles(new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			        return name.toLowerCase().endsWith(sFileExtension);
			    }
			});
			
			for(int i=0; i < xmlFiles.length; i++)
				xmlFiles[i].delete();
	    }		
    }
	
	public static void waitOnThreads(ExecutorService[] paThreads) {
		
		for(int i=0; i < paThreads.length; i++) {
			paThreads[i].shutdown();
		}
		
		for(int i=0; i < paThreads.length; i++) {
			
			try { 					
				paThreads[i].awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			}
			catch (InterruptedException e) {
				OnixValidator.logError(OnixValidator.getStackTrace(e));
		    }
		}		
    }

}
