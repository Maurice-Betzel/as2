//$Header: /as2/de/mendelson/util/modulelock/ModuleLock.java 4     1.11.18 10:36 Heller $
package de.mendelson.util.modulelock;

import de.mendelson.util.MecResourceBundle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Handles the locks of modules. Some modules are opened exclusive by a client -
 * all other clients should have just a read-only view.
 *
 * @author S.Heller
 * @version $Revision: 4 $
 */
public class ModuleLock {

    public static final String MODULE_SSL_KEYSTORE = "SSL keystore";
    public static final String MODULE_ENCSIGN_KEYSTORE = "ENC/SIGN keystore";
    public static final String MODULE_PARTNER = "Partner management";
    public static final String MODULE_SERVER_SETTINGS = "Server settings";

    private static Logger logger = Logger.getAnonymousLogger();

    public ModuleLock() {
    }

    /**
     * Tries to set the lock for a module and returns the lock keeper. If the
     * lock is set successful the lock keeper is the passed client information.
     * if no lock could be set just null is returned
     */
    public synchronized static LockClientInformation setLock(String moduleName, LockClientInformation requestingClient, Connection runtimeConnection) {
        LockClientInformation currentLockKeeper = getCurrentLockKeeper(moduleName, runtimeConnection);
        try {
            if (currentLockKeeper != null) {
                if (currentLockKeeper.equals(requestingClient)) {
                    //perform a refresh, the requesting client is the lock keeper
                    _refreshLock(moduleName, requestingClient, runtimeConnection);
                    return (requestingClient);
                } else {
                    //another client has the lock: just return its information
                    return (currentLockKeeper);
                }
            } else {
                //noone has the lock on this module: set it to the requesting client
                _setLock(moduleName, requestingClient, runtimeConnection);
                return (requestingClient);
            }
        } catch (Exception e) {
        }
        return (null);
    }

    /**
     * Tries to refresh the lock for a module. Returns the lockkeeper anyway. If
     * no lock is set this is null
     */
    public synchronized static LockClientInformation refreshLock(String moduleName, LockClientInformation requestingClient, Connection runtimeConnection) {
        LockClientInformation currentLockKeeper = getCurrentLockKeeper(moduleName, runtimeConnection);
        try {
            if (currentLockKeeper != null) {
                if (currentLockKeeper.equals(requestingClient)) {
                    //perform a refresh, the requesting client is the lock keeper
                    _refreshLock(moduleName, requestingClient, runtimeConnection);
                    return (requestingClient);
                }
            }
        } catch (Exception e) {
        }
        //this is an error: There is no lock on this module but a client tried to refresh it.
        return (null);
    }

    /**
     * Releases all locks, should be executed on server start
     */
    public synchronized static void releaseAllLocks(Connection runtimeConnection) {
        _deleteAllLocks(runtimeConnection, 0L);
    }

    /**
     * Releases all locks, should be executed on server start
     */
    public synchronized static void releaseAllLocksOlderThan(Connection runtimeConnection, long ageInms) {
        _deleteAllLocks(runtimeConnection, ageInms);
    }

    /**
     * Tries to release the lock for a module
     */
    public synchronized static void releaseLock(String moduleName, LockClientInformation clientInformation, Connection runtimeConnection) {
        _deleteLock(moduleName, runtimeConnection);
    }

    private static void _deleteAllLocks(Connection runtimeConnection, long ageInms) {
        PreparedStatement statement = null;
        try {
            if (ageInms > 0) {
                statement = runtimeConnection.prepareStatement("DELETE FROM modulelock WHERE refreshlockmillies<?");
                long absoluteAge = System.currentTimeMillis() - ageInms;
                statement.setLong(1, absoluteAge);
            } else {
                statement = runtimeConnection.prepareStatement("DELETE FROM modulelock");
            }
            statement.executeUpdate();
        } catch (Exception e) {
            logger.severe("ModuleLock._deleteAllLocks: " + e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    //NOP
                }
            }
        }
    }

    private static void _deleteLock(String moduleName, Connection runtimeConnection) {
        PreparedStatement statement = null;
        try {
            statement = runtimeConnection.prepareStatement("DELETE FROM modulelock WHERE modulename=?");
            statement.setString(1, moduleName);
            statement.executeUpdate();
        } catch (Exception e) {
            logger.severe("ModuleLock._deleteLock: " + e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private static void _setLock(String moduleName, LockClientInformation clientInformation, Connection runtimeConnection) throws Exception {
        PreparedStatement statement = null;
        try {
            statement = runtimeConnection.prepareStatement(
                    "INSERT INTO modulelock(modulename,startlockmillis,refreshlockmillies,clientip,clientid,username,clientpid)"
                    + "VALUES(?,?,?,?,?,?,?)");
            statement.setString(1, moduleName);
            long lockTime = System.currentTimeMillis();
            statement.setLong(2, lockTime);
            statement.setLong(3, lockTime);
            statement.setString(4, clientInformation.getClientIP());
            statement.setString(5, clientInformation.getUniqueid());
            statement.setString(6, clientInformation.getUsername());
            statement.setString(7, clientInformation.getPid());
            statement.execute();
        } catch (Exception e) {
            logger.severe("ModuleLock._setLock: " + e.getMessage());
            throw e;
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private static void _refreshLock(String moduleName, LockClientInformation clientInformation, Connection runtimeConnection) throws Exception {
        PreparedStatement statement = null;
        try {
            statement = runtimeConnection.prepareStatement(
                    "UPDATE modulelock SET refreshlockmillies=? WHERE modulename=? AND clientid=?");
            statement.setLong(1, System.currentTimeMillis());
            statement.setString(2, moduleName);
            statement.setString(3, clientInformation.getUniqueid());
            statement.execute();
        } catch (Exception e) {
            logger.severe("ModuleLock._refreshLock: " + e.getMessage());
            throw e;
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * Checks for an existing lock on the requested module and return the lock
     * keeper if there is one of null if there is none
     */
    public static LockClientInformation getCurrentLockKeeper(String moduleName, Connection runtimeConnection) {
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            statement = runtimeConnection.prepareStatement("SELECT * FROM modulelock WHERE modulename=?");
            statement.setString(1, moduleName);
            result = statement.executeQuery();
            if (result.next()) {
                String username = result.getString("username");
                String clientIP = result.getString("clientip");
                String uniqueId = result.getString("clientid");
                String pid = result.getString( "clientpid" );
                LockClientInformation clientInfo = new LockClientInformation(username, clientIP, uniqueId, pid);
                return (clientInfo);
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    //nop
                }
            }
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    //nop
                }
            }
        }
        return (null);
    }

    
    public static void displayDialogModuleLocked(JFrame parent, LockClientInformation lockKeeper, String nonLocalizedModuleName) {
        MecResourceBundle rb;
        //Load default resourcebundle
        try {
            rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleModuleLock.class.getName());
        } //load up resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        String text = rb.getResourceString("configuration.locked.otherclient",
                new Object[]{
                    rb.getResourceString( nonLocalizedModuleName),
                    lockKeeper.getClientIP(),
                    lockKeeper.getUsername(),
                    lockKeeper.getPid()
                });
        JOptionPane.showMessageDialog(parent,
                text,
                rb.getResourceString("modifications.notallowed.message"),
                JOptionPane.ERROR_MESSAGE);
    }
    
}
