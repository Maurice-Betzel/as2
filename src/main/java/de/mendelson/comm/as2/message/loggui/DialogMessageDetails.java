//$Header: /as2/de/mendelson/comm/as2/message/loggui/DialogMessageDetails.java 43    14.12.18 13:12 Heller $
package de.mendelson.comm.as2.message.loggui;

import de.mendelson.comm.as2.AS2Exception;
import de.mendelson.comm.as2.log.LogEntry;
import de.mendelson.comm.as2.message.AS2Info;
import de.mendelson.comm.as2.message.AS2MDNInfo;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.AS2Payload;
import de.mendelson.comm.as2.message.clientserver.MessageDetailRequest;
import de.mendelson.comm.as2.message.clientserver.MessageDetailResponse;
import de.mendelson.comm.as2.message.clientserver.MessageLogRequest;
import de.mendelson.comm.as2.message.clientserver.MessageLogResponse;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.clientserver.PartnerListRequest;
import de.mendelson.comm.as2.partner.clientserver.PartnerListResponse;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.BaseClient;
import de.mendelson.util.log.IRCColors;
import de.mendelson.util.tables.JTableColumnResizer;
import java.awt.Color;
import java.awt.image.BaseMultiResolutionImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Dialog to show the details of a transaction
 *
 * @author S.Heller
 * @version $Revision: 43 $
 */
public class DialogMessageDetails extends JDialog implements ListSelectionListener {

    public static final ImageIcon ICON_LOCALSTATION = new ImageIcon(new BaseMultiResolutionImage(
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/localstation16x16.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/localstation24x24.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/localstation32x32.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/localstation48x48.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/localstation64x64.gif")).getImage()
    ));
    public static final ImageIcon ICON_REMOTEPARTNER = new ImageIcon(new BaseMultiResolutionImage(
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/singlepartner16x16.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/singlepartner24x24.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/singlepartner32x32.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/singlepartner48x48.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/singlepartner64x64.gif")).getImage()
    ));
    public static final ImageIcon ICON_ARROW_OUTBOUND = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/arrow32x16.gif"));
    public static final ImageIcon ICON_ARROW_INBOUND = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/arrow_in32x16.gif"));
    public static final ImageIcon ICON_PENDING = new ImageIcon(new BaseMultiResolutionImage(
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/state_pending16x16.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/state_pending24x24.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/state_pending32x32.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/state_pending48x48.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/state_pending64x64.gif")).getImage()            
    ));
    public static final ImageIcon ICON_STOPPED = new ImageIcon(new BaseMultiResolutionImage(
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/state_stopped16x16.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/state_stopped24x24.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/state_stopped32x32.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/state_stopped48x48.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/state_stopped64x64.gif")).getImage()            
    ));
    public static final ImageIcon ICON_FINISHED = new ImageIcon(new BaseMultiResolutionImage(
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/state_finished16x16.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/state_finished24x24.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/state_finished32x32.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/state_finished48x48.gif")).getImage(),
            new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/state_finished64x64.gif")).getImage()            
    ));
    public static final ImageIcon OVERVIEWSTATE_OUTBOUND_OK = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/comm_ok_outbound.png"));
    public static final ImageIcon OVERVIEWSTATE_OUTBOUND_FAILED = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/comm_failed_outbound.png"));
    public static final ImageIcon OVERVIEWSTATE_INBOUND_FAILED = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/comm_failed_inbound.png"));
    public static final ImageIcon OVERVIEWSTATE_INBOUND_OK = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/comm_ok_inbound.png"));
    public static final ImageIcon OVERVIEWSTATE_OUTBOUND_CONN_FAILED = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/comm_failed_outbound_conn.png"));
    public static final ImageIcon OVERVIEWSTATE_INBOUND_ANSWER_FAILED = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/comm_failed_inbound_answer.png"));
    public static final ImageIcon OVERVIEWSTATE_PENDING = new ImageIcon(TableModelMessageOverview.class.getResource("/de/mendelson/comm/as2/message/loggui/comm_pending.png"));
    
