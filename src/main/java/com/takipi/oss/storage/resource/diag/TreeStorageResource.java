package com.takipi.oss.storage.resource.diag;

import com.takipi.oss.storage.TakipiStorageConfiguration;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/storage/v1/diag/tree")
@Consumes({"text/plain"})
@Produces({"application/json"})
public class TreeStorageResource {
  private static final Logger logger = LoggerFactory.getLogger(TreeStorageResource.class);
  
  protected final String folderPath;
  
  @Inject
  public TreeStorageResource() {
    TakipiStorageConfiguration configuration = new TakipiStorageConfiguration();
    this.folderPath = configuration.getFolderFs().getFolderPath();
  }
  
  @GET
  public Response get() {
    try {
      return Response.ok("{ \"result\": \"" + getTreeFormationOfFileSystem() + "\"}").build();
    } catch (Exception e) {
      logger.error("Failed retrieving File System Tree", e);
      return Response.serverError().entity("Failed retrieving File System Tree").build();
    } 
  }
  
  private String getTreeFormationOfFileSystem() {
    File dir = new File(this.folderPath);
    return getDirectoryTree(dir);
  }
  
  private String getDirectoryTree(File folder) {
    if (!folder.isDirectory())
      throw new IllegalArgumentException("Folder is not a Directory"); 
    StringBuilder sb = new StringBuilder();
    traverseDirectory(folder, 0, sb);
    return sb.toString();
  }
  
  private void traverseDirectory(File folder, int indent, StringBuilder sb) {
    appendDirectoryInfo(folder, indent, sb);
    for (File file : folder.listFiles()) {
      if (!file.isHidden())
        if (file.isDirectory()) {
          traverseDirectory(file, indent + 1, sb);
        } else {
          appendFileInfo(file, indent + 1, sb);
        }  
    } 
  }
  
  private void appendDirectoryInfo(File folder, int indent, StringBuilder sb) {
    appendNodeToTree(sb, getIndentString(indent), "+-- ", folder.getName(), "/\n");
  }
  
  private void appendFileInfo(File file, int indent, StringBuilder sb) {
    String indentation = getIndentString(indent);
    appendNodeToTree(sb, indentation, "+-| ", file.getName(), "\n");
    appendNodeToTree(sb, indentation, "  - ", getFileIntoString(file), "\n");
  }
  
  private void appendNodeToTree(StringBuilder sb, String indentation, String prefix, String content, String postfix) {
    sb.append(indentation);
    sb.append(prefix);
    sb.append(content);
    sb.append(postfix);
  }
  
  private String getFileIntoString(File file) {
    try {
      String permissions = PosixFilePermissions.toString(Files.getPosixFilePermissions(Paths.get(file.getAbsolutePath(), new String[0]), new java.nio.file.LinkOption[0]));
      String modifiedDate = Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/uu"));
      String fileSize = (FileUtils.sizeOf(file) / 1024L) + "Kb";
      return "\t" + permissions + "\t" + modifiedDate + "\t" + fileSize;
    } catch (Exception e) {
      logger.warn("Failed getting file info.", e);
      return "";
    } 
  }
  
  private String getIndentString(int indent) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < indent; i++)
      sb.append("|  "); 
    return sb.toString();
  }
}

