
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.*;
import java.nio.file.Paths;


public class Semantic{
	private boolean errorFlag = false;
	public void reportError(String filename, int lineNo, String error){
		errorFlag = true;
		System.err.println(filename+":"+lineNo+": "+error);
	}
	public boolean getErrorFlag(){
		return errorFlag;
	}

	public Semantic(PoolParser.ProgramContext program){
		Global.errorReporter = new ErrorReporter() {
			@Override
			public void report(String filename, int lineNo, String error) {
				reportError(filename, lineNo, error);
			}
		};
		Visitor visitor = new Visitor();
		visitor.visitProgram(program);
		errorFlag = visitor.getErrorFlag();
	}
	

}