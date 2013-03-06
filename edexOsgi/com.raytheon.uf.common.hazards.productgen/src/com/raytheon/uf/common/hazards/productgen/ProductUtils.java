/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.common.hazards.productgen;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Utility class to help display generated products.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 11, 2013            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductUtils {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductUtils.class);

    /** Maximum width of a warning */
    private static final int MAX_WIDTH = 69;

    private static final String INDENT = "  ";

    private static final String BULLET_START = "* ";

    private static final Pattern wrapUgcPtrn = Pattern.compile("(\\S{1,"
            + (MAX_WIDTH - 1) + "}-)");

    private static final Pattern wrapListOfNamesPtrn = Pattern.compile("(.{1,"
            + (MAX_WIDTH - 4) + "} \\w{2}-)");

    // Locations in 4th bullet or locations paragraph of followup
    // ex: ellipsis, spaces, period
    private static final Pattern wrapDefaultPtrn = Pattern
            .compile("(\\w{1,}\\.\\.\\.)|(AND \\w+\\.\\.\\.)|(\\w+\\.\\.\\.\\s{1,2})|\\S+\\.\\.\\.|"
                    + "(\\s*\\S+\\s+)|(.+\\.)|(\\S+$)");

    // ugc pattern
    private static final Pattern ugcPtrn = Pattern
            .compile("(^(\\w{2}[CZ]\\d{3}\\S*-\\d{6}-)$|((\\d{3}-)*\\d{6}-)$|((\\d{3}-)+))");

    // list of areas pattern
    private static final Pattern listOfAreaNamePtrn = Pattern
            .compile("^((((\\w+\\s{1})+\\w{2}-)*((\\w+\\s{1})+\\w{2}-)))");

    /**
     * Transforms the xml into a human readable format indenting sub tags by two
     * white spaces.
     * 
     * @param xml
     * @return
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    public static String prettyXML(String xml) {

        try {
            // Instantiate transformer input
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());

            // Configure transformer
            Transformer transformer = TransformerFactory.newInstance()
                    .newTransformer(); // An identity transformer
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);

            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            statusHandler
                    .error("Unable to transform. Verify it's in xml format.");
        }

        return xml;
    }

    /**
     * Wraps the text independent of being locked before or after.
     * 
     * @param text
     * @return
     */
    public static String wrapLegacy(String text) {
        StringBuffer sb = new StringBuffer();

        boolean inBullet = false;
        String addLine = "";
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.trim().length() == 0) { // BLANK LINE
                inBullet = false;
                addLine = line;
            } else if (line.length() <= MAX_WIDTH) { // LESS THAN MAX
                // Add indenting if the template didn't account for it yet.
                if (inBullet && !line.startsWith(INDENT)) {
                    sb.append(INDENT);
                }

                if (line.startsWith(BULLET_START)) {
                    inBullet = true;
                }

                addLine = line;
            } else { // NEEDS TO BE WRAPPED
                addLine = wrapLongLines(line, inBullet);
            }

            sb.append(addLine);
            if (i != lines.length - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Wraps lines longer than the MAX WIDTH
     * 
     * @param line
     * @param inBullet
     * @return
     */
    private static String wrapLongLines(String line, boolean inBullet) {
        StringBuffer sb = new StringBuffer(line.length());

        if (line.startsWith(BULLET_START)) {
            inBullet = true;
        }

        Pattern p = getPattern(line);
        Matcher m = p.matcher(line.trim());
        String tmp = inBullet && !line.startsWith(BULLET_START) ? INDENT : "";

        while (m.find()) {
            String group = m.group();

            int len = tmp.length();
            if (len + group.length() > MAX_WIDTH && tmp.trim().length() > 0) {
                sb.append(tmp + "\n");
                tmp = inBullet ? INDENT : "";
                tmp += group;
            } else {
                tmp += group;
            }
        }

        if (tmp.trim().length() > 0) {
            sb.append(tmp);
        }

        return sb.toString();
    }

    /**
     * Helper method to return matcher object for wrapping text.
     * 
     * @param line
     * @return
     */
    private static Pattern getPattern(String line) {

        Matcher m = ugcPtrn.matcher(line);
        // UGC line or FIPS line
        if (m.find()) {
            return wrapUgcPtrn;
        }

        m = listOfAreaNamePtrn.matcher(line);
        // List of area names.
        if (m.find() && !line.startsWith(BULLET_START)) {
            return wrapListOfNamesPtrn;
        }

        return wrapDefaultPtrn;
    }
}
