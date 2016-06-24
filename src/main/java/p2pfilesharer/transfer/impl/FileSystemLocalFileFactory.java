/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pfilesharer.transfer.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import p2pfilesharer.transfer.DaoException;
import p2pfilesharer.transfer.LocalFile;
import p2pfilesharer.transfer.LocalFileFactory;

/**
 *
 * @author Edward
 */
public class FileSystemLocalFileFactory implements LocalFileFactory {

    private final static Logger logger = LoggerFactory.getLogger(FileSystemLocalFileFactory.class);

    private final IdStrategy idStrategy;

    public FileSystemLocalFileFactory(IdStrategy idStrategy) {
        this.idStrategy = idStrategy;
    }

    @Override
    public LocalFile getResource(String path) throws DaoException {
        logger.debug("Attempting to retrieve LocalFile: {}", path);

        try {
            logger.debug("Checking if regular file exists at: {}", path);
            Path p = Paths.get(path);
            if (!Files.exists(p, LinkOption.NOFOLLOW_LINKS)) {
                return null;
            } else if (!Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)) {
                throw new DaoException("Path : " + p + " exists, but is not a regular file");
            }

            logger.debug("File exists. Determining size of file: {}");
            long len = Files.size(p);

            logger.debug("Length was {}. Now setting IdStrategy as {}", len, idStrategy);
            FileSystemLocalFile localFile = new FileSystemLocalFile(p, len, null, idStrategy);

            logger.debug("LocalFile successfully created");
            return localFile;

        } catch (IOException ex) {
            throw new DaoException(ex);
        } 
    }
}
