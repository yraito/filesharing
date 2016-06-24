/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pfilesharer.transfer.impl;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import p2pfilesharer.transfer.NamingStrategy;
import p2pfilesharer.transfer.RemoteFile;

/**
 *
 * @author Nick
 */
public class DefaultNamingStrategy implements NamingStrategy {

    String suffix;

    public DefaultNamingStrategy() {
    }

    public DefaultNamingStrategy(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public List<Path> getFilePaths(List<RemoteFile> remoteFiles, Path outputDir) {
        return remoteFiles.stream()
                .map(RemoteFile::getFullName)
                .map(s -> suffix == null ? s : s + "." + suffix)
                .map(s -> outputDir.resolve(s))
                .collect(Collectors.toList());
    }
    
}
