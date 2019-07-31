package com.takipi.oss.storage.resources.diag;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.takipi.oss.storage.TakipiStorageConfiguration;
import com.takipi.oss.storage.data.status.MachineStatus;
import com.takipi.oss.storage.helper.MachineStatusCollector;

@Path("/storage/v1/diag/status")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.APPLICATION_JSON)
public class StatusStorageResource {
	private final String basePath;
	
	public StatusStorageResource(TakipiStorageConfiguration configuration) {
		this.basePath = configuration.getFolderPath();
	}
	
	@POST
	@Timed
	public Response post() {
		MachineStatusCollector collector = new MachineStatusCollector(basePath);
		MachineStatus machineStatus = collector.collect();
		
		if (machineStatus != null) {
			return Response.ok(machineStatus).build();
		} else {
			return Response.serverError().entity("Failed retrieving System Status").build();
		}
	}
}
