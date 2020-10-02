package com.takipi.oss.storage.resources.diag;

import com.takipi.oss.storage.data.status.MachineStatus;
import com.takipi.oss.storage.helper.StatusUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/storage/v1/diag/status")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.APPLICATION_JSON)
public class MachineInfoOnlyStatusStorageResource {
	private static final Logger logger = LoggerFactory.getLogger(MachineInfoOnlyStatusStorageResource.class);
	
	@POST
	public Response post() {
		try {
			MachineStatus machineStatus = new MachineStatus();
			
			collectMachineInfo(machineStatus);

			return Response.ok(machineStatus).build();
		} catch (Exception e) {
			logger.error("Failed retrieving System Status", e);
			return Response.serverError().entity("Failed retrieving System Status").build();
		}
	}
	
	private void collectMachineInfo(MachineStatus machineStatus) {
		machineStatus.setMachineName(StatusUtil.getMachineName());
		machineStatus.setPid(StatusUtil.getProcessId());
		machineStatus.setJvmUpTimeMillis(StatusUtil.getJvmUpTimeInMilli());
		machineStatus.setAvailableProcessors(StatusUtil.getAvailableProcessors());
		machineStatus.setLoadAverage(StatusUtil.getLoadAvg());
		machineStatus.setProcessCpuLoad(StatusUtil.getProcessCpuLoad());
		machineStatus.setTotalRamBytes(StatusUtil.getTotalRamInBytes());
		machineStatus.setUsedRamBytes(StatusUtil.getUsedRamInBytes());
		machineStatus.setHeapSizeBytes(StatusUtil.getHeapSizeInBytes());
		machineStatus.setPermGenSizeBytes(StatusUtil.getPermGenSizeInBytes());
	}
}
