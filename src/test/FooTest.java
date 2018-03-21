package test;

import static org.junit.Assert.*;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import main.Visitor;

public class FooTest {
	Visitor vis;
	ASTParser parser;
	@Before 
	public void initializer(){
		String BASEDIR = System.getProperty("user.dir")+"\\BASEDIR";
		parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		
		parser.setUnitName("");
		
		parser.setEnvironment(null,
				new String[] {BASEDIR}, new String[]{"UTF-8"}, true);
		
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5); //or newer version
		parser.setCompilerOptions(options);
	}
	@After
	public void nullifier() {
		vis = null;
		parser = null;
	}
	@Test
	public void annotationTypeTest() {
		parser.setSource("@interface Foo{version = 1}".toCharArray());
		ASTNode node = parser.createAST(null);
		vis = new Visitor();
		node.accept(vis);
		assertEquals((int)vis.getMap().get("Foo")[1],1);
	}
	@Test
	public void classTest() {
		parser.setSource("package Foo; class Bar{class Bar2{}} class Bar3{void Bar4(){Bar foo = new Bar();}}".toCharArray());
		ASTNode node = parser.createAST(null);
		vis = new Visitor();
		node.accept(vis);
		assertEquals((int)vis.getMap().get("Foo.Bar")[1],1); //Declaration
		assertEquals((int)vis.getMap().get("Foo.Bar")[0],2); //Reference
	}
	@Test
	public void interfaceTest() {
		parser.setSource("interface Foo implements Foo2{} interfaceFoo3".toCharArray());
		ASTNode node = parser.createAST(null);
		vis = new Visitor();
		node.accept(vis);
		assertEquals((int)vis.getMap().get("Foo")[1],1);
	}
	@Test
	public void enumTest() {
		parser.setSource("enum Foo{Test1,Test2} enumFoo2{Test}".toCharArray());
		ASTNode node = parser.createAST(null);
		vis = new Visitor();
		node.accept(vis);
		assertEquals((int)vis.getMap().get("Foo")[1],1);
	}
	@Test
	public void primitiveTypeTest() {
		parser.setSource("class Foo{void Foo{int a = 3; char b = 1; Integer c = 6;}}".toCharArray());
		ASTNode node = parser.createAST(null);
		vis = new Visitor();
		node.accept(vis);
		assertEquals((int)vis.getMap().get("int")[0],1);
	}
	@Test
	public void simpleTypeTest() {
		parser.setSource("String b = \"Test\"; class Foo{void Bar(){public List<String> a; public Map<String, Integer> b;}}".toCharArray());
		ASTNode node = parser.createAST(null);
		vis = new Visitor();
		node.accept(vis);
		assertEquals((int)vis.getMap().get("java.lang.String")[0],2);
		assertEquals((int)vis.getMap().get("java.lang.Integer")[0],1);
		assertEquals((int)vis.getMap().get("List<java.lang.String>")[0],1);
		assertEquals((int)vis.getMap().get("Map<java.lang.String,java.lang.Integer>")[0],1);
	}
}
