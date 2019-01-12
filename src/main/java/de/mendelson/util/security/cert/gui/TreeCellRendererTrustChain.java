//$Header: /oftp2/de/mendelson/util/security/cert/gui/TreeCellRendererTrustChain.java 3     27-01-16 8:58a Heller $
package de.mendelson.util.security.cert.gui;

import de.mendelson.util.security.DNUtil;
import de.mendelson.util.security.cert.KeystoreCertificate;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * TreeCellRenderer that will display the icons of the trust chain tree
 *
 * @author S.Heller
 * @version $Revision: 3 $
 */
public class TreeCellRendererTrustChain extends DefaultTreeCellRenderer {

    public static final ImageIcon ICON_ROOT
            = new ImageIcon(TreeCellRendererTrustChain.class.getResource(
                            "/de/mendelson/util/security/cert/gui/cert_root16x16.gif"));
    public static final ImageIcon ICON_CERT
            = new ImageIcon(TreeCellRendererTrustChain.class.getResource(
                            "/de/mendelson/util/security/cert/gui/certificate16x16.gif"));
    public static final ImageIcon ICON_KEY
            = new ImageIcon(TreeCellRendererTrustChain.class.getResource(
                            "/de/mendelson/util/security/cert/gui/key16x16.gif"));
    public static final ImageIcon ICON_CERTIFICATE_UNTRUSTED
            = new ImageIcon(TreeCellRendererTrustChain.class.getResource(
                            "/de/mendelson/util/security/cert/gui/cert_untrusted16x16.gif"));

    /**
     * Stores the selected node
     */
    private DefaultMutableTreeNode selectedNode = null;

    /**
     * Constructor to create Renderer for console tree
     */
    public TreeCellRendererTrustChain() {
        super();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
            Object selectedObject, boolean selected,
            boolean expanded,
            boolean leaf,
            int row, boolean hasFocus) {
        this.selectedNode = (DefaultMutableTreeNode) selectedObject;
        Component component = super.getTreeCellRendererComponent(tree, selectedObject, selected, expanded,
                leaf, row, hasFocus);
        Object object = this.selectedNode.getUserObject();
        if (object != null) {
            if (object instanceof KeystoreCertificate) {
                KeystoreCertificate certificate = (KeystoreCertificate) object;
                StringBuilder builder = new StringBuilder();
                builder.append(certificate.getAlias());
                builder.append(" [");
                builder.append(DNUtil.getOrganization(certificate.getX509Certificate(), DNUtil.SUBJECT));
                builder.append("]");
                super.setText(builder.toString());
            } else if (object instanceof String) {
                //untrusted
                super.setText((String) object);
            }
        }
        return (component);
    }

    /**
     * Returns the defined Icon of the entry
     */
    private Icon getDefinedIcon() {
        Object object = this.selectedNode.getUserObject();
        if (object != null) {
            if (object instanceof KeystoreCertificate) {
                KeystoreCertificate certificate = (KeystoreCertificate) object;
                if (certificate.isRootCertificate()) {
                    return (ICON_ROOT);
                } else if (certificate.getIsKeyPair()) {
                    return (ICON_KEY);
                } else {
                    return (ICON_CERT);
                }
            }else if( object instanceof String ){
                return( ICON_CERTIFICATE_UNTRUSTED);
            }
        }
        //is this root node?
        return (null);
    }

    /**
     * Gets the Icon by the type of the object
     */
    @Override
    public Icon getLeafIcon() {
        Icon icon = this.getDefinedIcon();
        if (icon != null) {
            return (icon);
        }
        //nothing found: get default
        return (super.getLeafIcon());
    }

    @Override
    public Icon getOpenIcon() {
        Icon icon = this.getDefinedIcon();
        if (icon != null) {
            return (icon);
        }
        return (super.getOpenIcon());
    }

    @Override
    public Icon getClosedIcon() {
        Icon icon = this.getDefinedIcon();
        if (icon != null) {
            return (icon);
        }
        return (super.getClosedIcon());
    }

}
