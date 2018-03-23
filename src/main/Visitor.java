package main;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.*;

//Modified ASTVisitor
/**
 * AST Visitor for only visiting Declarations
 * count[1] is declarations
 * count[0] is references
 */
public class Visitor extends ASTVisitor{
	Map<String, Integer[]> map = new HashMap<String, Integer[]>();
	
	public Map<String, Integer[]> getMap(){
		return map;
	}
	
	//Visits when there is a primitive type (int, char, ...)
	@Override
	public boolean visit(PrimitiveType node) {
		if(!node.toString().equals("void")) {
			Integer[] count = map.get(node.resolveBinding().getQualifiedName());
			
			if(count != null) 
				count[0]++;
			else
				count = new Integer[] {1,0};
			
			map.put(node.resolveBinding().getQualifiedName(), count);
		}
		return super.visit(node);
	}
	
	//Visits when there is a SimpleType type (non-Primitive types like java.lang.String)
	@Override
	public boolean visit(SimpleType node) {	
		Integer[] count = map.get(node.resolveBinding().getQualifiedName());
		
		if(count != null) 
			count[0]++;
		else
			count = new Integer[] {1,0};
		
		map.put(node.resolveBinding().getQualifiedName(), count);
		
		return super.visit(node);
	}
	
	//1. AnnotationType declaration
	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		Integer[] count = map.get(node.resolveBinding().getQualifiedName());
		if(count != null) 
			count[1]++;
		else
			count = new Integer[] {0,1};
		
		if (node.resolveBinding().getQualifiedName().equals(""))
			map.put(node.resolveBinding().getName(), count);
		else
			map.put(node.resolveBinding().getQualifiedName(), count);
		
		return super.visit(node);
	}
	
	//2. Enum declaration
	@Override
	public boolean visit(EnumDeclaration node) {
		Integer[] count = map.get(node.resolveBinding().getQualifiedName());
		if(count != null) 
			count[1]++;
		else
			count = new Integer[] {0,1};
		
		if (node.resolveBinding().getQualifiedName().equals(""))
			map.put(node.resolveBinding().getName(), count);
		else
			map.put(node.resolveBinding().getQualifiedName(), count);
		
		return super.visit(node);
	}
	
	//3-4. Class / Interface declaration
	@Override
	public boolean visit(TypeDeclaration node) {
		Integer[] count = map.get(node.resolveBinding().getQualifiedName());
		if(count != null) 
			count[1]++;
		else
			count = new Integer[] {0,1};
		
		
		if (node.resolveBinding().getQualifiedName().equals(""))
			map.put(node.resolveBinding().getName(), count);
		else
			map.put(node.resolveBinding().getQualifiedName(), count);
		
		return super.visit(node);
	}
	
	// 5. Anonymous Class declaration
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		Integer[] count = map.get(node.resolveBinding().getQualifiedName());
		if(count != null) 
			count[1]++;
		else
			count = new Integer[] {0,1};
		
		
		if (node.resolveBinding().getQualifiedName().equals(""))
			map.put(node.resolveBinding().getKey()+" (Anonymous Class)", count);
		else
			map.put(node.resolveBinding().getQualifiedName(), count);
		
		return super.visit(node);
	}
	
	//Import Statement
	@Override
	public boolean visit(ImportDeclaration node) {
		Integer[] count = map.get(node.resolveBinding().getName());
		if(count != null) 
			count[0]++;
		else
			count = new Integer[] {1,0};
		
		
		map.put(node.resolveBinding().getName(), count);
		
		return super.visit(node);
	}
	
}
