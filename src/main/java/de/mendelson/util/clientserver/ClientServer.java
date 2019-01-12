//$Header: /as2/de/mendelson/util/clientserver/ClientServer.java 22    4/06/18 10:56a Heller $
package de.mendelson.util.clientserver;

import de.mendelson.util.clientserver.codec.ClientServerCodecFactory;
import de.mendelson.util.clientserver.messages.ClientServerMessage;
import de.mendelson.util.security.BCCryptoHelper;
import de.mendelson.util.security.keygeneration.KeyGenerationResult;
import de.mendelson.util.security.keygeneration.KeyGenerationValues;
import de.mendelson.util.security.keygeneration.KeyGenerator;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.UnorderedThreadPoolExecutor;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Server root for the mendelson client/server architecture
 *
 * @author S.Heller
 * @version $Revision: 22 $
 */
public class ClientServer {

    private long startTime = 0;
    private Logger logger;
    private ClientServerSessionHandler sessionHandler = null;
    private int port = 0;
    private String productName = "";

    /**
     * Creates a new instance of Server
     */
    public ClientServer(Logger logger, int port) {
        this.port = port;
        this.logger = logger;
    }

    public void setSessionHandler(ClientServerSessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }

    public void setClientServerPort(int port) {
        this.port = port;
    }

    /**
     * Returns the start time of the server
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Sends a message object to all connected clients
     */
    public void broadcastToClients(ClientServerMessage message) {
        if (this.sessionHandler != null) {
            sessionHandler.broadcast(message);
        }
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Finally starts the server
     */
    public void start() throws Exception {
        this.logger.log(Level.INFO, "Starting " + this.productName + " client-server interface, listening on port " + this.port);
        if (this.sessionHandler != null) {
            this.sessionHandler.setProductName(this.productName);
        } else {
            this.logger.log(Level.WARNING, "No session handler assigned to the client server!");
        }
        NioSocketAcceptor acceptor = new NioSocketAcceptor();
        //add SSL support
        SslFilter sslFilter = new SslFilter(this.createSSLContext());
        //If client authentication is disabled the client certificate must not be in the servers keystore
        sslFilter.setNeedClientAuth(false);
        sslFilter.setUseClientMode(false);
        acceptor.getFilterChain().addFirst("TLS", sslFilter);
        //add CPU bound tasks first
        acceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new ClientServerCodecFactory(this.logger, null)));
        //log client-server communication
        //acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        //see https://issues.apache.org/jira/browse/DIRMINA-682?page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel
        //..and now set up the thread pool
        acceptor.getFilterChain().addLast("executor", new ExecutorFilter(new UnorderedThreadPoolExecutor()));
        if (this.sessionHandler != null) {
            acceptor.setHandler(this.sessionHandler);
        }
        //finally bind the protocol handler to the port
        acceptor.bind(new InetSocketAddress(this.port));
        this.logger.log(Level.INFO, this.productName + " client-server interface started.");
        this.startTime = new Date().getTime();
    }

    /**
     * Instanciate a SSL context. This creates an SSL key on the server and uses
     * it for the SSL secured client-server communication. The SSL (TSLv1)
     * between client and server only delivers weak security as the client
     * trusts any key from the server (client and server certificates are not
     * exchanged using an other communication channel, there is no additional
     * shared secred between client and server) - Please be aware that this SSL
     * implementation is not safe against a man in the middle attack. Anyway a
     * man in the middle attack is not an easy attempt in this case.
     */
    private SSLContext createSSLContext() throws Exception {
        BCCryptoHelper helper = new BCCryptoHelper();
        SSLContext ctx = SSLContext.getInstance("TLSv1");
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        KeyStore keystore = helper.createKeyStoreInstance(BCCryptoHelper.KEYSTORE_PKCS12);
        //initialize keystore
        keystore.load(null, "dummy".toCharArray());
        KeyGenerationResult result = this.generateSSLKey();
        keystore.setKeyEntry("key", result.getKeyPair().getPrivate(), "dummy".toCharArray(), new Certificate[]{result.getCertificate()});
        keyManagerFactory.init(keystore, "dummy".toCharArray());
        KeyManager[] defaultKeymanager = keyManagerFactory.getKeyManagers();
        ctx.init(defaultKeymanager, null, SecureRandom.getInstance("SHA1PRNG"));
        return ctx;
    }

    private KeyGenerationResult generateSSLKey() throws Exception {
        KeyGenerator generator = new KeyGenerator();
        KeyGenerationValues parameter = new KeyGenerationValues();
        //generating a longer key takes some time. In the current test (09/2012): 1024bit 140ms, 2048bit 1250ms
        parameter.setKeySize(1024);
        parameter.setKeyType(KeyGenerationValues.KEYTYPE_RSA);
        //one shutdown every 10 years should be ok
        parameter.setKeyValidInDays(365 * 10);
        parameter.setSignatureAlgorithm(KeyGenerationValues.SIGNATUREALGORITHM_SHA1_WITH_RSA_OID);
        parameter.setOrganisationName(this.productName);
        parameter.setOrganisationUnit("Server");
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            parameter.setCommonName(hostName);
        } catch (Throwable e) {
            //ignore, no entry found in hosts file
        }
        parameter.setEmailAddress("nomail@nomail.to");
        parameter.setLocalityName(Locale.getDefault().getDisplayLanguage());
        //add SSL extended key usage
        KeyPurposeId[] extKeyUsage = new KeyPurposeId[2];
        extKeyUsage[0] = KeyPurposeId.id_kp_serverAuth;
        extKeyUsage[1] = KeyPurposeId.id_kp_clientAuth;
        parameter.setExtendedKeyExtension(new ExtendedKeyUsage(extKeyUsage));
        return (generator.generateKeyPair(parameter));
    }

    /**Returns the current sessions on this server*/
    public List<IoSession> getSessions() {
        if (this.sessionHandler != null) {
            return (this.sessionHandler.getSessions());
        } else {
            return (new ArrayList<IoSession>());
        }
    }

}
