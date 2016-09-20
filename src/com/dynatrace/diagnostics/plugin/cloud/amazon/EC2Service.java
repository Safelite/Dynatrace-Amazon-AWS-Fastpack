package com.dynatrace.diagnostics.plugin.cloud.amazon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;

public class EC2Service extends CloudWatchService
{

	private final Collection<Dimension> instance;
	private final Map<String, String> volumeIds;
	private final Collection<Dimension> volumes;

	public EC2Service(AWSCredentials awsCredentials, ClientConfiguration clientConfiguration, String region,String instanceId, Map<String, String> volumeIds)
	{
		super(awsCredentials,clientConfiguration,region);
		this.volumeIds = volumeIds;
		this.volumes = new ArrayList<Dimension>(volumeIds.size());
		for (Map.Entry<String, String> volume : volumeIds.entrySet())
		{
			this.volumes.add(new Dimension().withName("VolumeId").withValue(volume.getKey()));
		}
		this.instance = Collections.singletonList(new Dimension().withName("InstanceId").withValue(instanceId));
	}

	@Override
	protected Collection<Dimension> getDimensions(String namespace)
	{
		if (namespace.equals("AWS/EC2"))
			return this.instance;
		if (namespace.equals("AWS/EBS"))
			return this.volumes;
		return null;
	}

	@Override
	protected void setValue(MonitorEnvironment env, MonitorMeasure metric, Dimension d, double value)
	{
		if (metric.getParameter("Namespace").equals("AWS/EBS"))
		{
			MonitorMeasure dynamic = env.createDynamicMeasure(metric, "VolumeName", this.volumeIds.get(d.getValue()));
			dynamic.setValue(value);
		}
		else {
			super.setValue(env, metric, d, value);
		}
	}
}