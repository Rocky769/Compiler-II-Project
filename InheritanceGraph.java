
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Arrays;
import java.lang.StringBuilder;
import java.util.Collections;

public class InheritanceGraph {

    /* Member variables */
    // Index in the graph list where root node is stored
    private static final int ROOT_CLASS_INDEX = 0;

    // * Root AST.class_ and InheritanceGraph.Node
    // * Stored separately for easy access
    private static AST.class_ ROOT_AST_CLASS = new AST.class_(Global.Constants.ROOT_TYPE, null, null, new ArrayList<>(), 0);
    private static Node ROOT_AST_NODE = new Node(ROOT_AST_CLASS, ROOT_CLASS_INDEX);

    // List of nodes in the graph
    private List<Node> graph;
    
    // This map is used to find the index of particular class
    // in graph list
    private Map<String,Integer> classNameToIndexMap;

    // 'true' if graph has Global.Constants.MAIN_TYPE
    private boolean hasMain;

    /* Constructor */
    public InheritanceGraph() {
        graph = new ArrayList<>();
        classNameToIndexMap = new HashMap<>();
        hasMain = false;
        addBaseClasses();
    }

    /* Method definitions */

    public Node getRootNode() {
        return ROOT_AST_NODE;
    }

    public boolean hasMain() {
        return hasMain;
    }

    public boolean hasClass(String className) {
        return classNameToIndexMap.containsKey(className);
    }

    public List<Node> getNodeList() {
        return graph;
    }

    // * Returns the name of parent class of the given class name
    // * The existance of the passed class name in the graph
    //   has to be checked manually using 'hasClass' before
    //   calling this function
    public String getParentClassName(String className) {
        Node classNode = graph.get(classNameToIndexMap.get(className));
        return classNode.getAstClass().parent;
    }

    // Restricted base classes for redefinition
    private boolean isRestrictedClass(String name) {
        return Global.Constants.IO_TYPE.equals(name) || Global.Constants.INT_TYPE.equals(name) 
        || Global.Constants.STRING_TYPE.equals(name) || Global.Constants.BOOL_TYPE.equals(name);
    }

    // Restricted base classes for inheritance
    private boolean isRestrictedInheritanceClass(String name) {
        return Global.Constants.INT_TYPE.equals(name) || Global.Constants.STRING_TYPE.equals(name) 
        || Global.Constants.BOOL_TYPE.equals(name);
    }

    // Base classes with no methods in it
    public boolean isNoMethodClass(String name) {
        return Global.Constants.INT_TYPE.equals(name) || Global.Constants.BOOL_TYPE.equals(name);
    }

    // Used to add a class to the graph
    public void addClass(AST.class_ astClass) {
        if(classNameToIndexMap.containsKey(astClass.name)) {
            // A class with the given name already exists
            Global.errorReporter.report(Global.filename, astClass.getLineNo(),
                new StringBuilder().append("class '").append(astClass.name)
                    .append("' has been redefined").toString());
        } else if(isRestrictedClass(astClass.name)) {
            // Tried to redefine restricted classes
            Global.errorReporter.report(Global.filename, astClass.getLineNo(),
                new StringBuilder().append("Cannot redefine base class '")
                    .append(astClass.name).append("'").toString());
        } else {
            // Valid class, add it to the graph
            // NOTE: restricted inheritance is checked in 'updateParents'
            classNameToIndexMap.put(astClass.name, graph.size());
            graph.add(new Node(astClass, graph.size()));
            if(Global.Constants.MAIN_TYPE.equals(astClass.name)) {
                // Its a Main class
                hasMain = true;
            }
        }
    }

    // returns 'true' if type2 conforms to type1
    // else false
    public boolean isConforming(String type1, String type2) {
        if(type1.equals(type2) || Global.Constants.ROOT_TYPE.equals(type1)) {
            // if both types are same or type1 is the root of all types
            // not need to check further
            return true;
        } else if(isRestrictedInheritanceClass(type1) || isRestrictedInheritanceClass(type2)) {
            // Means that atleast one of the type is from restricted inheritance (int, bool, string)
            // Hence after the first if condition failed, according to cool rules
            // it does not conform
            return false;
        }

        // Checking if type1 comes in type2->parent->parent->parent... tree
        Node type1Node = graph.get(classNameToIndexMap.get(type1));
        Node type2Node = graph.get(classNameToIndexMap.get(type2));
        while(type2Node.parentExists()) {
            type2Node = type2Node.getParent();
            if(type1Node.equals(type2Node)) {
                return true;
            }
        }
        return false;
    }
    
    // returns join of type1 and type2
    public String getJoinOf(String type1, String type2) {
        if(type1.equals(type2)) {
            return type1;
        } else if(isRestrictedInheritanceClass(type1) || isRestrictedInheritanceClass(type2)) {
            return Global.Constants.ROOT_TYPE;
        }
        Node type1Node = graph.get(classNameToIndexMap.get(type1));
        Node type2Node = graph.get(classNameToIndexMap.get(type2));
        Node lca = getLCA(type1Node, type2Node);
        return lca.getAstClass().name;
    }

