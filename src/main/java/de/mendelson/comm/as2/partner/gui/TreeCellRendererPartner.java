//$Header: /as2/de/mendelson/comm/as2/partner/gui/TreeCellRendererPartner.java 5     9/09/15 2:43p Heller $
package de.mendelson.comm.as2.partner.gui;

import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.util.ImageUtil;
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
 * TreeCellRenderer that will display the icons of the config tree
 * @author  S.Heller
 * @version $Revision: 5 $
 */
public class TreeCellRendererPartner extends DefaultTreeCellRenderer {

    private final ImageIcon ICON_REMOTE = ListCellRendererPartner.ICON_REMOTESTATION;
    private final ImageIcon ICON_LOCAL = ListCellRendererPartner.ICON_LOCALSTATION;
    private final ImageIcon ICON_CONFIGERROR = ListCellRendererPartner.ICON_CONFIGERROR;
    
    /**Stores the selected node*/
    private DefaultMutableTreeNode selectedNode = null;

    /**Constructor to create Renderer for console tree*/
    public TreeCellRendererPartner() {
        super();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
            Object selectedObject, boolean sel,
            boolean expanded,
            boolean leaf,
            int row, boolean hasFocus) {
        this.selectedNode = (DefaultMutableTreeNode) selectedObject;
        return (super.getTreeCellRendererComponent(tree, selectedObject, sel, expanded,
                leaf, row, hasFocus));
    }

    /**Returns the defined Icon of the entry*/
    private Icon getDefinedIcon() {
        ImageIcon icon = null;
        Object object = this.selectedNode.getUserObject();
        //is this root node?
        if (object == null || !(object instanceof Partner)) {
            return(super.getOpenIcon());
        }
        Partner partner = (Partner) object;
        if (partner.isLocalStation()) {
            icon = this.ICON_LOCAL;
        }else{
            icon = this.ICON_REMOTE;
        }
        if( partner.hasConfigError()){
            //mix up the icon with the error icon
            ImageUtil imageUtil = new ImageUtil();
            icon = imageUtil.mixImages(icon, ICON_CONFIGERROR);
        }
        return( icon );
    }

    /**Gets the Icon by the type of the object*/
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