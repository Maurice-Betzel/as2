//$Header: /as4/de/mendelson/util/httpconfig/server/HTTPServerConfigInfo.java 6     9.10.18 12:29 Heller $
package de.mendelson.util.httpconfig.server;

import java.net.InetAddress;
import java.net.URL;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.net.ssl.SSLServerSocketFactory;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Stores information about the current HTTP server configuration
 *
 * @author S.Heller
 * @version $Revision: 6 $
 */
public class HTTPServerConfigInfo {
   
    private List<Listener> listenerList = new ArrayList<Listener>();
    private List<String> excludedProtocols = new ArrayList<String>();
    private List<String> possibleProtocols = new ArrayList<String>();
    private List<String> excludedCiphers = new ArrayList<String>();
    private List<String> possibleCiphers = new ArrayList<String>();
    private List<String> deployedWars = new ArrayList<String>();
    private String keystorePath = "";
    private boolean needClientAuthentication = false;
    private boolean embeddedHTTPServerStarted = false;
    private String receiptURLPath = "/as2/HttpReceiver";
    private String serverStatePath = "/as2/ServerState";
    private String jettyHTTPServerVersion = "unknown version";
    private Path httpServerConfigFile;
    private String javaVersion;
    private boolean sslEnabled = false;

    private HTTPServerConfigInfo() {
        this.httpServerConfigFile = Paths.get("./jetty9/etc/jetty.xml");        
        this.javaVersion = System.getProperty("java.version");
    }

    public void addDeployedWar(String warPath) {
        this.deployedWars.add(warPath);
    }

    public List<String> getDeployedWars() {
        List<String> tempList = new ArrayList<String>();
        tempList.addAll(this.deployedWars);
        return (tempList);
    }

    public void addExcludedProtocol(String protocol) {
        this.excludedProtocols.add(protocol);
    }

    public List<String> getExcludedProtocols() {
        List<String> tempList = new ArrayList<String>();
        tempList.addAll(this.excludedProtocols);
        return (tempList);
    }

    public void addPossibleProtocol(String protocol) {
        this.possibleProtocols.add(protocol);
    }

    public List<String> getPossibleProtocols() {
        List<String> tempList = new ArrayList<String>();
        tempList.addAll(this.possibleProtocols);
        return (tempList);
    }

    public void addPossibleCipher(String cipher) {
        this.possibleCiphers.add(cipher);
    }

    public List<String> getPossibleCipher() {
        List<String> tempList = new ArrayList<String>();
        tempList.addAll(this.possibleCiphers);
        return (tempList);
    }

    public void addExcludedCipher(String cipher) {
        this.excludedCiphers.add(cipher);
    }

    public List<String> getExcludedCipher() {
        List<String> tempList = new ArrayList<String>();
        tempList.addAll(this.excludedCiphers);
        return (tempList);
    }

    public void addListener(Listener listener) {
        this.listenerList.add(listener);
    }

    public List<Listener> getListener() {
        List<Listener> tempList = new ArrayList<Listener>();
        tempList.addAll(this.listenerList);
        return (tempList);
    }

    /**
     * @return the keystorePath
     */
    public String getKeystorePath() {
        return keystorePath;
    }

    /**
     * @param keystorePath the keystorePath to set
     */
    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    /**
     * @return the needClientAuthentication
     */
    public boolean needsClientAuthentication() {
        return needClientAuthentication;
    }

    /**
     * @param needClientAuthentication the needClientAuthentication to set
     */
    public void setNeedClientAuthentication(boolean needClientAuthentication) {
        this.needClientAuthentication = needClientAuthentication;
    }

    /**
     * @return the embeddedHTTPServerStarted
     */
    public boolean isEmbeddedHTTPServerStarted() {
        return embeddedHTTPServerStarted;
    }

    /**
     * @param embeddedHTTPServerStarted the embeddedHTTPServerStarted to set
     */
    public void setEmbeddedHTTPServerStarted(boolean embeddedHTTPServerStarted) {
        this.embeddedHTTPServerStarted = embeddedHTTPServerStarted;
    }

    /**
     * @return the serverStatePath
     */
    public String getServerStatePath() {
        return serverStatePath;
    }

    /**
     * @param serverStatePath the serverStatePath to set
     */
    public void setServerStatePath(String serverStatePath) {
        this.serverStatePath = serverStatePath;
    }

