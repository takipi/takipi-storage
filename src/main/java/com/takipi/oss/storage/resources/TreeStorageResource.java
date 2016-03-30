package com.takipi.oss.storage.resources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

@Path("/storage/v1/diag/tree")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class TreeStorageResource {
	private static final Logger logger = LoggerFactory.getLogger(TreeStorageResource.class);
	
	protected final String folderPath;
	
	public TreeStorageResource(String folderPath) {
		this.folderPath = folderPath;
	}
	
	@GET
	@Timed
	public Response get() {
		try {
			return Response.ok(getTreeFormationOfFileSystem()).build();
		} catch (Exception e) {
			logger.error("Failed retrieving File System Tree", e);
			return Response.serverError().entity("Failed retrieving File System Tree").build();
		}
	}
	
	private String getTreeFormationOfFileSystem() {
		File dir = new File(folderPath);
		
		return getDirectoryTree(dir);
	}
	
	private String getDirectoryTree(File folder) {
		if (!folder.isDirectory()) {
			throw new IllegalArgumentException("Folder is not a Directory");
		}
		
		StringBuilder sb = new StringBuilder();
		
		traverseDirectory(folder, 0, sb);
		
		return sb.toString();
	}
	
	private void traverseDirectory(File folder, int indent, StringBuilder sb) {
		appendDirectoryInfo(folder, indent, sb);
		indent++;
		
		for (File file : folder.listFiles()) {
			if (file.isHidden()) {
				continue;
			}
			
			if (file.isDirectory()) {
				traverseDirectory(file, indent, sb);
			} else {
				appendFileInfo(file, indent, sb);
			}
		}
	}
	
	private void appendDirectoryInfo(File folder, int indent, StringBuilder sb)
	{
		appendNodeToTree(sb, getIndentString(indent), "+-- ", folder.getName(), "/\n");
	}
	
	private void appendFileInfo(File file, int indent, StringBuilder sb) {
		String indentation = getIndentString(indent);
		
		appendNodeToTree(sb, indentation, "+-| ", file.getName(), "\n");
		appendNodeToTree(sb, indentation, "  - ", getFileIntoString(file), "\n");
	}
	
	private void appendNodeToTree(StringBuilder sb, String indentation, String prefix, String content, String postfix)
	{
		sb.append(indentation);
		sb.append(prefix);
		sb.append(content);
		sb.append(postfix);
	}
	
	private String getFileIntoString(File file) {
		try {
			String permissions = PosixFilePermissions.toString(Files.getPosixFilePermissions(Paths.get(file.getAbsolutePath())));
			String modifiedDate = new DateTime(file.lastModified()).toString("dd/MM/YY");
			String fileSize = (FileUtils.sizeOf(file) / 1024) + "Kb";
			
			return "\t" + permissions + "\t" + modifiedDate + "\t" + fileSize;
		} catch (IOException e) {
			logger.warn("Failed getting file info.", e);
			return "";
		}
	}
	
	private String getIndentString(int indent) {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < indent; i++) {
			sb.append("|  ");
		}
		
		return sb.toString();
	}
}