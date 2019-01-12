//$Header: /as2/de/mendelson/util/clientserver/connectiontest/ConnectionTestProxy.java 2     10.12.18 12:46 Heller $
package de.mendelson.util.clientserver.connectiontest;

import java.io.Serializable;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketAddress;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Stores the data for a SOCKS proxy. For a connection test the same proxy as
 * for the real connection should be used.
 *
 * @author S.Heller
 * @version $Revision: 2 $
 */
public class ConnectionTestProxy implements Serializable {

    /**
     * By default the standard telnet port is used
     */
    private int port = 23;
    private String address = null;
    private String userName = null;
    private String password = null;

    public ConnectionTestProxy() {
    }

    public Proxy asProxy() {
        SocketAddress socketAddress = new InetSocketAddress(this.getAddress(), this.getPort());
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, socketAddress);
        if( this.usesAuthentication()){
            System.setProperty("java.net.socks.username", this.getUserName());
            System.setProperty("java.net.socks.password", this.getPassword());
            Authenticator authenticator = new Authenticator(){                
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return (new PasswordAuthentication(getUserName(), getPassword().toCharArray()));
                }
            };
            Authenticator.setDefault(authenticator);
        }
        return( proxy );
    }

    public boolean usesAuthentication(){
        return( this.getUserName() != null && this.getPassword() != null);
    }
    
    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    

}
