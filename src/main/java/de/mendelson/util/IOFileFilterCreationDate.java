 //$Header: /as2/de/mendelson/util/IOFileFilterCreationDate.java 2     23.10.18 17:02 Heller $
package de.mendelson.util;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * File filter that filters the directory entries by their age
 *
 * @author S.Heller
 * @version $Revision: 2 $
 */
public class IOFileFilterCreationDate implements DirectoryStream.Filter {

    public static final int MODE_OLDER_THAN = 1;
    public static final int MODE_NOT_OLDER_THAN = 2;

    private int mode = MODE_OLDER_THAN;
    private long absoluteTime;
    private boolean includeDirecories = false;

    /**
     * Creates a new instance of the creation date File filter
     */
    public IOFileFilterCreationDate(final int MODE, long absoluteTime) {
        this.mode = MODE;
        this.absoluteTime = absoluteTime;
    }

    /**
     * Returns if this file filer accepts the passed file
     */
    @Override
    public boolean accept(Object entry) {
        if (entry == null || !(entry instanceof Path)) {
            return (false);
        }
        Path path = (Path) entry;
        if( path.getFileName().toString().equals( ".")
                || path.getFileName().toString().equals( "..")){
            return( false );
        }
        if (!this.includeDirecories) {
            if (Files.isDirectory(path)) {
                return (false);
            }
        }
        try {
            BasicFileAttributes view = Files.getFileAttributeView(path, BasicFileAttributeView.class).readAttributes();
            if (this.mode == MODE_OLDER_THAN) {
                return (view.creationTime().toMillis() < this.absoluteTime);
            } else {
                return (view.creationTime().toMillis() > this.absoluteTime);
            }
        } catch (Exception e) {
            return (false);
        }
    }

    /**
     * @return the includeDirecories
     */
    public boolean includesDirecories() {
        return includeDirecories;
    }

    /**
     * @param includeDirecories the includeDirecories to set
     */
    public void setIncludeDirecories(boolean includeDirecories) {
        this.includeDirecories = includeDirecories;
    }

//    public static final void main(String[] args) {
//        IOFileFilterCreationDate fileFilter 
//                = new IOFileFilterCreationDate(MODE_OLDER_THAN, System.currentTimeMillis()
//                        - TimeUnit.DAYS.toMillis(1));
//        fileFilter.setIncludeDirecories(false);
//        File dir = new File("c:/temp");
//        Path dirPath = dir.toPath();
//        try {
//            DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, fileFilter);
//            for (Path entry : stream) {
//                System.out.println(entry.getFileName());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
