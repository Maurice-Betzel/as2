//$Header: /as2/de/mendelson/comm/as2/sendorder/SendOrderAccessDB.java 13    7.11.18 17:14 Heller $
package de.mendelson.comm.as2.sendorder;

import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.systemevents.SystemEvent;
import de.mendelson.util.systemevents.SystemEventManagerImplAS2;
import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software. Other product
 * and brand names are trademarks of their respective owners.
 */
/**
 * Accesses the queue for the internal send orders
 *
 * @author S.Heller
 * @version $Revision: 13 $
 */
public class SendOrderAccessDB {

    /**
     * Connection to the database
     */
    private Connection runtimeConnection;
    private Connection configConnection;
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);

    /**
     * Creates new message I/O log and connects to localhost
     *
     * @param host host to connect to
     */
    public SendOrderAccessDB(Connection configConnection, Connection runtimeConnection) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
    }

    public void delete(int dbId) {
        if (dbId == -1) {
            return;
        }
        PreparedStatement statement = null;
        try {
            statement = this.runtimeConnection.prepareStatement("DELETE FROM sendorder WHERE id=?");
            statement.setInt(1, dbId);
            statement.executeUpdate();
        } catch (Exception e) {
            this.logger.severe("SendOrderAccessDB.delete: " + e.getMessage());
            SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY, statement);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
                }
            }
        }
    }

    /**
     * Reschedules an existing order
     */
    public void rescheduleOrder(SendOrder order, long nextExecutionTime) {
        PreparedStatement statement = null;
        try {
            statement = this.runtimeConnection.prepareStatement(
                    "UPDATE sendorder SET nextexecutiontime=?,sendorder=?,orderstate=? WHERE id=?");
            statement.setLong(1, nextExecutionTime);
            statement.setObject(2, order);
            statement.setInt(3, SendOrder.STATE_WAITING);
            //condition
            statement.setInt(4, order.getDbId());
            statement.executeUpdate();
        } catch (Exception e) {
            this.logger.severe("SendOrderAccessDB.rescheduleOrder: " + e.getMessage());
            SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY, statement);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
                }
            }
        }
    }

    public void add(SendOrder order) {
        PreparedStatement statement = null;
        try {
            statement = this.runtimeConnection.prepareStatement(
                    "INSERT INTO sendorder(scheduletime,nextexecutiontime,sendorder,orderstate)VALUES(?,?,?,?)");
            statement.setLong(1, System.currentTimeMillis());
            //execute as soon as possible
            statement.setLong(2, System.currentTimeMillis());
            statement.setObject(3, order);
            statement.setInt(4, SendOrder.STATE_WAITING);
            statement.executeUpdate();
        } catch (Exception e) {
            this.logger.severe("SendOrderAccessDB.add: " + e.getMessage());
            SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY, statement);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
                }
            }
        }
    }

    /**
     * On a server start all the available transaction should be reset to the
     * wait state
     */
    public void resetAllToWaiting() {
        PreparedStatement statement = null;
        try {
            statement = this.runtimeConnection.prepareStatement(
                    "UPDATE sendorder SET orderstate=?");
            statement.setInt(1, SendOrder.STATE_WAITING);
            statement.executeUpdate();
        } catch (Exception e) {
            this.logger.severe("SendOrderAccessDB.resetAllToWait: " + e.getMessage());
            SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY, statement);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
                }
            }
        }
    }

    /**
     * Sets a new state to a send order
     */
    private void setState(int id, int orderState) {
        PreparedStatement statement = null;
        try {
            statement = this.runtimeConnection.prepareStatement(
                    "UPDATE sendorder SET orderstate=? WHERE id=?");
            statement.setInt(1, orderState);
            statement.setLong(2, id);
            statement.executeUpdate();
        } catch (Exception e) {
            this.logger.severe("SendOrderAccessDB.setState: " + e.getMessage());
            SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY, statement);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
                }
            }
        }
    }

    /**
     * Returns the next n scheduled orders or an empty list if none exists
     */
    public List<SendOrder> getNext(int maxCount) {
        List<SendOrder> sendOrderList = new ArrayList<SendOrder>();
        PreparedStatement statement = null;
        ResultSet result = null;
        int count = 0;
        try {
            statement = this.runtimeConnection.prepareStatement(
                    "SELECT * FROM sendorder WHERE orderstate=? AND nextexecutiontime <=? ORDER BY nextexecutiontime");
            statement.setInt(1, SendOrder.STATE_WAITING);
            statement.setLong(2, System.currentTimeMillis());
            result = statement.executeQuery();
            while (result.next() && count < maxCount) {
                Object orderObject = null;
                try {
                    orderObject = result.getObject("sendorder");
                } catch (Throwable invalidClassExeption) {
                    //nop
                }
                SendOrder order = null;
                if (orderObject != null) {
                    if (orderObject instanceof SendOrder) {
                        //this happens if you read the serialized object from HSQLDB
                        order = (SendOrder) orderObject;
                        int id = result.getInt("id");
                        order.setDbId(id);
                        //do not pick it up until it is processed
                        this.setState(id, SendOrder.STATE_PROCESSING);
                        sendOrderList.add(order);
                        count++;
                    }else if( orderObject instanceof byte[]){
                        //this happens if you read the serialized object from mySQL
                        ByteArrayInputStream memIn = new ByteArrayInputStream((byte[])orderObject);
                        ObjectInput in = new ObjectInputStream(memIn);
                        SendOrder sendOrderObj = (SendOrder) in.readObject();
                        int id = result.getInt("id");
                        sendOrderObj.setDbId(id);
                        //do not pick it up until it is processed
                        this.setState(id, SendOrder.STATE_PROCESSING);
                        sendOrderList.add(sendOrderObj);
                        count++;
                    }
                } else {
                    //delete the entry from the database, its from an older version or an invalid entry
                    int id = result.getInt("id");
                    this.delete(id);
                    break;
                }
            }
        } catch (Exception e) {
            this.logger.severe("SendOrderAccessDB.getNext: " + e.getMessage());
            SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY, statement);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("SendOrderAccessDB.getNext: " + e.getMessage());
                    SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
                }
            }
        }
        return (sendOrderList);
    }
}
