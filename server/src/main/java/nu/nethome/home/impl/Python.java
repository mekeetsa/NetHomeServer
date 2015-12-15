package nu.nethome.home.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.__builtin__;
import org.python.util.PythonInterpreter; 

/**
 *
 * @author JS
 */
public class Python {
    private PyObject compiledCode = null;
    private String scriptSourceFileName = "/home/nethome/nethome.py";
    private long sourceFilelastModifiedDate = 0;
    private PythonInterpreter interp;
    private Map<String, Object> variables = new ConcurrentHashMap<String, Object>();
    private static Logger logger = Logger.getLogger(Python.class.getName());

    
    public Python(HomeServer server) {
        interp = new PythonInterpreter();
        interp.set("server", server);
        interp.set("variables", variables);
        interp.set("log", logger);
    }
    
    public boolean callFunction(String functionCall) throws FileNotFoundException, IOException
    {
        compileIfNeeded();
        interp.exec(compiledCode);
        String arguments = null;
        String functionName;
        int startIndex = functionCall.indexOf('(');
        if (startIndex >= 0)
        {
            functionName = functionCall.substring(0, startIndex);
            int endIndex = functionCall.lastIndexOf(')');
            if (endIndex > 0)
            {
                arguments = functionCall.substring(startIndex+1, endIndex);
            }
        }
        else    // no parameters
        {
            functionName = functionCall;
        }
        PyObject func = interp.get(functionName);
        if (func != null)
        {
            if (arguments != null)
            {
                func.__call__(new PyString(arguments));
            } 
            else
            {
                func.__call__();
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    
    private void compileIfNeeded() throws FileNotFoundException, IOException
    {
       File file = new File(getScriptSourceFileName());

       if (compiledCode == null || file.lastModified() > sourceFilelastModifiedDate)
       {
          compiledCode = compileScript(getScriptSourceFileName());
          sourceFilelastModifiedDate = file.lastModified();
       }
    }

    private PyObject compileScript(String fileName) throws FileNotFoundException, IOException
    {
       PyObject pyObject = null;
       try
       {
          FileInputStream fis = new FileInputStream(fileName);
          int size = fis.available();
          byte b[]= new byte[size];
          fis.read(b);
          String script = new String(b);
          pyObject = __builtin__.compile(script, "<>", "exec");
       } 
       catch (FileNotFoundException e)
       {
          throw(e);
       } 
       catch (IOException e)
       {
          throw(e);
       }
       return pyObject;
    }

    public String getScriptSourceFileName()
    {
       return scriptSourceFileName;
    }

    public void setScriptSourceFileName(String scriptSourceFileName)
    {
       this.scriptSourceFileName = scriptSourceFileName;
    }
}