    public static final HTTPServerConfigInfo computeHTTPServerConfigInfo(Server jettyHTTPServerInstance, boolean startHTTPServer,
            String receiptURLPath, String serverStatePath) {
        HTTPServerConfigInfo httpServerConfigInfo = new HTTPServerConfigInfo();
        //try to find out the HTTP server version, look at it in the jetty server jar MANIFEST file
        try {
            Class serverClazz = jettyHTTPServerInstance.getClass();
            String serverClassName = serverClazz.getSimpleName() + ".class";
            String jettyServerJar = serverClazz.getResource(serverClassName).toString();
            //ensure that this is a jar
            if (jettyServerJar.startsWith("jar")) {
                String manifestPath = jettyServerJar.substring(0, jettyServerJar.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
                Manifest manifest = new Manifest(new URL(manifestPath).openStream());
                Attributes jarAttributes = manifest.getMainAttributes();
                String versionValue = jarAttributes.getValue("Implementation-Version");
                httpServerConfigInfo.setJettyHTTPServerVersion(versionValue);
            }
        } catch (Throwable dontcare) {
            //NOP
        }
        httpServerConfigInfo.setReceiptURLPath(receiptURLPath);
        httpServerConfigInfo.setServerStatePath(serverStatePath);
        if (jettyHTTPServerInstance != null) {
            try {
                //find out the deployed wars
                Handler[] childHandler = jettyHTTPServerInstance.getChildHandlers();
                for (Handler singleHandler : childHandler) {
                    //System.out.println(singleHandler.getClass().getName());
                    if (singleHandler instanceof WebAppContext) {
                        WebAppContext context = (WebAppContext) singleHandler;
                        String warFilePath = context.getWar();
                        httpServerConfigInfo.addDeployedWar(warFilePath);
                    }
                }
            } catch (Throwable e) {
                //ignore
            }
            httpServerConfigInfo.setEmbeddedHTTPServerStarted(startHTTPServer);
            try {
                boolean sslParameterCollected = false;
                boolean serverUsesSSLPort = false;
                Connector[] connectors = jettyHTTPServerInstance.getConnectors();
                for (Connector connector : connectors) {
                    HTTPServerConfigInfo.Listener listener = new HTTPServerConfigInfo.Listener();
                    if (connector.getTransport() instanceof ServerSocketChannel) {
                        ServerSocketChannel channel = (ServerSocketChannel) connector.getTransport();
                        InetAddress address = channel.socket().getInetAddress();
                        listener.setAdapter(address.getHostAddress());
                    }
                    if (connector instanceof ServerConnector) {
                        ServerConnector serverConnector = (ServerConnector) connector;
                        listener.setProtocol(serverConnector.getDefaultProtocol());
                        listener.setPort(serverConnector.getPort());
                        httpServerConfigInfo.addListener(listener);
                        if( listener.getProtocol() != null && listener.getProtocol().toLowerCase().contains("ssl")){
                            serverUsesSSLPort = true;
                        }
                        //collect the SLL parameter
                        Collection<ConnectionFactory> connectionFactories = serverConnector.getConnectionFactories();
                        for (ConnectionFactory connectionFactory : connectionFactories) {
                            if (!sslParameterCollected && (connectionFactory instanceof SslConnectionFactory)) {
                                sslParameterCollected = true;
                                SslConnectionFactory sslFactory = (SslConnectionFactory) connectionFactory;
                                SslContextFactory sslContextFactory = sslFactory.getSslContextFactory();
                                List<String> excludedCiphers = Arrays.asList(sslContextFactory.getExcludeCipherSuites());
                                for (String cipher : excludedCiphers) {
                                    httpServerConfigInfo.addExcludedCipher(cipher);
                                }
                                List<String> excludedProtocols = Arrays.asList(sslContextFactory.getExcludeProtocols());
                                for (String protocol : excludedProtocols) {
                                    httpServerConfigInfo.addExcludedProtocol(protocol);
                                }
                                String keystorePath = sslContextFactory.getKeyStorePath();
                                httpServerConfigInfo.setKeystorePath(keystorePath);
                                boolean needClientAuth = sslContextFactory.getNeedClientAuth();
                                httpServerConfigInfo.setNeedClientAuthentication(needClientAuth);
                                SSLServerSocketFactory sslServerSocketFactory = sslContextFactory.getSslContext().getServerSocketFactory();
                                List<String> supportedCipherSuiteList = Arrays.asList(sslServerSocketFactory.getSupportedCipherSuites());
                                //find out which cipher suites are possible and remove the disabled ciphers
                                List<String> includedCipherSuites = new ArrayList<String>();
                                for (String cipher : supportedCipherSuiteList) {
                                    if (!excludedCiphers.contains(cipher)) {
                                        includedCipherSuites.add(cipher);
                                    }
                                }
                                for (String cipher : includedCipherSuites) {
                                    httpServerConfigInfo.addPossibleCipher(cipher);
                                }
                                String[] protocols = sslContextFactory.getSslContext().getDefaultSSLParameters().getProtocols();
                                for (String protocol : protocols) {
                                    httpServerConfigInfo.addPossibleProtocol(protocol);
                                }
                            }
                        }
                    }
                }
                httpServerConfigInfo.setSSLEnabled(serverUsesSSLPort);                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (httpServerConfigInfo);
    }

    /**
     * @return the jettyHTTPServerVersion
     */
    public String getJettyHTTPServerVersion() {
        return jettyHTTPServerVersion;
    }

    /**
     * @param jettyHTTPServerVersion the jettyHTTPServerVersion to set
     */
    public void setJettyHTTPServerVersion(String jettyHTTPServerVersion) {
        this.jettyHTTPServerVersion = jettyHTTPServerVersion;
    }

    /**
     * @return the httpServerConfigFile
     */
    public Path getHTTPServerConfigFile() {
        return httpServerConfigFile;
    }

    /**
     * @return the receiptURLPath
     */
    public String getReceiptURLPath() {
        return receiptURLPath;
    }

    /**
     * @param receiptURLPath the receiptURLPath to set
     */
    public void setReceiptURLPath(String receiptURLPath) {
        this.receiptURLPath = receiptURLPath;
    }

     /**
     * @return the javaVersion
     */
    public String getJavaVersion() {
        return this.javaVersion;
    }

    /**
     * @return the sslEnabled
     */
    public boolean isSSLEnabled() {
        return sslEnabled;
    }

    /**
     * @param sslEnabled the sslEnabled to set
     */
    public void setSSLEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public static class Listener {

        private String protocol = null;
        private int port = -1;
        private String adapter = null;

        public Listener() {
        }

        /**
         * @return the adapter
         */
        public String getAdapter() {
            return adapter;
        }

        /**
         * @param adapter the adapter to set
         */
        public void setAdapter(String adapter) {
            this.adapter = adapter;
        }

        /**
         * @return the protocol
         */
        public String getProtocol() {
            return protocol;
        }

        /**
         * @param protocol the protocol to set
         */
        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        /**
         * @return the port
         */
        public int getPort() {
            return port;
        }

        /**
         * @param port the port to set
         */
        public void setPort(int port) {
            this.port = port;
        }
    }
    
}