    // returns least common ancestor of node1 and node2
    private Node getLCA(Node node1, Node node2) {
        Node lca = null;
        List<Boolean> visited = new ArrayList<>(graph.size());
        visited.addAll(Collections.nCopies(graph.size(),Boolean.FALSE));
        while(node1!=null) {
            visited.set(node1.getIndex(),true);
            node1 = node1.getParent();
        }
        while(lca==null && node2!=null) {
            if(visited.get(node2.getIndex())) {
                lca = node2;
            }
            node2 = node2.getParent();
        }
        return lca;
    }

    // Analyses the current graph (also updates parent-child links)
    // returns 'true' if any error reported, else false
    public boolean analyze() {
        boolean hasError = false;

        // Updating the links between the child and the parents
        updateParents();

        if(!hasMain()) {
            // There is no Main class in the graph
            hasError = true;
            Global.errorReporter.report(Global.filename, 0,"'Main' class is missing.");
        }

        // Checking for cycles
        List<Stack<Node>> cycles = getCyclesInGraph();
        if(!cycles.isEmpty()) {
            // Cycles found
            // Building the error with the cycles and reporting
            hasError = true;
            StringBuilder errorString = new StringBuilder();
            StringBuilder cyclePath = new StringBuilder();
            for(Stack<Node> cycle: cycles) {
                cyclePath.setLength(0);
                int size = cycle.size();
                for(int i=0; i<size-1; i++) {
                    cyclePath.append(cycle.pop().getAstClass().name).append(" -> ");
                }

                AST.class_ lastClass = cycle.pop().getAstClass();
                String lastClassName = lastClass.name;
                
                errorString.setLength(0);
                errorString.append("Classes have cyclic dependency: ");
                errorString.append(lastClassName).append(" -> ");
                errorString.append(cyclePath).append(lastClassName);
                Global.errorReporter.report(Global.filename, lastClass.getLineNo(), errorString.toString());
            }
        }
        return hasError;
    }

    // Updates the links between parents and child
    private void updateParents() {
        for(Node cl: graph) {
            if(cl.getAstClass().parent!=null) {
                // node has a parent

                if(isRestrictedInheritanceClass(cl.getAstClass().parent)) {
                    // parent is a restricted class for inheritance
                    Global.errorReporter.report(Global.filename, cl.getAstClass().getLineNo(), 
                                new StringBuilder().append("Cannot inherit base class '").append(cl.getAstClass().parent)
                                .append("'").toString());
                } else if(classNameToIndexMap.containsKey(cl.getAstClass().parent)) {
                    // Parent exists in the graph
                    // connect them
                    int parentIndex = classNameToIndexMap.get(cl.getAstClass().parent);
                    cl.setParent(graph.get(parentIndex));
                    graph.get(parentIndex).addChild(cl);
                } else {
                    // Parent class was not defined (or not added in the graph)
                    Global.errorReporter.report(Global.filename, cl.getAstClass().getLineNo(), 
                                new StringBuilder().append("Inherited class '").append(cl.getAstClass().parent)
                                .append("' for '").append(cl.getAstClass().name).append("' has not been declared").toString());
                }
            } else {
                // Root class is the default parent
                // Hence add it if no parent specified
                if(!Global.Constants.ROOT_TYPE.equals(cl.getAstClass().name)) {
                    cl.setParent(ROOT_AST_NODE);
                    ROOT_AST_NODE.addChild(cl);
                }
            }
        }
    }

    // returns list of cycles in the graph
    private List<Stack<Node>> getCyclesInGraph() {

        int v = graph.size();
        List<Boolean> visited = new ArrayList<>();
        List<Boolean> recStack = new ArrayList<>();
        Stack<Node> cycle = new Stack<>();
        for(int i = 0; i < v; i++) {
            visited.add(false);
            recStack.add(false);
        }
         
        List<Stack<Node>> cycles = new ArrayList<>();
        for(int i = 0; i < v; i++)
            if (getCyclesInGraphUtil(i, visited, recStack, cycle)) {
                // cycle found
                cycles.add(cycle);
                cycle = new Stack<>();
            }
     
        return cycles;
    }

    // Used to detetct cycles
    private boolean getCyclesInGraphUtil(int v, List<Boolean> visited, List<Boolean> recStack, Stack<Node> cycle) {
        Node currentNode = graph.get(v);
        cycle.push(currentNode);
        if(visited.get(v) == false) {
            visited.set(v, true);
            recStack.set(v, true);
            if(currentNode.parentExists()) {
                int parentIndex = currentNode.getParent().getIndex();
                if(parentIndex != Node.NO_PARENT) {
                    if ( (!visited.get(parentIndex) && getCyclesInGraphUtil(parentIndex, visited, recStack, cycle)) 
                          || recStack.get(parentIndex) ) {
                        return true;
                    }
                }
            }
        }
        cycle.pop();
        recStack.set(v, false);
        return false;
    }

