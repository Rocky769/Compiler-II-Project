
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

    private Map<String, List<String>> adj_list; // Stores adjacent vertices corresponding to a node in the graph
    private Map<String, NodeGraph> nodes;       // Maps each class to a node
    private Map<String, Integer> nodeMap;       // Maps each class to an integer
    private Integer depth = 0;                  // Globar variable to determine depth during depth first search
    private Integer[] inTime;                   // Array which stores entry time during DFS of a node
    private Integer[] outTime;                  // Array which stores exit time during DFS of a node
    ScopeTable<attr> scopeTab = new ScopeTable<>();
    String filename;

    // The objects of this class represent each class of a COOL program
    private class NodeGraph {
        public String parent;
        public String filename;
        public Integer lineNo;
        public Map<String, attr> attributes;
        public Map<String, method> methods;

        // Constructor for calling on default classes of COOL
        public NodeGraph(String p) {
            parent = p;
            filename = "COOL_LIBRARY";
            lineNo = 0;
            attributes = new HashMap<>();
            methods = new HashMap<>();
        }

        // Constructor for calling on new classes of COOL
        public NodeGraph(class_ clsObj) {
            filename = clsObj.filename;
            parent = clsObj.parent;
            lineNo = clsObj.lineNo;
            attributes = new HashMap<>();
            methods = new HashMap<>();
            
            // This loop is separating the features into attributes and methods
            for (feature featureName : clsObj.features) 
            {
                if (featureName instanceof attr) {
                    attr refTemp = (attr) featureName;

                    // Checks if an attribute with same name is not being redefined
                    if (attributes.containsKey(refTemp.name))
                        reportError(filename, refTemp.lineNo, "Attribute " + refTemp.name + " is multiply defined.");
                    else
                        attributes.put(refTemp.name, refTemp);
                } 
                else if (featureName instanceof method) {
                    method refTemp = (method) featureName;
                    // Checks if an method with same name is not being redefined
                    if (methods.containsKey(refTemp.name))
                        reportError(filename, refTemp.lineNo, "Method " + refTemp.name + " is multiply defined.");
                    else
                        methods.put(refTemp.name, refTemp);
                }
            }
        }
    }

    public void preProcessInheritanceGraph()
    {
        nodes = new HashMap<>();

        // Adding a Node 'Object' in the Nodegraph along with methods like abort(), type_name().
        NodeGraph Object = new NodeGraph("");
        Object.methods.put("abort", new method("abort", new ArrayList<>(), "Object", new object("", 0), 0));
        Object.methods.put("type_name", new method("type_name", new ArrayList<>(), "String", new string_const("", 0), 0));
        Object.methods.put("copy", new method("copy", new ArrayList<>(), "Object", new object("", 0), 0));
        nodes.put("Object", Object);

        // Adding a Node 'IO' in the Nodegraph along with methods like out_string(), out_int(), in_string(), in_int().
        NodeGraph IO = new NodeGraph("Object");
        List<formal> f1 = Arrays.asList(new formal("out_string", "String", 0));
        List<formal> f2 = Arrays.asList(new formal("out_int", "Int", 0));
        IO.methods.put("out_string",new method("out_string", f1, "Object", new object("", 0), 0));
        IO.methods.put("out_int", new method("out_int", f2, "Object", new object("", 0), 0));
        IO.methods.put("in_string",new method("in_string", new ArrayList<>(), "String", new string_const("", 0), 0));
        IO.methods.put("in_int",new method("in_int", new ArrayList<>(), "Int", new int_const(0, 0), 0));
        nodes.put("IO", IO);
        
        NodeGraph Int = new NodeGraph("Object");
        nodes.put("Int", Int);
        
        NodeGraph Bool = new NodeGraph("Object");
        nodes.put("Bool", Bool);
        
        // Adding a Node 'str' in the Nodegraph along with methods like length(), concat(), substr().
        NodeGraph str = new NodeGraph("Object");
        List<formal> f3 = Arrays.asList(new formal("s", "String", 0));
        List<formal> f4 = Arrays.asList(new formal("i", "Int", 0), new formal("l", "Int", 0));
        str.methods.put("length",new method("length", new ArrayList<>(), "Int", new int_const(0, 0), 0));
        str.methods.put("concat", new method("concat", f3, "String", new string_const("", 0), 0));
        str.methods.put("substr", new method("substr", f4, "String", new string_const("", 0), 0));
        nodes.put("String", str);
    }

    // This creates the NodeGraph objects and checks for basic inheritance
    public void checkInheritance(program program) { 
        preProcessInheritanceGraph();
       
        // Checks if the class is not being redefined or inheriting from Bool,Int or String
        for (class_ className : program.classes) 
        {
            if (nodes.get(className.name) == null) {
                if (className.parent.equals("Int") || className.parent.equals("Bool") || className.parent.equals("String") || className.parent.equals("SELF_TYPE")) {
                    reportError(className.filename, className.lineNo, "Class " + className.name + " cannot inherit class " + className.parent + ".");
                    return;
                }
                NodeGraph temp = new NodeGraph(className);
                nodes.put(className.name, temp);
            } 
            else {
                reportError(className.filename, className.lineNo, "Class " + className.name + " was previously defined.");
                return;
            }
        }
       
        // Check for Class Main
        NodeGraph n = nodes.get("Main");
        if (n == null) {
            reportError(program.classes.get(0).filename, program.lineNo, "Class Main is not defined");
            return;
        } 
        // Check for 'main' Functions
        else {
            if (!n.methods.containsKey("main")) {
                reportError(n.filename, n.lineNo, "No 'main' method in class Main");
                return;
            }
            else {
                if(!n.methods.get("main").formals.isEmpty()){
                    reportError(n.filename, n.lineNo, "'main' method in class Main should have no arguments");
                    return;
                }
            }
        }
    }

    // This function creates the adjacency lists and hence the graph
    public void createAdjacencyList() 
    {
        //If there was an error previously then stop doing checks
        if (errorFlag) return;
        adj_list = new HashMap<>();

        for (Map.Entry<String, NodeGraph> cls : nodes.entrySet())
            adj_list.put(cls.getKey(), new LinkedList<>());

        // Checks if the parent class a class is deriving from exists.
        for (Map.Entry<String, NodeGraph> node : nodes.entrySet()) {
            if (node.getKey().equals("Object"))
                continue;
            if (adj_list.get(node.getValue().parent) == null) {
                reportError(node.getValue().filename, node.getValue().lineNo,
                        "Class " + node.getKey() + " inherits from an undefined class " + node.getValue().parent + ".");
                return;
            } else
                adj_list.get(node.getValue().parent).add(node.getKey());
        }
    }

    // Detects if an inheritance graph is cyclic or not
    public void detectCycles() { 
        if (errorFlag)
            return;
        nodeMap = new HashMap<>();
        Integer len = adj_list.size();
        inTime = new Integer[len];
        outTime = new Integer[len];
        boolean[] visited = new boolean[len];
        Integer temp = 0;

        // Here we initialise the map from a class to an integer
        for (Map.Entry<String, NodeGraph> cls : nodes.entrySet()) {
            nodeMap.put(cls.getKey(), temp);
            temp = temp + 1;
        }

        temp = nodeMap.get("Object"); // All classes must have Object as their ancestor
        depthFirstSearch(visited, temp, "Object"); // Call depth first search from Object

        // If any node was unvisited then they must have formed cycle disconnectedly
        for (Map.Entry<String, NodeGraph> cls : nodes.entrySet()) { 
            Integer i = nodeMap.get(cls.getKey());
            if (!visited[i])
                reportError(cls.getValue().filename, cls.getValue().lineNo, "Inheritance Graph is Cyclic at " + cls.getKey());
        }
    }

    
}