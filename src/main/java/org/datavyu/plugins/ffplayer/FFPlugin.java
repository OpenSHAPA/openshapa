package org.datavyu.plugins.ffplayer;

import com.google.common.collect.Lists;
import com.sun.jna.Platform;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.datavyu.Datavyu;
import org.datavyu.plugins.DataViewer;
import org.datavyu.plugins.Filter;
import org.datavyu.plugins.FilterNames;
import org.datavyu.plugins.Plugin;
import org.datavyu.util.VersionRange;

import javax.swing.*;
import java.awt.*;
import java.io.FileFilter;
import java.util.List;

public class FFPlugin implements Plugin {

    private static final List<Datavyu.Platform> validOperatingSystem = Lists.newArrayList(Datavyu.Platform.WINDOWS);

    private static final Filter videoFilter = new Filter() {
        final List<String> fileEndings = Lists.newArrayList(".avi", ".mov", ".mpg", ".mp4");
        final SuffixFileFilter fileFilter = new SuffixFileFilter(fileEndings, IOCase.INSENSITIVE);

        @Override
        public FileFilter getFileFilter() {
            return fileFilter;
        }

        @Override
        public String getName() {
            return FilterNames.VIDEO.getFilterName();
        }

        @Override
        public Iterable<String> getExtensions() {
            return fileEndings;
        }
    };

    @Override
    public DataViewer getNewDataViewer(Frame parent, boolean modal) {
        return Platform.isWindows() ? new FFViewer(parent, modal) : null;
    }

    @Override
    public Class<? extends DataViewer> getViewerClass() {
        return Platform.isWindows() ? FFViewer.class : null;
    }

    @Override
    public ImageIcon getTypeIcon() {
        return new ImageIcon(getClass().getResource(
                "/icons/gstreamerplugin-icon.png"));
    }

    @Override
    public String getPluginName() {
        return "FF Plugin";
    }

    @Override
    public String getNamespace() {
        return "datavyu.video";
    }

    @Override
    public Filter[] getFilters() {
        return new Filter[]{videoFilter};
    }

    @Override
    public List<Datavyu.Platform> getValidPlatforms() {
        return validOperatingSystem;
    }

    @Override
    public VersionRange getValidVersions() {
        return new VersionRange(0, 10); // Start with OS version 10, go to 99 for future
    }
}
