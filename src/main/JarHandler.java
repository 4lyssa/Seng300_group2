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
	
/*	
	public static void main(String[] args) {
		
		File baseDir = new File(BASEDIR); 
		
		try {
			extractJars(baseDir); // extract ALL jars recursively down from a base directory
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}
*/	
	
	/**
	 * Recursively extract ALL jars contained within file
	 * @param file (either a folder, .jar file, or other file)
	 * @throws IOException
	 */
	public static void extractJars(File file) throws IOException {
		if (!file.isDirectory()) { // if file is a simple file...
			String filename = file.getName(); 
			if (filename.toLowerCase().endsWith(".jar")) {
				String noExtensionName = filename.substring(0, filename.length() - 4);  
				File folder = new File(file.getParentFile().getAbsolutePath() + File.separator + noExtensionName + "TEMP"); 
				if (folder.mkdir()) { // if new TEMP folder was created...
					tempFolders.add(folder); 
					JarFile jarFile = new JarFile(file); 
					extractJarToFolder(jarFile, folder); 
					jarFile.close();
					extractJars(folder); 
				}
				else {
					System.out.println("ERROR - DIRECTORY ALREADY EXISTS!"); // debug message
				}
			}
			return; 
		}
		else { // if file is a directory... 
			for (File f : file.listFiles()) {
				extractJars(f); 
			}
		}
			 
	}
	
	public static void extractJarToFolder(JarFile jarFile, File folder) throws IOException {
		if (!folder.isDirectory()) {
			return; 
		}
		
		Enumeration<JarEntry> contents = jarFile.entries();
		while (contents.hasMoreElements()) {
			JarEntry elem = contents.nextElement();
			File copy = new File(folder.getAbsolutePath() + File.separator + elem.getName());
			if (elem.isDirectory()) {
				copy.mkdir();
				continue; 
			}
			
			InputStream input = jarFile.getInputStream(elem); 
			FileOutputStream output = new FileOutputStream(copy); // write to folder
			while (input.available() > 0) { // there are remaining bytes to read 
				output.write(input.read()); // read next byte from elem and write to file copy 
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










