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
package com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.request.GetRegistryInfoRequest;
import com.raytheon.uf.common.security.encryption.AESEncryptor;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Abstract web services client for access web services exposed by the registry.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 4, 2015  6895     Ben.Phillippe Finished HS data access refactor
 * Aug 20, 2015 6895     Ben.Phillippe Routing registry requests through request server
 * Mar 14, 2016 16534    mduff         Update for new AESEncryptor.
 * May 06, 2016 18202    Robert.Blum   Changes for operational mode.
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class AbstractHazardEventServicesSoapClient extends Service {

    /** The logger */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AbstractHazardEventServicesSoapClient.class);

    static {
        try {
            initializeRegistryInfo();
        } catch (HazardEventServiceException e) {
            throw new RuntimeException(
                    "Error retrieving Registry connection information!", e);
        }
    }

    /** TLS constant string */
    private static final String TLS = "TLS";

    /** SHA1 constant string */
    private static final String SHA1 = "SHA1";

    /** MD5 constant string */
    private static final String MD5 = "MD5";

    /** The url of the registry */
    private static String REGISTRY_BASE_PATH;

    /** The user used to communicate with the registry */
    private static String REGISTRY_USER;

    /** The user passsword of the user used to communicate with the registry */
    private static String REGISTRY_USER_PASSWORD;

    /** The path to the local trust store */
    private static String TRUST_STORE_PATH;

    /** The local trust store password private static String */
    private static String TRUST_STORE_PASSWORD;

    /**
     * Creates a new web service client
     * 
     * @param path
     *            The location of the web services wsdl
     * @param namespace
     *            The web service namespace
     * @param serviceName
     *            The name of the service
     * @param practice
     *            Practice or operational mode
     */
    protected AbstractHazardEventServicesSoapClient(String path,
            String namespace, String serviceName, boolean practice) {
        super(getWsdl(path, practice), new QName(namespace, serviceName));
    }

    /**
     * Gets the URL to the service wsdl
     * 
     * @param path
     *            The url path
     * @param practice
     *            Practice or operational mode
     * @return The URL to the service wsdl
     */
    private static URL getWsdl(String path, boolean practice) {
        try {
            if (practice) {
                return new URL(REGISTRY_BASE_PATH + path + "/practice?wsdl");
            } else {
                return new URL(REGISTRY_BASE_PATH + path + "?wsdl");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(
                    "Error initializing HazardEventServices!", e);
        }
    }

    /**
     * Initializes the information necessary to connect to the registry
     * 
     * @throws HazardEventServiceException
     *             If errors occur during initialization
     */
    private static void initializeRegistryInfo()
            throws HazardEventServiceException {
        try {
            Map<String, String> properties = HazardEventRequestServices
                    .getServices(false).getRegistryConnectionInfo();
            getTrustStore();
            REGISTRY_BASE_PATH = properties
                    .get(GetRegistryInfoRequest.REGISTRY_URL_KEY);
            REGISTRY_USER = properties
                    .get(GetRegistryInfoRequest.REGISTRY_USER_KEY);
            REGISTRY_USER_PASSWORD = properties
                    .get(GetRegistryInfoRequest.REGISTRY_USER_PASSWORD_KEY);

            // Set the trust store system properties
            System.setProperty("javax.net.ssl.trustStore", TRUST_STORE_PATH);
            System.setProperty("javax.net.ssl.trustStorePassword",
                    TRUST_STORE_PASSWORD);

            // Assign the default authena ticator to use for web service
            // requests
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(REGISTRY_USER,
                            REGISTRY_USER_PASSWORD.toCharArray());
                }
            });
            importCertificates();
        } catch (Exception e) {
            throw new HazardEventServiceException(
                    "Error initializing registry info!", e);
        }
    }

    /**
     * Gets the path to the cert store
     * 
     * @return The trust store path
     * @throws Exception
     */
    private static void getTrustStore() throws Exception {
        TRUST_STORE_PATH = System.getProperty("edex.security.truststore.path");
        if (TRUST_STORE_PATH == null) {
            File file = new File("jssecacerts");
            if (!file.isFile()) {
                File dir = new File(
                        new File(System.getProperty("java.home"), "lib"),
                        "security");
                file = new File(dir, "jssecacerts");
                if (!file.isFile()) {
                    file = new File(dir, "cacerts");
                }
            }
            TRUST_STORE_PATH = file.getPath();
            TRUST_STORE_PASSWORD = "changeit";
        } else {
            AESEncryptor encryptor = new AESEncryptor(
                    System.getProperty("edex.security.encryption.key"));
            TRUST_STORE_PASSWORD = encryptor.decrypt(
                    System.getProperty("edex.security.truststore.password"));
        }
    }

    /**
     * Imports certificates from a server
     * 
     * @param host
     *            The host name
     * @param port
     *            The port number
     * @param trustStorePath
     *            The path to the local trust store where the certs are to be
     *            stored
     * @param trustStorePassword
     *            The password used to access the local trust store
     */
    private static void importCertificates() throws Exception {
        URL registryUrl = new URL(REGISTRY_BASE_PATH);
        String host = registryUrl.getHost();
        int port = registryUrl.getPort();

        SavingTrustManager tm;
        KeyStore ks;
        X509TrustManager defaultTrustManager;

        /*
         * Initialize the local trust store in preparation for retrieving the
         * remote certificates
         */
        File file = new File(TRUST_STORE_PATH);
        statusHandler.info("Loading KeyStore " + file + "...");

        InputStream in = new FileInputStream(file);
        ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(in, System.getProperty("javax.net.ssl.trustStorePassword")
                .toCharArray());
        in.close();
        statusHandler.info("Initializing Truststore...");
        TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        tm = new SavingTrustManager(defaultTrustManager);

        /*
         * Execute an SSL handshake to get the certificate
         */
        SSLContext context = SSLContext.getInstance(TLS);
        context.init(null, new TrustManager[] { tm }, null);
        SSLSocketFactory factory = context.getSocketFactory();
        statusHandler
                .info("Opening connection to " + host + ":" + port + "...");
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.setSoTimeout(10000);
        try {
            statusHandler.info("Starting SSL handshake ...");
            socket.startHandshake();
            socket.close();
            statusHandler.info("No errors, certificate is already trusted\n");
        } catch (SSLException exc) {
            statusHandler.info("Handshake complete");
        }

        X509Certificate[] chain = tm.chain;
        if (chain == null) {
            statusHandler.error("Unable to retrieve certificates!");
            return;
        }

        statusHandler.info("Server sent " + chain.length + " certificate(s)\n");

        /*
         * Iterate over the retrieved certs and stored them in the local
         * truststore
         */
        MessageDigest sha1 = MessageDigest.getInstance(SHA1);
        MessageDigest md5 = MessageDigest.getInstance(MD5);
        for (int i = 0; i < chain.length; i++) {
            X509Certificate cert = chain[i];
            for (int j = i; j >= 0; j--) {
                if (Arrays.asList(defaultTrustManager.getAcceptedIssuers())
                        .contains(chain[j])) {
                    break;
                }
            }
            sha1.update(cert.getEncoded());
            md5.update(cert.getEncoded());

            statusHandler.info("Adding certificate for host " + host + "...");
            OutputStream out = null;
            try {
                ks.setCertificateEntry(host, cert);
                out = new BufferedOutputStream(
                        new FileOutputStream(TRUST_STORE_PATH));
                ks.store(out,
                        System.getProperty("javax.net.ssl.trustStorePassword")
                                .toCharArray());
            } catch (Exception e) {
                statusHandler.error("Error adding certificate to keystore!", e);
            } finally {
                if (out != null) {
                    out.close();
                }
            }
            statusHandler.info("Added certificate to keystore!");
        }
    }

    private static class SavingTrustManager implements X509TrustManager {
        private final X509TrustManager tm;

        private X509Certificate[] chain;

        SavingTrustManager(X509TrustManager tm) {
            this.tm = tm;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            this.chain = chain;
            tm.checkServerTrusted(chain, authType);
        }
    }
}
