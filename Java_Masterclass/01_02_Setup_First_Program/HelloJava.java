/*
 * ============================================================================
 *  SECTIONS 01-02 : SETUP, THE JDK, JSHELL AND YOUR FIRST PROGRAM
 * ============================================================================
 *
 *  JDK vs JRE vs JVM
 *  -----------------
 *   JVM : the Java Virtual Machine. Runs bytecode (.class files) on any OS.
 *   JRE : JVM + the standard class library. Enough to RUN programs.
 *   JDK : JRE + developer tools (javac compiler, jshell, javadoc, jar...).
 *         You need the JDK to WRITE programs.
 *
 *  FROM SOURCE TO RUNNING PROGRAM
 *  ------------------------------
 *     HelloJava.java  --javac-->  HelloJava.class (bytecode)  --java-->  JVM
 *                                                                (interprets +
 *                                                                 JIT-compiles hot
 *                                                                 code to machine code)
 *   "Write once, run anywhere": the same .class runs on Windows, macOS, Linux.
 *
 *  ANATOMY OF A JAVA PROGRAM
 *  -------------------------
 *   public class HelloJava {                 <- class name MUST match file name
 *       public static void main(String[] args) {   <- entry point the JVM calls
 *           System.out.println("Hello");     <- statement ends with ;
 *       }
 *   }
 *   public  : visible everywhere      static : belongs to the class, no object needed
 *   void    : returns nothing         String[] args : command-line arguments
 *
 *  JSHELL (Java's REPL, JDK 9+)
 *  ---------------------------
 *   $ jshell
 *   jshell> int x = 10;
 *   jshell> x * 2
 *   $2 ==> 20
 *   jshell> /vars     /methods    /list     /exit
 *   Great for trying a one-liner without writing a class.
 *
 *  HOW TO RUN THIS FILE
 *  --------------------
 *   javac HelloJava.java && java HelloJava Srikar
 *   java HelloJava.java Srikar          (Java 11+: compile + run in one step)
 * ============================================================================
 */
public class HelloJava {

    public static void main(String[] args) {
        // 1) The classic first line
        System.out.println("Hello, Java!");

        // 2) Command-line arguments arrive in args[]
        String name = args.length > 0 ? args[0] : "Developer";
        System.out.println("Welcome, " + name + "!");

        // 3) print vs println vs printf
        System.out.print("print() does not add a newline... ");
        System.out.println("println() does.");
        System.out.printf("printf formats: %s is %d years old and %.1f%% done%n", "Java", 30, 99.5);

        // 4) Comments: // single line, /* block */, /** Javadoc */ (used to generate API docs)

        // 5) Escape sequences inside strings
        System.out.println("Tab:\t| Quote: \" | Backslash: \\ | char code 65 = " + (char) 65);

        // 6) Text block (Java 15+): multi-line strings without \n everywhere
        String banner = """
                +----------------------------+
                |   Java Masterclass Lab     |
                +----------------------------+""";
        System.out.println(banner);
    }
}
