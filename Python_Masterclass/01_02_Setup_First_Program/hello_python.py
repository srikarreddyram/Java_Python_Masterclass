"""
============================================================================
 SECTIONS 01-02 : SETUP, THE INTERPRETER, THE REPL AND YOUR FIRST PROGRAM
                  (Python track - read alongside HelloJava.java)
============================================================================

 HOW PYTHON RUNS (vs Java)
 -------------------------
   Java  : HelloJava.java --javac--> .class bytecode --java--> JVM
   Python: hello_python.py --python--> compiled to bytecode (.pyc, cached in
           __pycache__) automatically --> executed by the Python VM (CPython)
   There is no separate compile step and no 'main' class required: the file
   runs top to bottom.

 THE __name__ == "__main__" IDIOM
   When you RUN a file, Python sets __name__ = "__main__".
   When you IMPORT it, __name__ = the module name.
   So code under  if __name__ == "__main__":  runs only when executed directly
   - the Python equivalent of public static void main.

 THE REPL (like JShell)
   $ python3
   >>> x = 10
   >>> x * 2
   20
   >>> help(str.split)      # built-in docs
   >>> dir(x)               # list attributes of any object
   >>> exit()

 ENVIRONMENT HYGIENE (no Java equivalent built in)
   python3 -m venv .venv          # isolated environment per project
   source .venv/bin/activate      # Windows: .venv\\Scripts\\activate
   pip install requests           # install packages into the venv
   pip freeze > requirements.txt  # pin versions (like a pom.xml / build.gradle)

 STYLE: PEP 8 - 4-space indents, snake_case for functions/variables,
 PascalCase for classes, UPPER_CASE for constants. Indentation IS syntax.

 HOW TO RUN:  python3 hello_python.py Srikar
============================================================================
"""
import sys


def main() -> None:
    # 1) The classic first line
    print("Hello, Python!")

    # 2) Command-line arguments: sys.argv[0] is the script name (Java's args[] excludes it)
    name = sys.argv[1] if len(sys.argv) > 1 else "Developer"
    print(f"Welcome, {name}!")

    # 3) print options: sep and end replace Java's print/println distinction
    print("print() adds a newline by default...", end=" ")
    print("unless you pass end=")
    print("a", "b", "c", sep=" | ")

    # 4) f-strings (Java's printf / String.format equivalent)
    lang, age, done = "Python", 35, 99.5
    print(f"f-strings format: {lang} is {age} years old and {done:.1f}% done")

    # 5) Comments: # single line. Triple-quoted strings at the top of a module,
    #    class or function are DOCSTRINGS (Python's Javadoc), visible via help().

    # 6) Escapes and raw strings
    print("Tab:\t| Quote: \" | Backslash: \\ | chr(65) =", chr(65))
    print(r"raw string keeps backslashes: C:\new\table")

    # 7) Multi-line strings (Java text blocks)
    banner = """\
+----------------------------+
|   Python Masterclass Lab   |
+----------------------------+"""
    print(banner)

    # 8) Everything is an object - even functions and modules
    print(f"type(42) = {type(42).__name__}, type(main) = {type(main).__name__}, type(sys) = {type(sys).__name__}")


if __name__ == "__main__":
    main()