    private Logger logger = Logger.getLogger("de.mendelson.as2.client");
    /**
     * Localize the GUI
     */
    private MecResourceBundle rb = null;
    /**
     * Stores information about the message
     */
    private AS2MessageInfo overviewInfo = null;
    /**
     * Stores the payloads
     */
    private List<AS2Payload> payloadList = null;
    private JPanelFileDisplay jPanelFileDisplayRaw;
    private JPanelFileDisplay jPanelFileDisplayHeader;
    private JPanelFileDisplay[] jPanelFileDisplayPayload;
    private BaseClient baseClient;

    /**
     * Creates new form AboutDialog
     */
    public DialogMessageDetails(JFrame parent, BaseClient baseClient, AS2MessageInfo overviewInfo, 
            List<AS2Payload> payloadList) {
        super(parent, true);
        this.baseClient = baseClient;
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleMessageDetails.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle "
                    + e.getClassName() + " not found.");
        }
        this.jPanelFileDisplayRaw = new JPanelFileDisplay(baseClient);
        this.jPanelFileDisplayHeader = new JPanelFileDisplay(baseClient);
        this.payloadList = payloadList;
        this.overviewInfo = overviewInfo;
        if( overviewInfo.getMessageType() == AS2Message.MESSAGETYPE_CEM){
            this.setTitle(this.rb.getResourceString("title.cem"));
        }else{
            this.setTitle(this.rb.getResourceString("title"));
        }
        this.initComponents();
        this.populateTransactionOverviewPanel(overviewInfo);
        this.getRootPane().setDefaultButton(this.jButtonOk);
        this.jTableMessageDetails.getTableHeader().setReorderingAllowed(false);
        //first icon
        TableColumn column = this.jTableMessageDetails.getColumnModel().getColumn(0);
        column.setMaxWidth(20);
        column.setResizable(false);
        column = this.jTableMessageDetails.getColumnModel().getColumn(2);
        column.setMaxWidth(20);
        column.setResizable(false);
        this.displayData(overviewInfo);
        this.jTabbedPane.addTab(this.rb.getResourceString("message.raw.decrypted"), jPanelFileDisplayRaw);
        this.jTabbedPane.addTab(this.rb.getResourceString("message.header"), jPanelFileDisplayHeader);
        this.jPanelFileDisplayPayload = new JPanelFileDisplay[payloadList.size()];
        for (int i = 0; i < this.payloadList.size(); i++) {
            this.jPanelFileDisplayPayload[i] = new JPanelFileDisplay(baseClient);
            if (payloadList.size() == 1) {
                this.jTabbedPane.addTab(this.rb.getResourceString("message.payload"), this.jPanelFileDisplayPayload[0]);
            } else {
                this.jTabbedPane.addTab(this.rb.getResourceString("message.payload.multiple",
                        String.valueOf(i + 1)), this.jPanelFileDisplayPayload[i]);
            }
        }
        this.jTableMessageDetails.getSelectionModel().addListSelectionListener(this);
        this.displayProcessLog();
        JTableColumnResizer.adjustColumnWidthByContent(this.jTableMessageDetails);
        this.jTableMessageDetails.getSelectionModel().setSelectionInterval(0, 0);
    }

    /**
     * Displays overview information about the transaction
     */
    private void populateTransactionOverviewPanel(AS2MessageInfo overviewInfo) {
        String messageTypeStr = "AS2";
        if( overviewInfo.getMessageType() == AS2Message.MESSAGETYPE_CEM){
            messageTypeStr = "CEM";
        }
        //get all partner from server
        PartnerListRequest partnerRequest = new PartnerListRequest(PartnerListRequest.LIST_BY_AS2_ID);
        partnerRequest.setAdditionalListOptionStr(overviewInfo.getSenderId());
        PartnerListResponse partnerResponse = (PartnerListResponse) this.baseClient.sendSync(partnerRequest);
        List<Partner> partnerList = partnerResponse.getList();
        Partner sender = null;
        if (!partnerList.isEmpty()) {
            sender = partnerList.get(0);
        }
        partnerRequest = new PartnerListRequest(PartnerListRequest.LIST_BY_AS2_ID);
        partnerRequest.setAdditionalListOptionStr(overviewInfo.getReceiverId());
        partnerResponse = (PartnerListResponse) this.baseClient.sendSync(partnerRequest);
        partnerList = partnerResponse.getList();
        Partner receiver = null;
        if (!partnerList.isEmpty()) {
            receiver = partnerList.get(0);
        }
        this.jLabelTransactionStateDetails.setVisible(false);
        this.jLabelAS2TransmissionGraphLocalstation.setIcon(ICON_LOCALSTATION);
        this.jLabelAS2TransmissionGraphRemotepartner.setIcon(ICON_REMOTEPARTNER);
        if (overviewInfo.getDirection() == AS2MessageInfo.DIRECTION_OUT) {
            this.jLabelAS2TransmissionArrow.setIcon(ICON_ARROW_OUTBOUND);
            if (sender == null) {
                this.jLabelAS2TransmissionGraphLocalstation.setText(overviewInfo.getSenderId());
            } else {
                this.jLabelAS2TransmissionGraphLocalstation.setText(sender.getName());
            }
            if (receiver == null) {
                this.jLabelAS2TransmissionGraphRemotepartner.setText(overviewInfo.getReceiverId());
            } else {
                this.jLabelAS2TransmissionGraphRemotepartner.setText(receiver.getName());
            }
        } else {
            this.jLabelAS2TransmissionArrow.setIcon(ICON_ARROW_INBOUND);
            if (receiver == null) {
                this.jLabelAS2TransmissionGraphLocalstation.setText(overviewInfo.getReceiverId());
            } else {
                this.jLabelAS2TransmissionGraphLocalstation.setText(receiver.getName());
            }
            if (sender == null) {
                this.jLabelAS2TransmissionGraphRemotepartner.setText(overviewInfo.getSenderId());
            } else {
                this.jLabelAS2TransmissionGraphRemotepartner.setText(sender.getName());
            }
        }
        //display some general transaction details
        StringBuilder transactionDetailsText = new StringBuilder();
        if (overviewInfo.getDirection() == AS2MessageInfo.DIRECTION_OUT) {
            transactionDetailsText.append(this.rb.getResourceString("transactiondetails.outbound", this.jLabelAS2TransmissionGraphRemotepartner.getText()));
            if (overviewInfo.requestsSyncMDN()) {
                transactionDetailsText.append(this.rb.getResourceString("transactiondetails.outbound.sync"));
            }else{
                transactionDetailsText.append(this.rb.getResourceString("transactiondetails.outbound.async"));
            }
        } else {
            transactionDetailsText.append(this.rb.getResourceString("transactiondetails.inbound", this.jLabelAS2TransmissionGraphRemotepartner.getText()));
            if (overviewInfo.requestsSyncMDN()) {
                transactionDetailsText.append(this.rb.getResourceString("transactiondetails.inbound.sync"));
            }else{
                transactionDetailsText.append(this.rb.getResourceString("transactiondetails.inbound.async"));
            }
        }
        this.jLabelTransmissionDescription.setText( "<HTML>" + transactionDetailsText.toString() + "</HTML>");
        
        this.jLabelTransactionStateDetails.setVisible(false);
        List<AS2Info> transactionDetails = null;
        try {
            transactionDetails = ((MessageDetailResponse) this.baseClient.sendSync(new MessageDetailRequest(overviewInfo.getMessageId()))).getList();
        } catch (Exception e) {
        }
        if (overviewInfo.getState() == AS2Message.STATE_STOPPED) {
            this.jLabelTransactionStateGeneral.setForeground(Color.red.darker());
            this.jLabelTransactionStateDetails.setForeground(Color.red.darker());
            if (transactionDetails == null) {
                this.jLabelTransactionStateDetails.setVisible(false);
            } else {
                if (transactionDetails.size() < 2) {
                    this.jLabelStateOverviewImage.setIcon(OVERVIEWSTATE_OUTBOUND_CONN_FAILED);
                    this.jLabelTransactionStateGeneral.setText(this.rb.getResourceString("transactionstate.error.connectionrefused"));
                    this.jLabelTransactionStateDetails.setVisible(true);
                    this.jLabelTransactionStateDetails.setText(this.rb.getResourceString("transactionstate.error.connectionrefused.details"));
                } else {
                    //get last MDN info
                    AS2MDNInfo mdnInfo = null;
                    for (int i = transactionDetails.size() - 1; i > 0; i--) {
                        AS2Info info = transactionDetails.get(i);
                        if (info.isMDN()) {
                            mdnInfo = (AS2MDNInfo) info;
                            break;
                        }
                    }
                    if (mdnInfo != null) {
                        String dispositionState = mdnInfo.getDispositionState();
                        if (dispositionState == null) {
                            dispositionState = "Unknown";
                        }
                        if (overviewInfo.getDirection() == AS2MessageInfo.DIRECTION_OUT) {
                            this.jLabelStateOverviewImage.setIcon(OVERVIEWSTATE_OUTBOUND_FAILED);
                            this.jLabelTransactionStateGeneral.setText(this.rb.getResourceString("transactionstate.error.out",
                                    new Object[]{
                                        messageTypeStr,
                                        this.jLabelAS2TransmissionGraphRemotepartner.getText(),
                                        dispositionState}));
                        } else {
                            this.jLabelStateOverviewImage.setIcon(OVERVIEWSTATE_INBOUND_FAILED);
                            this.jLabelTransactionStateGeneral.setText(this.rb.getResourceString("transactionstate.error.in",
                                    new Object[]{
                                        messageTypeStr,
                                        this.jLabelAS2TransmissionGraphRemotepartner.getText(),
                                        dispositionState
                                    }));
                            //special: If the transaction direction was inbound and the transaction state is stopped anyway but the MDN state
                            //is processed and the MDN was async then there is a connection problem sending the async MDN or the async MDN has been
                            //rejected with a HTTP 400 by the partner
                            if (mdnInfo.getState() == AS2Message.STATE_FINISHED && !overviewInfo.requestsSyncMDN()) {
                                this.jLabelStateOverviewImage.setIcon(OVERVIEWSTATE_INBOUND_ANSWER_FAILED);
                                this.jLabelTransactionStateGeneral.setText(this.rb.getResourceString("transactionstate.error.asyncmdnsend"));
                                this.jLabelTransactionStateDetails.setVisible(true);
                                this.jLabelTransactionStateDetails.setText(this.rb.getResourceString("transactionstate.error.asyncmdnsend.details"));
                            }
                        }
                        //get some more details
                        if (dispositionState.contains(AS2Exception.UNKNOWN_TRADING_PARTNER_ERROR)) {
                            this.jLabelTransactionStateDetails.setVisible(true);
                            this.jLabelTransactionStateDetails.setText(this.rb.getResourceString("transactionstate.error.unknown-trading-partner",
                                    new Object[]{
                                        overviewInfo.getSenderId(),
                                        overviewInfo.getReceiverId(),}));
                        } else if (dispositionState.contains(AS2Exception.AUTHENTIFICATION_ERROR)) {
                            this.jLabelTransactionStateDetails.setVisible(true);
                            this.jLabelTransactionStateDetails.setText(this.rb.getResourceString("transactionstate.error.authentication-failed"));
                        } else if (dispositionState.contains(AS2Exception.DECOMPRESSSION_ERROR)) {
                            this.jLabelTransactionStateDetails.setVisible(true);
                            this.jLabelTransactionStateDetails.setText(this.rb.getResourceString("transactionstate.error.decompression-failed"));
                        } else if (dispositionState.contains(AS2Exception.INSUFFICIENT_SECURITY_ERROR)) {
                            this.jLabelTransactionStateDetails.setVisible(true);
                            this.jLabelTransactionStateDetails.setText(this.rb.getResourceString("transactionstate.error.insufficient-message-security"));
                        } else if (dispositionState.contains(AS2Exception.PROCESSING_ERROR)) {
                            this.jLabelTransactionStateDetails.setVisible(true);
                            this.jLabelTransactionStateDetails.setText(this.rb.getResourceString("transactionstate.error.unexpected-processing-error"));
                        } else if (dispositionState.contains(AS2Exception.DECRYPTION_ERROR)) {
                            this.jLabelTransactionStateDetails.setVisible(true);
                            this.jLabelTransactionStateDetails.setText(this.rb.getResourceString("transactionstate.error.decryption-failed"));
                        }
                    } else {
                        this.jLabelStateOverviewImage.setIcon(null);
                        this.jLabelTransactionStateGeneral.setText(this.rb.getResourceString("transactionstate.error.unknown"));
                        this.jLabelTransactionStateDetails.setVisible(false);
                    }

                }
            }
        } else if (overviewInfo.getState() == AS2Message.STATE_FINISHED) {
            this.jLabelTransactionStateGeneral.setForeground(Color.green.darker().darker());
            this.jLabelTransactionStateDetails.setForeground(Color.green.darker().darker());
            if (overviewInfo.getDirection() == AS2MessageInfo.DIRECTION_OUT) {
                this.jLabelStateOverviewImage.setIcon(OVERVIEWSTATE_OUTBOUND_OK);
                this.jLabelTransactionStateGeneral.setText(this.rb.getResourceString("transactionstate.ok.send",
                        new Object[]{
                            messageTypeStr,
                            this.jLabelAS2TransmissionGraphRemotepartner.getText()
                        }
                ));
                this.jLabelTransactionStateDetails.setVisible(true);
                this.jLabelTransactionStateDetails.setText(this.rb.getResourceString("transactionstate.ok.details"));
            } else {
                this.jLabelStateOverviewImage.setIcon(OVERVIEWSTATE_INBOUND_OK);
                this.jLabelTransactionStateGeneral.setText(this.rb.getResourceString("transactionstate.ok.receive",
                        new Object[]{
                            messageTypeStr,
                            this.jLabelAS2TransmissionGraphRemotepartner.getText(),
                        }));
                this.jLabelTransactionStateDetails.setVisible(true);
                this.jLabelTransactionStateDetails.setText(this.rb.getResourceString("transactionstate.ok.details"));
            }
        } else if (overviewInfo.getState() == AS2Message.STATE_PENDING) {
            this.jLabelStateOverviewImage.setIcon(OVERVIEWSTATE_PENDING);
            this.jLabelTransactionStateGeneral.setForeground(Color.yellow.darker().darker());
            this.jLabelTransactionStateGeneral.setText(this.rb.getResourceString("transactionstate.pending"));
            this.jLabelTransactionStateDetails.setVisible(false);
        }
    }

    /**
     * Displays the message details log
     */
    private void displayProcessLog() {
        StyledDocument document = (StyledDocument) this.jTextPaneLog.getDocument();
        StyleContext context = StyleContext.getDefaultStyleContext();
        Style currentStyle = context.getStyle(StyleContext.DEFAULT_STYLE);
        Color defaultColor = (Color) currentStyle.getAttribute(StyleConstants.Foreground);
        List<LogEntry> entries = ((MessageLogResponse) this.baseClient.sendSync(new MessageLogRequest(overviewInfo.getMessageId()))).getList();
        StringBuilder buffer = new StringBuilder();
        DateFormat format = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        for (int i = 0; i < entries.size(); i++) {
            currentStyle.removeAttribute(StyleConstants.Foreground);
            currentStyle.addAttribute(StyleConstants.Foreground, defaultColor);
            buffer.append("[").append(format.format(entries.get(i).getMillis())).append("] ");
            try {
                document.insertString(document.getLength(), buffer.toString(), currentStyle);
            } catch (Throwable ignore) {
                //nop
            }
            buffer.setLength(0);
            if (entries.get(i).getLevel().equals(Level.WARNING)) {
                currentStyle.addAttribute(StyleConstants.Foreground, IRCColors.COLOR_NAVY.brighter());
            } else if (entries.get(i).getLevel().equals(Level.SEVERE)) {
                currentStyle.addAttribute(StyleConstants.Foreground, IRCColors.COLOR_RED);
            } else if (entries.get(i).getLevel().equals(Level.FINE)) {
                currentStyle.addAttribute(StyleConstants.Foreground, IRCColors.COLOR_GREEN);
            } else {
                currentStyle.addAttribute(StyleConstants.Foreground, defaultColor);
            }
            buffer.append(entries.get(i).getMessage()).append("\n");
            try {
                document.insertString(document.getLength(), buffer.toString(), currentStyle);
            } catch (Throwable ignore) {
                //nop
            }
            buffer.setLength(0);
            currentStyle.removeAttribute(StyleConstants.Foreground);
            currentStyle.addAttribute(StyleConstants.Foreground, defaultColor);
        }
    }

    /**
     * Displays all messages that contain to the passed overview object
     */
    private void displayData(AS2MessageInfo overviewRow) {
        try {
            List<AS2Info> details = ((MessageDetailResponse) this.baseClient.sendSync(new MessageDetailRequest(overviewRow.getMessageId()))).getList();
            ((TableModelMessageDetails) this.jTableMessageDetails.getModel()).passNewData(details);
        } catch (Exception e) {
            JFrame parent = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
            JOptionPane.showMessageDialog(parent, e.getMessage());
        }
    }

    /**
     * ListSelectionListener
     */
    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        int selectedRow = this.jTableMessageDetails.getSelectedRow();
        if (selectedRow >= 0) {
            AS2Info info = ((TableModelMessageDetails) this.jTableMessageDetails.getModel()).getRow(selectedRow);
            String rawFileName = null;
            if (!info.isMDN()) {
                AS2MessageInfo messageInfo = (AS2MessageInfo) info;
                if (messageInfo.getRawFilenameDecrypted() != null) {
                    rawFileName = messageInfo.getRawFilenameDecrypted();
                } else if (messageInfo.getRawFilename() != null) {
                    rawFileName = messageInfo.getRawFilename();
                }
            } else {
                if (info.getRawFilename() != null) {
                    rawFileName = info.getRawFilename();
                }
            }
            this.jPanelFileDisplayRaw.displayFile(rawFileName);
            String headerFilename = null;
            if (info.getHeaderFilename() != null) {
                headerFilename = info.getHeaderFilename();
            }
            this.jPanelFileDisplayHeader.displayFile(headerFilename);
            try {
                if (this.payloadList.size() > 0) {
                    for (int i = 0; i < payloadList.size(); i++) {
                        String payloadFilename = this.payloadList.get(i).getPayloadFilename();
                        this.jPanelFileDisplayPayload[i].displayFile(payloadFilename);
                    }
                }
            } catch (Exception e) {
                //nop
            }

        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanelMain = new javax.swing.JPanel();
        jPanelHeader = new javax.swing.JPanel();
        jLabelAS2TransmissionGraph = new javax.swing.JLabel();
        jLabelAS2TransmissionGraphLocalstation = new javax.swing.JLabel();
        jLabelAS2TransmissionArrow = new javax.swing.JLabel();
        jLabelAS2TransmissionGraphRemotepartner = new javax.swing.JLabel();
        jLabelTransactionState = new javax.swing.JLabel();
        jLabelTransactionStateGeneral = new javax.swing.JLabel();
        jLabelTransactionStateDetails = new javax.swing.JLabel();
        jLabelStateOverviewImage = new javax.swing.JLabel();
        jPanelSpace = new javax.swing.JPanel();
        jLabelTransmissionDescription = new javax.swing.JLabel();
        jPanelSep = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanelSep2 = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();
        jPanelInfo = new javax.swing.JPanel();
        jSplitPane = new javax.swing.JSplitPane();
        jScrollPaneList = new javax.swing.JScrollPane();
        jTableMessageDetails = new javax.swing.JTable();
        jTabbedPane = new javax.swing.JTabbedPane();
        jPanelProcessLog = new javax.swing.JPanel();
        jScrollPaneLog = new javax.swing.JScrollPane();
        jTextPaneLog = new javax.swing.JTextPane();
        jPanelButton = new javax.swing.JPanel();
        jButtonOk = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanelMain.setLayout(new java.awt.GridBagLayout());

        jPanelHeader.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanelHeader.setLayout(new java.awt.GridBagLayout());

        jLabelAS2TransmissionGraph.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelAS2TransmissionGraph.setText(this.rb.getResourceString("label.transmissiongraph"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jPanelHeader.add(jLabelAS2TransmissionGraph, gridBagConstraints);

        jLabelAS2TransmissionGraphLocalstation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/message/loggui/localstation16x16.gif"))); // NOI18N
        jLabelAS2TransmissionGraphLocalstation.setText("Local station");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jPanelHeader.add(jLabelAS2TransmissionGraphLocalstation, gridBagConstraints);

        jLabelAS2TransmissionArrow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/message/loggui/arrow32x16.gif"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        jPanelHeader.add(jLabelAS2TransmissionArrow, gridBagConstraints);

        jLabelAS2TransmissionGraphRemotepartner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/message/loggui/singlepartner16x16.gif"))); // NOI18N
        jLabelAS2TransmissionGraphRemotepartner.setText("Remote Partner");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jPanelHeader.add(jLabelAS2TransmissionGraphRemotepartner, gridBagConstraints);

        jLabelTransactionState.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelTransactionState.setText(this.rb.getResourceString("label.transactionstate"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jPanelHeader.add(jLabelTransactionState, gridBagConstraints);

        jLabelTransactionStateGeneral.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelTransactionStateGeneral.setText("<General transaction state>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jPanelHeader.add(jLabelTransactionStateGeneral, gridBagConstraints);

        jLabelTransactionStateDetails.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        jLabelTransactionStateDetails.setText("<Transaction state details>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jPanelHeader.add(jLabelTransactionStateDetails, gridBagConstraints);

        jLabelStateOverviewImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/message/loggui/comm_ok_outbound.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        jPanelHeader.add(jLabelStateOverviewImage, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelHeader.add(jPanelSpace, gridBagConstraints);

        jLabelTransmissionDescription.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        jLabelTransmissionDescription.setText("<Transmission description>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 5);
        jPanelHeader.add(jLabelTransmissionDescription, gridBagConstraints);

        jPanelSep.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelSep.add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelHeader.add(jPanelSep, gridBagConstraints);

        jPanelSep2.setLayout(new java.awt.GridBagLayout());

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        jPanelSep2.add(jSeparator2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        jPanelHeader.add(jPanelSep2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelMain.add(jPanelHeader, gridBagConstraints);

        jPanelInfo.setLayout(new java.awt.GridBagLayout());

        jSplitPane.setDividerLocation(120);
        jSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jTableMessageDetails.setModel(new TableModelMessageDetails());
        jTableMessageDetails.setShowHorizontalLines(false);
        jTableMessageDetails.setShowVerticalLines(false);
        jScrollPaneList.setViewportView(jTableMessageDetails);

        jSplitPane.setLeftComponent(jScrollPaneList);

        jPanelProcessLog.setLayout(new java.awt.GridBagLayout());

        jTextPaneLog.setEditable(false);
        jScrollPaneLog.setViewportView(jTextPaneLog);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelProcessLog.add(jScrollPaneLog, gridBagConstraints);

        jTabbedPane.addTab(this.rb.getResourceString( "tab.log"), jPanelProcessLog);

        jSplitPane.setRightComponent(jTabbedPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelInfo.add(jSplitPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelMain.add(jPanelInfo, gridBagConstraints);

        jButtonOk.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButtonOk.setText(this.rb.getResourceString( "button.ok" ));
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonOk);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanelMain.add(jPanelButton, gridBagConstraints);

        getContentPane().add(jPanelMain, java.awt.BorderLayout.CENTER);

        setSize(new java.awt.Dimension(1131, 728));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jButtonOkActionPerformed

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_closeDialog
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonOk;
    private javax.swing.JLabel jLabelAS2TransmissionArrow;
    private javax.swing.JLabel jLabelAS2TransmissionGraph;
    private javax.swing.JLabel jLabelAS2TransmissionGraphLocalstation;
    private javax.swing.JLabel jLabelAS2TransmissionGraphRemotepartner;
    private javax.swing.JLabel jLabelStateOverviewImage;
    private javax.swing.JLabel jLabelTransactionState;
    private javax.swing.JLabel jLabelTransactionStateDetails;
    private javax.swing.JLabel jLabelTransactionStateGeneral;
    private javax.swing.JLabel jLabelTransmissionDescription;
    private javax.swing.JPanel jPanelButton;
    private javax.swing.JPanel jPanelHeader;
    private javax.swing.JPanel jPanelInfo;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanelProcessLog;
    private javax.swing.JPanel jPanelSep;
    private javax.swing.JPanel jPanelSep2;
    private javax.swing.JPanel jPanelSpace;
    private javax.swing.JScrollPane jScrollPaneList;
    private javax.swing.JScrollPane jScrollPaneLog;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JTable jTableMessageDetails;
    private javax.swing.JTextPane jTextPaneLog;
    // End of variables declaration//GEN-END:variables
}
