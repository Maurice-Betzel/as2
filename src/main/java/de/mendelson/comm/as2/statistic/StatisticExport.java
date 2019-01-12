//$Header: /mec_as2/de/mendelson/comm/as2/statistic/StatisticExport.java 3     17.04.12 11:32 Heller $
package de.mendelson.comm.as2.statistic;

import de.mendelson.comm.as2.partner.Partner;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Exports the statistic data to a passed export file, format is XML
 * @author S.Heller
 * @version $Revision: 3 $
 */
public class StatisticExport {

    public StatisticExport(Connection configConnection, Connection runtimeConnection) {        
    }

    /**Exports the statistic data to a passed export file*/
    public void export(OutputStream streamout, long startDate, long endDate, long timestep, Partner localStation, Partner partner)
            throws SQLException, IOException {           
    }

    
}
