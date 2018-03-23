package main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

public class Foo {
	/**
	 * Points to a base directory of a machine</br>
	 */
	private static String BASEDIR = "\\some-path\\";
	/**
	 * Constructor, sets which testString will be examined </br>	 * 
	 */
	public Foo(String dir) {
		Integer[] keyCount = new Integer[]{0,0}; // keyCount[0] is references, keyCount[1] is declarations
		
		File directory = new File(dir);
		List<File> javaFiles = new ArrayList<File>();
		
		if (directory.isDirectory()) {
			// FIRST: recursively extract all jars in this directory to TEMP folders in same directory as the corresponding jar file
			try {
				JarHandler.extractJars(directory);
			} catch (IOException e1) {
				e1.printStackTrace();
			}	
			
			// SECOND: recursively search through directory (including TEMP folders) to retrieve all .java files
			searchFiles(directory, javaFiles);
		}
		else if (directory.getName().endsWith(".jar")) {
			// FIRST: extract this jar file and any nested jars to TEMP folders in same directory as the corresponding jar file
			try {
				JarHandler.extractJars(directory);
			} catch (IOException e1) {
				e1.printStackTrace();
			}	
			
			// SECOND: recursively search through jar directory (including TEMP folders) to retrieve all .java files
			String noExtensionName = dir.substring(0, dir.length() - 4); // cut off the .jar from end of dir   
			String jarDirectoryPath = noExtensionName + "TEMP"; // add TEMP to end of path to get path to created TEMP folder 
			File jarDirectory = new File(jarDirectoryPath);  
			searchFiles(jarDirectory, javaFiles);
		}
		
		else {
			System.out.println("Please specify a pathname to a directory/jar file.");
			return; 
		}
		
			Map<String, Integer[]> globalMap = new HashMap<String, Integer[]>();
			
			// FOURTH: parse java files
			int i = 0;
			while (i < javaFiles.size()) {
				File javaFile = javaFiles.get(i);
				//System.out.println("\n" + javaFile + "\n");
				try {
					String str = String.join(" ", Files.readAllLines(javaFile.toPath()));
					Map<String, Integer[]> localMap = visit(parse(str, javaFile.getParent()));
					for (String key : localMap.keySet()) {
						Integer[] globalCount = globalMap.get(key);
						Integer[] localCount = localMap.get(key); 
						if(globalCount != null) {// if key exists globally
							globalCount[0] += localCount[0]; 
							globalCount[1] += localCount[1];
						}	
						else {
							globalCount = new Integer[] {localCount[0], localCount[1]}; 
						}
						globalMap.put(key, globalCount);
					}
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			i++;
			}
			
		// Print info for EVERY type found:
		for (String key : globalMap.keySet()) {
			System.out.println("-------------------------------------------------------------------------------------------------");	
			System.out.format("%-50sDeclarations Found:%5d References Found:%5d\n", key, globalMap.get(key)[1], globalMap.get(key)[0]); 
		}
		
		// Finally: delete all TEMP folders to prevent clutter
		JarHandler.deleteTempFolders();
		
	}
	
	/**
	 * Searches for all java files in a given directory and its sub-directories recursively
	 */
	public void searchFiles(File directory, List<File> javaFiles) {
		if (directory == null)
			return;
		if (directory.isDirectory()) {
			for (File file : directory.listFiles()) {
				searchFiles(file, javaFiles);
			}
		}
		else if (directory.isFile() && directory.getName().endsWith(".java")) {
			javaFiles.add(directory);
		}
	}

	/**
	 * Parses source String into AST
	 * @param sourceCode
	 *  Source String to be parsed by ASTParser
	 * @param source
	 *  Source path of the file to parse
	 * @return
	 * 	Root node of a parsed AST
	 */
	public ASTNode parse(String sourceCode, String sourcePath) {
		ASTParser parser = ASTParser.newParser(AST.JLS9);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(sourceCode.toCharArray());
		
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		
		parser.setUnitName("");
		
		parser.setEnvironment(null,
				new String[] {sourcePath}, new String[]{"UTF-8"}, true);
		
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5); //or newer version
		parser.setCompilerOptions(options);
		
		return parser.createAST(null);
	}
	/**
	 * Visits AST tree, counting declarations
	 * @param node
	 * Root node of a AST
	 * @return Integer[0] and Integer[1]
	 * Declarations[1] and references[0] of the specified qualified name type for that node
	 */
	public Map<String, Integer[]> visit(ASTNode node) {
		Visitor vis = new Visitor();
		CompilationUnit cu = (CompilationUnit)node;
		cu.accept(vis);
		Map<String, Integer[]> map = vis.getMap();
		return map; 
	}
	
	public static void main(String[] args) {
		
		if (args.length == 1) {
			new Foo(args[0]);
		}
		else {
			System.out.println("Usage: java Foo <directoryPath or jarPath>");
		}
		
	}
}
