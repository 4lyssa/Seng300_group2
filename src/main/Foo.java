package main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
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
	public Foo(String key, String dir) {
		Integer[] keyCount = new Integer[]{0,0}; // keyCount[0] is references, keyCount[1] is declarations
		
		File directory = new File(dir);
		List<File> javaFiles = new ArrayList<File>();
		
		if (directory.getName().endsWith(".jar")) {
			// Something else with jar files
		}
		else if (directory.isDirectory()) {
			searchFiles(directory, javaFiles);
			
			int i = 0;
			while (i < javaFiles.size()) {
				File javaFile = javaFiles.get(i);
				System.out.println("\n" + javaFile + "\n");
				try {
					String str = String.join(" ", Files.readAllLines(javaFile.toPath()));
					Integer[] result = visit(parse(str, javaFile.getParent()), key);
					if(result != null) {
						keyCount[0] += result[0];
						keyCount[1] += result[1];
					}
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			i++;
			}
		}
		else {
			System.out.println("Please specify pathname to a directory/jar file and a qualified name of a Java type");
		}

		// Print specified qualified name (key)
		System.out.println("-------------------------------------------------------------------------------------------------");
		System.out.format("%-50sDeclarations Found:%5d References Found:%5d\n", key, keyCount[1], keyCount[0]);
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
	public Integer[] visit(ASTNode node, String key) {
		Visitor vis = new Visitor();
		CompilationUnit cu = (CompilationUnit)node;
		cu.accept(vis);
		Map<String, Integer[]> map = vis.getMap();
		for(String otherKey: map.keySet()) {
			System.out.format("%-50sDeclarations Found:%5d References Found:%5d\n", otherKey, map.get(otherKey)[1], map.get(otherKey)[0]);
		}
		return map.get(key);
	}
	
	public static void main(String[] args) {
		// Use base directory if only one argument (Qualified Name) is given
		if (args.length == 1) {
			BASEDIR = System.getProperty("user.dir")+"\\BASEDIR";
			new Foo(args[0], BASEDIR);
		}
		// Change base directory if two arguments are given; error otherwise
		else if (args.length == 2){
			try {
				BASEDIR = args[0];
				new Foo(args[1], BASEDIR);
			}
			catch (NullPointerException e) {
				System.out.println("Please specify pathname to a directory and a qualified name of a Java type");
			}
		}
		else
			System.out.println("Please specify pathname to a directory and a qualified name of a Java type");
	}
}