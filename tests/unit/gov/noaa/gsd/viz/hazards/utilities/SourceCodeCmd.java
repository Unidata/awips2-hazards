package gov.noaa.gsd.viz.hazards.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class allows one to perform a transaction with an arbitrary program
 * located in source code directories and return its output from stdout.
 * Mostly meant to support situations where a java unit test needs access
 * to python logic in a non-JEP enabled environment.  This can be done by
 * running standalone python programs in the shell and capturing the output.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 
 * </pre>
 * 
 * @author jramer
 */

public class SourceCodeCmd {
    private static List<String> possibleLocs;

    private static Map<String, String> cmdPathsMap;

    private String cmdPath;

    public SourceCodeCmd(String cmdName) {
        if (cmdPathsMap == null) {
            cmdPathsMap = new HashMap<String, String>();
            possibleLocs = new ArrayList<String>();
            possibleLocs.add("/awips2/fxa/bin");
            String codePath = SourceCodeCmd.class.getProtectionDomain()
                    .getCodeSource().getLocation().toExternalForm();
            String[] codeParts = codePath.split("/");
            codePath = "/";
            for (int i = 1; i < codeParts.length; i++) {
                codePath += codeParts[i];
                if (codeParts[i].equals("hazardServices")) {
                    break;
                }
                codePath += "/";
            }
            possibleLocs.add(codePath);
        }
        this.cmdPath = cmdPathsMap.get(cmdName);
        if (this.cmdPath != null) {
            return;
        }
        int n = possibleLocs.size();
        for (int i = 0; i < n; i++) {
            System.out.flush();
            if (this.cmdPath != null) {
                return;
            }
            String froot = possibleLocs.get(i);
            String cmd = "find " + froot + " -name " + cmdName;
            Process myprocess = null;
            InputStream mystream = null;
            BufferedReader myreader = null;
            String result = null;
            try {
                myprocess = Runtime.getRuntime().exec(cmd);
                mystream = myprocess.getInputStream();
                myreader = new BufferedReader(new InputStreamReader(mystream));
                result = myreader.readLine();
                if (result != null && result.length() > 10) {
                    this.cmdPath = result;
                    cmdPathsMap.put(cmdName, result);
                }
                myreader.close();
                mystream.close();
                myprocess.destroy();
            } catch (Exception e) {
                try {
                    if (myreader != null) {
                        myreader.close();
                    }
                    if (mystream != null) {
                        mystream.close();
                    }
                    if (myprocess != null) {
                        myprocess.destroy();
                    }
                } catch (IOException ee) {
                    continue;
                }
            }
        }
        System.out.flush();
    }

    public boolean ok() {
        return this.cmdPath != null;
    }

    public String transaction(String arguments) {
        if (this.cmdPath == null) {
            return null;
        }
        String cmd = this.cmdPath + " " + arguments;
        Process myprocess = null;
        InputStream mystream = null;
        BufferedReader myreader = null;
        StringBuffer cmdOut = new StringBuffer();
        try {
            myprocess = Runtime.getRuntime().exec(cmd);
            mystream = myprocess.getInputStream();
            myreader = new BufferedReader(new InputStreamReader(mystream));
            while (true) {
                String result = myreader.readLine();
                if (result == null) {
                    break;
                }
                cmdOut.append(result);
            }
            myreader.close();
            mystream.close();
            myprocess.destroy();
            if (cmdOut.length() < 2) {
                return null;
            }
            return cmdOut.toString();
        } catch (IOException e) {
            try {
                if (myreader != null) {
                    myreader.close();
                }
                if (mystream != null) {
                    mystream.close();
                }
                if (myprocess != null) {
                    myprocess.destroy();
                }
            } catch (IOException ee) {
                return null;
            }
        }
        if (cmdOut.length() < 2) {
            return null;
        }
        return cmdOut.toString();
    }

}
