/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.home.items.web.servergui;

import nu.nethome.home.system.HomeService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class MediaPage extends PortletPage {

    private static Logger logger = Logger.getLogger(MediaPage.class.getName());
    private final HomeService server;
    private final SimpleDateFormat dateFormat;
    private final String mediaFileDirectory;

    public MediaPage(String mLocalURL, HomeService server, String mediaFileDirectory) {
        super(mLocalURL + "&subpage=media");
        this.server = server;
        this.mediaFileDirectory = mediaFileDirectory;
        dateFormat = new SimpleDateFormat("yy.MM.dd'&nbsp;'HH:mm:ss");
    }

    @Override
    public String getPageNameURL() {
        return "media";
    }

    public String getPageName() {
        return "Media";
    }

    @Override
    public String getIconUrl() {
        return "web/home/image32.png";
    }

    /**
     * This is the main entrance point of the class. This is called when a http
     * request is routed to this servlet.
     */
    public void printPage(HttpServletRequest req, HttpServletResponse res,
                          HomeService server) throws ServletException, IOException {
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType("text/html");
        PrintWriter p = res.getWriter();
        if (ServletFileUpload.isMultipartContent(req)) {
            handleFileUpload(req, p);
        }
        printMediaHeading(p);

        printMediaFiles(p);
        printFileSelector(p);

        printMediaFooter(p);
    }

    private void handleFileUpload(HttpServletRequest req, PrintWriter p) {
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        try {
            List<FileItem> fields = upload.parseRequest(req);
            for (FileItem field : fields) {
                if (!field.isFormField()) {
                    String fileName = field.getName();
                    File uploadedFile = new File(mediaFileDirectory + File.separator + fileName);
                    field.write(uploadedFile);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to upload file", e);
        }
    }

    private void printMediaFiles(PrintWriter p) {
        p.println("<div class=\"medialist\">");
        p.println(" <table>");
        File folder = new File(mediaFileDirectory);
        File[] listOfFiles = folder.listFiles();
        p.println("  <tr class=\"logrowsheader\"><td></td><td>Name</td><td>Size</td><td>Time</td><td></td></tr>");
        for (File file : listOfFiles) {
            if (file.isFile()) {
                final String imageSize = "" + file.length();
                final boolean isImageFile = isImageFile(file);
                final String iconFile = isImageFile ? "media/" + file.getName() : "web/home/log32.png";
                final String fileName = isImageFile ? "<a href=\"media/" + file.getName() + "\">" + file.getName() + "</a>" :
                        file.getName();
                p.println("  <tr>");
                p.println("   <td><img src=\"" + iconFile + "\" height=\"32\" width=\"32\" /></td>");
                p.println("   <td>" + fileName + "</td>");
                p.println("   <td>" + imageSize + "</td>");
                p.println("   <td>" + dateFormat.format(new Date(file.lastModified())) + "</td>");
                p.println("   <td></td>");
                p.println("  </tr>");
            }
        }
        p.println(" </table>");
        p.println("</div>");
    }

    boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".gif") ||
                fileName.endsWith(".bmp") || fileName.endsWith(".jpeg") || fileName.endsWith(".svg");
    }

    private String getImageSize(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image != null) {
                return "" + image.getWidth() + "&nbsp;x&nbsp;" + image.getHeight();
            }
        } catch (IOException e) {
            // Ignore
        }
        return "";
    }

    private void printFileSelector(PrintWriter p) {
        p.println("<form class=\"uploadPanel\" action=\"" + localURL + "\" method=\"post\" enctype=\"multipart/form-data\">\n" +
                "    Select a new image to add:\n" +
                "    <input type=\"file\" name=\"fileToUpload\" id=\"fileToUpload\">\n" +
                "    <input type=\"submit\" value=\"Add Image\" name=\"submit\">\n" +
                "</form>");
    }

    private void printMediaFooter(PrintWriter p) {
        p.println("        <div class=\"footer thin\"></div>");
        p.println("    </div>");
        p.println("</div>");
    }

    protected void printMediaHeading(PrintWriter p) {
        p.println("<div class=\"itemcolumn log\">");
        p.println("<div class=\"item_details\">");
        p.println("<div class=\"iheader thin\">");
        p.println(" <div class=\"homeiteminfo\">");
        p.println("   <div class=\"header\"  id='itemInfo'>Media Files</div>");
        p.println(" </div>");
        p.println("</div>");
    }
}
