/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.utilities;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

/**
 * Description: Simple driver to validate HazardServices XML products against
 * the HazardServicesProduct schema file.
 * <p>
 * This application takes two arguments:
 * <p>
 * The path to the XML file and the URL of the schema file.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 17, 2013            bryon.lawrence      Initial creation
 * 
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class ProductXMLValidator {
    public static void main(String args[]) {
        if (args.length != 2) {
            System.out.println("Arguments: <XML filepath> <Schema fileURL>");
        } else {
            Source xmlFileSource = new StreamSource(new File(args[0]));
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);

            try {
                URL schemaURL = new URL(args[1]);
                Schema schema = schemaFactory.newSchema(schemaURL);
                Validator validator = schema.newValidator();
                validator.validate(xmlFileSource);
                System.out.println(xmlFileSource.getSystemId() + " is valid");
            } catch (SAXException e) {
                System.out.println(xmlFileSource.getSystemId()
                        + " is NOT valid");

                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
