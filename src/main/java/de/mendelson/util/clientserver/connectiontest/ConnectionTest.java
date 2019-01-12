//$Header: /as2/de/mendelson/util/clientserver/connectiontest/ConnectionTest.java 8     10.12.18 12:46 Heller $
package de.mendelson.util.clientserver.connectiontest;

import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.security.KeyStoreUtil;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.util.security.cert.KeystoreCertificate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.security.cert.X509Certificate;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Performs a connection test and returns information about the results
 *
 * @author S.Heller
 * @version $Revision: 8 $
 */
public class ConnectionTest {

    public static final int CONNECTION_TEST_OFTP2 = 1;
    public static final int CONNECTION_TEST_AS2 = 2;

    private Logger logger = null;
    private InetSocketAddress remoteAddress = null;
    private MecResourceBundle rb = null;
    private int testType = -1;
    private ConnectionTestProxy proxy = null;

    public ConnectionTest(Logger logger, final int TEST_TYPE) {
        this.logger = logger;
        this.testType = TEST_TYPE;
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleConnectionTest.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    private String getLogTag() {
        return ("[" + this.rb.getResourceString("tag", this.remoteAddress.toString()) + "] ");
    }

    /**
     * Let the user examine the contents of a certificate file from a SSL
     * connection.
     *
     * @return True if the user was able to examine the certificate, false
     * otherwise
     */
    public ConnectionTestResult checkConnectionPlain(String host, int port, long timeout) {
        this.remoteAddress = new InetSocketAddress(host, port);
        ConnectionTestResult testResult = new ConnectionTestResult(this.remoteAddress, false);
        Socket socket = null;
        try {
            try {
                Proxy proxy = null;
                if (this.proxy == null) {
                    //direct socket connection
                    socket = new Socket();
                    this.logger.info(this.getLogTag() + this.rb.getResourceString("test.connection.direct"));
                } else {
                    proxy = this.proxy.asProxy();
                    //proxy connected socket
                    socket = new Socket(proxy);
                    if (this.proxy.usesAuthentication()) {
                        this.logger.info(this.getLogTag() + this.rb.getResourceString("test.connection.proxy.auth",
                                new Object[]{
                                    this.proxy.getAddress() + ":" + this.proxy.getPort(),
                                    this.proxy.getUserName()
                                }));
                    } else {
                        this.logger.info(this.getLogTag() + this.rb.getResourceString("test.connection.proxy.noauth",
                                this.proxy.getAddress() + ":" + this.proxy.getPort()));
                    }
                }

                this.logger.info(this.getLogTag() + this.rb.getResourceString("test.start.plain", this.remoteAddress.toString()));
                this.logger.info(this.getLogTag() + this.rb.getResourceString("timeout.set", String.valueOf(timeout)));
                socket.setSoTimeout((int) timeout);
                socket.connect(this.remoteAddress, (int) timeout);
                testResult.setConnectionIsPossible(true);
                this.logger.config(this.getLogTag() + this.rb.getResourceString("connection.success", this.remoteAddress.toString()));
            } catch (Exception exception) {
                testResult.setException(exception);
                testResult.setConnectionIsPossible(false);
                 this.logger.severe(this.getLogTag() + this.rb.getResourceString("result.exception",
                    "[" + exception.getClass().getSimpleName() + "]: " + exception.getMessage()));                
                this.logger.severe(this.getLogTag() + this.rb.getResourceString("connection.problem", this.remoteAddress.toString()));
                return (testResult);
            }
            if (this.testType == CONNECTION_TEST_OFTP2) {
                /* read SSRM */
                BufferedReader in = null;
                String foundSSRM = "";
                try {
                    this.logger.info(this.getLogTag() + this.rb.getResourceString("check.for.service.oftp2"));
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    foundSSRM = in.readLine();
                } catch (Exception e) {
                    this.logger.severe(this.getLogTag() + this.rb.getResourceString("exception.occured.oftpservice", new Object[]{
                        e.getClass().getSimpleName(), e.getMessage()
                    }));
                    testResult.setException(e);
                    return (testResult);
                } finally {
                    in.close();
                }
                OFTP2SSRM ssrm = new OFTP2SSRM();
                StringBuilder expectedSSRM = new StringBuilder();
                expectedSSRM.append(ssrm.getIndicator());
                expectedSSRM.append(new String(ssrm.getField(OFTP2SSRM.SSRMMSG).getDefaultValue()));
                this.logger.info(this.getLogTag() + this.rb.getResourceString("remote.service.identification", foundSSRM));
                if (foundSSRM != null && foundSSRM.endsWith(expectedSSRM.toString())) {
                    this.logger.config(this.getLogTag() + this.rb.getResourceString("service.found.success", remoteAddress));
                    testResult.setOftpServiceFound(true);
                } else {
                    testResult.setOftpServiceFound(false);
                    this.logger.severe(this.getLogTag() + this.rb.getResourceString("service.found.failure", remoteAddress));
                }
            }
        } catch (Exception e) {
            this.logger.severe(this.getLogTag() + this.rb.getResourceString("exception.occured", new Object[]{
                e.getClass().getName(), e.getMessage()
            }));
            testResult.setException(e);
            return (testResult);
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    //nop
                }
            }
        }
        return (testResult);
    }

    /**
     * Let the user examine the contents of a certificate file from a SSL
     * connection.
     *
     * @param certificateManagerSSL The certificate manager to check if the
     * remote certificate has been already imported in the local SSL keystore,
     * might be null - then no test is performed
     *
     * @return True if the user was able to examine the certificate, false
     * otherwise
     */
    public ConnectionTestResult checkConnectionSSL(String host, int port, long timeout, CertificateManager certificateManagerSSL) {
        this.remoteAddress = new InetSocketAddress(host, port);
        ConnectionTestResult testResult = new ConnectionTestResult(this.remoteAddress, true);
        SSLSocket sslSocket = null;
        SSLSession sslSession = null;
        try {
            SSLSocketFactory socketFactory;
            SSLContext sslContext = SSLContext.getInstance("TLS");
            X509TrustManager[] trustManagerTrustAll = {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] certChain, String auth) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certChain, String auth) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }};
            sslContext.init(null, trustManagerTrustAll, null);
            socketFactory = sslContext.getSocketFactory();
            if (this.proxy == null) {
                //create a unconnected socket            
                sslSocket = (SSLSocket) socketFactory.createSocket();
                this.logger.info(this.getLogTag() + this.rb.getResourceString("test.connection.direct"));
            } else {
                Proxy proxy = this.proxy.asProxy();
                //create a unconnected socket            
                sslSocket = (SSLSocket) socketFactory.createSocket(new Socket(proxy), this.proxy.getAddress(), this.proxy.getPort(), true);
                if (this.proxy.usesAuthentication()) {
                    this.logger.info(this.getLogTag() + this.rb.getResourceString("test.connection.proxy.auth",
                                new Object[]{
                                    this.proxy.getAddress() + ":" + this.proxy.getPort(),
                                    this.proxy.getUserName()
                                }));
                } else {
                    this.logger.info(this.getLogTag() + this.rb.getResourceString("test.connection.proxy.noauth",
                                this.proxy.getAddress() + ":" + this.proxy.getPort()));
                }
            }
            sslSocket.setSoTimeout((int) timeout);
            try {
                this.logger.info(this.getLogTag() + this.rb.getResourceString("test.start.ssl", this.remoteAddress.toString()));
                this.logger.info(this.getLogTag() + this.rb.getResourceString("timeout.set", String.valueOf(timeout)));
                sslSocket.connect(this.remoteAddress, (int) timeout);
            } catch (Exception ex) {
                testResult.setException(ex);
                testResult.setConnectionIsPossible(false);
                this.logger.severe(this.getLogTag() + this.rb.getResourceString("connection.problem", this.remoteAddress.toString()));
                return (testResult);
            }
            //IP connection works
            this.logger.config(this.getLogTag() + this.rb.getResourceString("connection.success", this.remoteAddress.toString()));
            testResult.setConnectionIsPossible(true);
            sslSession = sslSocket.getSession();
            String protocol = sslSession.getProtocol();
            String usedCipherSuite = sslSession.getCipherSuite();
            String[] supportedCipherSuites = sslSocket.getSupportedCipherSuites();
            String[] enabledCipherSuites = sslSocket.getEnabledCipherSuites();
            testResult.setProtocol(protocol);
            testResult.setUsedCipherSuite(usedCipherSuite);
            testResult.setSupportedCipherSuites(supportedCipherSuites);
            testResult.setEnabledCipherSuites(enabledCipherSuites);
            if (protocol == null || (!protocol.contains("TLS") && !protocol.contains("SSL"))) {
                String errorMessage = this.rb.getResourceString("wrong.protocol", protocol);
                this.logger.severe(this.getLogTag() + errorMessage);
                this.logger.warning(this.getLogTag() + this.rb.getResourceString("wrong.protocol.hint"));
                Exception e = new Exception();
                testResult.setException(e);
                return (testResult);
            }
            this.logger.info(this.getLogTag() + this.rb.getResourceString("protocol.information",
                    new Object[]{protocol, sslSession.getCipherSuite()}));
            this.logger.info(this.getLogTag() + this.rb.getResourceString("requesting.certificates"));
            X509Certificate[] certs = (X509Certificate[]) sslSession.getPeerCertificates();
            testResult.setFoundCertificates(certs);
            this.logger.config(this.getLogTag() + this.rb.getResourceString("certificates.found", String.valueOf(certs.length)));
            //order the certificates if this is possible
            KeyStoreUtil keystoreUtil = new KeyStoreUtil();
            certs = keystoreUtil.orderX509CertChain(certs);
            for (int i = 0; i < certs.length; i++) {
                KeystoreCertificate keystoreCert = new KeystoreCertificate();
                keystoreCert.setCertificate(certs[i]);
                StringBuilder certDescription = new StringBuilder();
                certDescription.append(keystoreCert.getSubjectDN());
                if (keystoreCert.isCACertificate()) {
                    certDescription.append(" (" + this.rb.getResourceString("certificate.ca") + ")");
                } else {
                    certDescription.append(" (" + this.rb.getResourceString("certificate.enduser") + ")");
                }
                if (keystoreCert.isSelfSigned()) {
                    certDescription.append(" (" + this.rb.getResourceString("certificate.selfsigned") + ")");
                }
                this.logger.config(this.getLogTag() + this.rb.getResourceString("certificates.found.details",
                        new Object[]{
                            String.valueOf(i + 1),
                            String.valueOf(certs.length),
                            certDescription.toString()
                        }));
                //check if the request certificate is already in the local SSL store
                if (certificateManagerSSL != null) {
                    String foundFingerPrintSHA1 = keystoreCert.getFingerPrintSHA1();
                    String localAlias = certificateManagerSSL.getAliasByFingerprint(foundFingerPrintSHA1);
                    if (localAlias == null) {
                        this.logger.warning(this.getLogTag() + this.rb.getResourceString("certificate.does.not.exist.local"));
                    } else {
                        this.logger.config(this.getLogTag() + this.rb.getResourceString("certificate.does.exist.local",
                                localAlias));
                    }
                }
            }
            if (this.testType == CONNECTION_TEST_OFTP2) {
                this.logger.info(this.getLogTag() + this.rb.getResourceString("check.for.service.oftp2"));
                /* read SSRM */
                BufferedReader in = null;
                String foundSSRM = "";
                try {
                    in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
                    foundSSRM = in.readLine();
                } catch (Exception e) {
                    this.logger.severe(this.getLogTag() + this.rb.getResourceString("exception.occured.oftpservice", new Object[]{
                        e.getClass().getSimpleName(), e.getMessage()
                    }));
                    testResult.setException(e);
                    return (testResult);
                } finally {
                    in.close();
                }
                OFTP2SSRM ssrm = new OFTP2SSRM();
                StringBuilder expectedSSRM = new StringBuilder();
                expectedSSRM.append(ssrm.getIndicator());
                expectedSSRM.append(new String(ssrm.getField(OFTP2SSRM.SSRMMSG).getDefaultValue()));
                this.logger.info(this.getLogTag() + this.rb.getResourceString("remote.service.identification", foundSSRM));
                if (foundSSRM != null && foundSSRM.endsWith(expectedSSRM.toString())) {
                    this.logger.config(this.getLogTag() + this.rb.getResourceString("service.found.success", remoteAddress));
                    testResult.setOftpServiceFound(true);
                } else {
                    testResult.setOftpServiceFound(false);
                    this.logger.severe(this.getLogTag() + this.rb.getResourceString("service.found.failure", remoteAddress));
                }
            }
        } catch (Exception e) {
            this.logger.severe(this.getLogTag() + this.rb.getResourceString("exception.occured", new Object[]{
                e.getClass().getName(), e.getMessage()
            }));
            testResult.setException(e);
            return (testResult);
        } finally {
            if (sslSocket != null && !sslSocket.isClosed()) {
                try {
                    sslSocket.close();
                } catch (IOException e) {
                    //nop
                }
            }
            if (sslSession != null) {
                sslSession.invalidate();
            }
        }
        return (testResult);
    }

    /**
     * @return the proxy
     */
    public ConnectionTestProxy getProxy() {
        return proxy;
    }

    /**
     * @param proxy the proxy to set
     */
    public void setProxy(ConnectionTestProxy proxy) {
        this.proxy = proxy;
    }

}
