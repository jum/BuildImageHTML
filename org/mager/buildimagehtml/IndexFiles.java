/*
 * $Id$
 * This is an unpublished work copyright (c) 2004 Jens-Uwe Mager
 * 30177 Hannover, Germany, jum@anubis.han.de
 */

package org.mager.buildimagehtml;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author jum
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class IndexFiles implements FilenameFilter {

    /* (non-Javadoc)
     * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
     */
    public boolean accept(File dir, String name) {
        return name.matches("index\\d+\\.(html|jpg)");
    }
}
