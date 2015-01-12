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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
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

import com.raytheon.uf.common.activetable.SendPracticeProductRequest;
import com.raytheon.uf.common.dataplugin.text.AfosWmoIdDataContainer;
import com.raytheon.uf.common.dataplugin.text.db.AfosToAwips;
import com.raytheon.uf.common.dataplugin.text.request.GetAfosIdRequest;
import com.raytheon.uf.common.dissemination.OUPRequest;
import com.raytheon.uf.common.dissemination.OfficialUserProduct;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.SimulatedTime;

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
 * Jun 20, 2013 1122       jsanchez     Added a disseminate method.
 * Sep 12, 2013 717        jsanchez     Changed disseminate to return void type.
 * Apr  4, 2013            jsanchez     Handled dissemination differently for operational mode and practice mode.
 * Dec 09, 2014 2826       dgilling     Ensure all necessary fields are set on
 *                                      SendPracticeProductRequest.
 * Dec 10, 2014 4933       Robert.Blum  Changes to allow for mixed case in the
 *                                      legacy product.
 * Jan 06, 2015 4937       Robert.Blum  wrapLegacy was incorrectly indenting bullets.
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

    private static Pattern wmoHeaderPattern = Pattern
            .compile("(\\w{4}\\d{2}) (\\w{4})");

    /**
     * Transforms the xml into a human readable format indenting sub tags by two
     * white spaces.
     * 
     * @param xml
     * @return
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    public static String prettyXML(String xml, boolean includeVersion) {

        try {
            // Instantiate transformer input
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());

            // Configure transformer
            Transformer transformer = TransformerFactory.newInstance()
                    .newTransformer(); // An identity transformer
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "0");
            if (includeVersion == false) {
                transformer.setOutputProperty("omit-xml-declaration", "yes");
            }
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

        // Maintains the number of newline characters on the end of the text.
        int newlinesAtTheEnd = 0;
        for (int i = text.length() - 1; i >= 0; i--) {
            if (text.charAt(i) == '\n') {
                newlinesAtTheEnd++;
            } else {
                break;
            }

        }

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
                if (line.startsWith(BULLET_START)) {
                    inBullet = true;
                }
                // Add indenting if the template didn't account for it yet.
                else if (inBullet && !line.startsWith(INDENT)) {
                    sb.append(INDENT);
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

        // Adds back the new line characters that were stripped off
        for (int i = 0; i < newlinesAtTheEnd; i++) {
            sb.append("\n");
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

    /**
     * Disseminates the text product via the OUPRequest.
     * 
     * @param product
     * @param operational
     */
    public static void disseminate(String product, boolean operational) {

        try {
            if (operational) {
                String awipsWanPil = null;
                String awipsID = null;

                Matcher m = wmoHeaderPattern.matcher(product);

                if (m.find()) {
                    GetAfosIdRequest afosIdRequest = new GetAfosIdRequest();
                    afosIdRequest.setTtaaii(m.group(1));
                    afosIdRequest.setCccc(m.group(2));
                    AfosWmoIdDataContainer container = (AfosWmoIdDataContainer) RequestRouter
                            .route(afosIdRequest);
                    if (container != null && !container.getIdList().isEmpty()) {
                        AfosToAwips item = container.getIdList().get(0);
                        awipsID = item.getAfosid().substring(3);
                        awipsWanPil = m.group(2) + awipsID;
                    }
                }

                SimpleDateFormat formatter = new SimpleDateFormat("ddHHmm");
                formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
                String userDateTimeStamp = formatter.format(SimulatedTime
                        .getSystemTime().getTime());

                OfficialUserProduct oup = new OfficialUserProduct();
                oup.setAwipsWanPil(awipsWanPil);
                oup.setNeedsWmoHeader(false);
                oup.setProductText(product);
                oup.setSource("HazardServices");
                oup.setWmoType("");
                oup.setUserDateTimeStamp(userDateTimeStamp);
                oup.setFilename(awipsID + ".wan"
                        + (System.currentTimeMillis() / 1000));
                oup.setAddress("ALL");

                OUPRequest req = new OUPRequest();
                req.setCheckBBB(true);
                req.setProduct(oup);
                RequestRouter.route(req);
            } else {
                SendPracticeProductRequest req = new SendPracticeProductRequest();
                req.setProductText(product);
                req.setNotifyGFE(true);
                if (!SimulatedTime.getSystemTime().isRealTime()) {
                    DateFormat dateFormatter = new SimpleDateFormat(
                            "yyyyMMdd_HHmm");
                    dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
                    req.setDrtString(dateFormatter.format(SimulatedTime
                            .getSystemTime().getTime()));
                }

                RequestRouter.route(req);
            }
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Error transmitting text product", e);
        }
    }
}