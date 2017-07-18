/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.datavyu.plugins;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * A group of file filters. This is the intersection of all filters to define a file filter.
 */
public final class GroupFileFilter extends FileFilter {

    /** Description for this group file filter */
    private final String description;

    /** Listing of filters */
    private List<Filter> filters;

    public GroupFileFilter(final String description) {
        this.description = description;
        filters = Lists.newArrayList();
    }

    @Override
    public boolean accept(final File f) {
        if (f.isDirectory()) {
            return true;
        }
        for (Filter filter : filters) {
            if (filter.getFileFilter().accept(f)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        // Find the intersection of all extensions
        Set<String> extensions = Sets.newTreeSet();
        for (Filter filter : filters) {
            for (String ext : filter.getExtensions()) {
                extensions.add(ext);
            }
        }
        // Build the description with the intersection of all descriptions
        StringBuilder stringBuilder = new StringBuilder(this.description);
        stringBuilder.append(":");
        for (String ext : extensions) {
            stringBuilder.append(" ");
            stringBuilder.append(ext);
            stringBuilder.append(",");
        }
        // Remove the trailing comma
        if (!extensions.isEmpty()) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }

    /**
     * Add another filter to the group.
     *
     * @param filter A filter.
     */
    public void addFileFilter(final Filter filter) {
        filters.add(filter);
    }
}
