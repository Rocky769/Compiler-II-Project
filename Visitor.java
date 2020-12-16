
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


import java.util.*;
public class Visitor extends PoolParserBaseVisitor<String> {
	private boolean errorFlag = false;

    public void reportError(String filename, int lineNo, String error) {
        errorFlag = true;
        System.err.println(filename + ":" + lineNo + ": " + error);
    }

    public boolean getErrorFlag() {
        return errorFlag;
    }

    
    @Override public String visitProgram(PoolParser.ProgramContext ctx) {
    	visitChildren(ctx); 
    	return "";
    }

   	@Override public String visitImport_stat(PoolParser.Import_statContext ctx) {
   		//visitChildren(ctx); 
   		if(ctx.TYPEID() == null) {
   			if(ctx.OBJECTID(1) == null)
   				Global.imports.put(ctx.OBJECTID(0).getText(), ctx.OBJECTID(0).getText());
   			else
   				Global.imports.put(ctx.OBJECTID(1).getText(), ctx.OBJECTID(0).getText());   				
   		}
   		else {
   			if(ctx.OBJECTID(1) == null)
   				Global.imports.put(ctx.TYPEID().getText(), ctx.TYPEID().getText());
   			else
   				Global.imports.put(ctx.OBJECTID(1).getText(), ctx.TYPEID().getText());   	
   		}
   		return "";
       }
       
    @Override public String visitAlias_stat(PoolParser.Alias_statContext ctx) {
        //visitChildren(ctx); 
        if(ctx.TYPEID(0)==null){
            if(ctx.TYPEID(1)==null){
                Global.aliases.put(ctx.OBJECTID(1).getText(), ctx.OBJECTID(0).getText());
            }
            else{
                Global.aliases.put(ctx.TYPEID(1).getText(), ctx.OBJECTID(0).getText());
            }
        }
        else{
            if(ctx.OBJECTID(1)==null){
                Global.aliases.put(ctx.TYPEID(1).getText(), ctx.TYPEID(0).getText());
            }else{
                Global.aliases.put(ctx.OBJECTID(1).getText(), ctx.TYPEID(0).getText());
            }
        }
        return "";
    }

	@Override public String visitClassblock(PoolParser.ClassblockContext ctx) {
        //visitChildren(ctx);
        Global.inheritanceGraph.addClass(ctx.TYPEID(0),ctx.TYPEID(1));//add this class TYPEID(0) whose parent is TYPEID(1)
        visitInClass(ctx.inClass());
        return "";
	}

    @Override public String visitInClass(PoolParser.InClassContext ctx) {
        //(access_specifier? declaration | method)+;
    };

    @Override public String visitDeclaration(PoolParser.DeclarationContext ctx) { 
        //return visitChildren(ctx); 
    }
	@Override public String visitExpression(PoolParser.ExpressionContext ctx) {
         //visitChildren(ctx); 
         if(ctx.expression() == null)
            visitAssignment_expr(ctx.assignment_expr());
         else {
            visitExpression(ctx.expression());
            visitExpression(ctx.assignment_expr());
         }
         return "";
    }
    @Override public String visitEquality_expr(PoolParser.Equality_exprContext ctx) {
          //visitChildren(ctx); 
          if(ctx.TEQUALS() != null) {
              if(visitEquality_expr(ctx.equality_expr()) != visitRelational_expr(ctx.relational_expr()))
                return "False";//raise syntax error
              else {
                  return "True";
              }
          }
          return "";
        }
    @Override public String visitPrimary_expr(PoolParser.Primary_exprContext ctx) { 
        if(ctx.INT_CONST() != null) {
            return "Int";
        }
        if(ctx.OBJECTID() != null) {
            return Global.scopeTable.lookUpLocal(ctx.OBJECTID());
        }
        if(ctx.FLOAT_CONST() != null) {
            return "Float";
        }
        if(ctx.STR_CONST() != null) {
            return "String";
        }
        if(ctx.BOOL_CONST() != null) {
            return "Bool";
        }
        if(ctx.CHAR_CONST() != null) {
            return "Char";
        }
        else {
            return visitExpression(ctx.expression());
        }
    }

}