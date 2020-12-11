import java.util.List;

public class ASTPool{

  abstract public static class ASTNode {
      int lineNo;
      public int getLineNo() {
          return lineNo;
      }
      abstract public void accept(Visitor visitor);
  }

  public static String sp = "  ";

  static String escapeSpecialCharacters(String text) {
          return text
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
                  .replaceAll("\000", "\\\\000");
  }

  public static class program extends ASTNode {
      public List<classblock> classes;
      public List<import_stat> imports;
      public List<alias_stat> aliases;
      public program(List<classblock> c, List<import_stat> imp,List<alias_stat> al,int l){
          classes = c;
          imports = imp;
          aliases = al;
          lineNo = l;
      }
      String getString(String space){
          String str;
          str = space+"#"+lineNo+"\n"+space+"_program";
          for ( classblock c : classes ) {
              str += "\n"+c.getString(space+sp);
          }
          //to be changed
          return str;
      }
      public void accept(Visitor visitor) {
          visitor.visit(this);
      }
  }

  public static class classblock extends ASTNode {
      public String name;
      public String filename;
      public String parent;
      public List<feature> features;
      public classblock(String n, String f, String p, List<feature> fs, int l){
          name = n;
          filename = f;
          parent = p;
          features = fs;
          lineNo = l;
      }
      String getString(String space){
          String str;
          str = space+"#"+lineNo+"\n"+space+"_class\n"+space+sp+name+"\n"+space+sp+parent+"\n"+space+sp+"\""+filename+"\""+"\n"+space+sp+"(\n";
          for ( feature f : features ) {
              str += f.getString(space+sp)+"\n";
          }
          str += space+sp+")";
          return str;//to be changed
      }
      public void accept(Visitor visitor) {
          visitor.visit(this);
      }
  }

  public static class import_stat extends ASTNode {
    public String lib;
    public String lib_alias;

    public import_stat(String lb, String la, String l) {
      lineNo = l;
      lib = lb;
      lib_alias = la;
    }
    String getString(String space){
      String str = "";
      return str;// to be changed
    }
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
  }

  public static class alias_stat extends ASTNode {
    public String obj;
    public String obj_alias;

    public alias_stat(String lb, String la, String l) {
      lineNo = l;
      obj = lb;
      obj_alias = la;
    }
    String getString(String space){
      String str = "";
      return str;// to be changed
    }
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
  }

}
