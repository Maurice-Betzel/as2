//$Header: /mec_as2/de/mendelson/comm/as2/statistic/QuotaAccessDB.java 7     8.01.19 9:48 Heller $
package de.mendelson.comm.as2.statistic;

import de.mendelson.comm.as2.partner.Partner;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * Dummy class, not used
 * @author S.Heller
 * @version $Revision: 7 $
 */
public class QuotaAccessDB {


    /** Creates new message I/O log and connects to localhost
     *@param host host to connect to
     */
    public QuotaAccessDB(Connection configConnection, Connection runtimeConnection){
    }

    /**Resets a counter/quota entry in the db for a localstation/partner combination*/
    public void resetCounter(String localStationId, String partnerId) {
    }


    public static synchronized void incSentMessages(Connection configConnection, Connection runtimeConnection, Partner localStation, Partner partner, int state, String messageId) {
    }

    public static synchronized void incReceivedMessages(Connection configConnection, Connection runtimeConnection, Partner localStation, Partner partner, int state, String messageId) {
    }

    public static synchronized void incSentMessages(Connection configConnection, Connection runtimeConnection, String localStationId, String partnerId, int state, String messageId) {
    }

    public static synchronized void incReceivedMessages(Connection configConnection, Connection runtimeConnection, String localStationId, String partnerId, int state, String messageId) {
    }

    public StatisticOverviewEntry getStatisticOverview(String localStationId, String partnerId){
        return( new StatisticOverviewEntry());
    }
    
    public List<StatisticOverviewEntry> getStatisticOverview(String localStationId) {
        return( new ArrayList<StatisticOverviewEntry>());
    }    
    
    /**Closes the internal database connection.*/
    public void close() {
    }
}
