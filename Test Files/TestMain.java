import static org.junit.Assert.*;
import java.io.*;
import java.util.Scanner;

import org.eclipse.jdt.core.dom.ASTParser;
import org.junit.Test;

public class TestMain {
	
	// BASE DIRECTORY
	String BASEDIR = "C:\\Users\\Alyssa\\Documents\\Programs\\SENG_300_Group2\\";
	
	@Test
	public void test_simple() {
		OutputStream outContent = new ByteArrayOutputStream();
	    System.setOut(new PrintStream(outContent));
	    String directory = BASEDIR + "subDirectory";   // subDirectory: directory with specific file to test
	    
		String[] args = {directory};
		Foo.main(args);
		
	    
		String actual = outContent.toString();
		
		String expected = "";
		
		assertEquals(expected, actual);
	}
}