    // Adds the default base classes of cool in the graph
    private void addBaseClasses() {
        addObject();
        addIO();
        addString();

        // Int and Bool does not have any methods
        // Hence only updating the names
        classNameToIndexMap.put(Global.Constants.INT_TYPE, -1);
        classNameToIndexMap.put(Global.Constants.BOOL_TYPE, -1);
    }

    // Adds the root class Object into the graph
    private void addObject() {
        // methods of Object
        ROOT_AST_CLASS.features = new ArrayList<>();
        ROOT_AST_CLASS.features.add(new AST.method("abort", new ArrayList<>(), Global.Constants.ROOT_TYPE, null, 0));
        ROOT_AST_CLASS.features.add(new AST.method("type_name", new ArrayList<>(), Global.Constants.STRING_TYPE, null, 0));
        ROOT_AST_CLASS.features.add(new AST.method("copy", new ArrayList<>(), Global.Constants.ROOT_TYPE, null, 0));

        classNameToIndexMap.put(Global.Constants.ROOT_TYPE, ROOT_CLASS_INDEX);
        graph.add(ROOT_AST_NODE);
    }

    // Adds the base class IO into the graph
    private void addIO() {

        List<AST.formal> stringFormalList = new ArrayList<>(Arrays.asList(new AST.formal("x", Global.Constants.STRING_TYPE, 0)));

        List<AST.feature> ioFeatures = new ArrayList<>();
        List<AST.formal> intFormalList = new ArrayList<>(Arrays.asList(new AST.formal("x", Global.Constants.INT_TYPE, 0)));

        ioFeatures.add(new AST.method("out_string", stringFormalList, Global.Constants.IO_TYPE, null, 0));
        ioFeatures.add(new AST.method("out_int", intFormalList, Global.Constants.IO_TYPE, null, 0));
        ioFeatures.add(new AST.method("in_string", new ArrayList<>(), Global.Constants.STRING_TYPE, null, 0));
        ioFeatures.add(new AST.method("in_int", new ArrayList<>(), Global.Constants.INT_TYPE, null, 0));

        AST.class_ ioAstClass = new AST.class_(Global.Constants.IO_TYPE, null, Global.Constants.ROOT_TYPE, ioFeatures, 0);
        Node ioNode = new Node(ioAstClass, 0);

        classNameToIndexMap.put(Global.Constants.IO_TYPE, graph.size());
        graph.add(ioNode);
    }

    // Adds the base class String into the graph
    private void addString() {
        List<AST.formal> stringFormalList = new ArrayList<>(Arrays.asList(new AST.formal("x", Global.Constants.STRING_TYPE, 0)));

        List<AST.formal> intFormalList = new ArrayList<>(Arrays.asList(new AST.formal("x", Global.Constants.INT_TYPE, 0)
            ,new AST.formal("y", Global.Constants.INT_TYPE, 0)));
        List<AST.feature> stringFeatures = new ArrayList<>();

        stringFeatures.add(new AST.method("length", new ArrayList<>(), Global.Constants.INT_TYPE, null, 0));
        stringFeatures.add(new AST.method("concat", stringFormalList, Global.Constants.STRING_TYPE, null, 0));
        stringFeatures.add(new AST.method("substr", intFormalList, Global.Constants.STRING_TYPE, null, 0));

        AST.class_ stringAstClass = new AST.class_(Global.Constants.STRING_TYPE, null, Global.Constants.ROOT_TYPE, stringFeatures, 0);
        Node stringNode = new Node(stringAstClass, 0);

        classNameToIndexMap.put(Global.Constants.STRING_TYPE, graph.size());
        graph.add(stringNode);
        
    }

    // The basic node in the graph
    public static class Node {

        public static final int NO_PARENT = -1;

        private AST.class_ astClass;
        private int index;
        private Node parent;
        private List<Node> children;
        private boolean isInitiated;

        public Node(AST.class_ astClass, int index) {
            this.isInitiated = false;
            init(astClass, index);
        }

        private void init(AST.class_ astClass, int index) {
            if(isInitiated) return;
            this.astClass = astClass;
            this.index = index;
            this.children = new ArrayList<>();
            this.parent = null;
            this.isInitiated = true;
        }

        public void addChild(Node child) {
            children.add(child);
        }

        public AST.class_ getAstClass() {
            return astClass;
        }

        public int getIndex() {
            return index;
        }

        public boolean parentExists() {
            return parent!=null;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public List<Node> getChildren() {
            return children;
        }
        
        public boolean equals(Node node) {
            return this.index == node.getIndex();
        }
    }
}