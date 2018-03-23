package main;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.JarEntry; 
import java.util.Enumeration; 
import java.io.InputStream;
import java.io.FileOutputStream; 
import java.util.List;
import java.util.ArrayList; 

public class JarHandler {
	
	private static List<File> tempFolders = new ArrayList<File>(); // keep track of all TEMP folders to delete after processing all .java files	
	
	/**
	 * Recursively extract ALL jars contained within file
	 * @param file (either a folder, .jar file, or other file)
	 * @throws IOException
	 */
	public static void extractJars(File file) throws IOException {
		if (!file.isDirectory()) { // if file is a simple file...
			String filename = file.getName(); 
			if (filename.toLowerCase().endsWith(".jar")) {
				String noExtensionName = filename.substring(0, filename.length() - 4); // cut off .jar from name of file
				File folder = new File(file.getParentFile().getAbsolutePath() + File.separator + noExtensionName + "TEMP"); // make file out of path name to TEMP folder 
				if (folder.mkdir()) { // if new TEMP folder was created (meaning a folder of same name didn't already exist)...
					tempFolders.add(folder); // keep track of TEMP folder
					JarFile jarFile = new JarFile(file); 
					extractJarToFolder(jarFile, folder); // extract contents of jar file into TEMP folder
					jarFile.close();
					extractJars(folder); // recursively search for and extract any jars found in TEMP folder
				}
				else {
					System.out.println("ERROR - DIRECTORY ALREADY EXISTS!"); // debug message
				}
			}
			return; 
		}
		else { // if file is a directory... 
			for (File f : file.listFiles()) {
				extractJars(f); // recursive search & extract
			}
		}
			 
	}
	
	/**
	 * Extracts the contents of a jar file into a directory
	 * @param jarFile
	 * jar file to extract
	 * @param folder
	 * folder to extract to
	 * @throws IOException
	 */
	public static void extractJarToFolder(JarFile jarFile, File folder) throws IOException {
		if (!folder.isDirectory()) { // if caller specifies an invalid folder, do nothing
			return; 
		}
		
		Enumeration<JarEntry> contents = jarFile.entries(); // retrieve all contents of the jar file
		while (contents.hasMoreElements()) { // while there are more contents to deal with
			JarEntry elem = contents.nextElement(); // deal with next element
			File copy = new File(folder.getAbsolutePath() + File.separator + elem.getName()); // make a copy of element
			if (elem.isDirectory()) {
				copy.mkdir(); // make a new directory for this directory element 
				continue; 
			}
			
			InputStream input = jarFile.getInputStream(elem); // get input stream of bytes for the element
			FileOutputStream output = new FileOutputStream(copy); // set output stream to write to file copy
			while (input.available() > 0) { // while there are remaining bytes to read from element
				output.write(input.read()); // read next byte from element and write it to file copy 
			}
			output.close();
			input.close();
		}		
	}
	
	// use this method to delete all TEMP folders after the data (e.g. .java file parsing / checking for .jar files) has been processed within them
	public static void deleteTempFolders() {
		for (int i = 0; i < tempFolders.size(); i++) {
			deleteFile(tempFolders.get(i));
		}
		tempFolders.clear();
	}
	
	// if file is one file, it will simply be deleted
	// if file is a folder, it will be deleted along with all of its contents
	public static void deleteFile(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) { // delete contents of folder...
				deleteFile(f); 
			}
			file.delete(); // delete the now-empty folder
		}
		else {
			file.delete(); // delete a simple file
		}
	}

}










