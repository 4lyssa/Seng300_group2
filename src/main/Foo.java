package main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
	public Foo(String key) {
		Integer[] count = new Integer[]{0,0}; // count[0] is references, count[1] is declarations
		
		if (BASEDIR.endsWith(".jar")) {
		}
		File directory = new File(BASEDIR);
		File javaFiles;
		for(String files : directory.list())
			if(files.endsWith(".java")) {
				javaFiles = new File(BASEDIR + "\\" + files);
				System.out.println("\n" + javaFiles + "\n");
				try {
					String cat = String.join(" ", Files.readAllLines(javaFiles.toPath()));
					Integer[] result = visit(parse(cat),key);
					if(result != null) {
						count[0] += result[0];
						count[1] += result[1];
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		System.out.println("-------------------------------------------------------------------------------------------------");
		System.out.format("%-50sDeclarations Found:%5d References Found:%5d\n", key, count[1], count[0]);
	}
	/**
	 * Parses source String into AST
	 * @param sourceCode
	 * 	Source String to be parsed by ASTParser
	 * @return
	 * 	Root node of a parsed AST
	 */
	public ASTNode parse(String sourceCode) {
		ASTParser parser = ASTParser.newParser(AST.JLS9);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(sourceCode.toCharArray());
		
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		
		parser.setUnitName("");
		
		parser.setEnvironment(null,
				new String[] {BASEDIR}, new String[]{"UTF-8"}, true);
		
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5); //or newer version
		parser.setCompilerOptions(options);
		
		return parser.createAST(null);
	}
	/**
	 * Visits AST tree, counting declarations
	 * @param node
	 * Root node of a AST
	 */
	public Integer[] visit(ASTNode node,String key) {
		Visitor vis = new Visitor();
		CompilationUnit cu = (CompilationUnit)node;
		cu.accept(vis);
		Map<String, Integer[]> map = vis.getMap();
		for(String truekey: map.keySet()) {
			System.out.format("%-50sDeclarations Found:%5d References Found:%5d\n", truekey, map.get(truekey)[1], map.get(truekey)[0]);
		}
		return map.get(key);
	}
	public static void main(String[] args) {
		// Use base directory if only one argument (Qualified Name) is given
		if (args.length == 1) {
			BASEDIR = System.getProperty("user.dir")+"\\BASEDIR";
			new Foo(args[0]);
		}
		// Change base directory if two arguments are given; error otherwise
		else if (args.length == 2){
			try {
			BASEDIR = args[0];
			new Foo(args[1]);
			}
			catch (NullPointerException e) {
				System.out.println("Please specify pathname to a directory and a qualified name of a Java type");
			}
		}
		else
			System.out.println("Please specify pathname to a directory and a qualified name of a Java type");
	}
}