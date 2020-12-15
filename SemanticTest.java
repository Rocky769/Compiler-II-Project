
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

public class SemanticTest {

	
	public static final int
		ERROR=1, TYPEID=2, CLASS_ID=3, OBJECTID=4, BOOL_CONST=5, INT_CONST=6, 
		STR_CONST=7, LPAREN=8, RPAREN=9, COLON=10, ATSYM=11, SEMICOLON=12, COMMA=13, 
		PLUS=14, MINUS=15, STAR=16, SLASH=17, TILDE=18, LT=19, EQUALS=20, LBRACE=21, 
		RBRACE=22, DOT=23, PTR_OP=24, LE=25, ASSIGN=26, CLASS=27, ELSE=28, FI=29, 
		IF=30, IN=31, INHERITS=32, LET=33, LOOP=34, POOL=35, THEN=36, WHILE=37, 
		CASE=38, ESAC=39, OF=40, NEW=41, ISVOID=42, NOT=43, GT=44, TEQUALS=45, 
		NOTEQUAL=46, NOT_TEQUAL=47, LSQUARE=48, RSQUARE=49, MOD=50, GE=51, LSHIFT=52, 
		RSHIFT=53, POWER=54, MUL_ASSIGN=55, DIV_ASSIGN=56, ADD_ASSIGN=57, SUB_ASSIGN=58, 
		MOD_ASSIGN=59, AND_ASSIGN=60, OR_ASSIGN=61, XOR_ASSIGN=62, LSHIFT_ASSIGN=63, 
		RSHIFT_ASSIGN=64, INCRE_OP=65, DECRE_OP=66, BITAND=67, BITOR=68, BITXOR=69, 
		ELIF=70, FOR=71, DELETE=72, VOID=73, AND=74, OR=75, NULL=76, PRIVATE=77, 
		PUBLIC=78, SELF=79, TRY=80, EXCEPT=81, RAISE=82, BREAK=83, CONTINUE=84, 
		LAMBDA=85, IMPORT=86, USING=87, AS=88, RETURN=89, FLOAT_CONST=90, CHAR_CONST=91, 
		WS=92, PER_STR=93, NONTER_STR=94, ERR_ST=95, EOF_STR=96, LINE_COMMENT=97, 
		END_COMMENT=98, UN_COMMENT=99, NON_COMMENT=100, ST_COMMENT=101, EOF1_START=102, 
		COM_START=103, COM_END=104, EOF_COM=105, ORELSE=106, EOF2_START=107, EOF1_COM=108, 
		COM2_START=109, COM2_END=110, EOF2_COM=111, ORELSE2=112;

	static int parser_error_flag = 0;
	
	static String escapeSpecialCharacters(String text) {
		return
			text
				.replaceAll("\\\\", "\\\\\\\\")
				.replaceAll("\n", "\\\\n")
				.replaceAll("\t", "\\\\t")
				.replaceAll("\b", "\\\\b")
				.replaceAll("\f", "\\\\f")
				.replaceAll("\"", "\\\\\"")
				.replaceAll("\r", "\\\\015")
				.replaceAll("\033","\\\\033")
				.replaceAll("\001","\\\\001")
				.replaceAll("\002","\\\\002")
				.replaceAll("\003","\\\\003")
				.replaceAll("\004","\\\\004")
				.replaceAll("\022","\\\\022")
				.replaceAll("\013","\\\\013")
				.replaceAll("\000", "\\\\000")
				;
	}

	static void processfile(String filename) throws Exception{
		ANTLRInputStream inStream=null;
		try{
			inStream = new ANTLRInputStream(new FileInputStream(filename));
		}catch(Exception e){
			System.err.println("Could not read file "+filename);
			return;
		}
		
		PoolLexer lexer = new PoolLexer(inStream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		tokens.fill();		
		int lexer_flag = 0;
		for(Token t : tokens.getTokens()){
			if ( t.getType() == 1 ){
				lexer_flag = 1;
				System.err.println("Lexical error at "+t.getLine()+": "+escapeSpecialCharacters(t.getText()));	
			}
		}
		if (lexer_flag == 1)
			return;
		
		parser_error_flag = 0;
		PoolParser parser = new PoolParser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(new ParserError(Paths.get(filename).getFileName().toString()));
		parser.setFilename(Paths.get(filename).getFileName().toString());

		PoolParser.ProgramContext prog = null;
		try{
			prog = parser.program();
		}catch(Exception e){
		//	e.printStackTrace();
		}
		if(parser_error_flag == 1){
			System.err.println("Compilation halted due to lex and parse errors");
			return;
		}
		Semantic semanticAnalyzer=new Semantic(prog);
		if (semanticAnalyzer.getErrorFlag()){
			System.err.println("Compilation halter due to semantic errors.");
			return;
		}
		//System.out.println(prog.value.getString(""));
	}

	public static void main(String args[]) throws Exception{

		if(args.length < 1) {
			System.err.println("No files given");
			System.exit(1);
		}
		processfile(args[0]);
	}

	public static class ParserError extends BaseErrorListener {
		
		String filename;
		public ParserError(String fn) {
			super();
			filename=fn;
		}
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionIntLine, String msg, RecognitionException e){
			parser_error_flag=1;
			String sourceName = recognizer.getInputStream().getSourceName();
			String errorMessage="";
			if(filename!=null){
				if(offendingSymbol instanceof CommonToken){
					errorMessage += "\""+filename+"\", line "+line+": syntax error at or near ";
					
				}
			}
			System.err.println(errorMessage);
			throw new RuntimeException("One error found!");
		}
	}		
}
