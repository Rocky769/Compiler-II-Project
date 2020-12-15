import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.lang.StringBuilder;

public class Global {

    // Type constants
    public static class Constants {
        public static final String ROOT_TYPE = "Object";
        public static final String IO_TYPE = "IO";
        public static final String INT_TYPE = "Int";
        public static final String BOOL_TYPE = "Bool";
        public static final String STRING_TYPE = "String";
        public static final String MAIN_TYPE = "Main";
    }

    // Current file name of the class
    // Updated before filling classes in inheritance graph
    public static String filename;

    // Contains graph after parsing all the classes and its parents
    // The base classes are also updated in this.
    public static InheritanceGraph inheritanceGraph;

    // Contains all the variables in the scope
    // mapped with their type: variable_name -> Type
    public static ScopeTable<String> scopeTable;

    // Constains all functions defined in inhetiance tree
    // mapped with their mangled name: function_name -> type_mangled_function_name
    public static ScopeTable<String> methodDefinitionScopeTable;

    // Map of function mangled names with its type
    // class_mangled_name_of_function -> return_type_of_function
    public static Map<String,String> mangledNameMap;
    
    public static Map<String,String> imports;
    // Used while visiting the AST classes
    // Should be updated when we start parsing a class
    public static String currentClass;

    // Used to report an error
    public static ErrorReporter errorReporter;

    static {
        currentClass = "";
        scopeTable = new ScopeTable<>();
        methodDefinitionScopeTable = new ScopeTable<>();
        mangledNameMap = new HashMap<>();
    }
