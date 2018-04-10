package com.takipi.oss.storage.resources.fs;

import java.io.File;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Predicate;
import com.takipi.oss.storage.TakipiStorageConfiguration;
import com.takipi.oss.storage.data.simple.SimpleSearchRequest;
import com.takipi.oss.storage.data.simple.SimpleSearchResponse;
import com.takipi.oss.storage.helper.FilesystemUtil;
import com.takipi.oss.storage.resources.fs.base.SimpleFileSystemStorageResource;

@Path("/storage/v1/json/simplesearch")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonSimpleSearchStorageResource extends SimpleFileSystemStorageResource {
	private static final Logger logger = LoggerFactory.getLogger(JsonSimpleSearchStorageResource.class);

	public JsonSimpleSearchStorageResource(TakipiStorageConfiguration configuration) {
		super(configuration);
	}

	@POST
	@Timed
	public Response post(SimpleSearchRequest request) {
		try {
			return handleResponse(request);
		} catch (Exception e) {
			return Response.serverError().entity("Problem simple searching").build();
		}
	}

	private Response handleResponse(SimpleSearchRequest request) {
		try {
			File searchRoot = new File(fs.getRoot(), FilesystemUtil.fixPath(request.baseSearchPath));
			
			ResourceFileCallback fileCallback = new ResourceFileCallback(request.name, request.preventDuplicates);
			FilesystemUtil.listFilesRecursively(searchRoot, fileCallback);
			File result = fileCallback.getFoundFile();
			
			if (result == null) {
				return searchFailed(request.name);
			}
			
			String relFSPath = result.getAbsolutePath().replace(fs.getRoot().getAbsolutePath(), "");
			String data = FilesystemUtil.read(fs, relFSPath, request.encodingType);
			
			if (data == null) {
				return searchFailed(request.name);
			}
			
			return Response.ok(new SimpleSearchResponse(data, relFSPath.replace(request.name, ""))).build();
			
		} catch (Exception e) {
			logger.error("Problem getting: " + request.name, e);
			return Response.serverError().entity("Problem getting " + request.name).build();
		}
	}
	
	private Response searchFailed(String name) {
		logger.warn("File not found: {}", name);
		return Response.status(404).entity("File not found" + name).build();
	}
	
	private static class ResourceFileCallback implements Predicate<File>
	{
		private final String resourceName;
		private final boolean preventDuplicates;
		
		private File foundFile;
		
		protected ResourceFileCallback(String resourceName, boolean preventDuplicates)
		{
			this.resourceName = resourceName;
			this.preventDuplicates = preventDuplicates;
			
			this.foundFile = null;
		}
		
		@Override
		public boolean apply(File file)
		{
			return test(file);
		}
		
		@Override
		public boolean test(File file)
		{
			if (!resourceName.equals(file.getName()))
			{
				return false;
			}
			
			if ((preventDuplicates) &&
				(foundFile != null))
			{
				foundFile = null; // never find more than one result if preventing duplicates
				return true;
			}
			
			foundFile = file;
			
			return !preventDuplicates; // if we don't prevent duplicates, we stop right now
		}
		
		public File getFoundFile()
		{
			return foundFile;
		}
	}
}
