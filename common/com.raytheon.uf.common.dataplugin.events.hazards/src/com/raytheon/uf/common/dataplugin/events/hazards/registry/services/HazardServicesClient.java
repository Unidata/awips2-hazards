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
package com.raytheon.uf.common.dataplugin.events.hazards.registry.services;

import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.CertificateRetriever;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.registry.ebxml.RegistryUtil;
import com.raytheon.uf.common.security.encryption.AESEncryptor;

/**
 * 
 * This class creates and provides access to the hazard service web clients
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class HazardServicesClient {

    /** Property name of the registry location */
    public static final String REGISTRY_LOCATION = "hazard.services.registry.location";

    /** Property name for user name */
    public static final String USER_NAME = "edex.security.auth.user";

    /** Property name for password */
    public static final String PASSWORD = "edex.security.auth.password";

    /** Property name for the trust store location */
    public static final String TRUST_STORE_LOCATION = "edex.security.truststore.path";

    /** Property name for the trust store password */
    public static final String TRUST_STORE_PASSWORD = "edex.security.truststore.password";

    /** Property name for the encryption key */
    public static final String ENCRYPTION_KEY = "edex.security.encryption.key";

    /** WSDL String */
    private static final String WSDL = "?wsdl";

    /** URL fragment for the practice web services */
    private static final String PRACTICE_PATH = "/practice";

    /**
     * Gets the hazard event web services client for the given mode
     * 
     * @param mode
     *            The mode
     * @return A hazard event web services client object
     */
    public static IHazardEventServices getHazardEventServices(Mode mode) {
        URL wsdl = getWsdl(mode,
                Mode.PRACTICE.equals(mode) ? IHazardEventServices.PATH
                        + PRACTICE_PATH : IHazardEventServices.PATH);
        return Service.create(
                wsdl,
                new QName(IHazardEventServices.NAMESPACE,
                        IHazardEventServices.SERVICE_NAME)).getPort(
                IHazardEventServices.class);

    }

    /**
     * Gets the hazard event web services client for the given mode
     * 
     * @param practice
     *            True if for practice mode, else false for operational mode
     * @return A hazard event web services client object
     */
    public static IHazardEventServices getHazardEventServices(boolean practice) {
        return getHazardEventServices(practice ? Mode.PRACTICE
                : Mode.OPERATIONAL);
    }

    /**
     * Gets the hazard event web services interoperability client for the given
     * mode
     * 
     * @param mode
     *            The mode
     * @return A hazard event web services interoperability client object
     */
    public static IHazardEventInteropServices getHazardEventInteropServices(
            Mode mode) {
        URL wsdl = getWsdl(mode,
                Mode.PRACTICE.equals(mode) ? IHazardEventInteropServices.PATH
                        + PRACTICE_PATH : IHazardEventInteropServices.PATH);
        return Service.create(
                wsdl,
                new QName(IHazardEventInteropServices.NAMESPACE,
                        IHazardEventInteropServices.SERVICE_NAME)).getPort(
                IHazardEventInteropServices.class);
    }

    /**
     * Gets the hazard event web services interoperability client for the given
     * mode
     * 
     * @param practice
     *            True if for practice mode, else false for operational mode
     * @return A hazard event web services interoperability client object
     */
    public static IHazardEventInteropServices getHazardEventInteropServices(
            boolean practice) {
        return getHazardEventInteropServices(practice ? Mode.PRACTICE
                : Mode.OPERATIONAL);
    }

    /**
     * Gets the URL to the WSDL for the given mode and service path
     * 
     * @param mode
     *            The mode
     * @param servicePath
     *            The path to the service
     * @return The URL to the WSDL
     */
    private static URL getWsdl(Mode mode, String servicePath) {
        try {
            String registryLocation = System.getProperty(REGISTRY_LOCATION) == null ? RegistryUtil.LOCAL_REGISTRY_ADDRESS
                    : System.getProperty(REGISTRY_LOCATION);
            return new URL(registryLocation + servicePath + WSDL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(
                    "Error initializing HazardEventManager!", e);
        }
    }

    /**
     * Checks connectivity to the hazard web services
     */
    public static void checkConnectivity() {
        getHazardEventServices(Mode.PRACTICE).ping();
        getHazardEventServices(Mode.OPERATIONAL).ping();
        getHazardEventInteropServices(Mode.PRACTICE).ping();
        getHazardEventInteropServices(Mode.OPERATIONAL).ping();
    }

    /**
     * Initializes environment variables and registry connection configuration
     * 
     * @param registryLocation
     *            The URL of the registry
     * @param username
     *            The user name used to connect to the registry
     * @param password
     *            The user's password
     * @param truststorePath
     *            The path to the local trust store
     * @param truststorePassword
     *            The password for the local trust store
     * @param encryptionKey
     *            The encryption key for decrypting passwords
     */
    public static void init(final String registryLocation,
            final String username, final String password,
            final String truststorePath, final String truststorePassword,
            final String encryptionKey) {
        try {
            final AESEncryptor encryptor = new AESEncryptor();
            String decryptedTrustStorePassword = encryptor.decrypt(
                    encryptionKey, truststorePassword);
            final String decryptedPassword = encryptor.decrypt(encryptionKey,
                    password);
            System.setProperty(REGISTRY_LOCATION, registryLocation);
            System.setProperty("javax.net.ssl.trustStore", truststorePath);
            System.setProperty("javax.net.ssl.trustStorePassword",
                    decryptedTrustStorePassword);
            URL registryUrl = new URL(registryLocation);
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username,
                            decryptedPassword.toCharArray());
                }
            });
            CertificateRetriever.importCertificates(registryUrl.getHost(),
                    registryUrl.getPort(), truststorePath,
                    decryptedTrustStorePassword);
        } catch (Exception e) {
            throw new RuntimeException("Error Initializing Registry Services!",
                    e);
        }
    }
}
