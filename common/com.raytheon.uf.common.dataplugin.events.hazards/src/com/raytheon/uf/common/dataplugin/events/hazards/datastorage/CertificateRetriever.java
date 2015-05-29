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
package com.raytheon.uf.common.dataplugin.events.hazards.datastorage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Retrieves the certificate from a given server and stores it in the designated
 * trust store
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
public class CertificateRetriever {

    /** The logger */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(CertificateRetriever.class);

    /** TLS constant string */
    private static final String TLS = "TLS";

    /** SHA1 constant string */
    private static final String SHA1 = "SHA1";

    /** MD5 constant string */
    private static final String MD5 = "MD5";

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
    public static void importCertificates(String host, int port,
            String trustStorePath, String trustStorePassword) throws Exception {

        SavingTrustManager tm;
        KeyStore ks;
        X509TrustManager defaultTrustManager;

        /*
         * Initialize the local trust store in preparation for retrieving the
         * remote certificates
         */
        File file = new File(trustStorePath);
        statusHandler.info("Loading KeyStore " + file + "...");

        InputStream in = new FileInputStream(file);
        ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(in, trustStorePassword.toCharArray());
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
                        new FileOutputStream(trustStorePath));
                ks.store(out, trustStorePassword.toCharArray());
            } catch (Exception e) {
                statusHandler.error("Error adding certificate to keystore!", e);
            } finally {
                if (out != null){
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

        public X509Certificate[] getAcceptedIssuers() {
            throw new UnsupportedOperationException();
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            throw new UnsupportedOperationException();
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            this.chain = chain;
            tm.checkServerTrusted(chain, authType);
        }
    }
}
