/*
 * $Id$
 * This is an unpublished work copyright (c) 2004 Jens-Uwe Mager
 * 30177 Hannover, Germany, jum@anubis.han.de
 */

package org.mager.buildimagehtml;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;

import javax.imageio.*;
import javax.swing.*;

/**
 * @author jum
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class BuildImageHTML {

    static final String LORES = "lores";
    static final int LORES_WIDTH = 800;
    static final int LORES_HEIGHT = 600;
    
    static final int THUMB_WIDTH = 800;
    static final int THUMB_HEIGHT = 600;
    static final int THUMB_COLUMNS = 6;
    static final int THUMB_ROWS = 4;
    static final int THUMB_ITEM_WIDTH = 128;
    static final int THUMB_ITEM_HEIGHT = 96;
    static final int THUMB_TOP_MARGIN = 6;
    static final int THUMB_LEFT_MARGIN = 6;
    static final int THUMB_HORIZONTAL_GAP = 4;
    static final int THUMB_VERTICAL_GAP = 53;
    static final int THUMB_TITLE_GAP = 1;
    
    //static final Object interpolation = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
    static final Object interpolation  = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
    
    static boolean useGUI = false;
    static Preferences prefs = Preferences.userNodeForPackage(BuildImageHTML.class);
    static final String DEFAULT_DIR_KEY = "DefaultDir";
    
    public static void main(String[] args) throws Throwable {
        File theDir;
        if (args.length > 0)
            theDir = new File(args[0]);
        else {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            useGUI = true;
            JFileChooser fc = new JFileChooser(prefs.get(DEFAULT_DIR_KEY, "."));
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
                System.exit(1);
            theDir = fc.getSelectedFile();
            prefs.put(DEFAULT_DIR_KEY, theDir.getAbsolutePath());
        }
        if (!useGUI)
            System.out.println("BuildImageHTML:" + theDir);
        cleanIndexFiles(theDir);
        File loresDir = new File(theDir, LORES);
        if (loresDir.exists())
            deleteRecursive(loresDir);
        loresDir.mkdir();
        File[] items = theDir.listFiles(new ImageFiles());
        ProgressMonitor pm = null;
        if (useGUI) {
            pm = new ProgressMonitor(null, "Building lores images", "", 0, items.length);
            pm.setMillisToDecideToPopup(0);
        }
        for (int i = 0; i < items.length; i++) {
            if (useGUI) {
                pm.setProgress(i);
                pm.setNote(items[i].getName());
                if (pm.isCanceled())
                    System.exit(1);
            } else
                System.out.println("Building lores for " + items[i]);
            BufferedImage src = ImageIO.read(items[i]);
            int width = LORES_WIDTH;
            int height = LORES_HEIGHT;                
            if ((float)src.getWidth()/(float)src.getHeight() == 0.75) {
                width = LORES_HEIGHT;
                height = LORES_WIDTH;
            }
            BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = dest.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);
            graphics2D.drawImage(src, 0, 0, width, height, null);
            ImageIO.write(dest, "jpeg", new File(loresDir, items[i].getName()));
        }
        if (useGUI)
            pm.close();
        buildThumbs(theDir, "index.html");
        buildThumbs(loresDir, "../index.html");
        if (!useGUI)
            System.out.println("done");
        System.exit(0);
   }
    
    static void deleteRecursive(File dir) {
        File[] items = dir.listFiles();
        for (int i = 0; i < items.length; i++) {
            if (items[i].isDirectory())
                deleteRecursive(items[i]);
            else
                items[i].delete();
        }
        dir.delete();
    }
    
    static void cleanIndexFiles(File dir) {
        File[] ixFiles = dir.listFiles(new IndexFiles());
        for (int i = 0; i < ixFiles.length; i++) {
            ixFiles[i].delete();
        }
    }
    
    static void buildThumbs(File dir, String mainindex) throws IOException {
        int index = 0;
        int xpos = THUMB_LEFT_MARGIN;
        int ypos = THUMB_TOP_MARGIN;
        int column = 0;
        int row = 0;
        PrintWriter out;
        out = new PrintWriter(new FileOutputStream(new File(dir, "index"+index+".html")));
        BufferedImage thumbs = new BufferedImage(THUMB_WIDTH, THUMB_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D thumbsG2d = thumbs.createGraphics();
        thumbsG2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);
        thumbsG2d.setBackground(Color.WHITE);
        thumbsG2d.setColor(Color.BLACK);
        thumbsG2d.clearRect(0, 0, THUMB_WIDTH, THUMB_HEIGHT);
        FontMetrics metrics = thumbsG2d.getFontMetrics();
        File[] items = dir.listFiles(new ImageFiles());
        Arrays.sort(items, new Comparator() {
            public int compare(Object o1, Object o2) {
                    return ((File)o1).getName().compareToIgnoreCase(((File)o2).getName());
            }
        });
        header(out, mainindex, index, items.length >= THUMB_ROWS * THUMB_COLUMNS);
        ProgressMonitor pm = null;
        if (useGUI) {
            pm = new ProgressMonitor(null, "Building thumbnails for " + dir.getName(), "", 0, items.length);
            pm.setMillisToDecideToPopup(0);
        }
        for (int i = 0; i < items.length; i++) {
            if (useGUI) {
                pm.setProgress(i);
                pm.setNote(items[i].getName());
                if (pm.isCanceled())
                    System.exit(1);
            } else
                System.out.println("Building thumb for " + items[i]);
            BufferedImage src = ImageIO.read(items[i]);
            int width = THUMB_ITEM_WIDTH;
            int height = THUMB_ITEM_HEIGHT;
            int centerOff = 0;
            if ((float)src.getWidth()/(float)src.getHeight() == 0.75) {
                width = THUMB_ITEM_HEIGHT;
                height = THUMB_ITEM_WIDTH;
                centerOff = (THUMB_ITEM_WIDTH-width)/2;
            }
            thumbsG2d.drawImage(src, xpos+centerOff, ypos, width, height, null);
            String title = items[i].getName();
            int left = THUMB_ITEM_WIDTH + THUMB_HORIZONTAL_GAP - metrics.stringWidth(title);
            thumbsG2d.drawString(title, xpos+left/2, ypos+height+THUMB_TITLE_GAP+metrics.getHeight());
            out.println("<area href=\"" + title + "\" shape=rect coords="+ xpos + "," + ypos + "," + (xpos+THUMB_ITEM_WIDTH+THUMB_HORIZONTAL_GAP) + "," + (ypos+THUMB_ITEM_HEIGHT+THUMB_VERTICAL_GAP) + ">");
            xpos += THUMB_ITEM_WIDTH + THUMB_HORIZONTAL_GAP;
            column++;
            if (column >= THUMB_COLUMNS) {
                column = 0;
                xpos = THUMB_LEFT_MARGIN;
                ypos += THUMB_ITEM_HEIGHT + THUMB_VERTICAL_GAP;
                row++;
                if (row >= THUMB_ROWS) {
                    footer(out, mainindex, index, i < items.length-1);
                    out.close();
                    ImageIO.write(thumbs, "jpeg", new File(dir, "index" + index++ + ".jpg"));
                    out = new PrintWriter(new FileOutputStream(new File(dir, "index"+index+".html")));
                    header(out, mainindex, index, items.length - i - 1 > THUMB_ROWS * THUMB_COLUMNS);
                    ypos = THUMB_TOP_MARGIN;
                    row = 0;
                    thumbsG2d.clearRect(0, 0, THUMB_WIDTH, THUMB_HEIGHT);                    
                }
            }
        }
        if (column != 0 || row != 0) {
            footer(out, mainindex, index, false);
            out.close();
            ImageIO.write(thumbs, "jpeg", new File(dir, "index" + index++ + ".jpg"));
        } else {
            out.close();
            new File(dir, "index"+index+".html").delete();
        }
        if (useGUI)
            pm.close();
    }
    
    static void header(PrintWriter out, String mainindex, int index, boolean doNext) {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>index" + index + "</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<center>");
        out.println("<h1>index" + index + ".jpg</h1>");
        out.println("<br>");
        links(out, mainindex, index, doNext);
        out.println("<br>");
        out.println("<img ismap usemap=#index" + index + " src=\"index" + index + ".jpg\" border=0>");
        out.println("<map name=index" + index + ">");
    }
    
    static void links(PrintWriter out, String mainindex, int index, boolean doNext) {
        int prevno = index - 1;
        out.print("<a href=\"" + mainindex + "\">home</a>");
        if (prevno >= 0)
            out.print("&nbsp;<a href=\"index" + prevno + ".html\">previous</a>");
        else
            out.print("&nbsp;previous");
        if (doNext) {
            out.print("&nbsp;<a href=\"index" + (index+1) + ".html\">next</a>");
        } else
            out.print("&nbsp;next");
        out.println("<br>");
        
    }
    
    static void footer(PrintWriter out, String mainindex, int index, boolean doNext) {
        out.println("<br>");
        links(out, mainindex, index, doNext);
        out.println("</map>");
        out.println("</center>");
        out.println("</body>");
        out.println("</html>");        
    }
}
