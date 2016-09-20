package com.dynatrace.diagnostics.plugin.cloud.amazon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceBlockDeviceMapping;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.dynatrace.diagnostics.pdk.Monitor;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.PluginEnvironment.Host;
import com.dynatrace.diagnostics.pdk.Status;

public class CloudWatchMonitor implements Monitor
{

	static final Logger log = Logger.getLogger(CloudWatchMonitor.class.getName());
	private CloudWatchService service;

	public Status setup(MonitorEnvironment env)
			throws Exception
	{
		String accessKeyId = env.getConfigString("accessKeyID");
		if ((accessKeyId == null) || (accessKeyId.length() == 0)) {
			throw new IllegalArgumentException("Parameter <accesKeyID> must not be empty");
		}
		String secretAccessKey = env.getConfigString("secretAccessKey");
		if ((secretAccessKey == null) || (secretAccessKey.length() == 0))
			throw new IllegalArgumentException("Parameter <secretAccessKey> must not be empty");
		String region = env.getConfigString("region");
		ClientConfiguration clientConfiguration = new ClientConfiguration();

		String proxyHost = env.getConfigString("httpProxy");
		if (proxyHost != null)
		{
			clientConfiguration = clientConfiguration.withProxyHost(proxyHost);
			long port=0;
			port = env.getConfigLong("httpProxyPort");
			if (port != 0)
				clientConfiguration.setProxyPort((int) port);
		}
		try {
			BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
			AmazonEC2Client amazonEC2Client = new AmazonEC2Client(awsCredentials,clientConfiguration );
			Utils.setEndPoint(amazonEC2Client,region);
			String instanceId = env.getConfigString("instanceID");
			Host host = env.getHost();

			Map<String,String> volumeIds = new HashMap<String,String>();
			if (host != null) {
				Instance i = Utils.tryIdentifyEC2Instance(amazonEC2Client, host.getAddress());

				if (i != null) {
					log.severe("Resolved hostname \"" + host.getAddress() + "\":" + i.getInstanceId());
					for (InstanceBlockDeviceMapping b : i.getBlockDeviceMappings()) {
						if (b.getEbs() != null) {
							volumeIds.put(b.getEbs().getVolumeId(), b.getDeviceName());
						}
					}
					instanceId = i.getInstanceId();
					service = new EC2Service(awsCredentials,clientConfiguration,region, instanceId, volumeIds);
				}
				else {
					DescribeDBInstancesResult describeDBInstances = Utils.setEndPoint(new AmazonRDSClient(awsCredentials,clientConfiguration),region).describeDBInstances();

					for (DBInstance db : describeDBInstances.getDBInstances())
					{
						if (db.getEndpoint().getAddress().equals(host.getAddress()))
						{
							service = new RDSService(awsCredentials,clientConfiguration,region, db.getDBInstanceIdentifier());
							break;
						}
					}

					if (service == null)
					{
						DescribeLoadBalancersResult describeLoadBalancers = Utils.setEndPoint(new AmazonElasticLoadBalancingClient(awsCredentials,clientConfiguration),region).describeLoadBalancers();
						for (LoadBalancerDescription lb : describeLoadBalancers.getLoadBalancerDescriptions())
						{
							if (lb.getDNSName().equals(host.getAddress()))
							{
								service = new ELBService(awsCredentials,clientConfiguration,region, lb.getLoadBalancerName());
								break;
							}
						}
					}
					if (service == null)
						return new Status(Status.StatusCode.ErrorTargetService,
								"Could not find either EC2 or RDS Instance with defined hostname");
				}
			}
			else if (instanceId != null) {
				instanceId = instanceId.trim();

				DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
				describeInstancesRequest.setInstanceIds(Collections.singleton(instanceId));
				DescribeInstancesResult describeInstances = amazonEC2Client.describeInstances(describeInstancesRequest);
				if (describeInstances.getReservations().isEmpty())
					return new Status(Status.StatusCode.ErrorTargetService,
							"Could not find EC2 Instance with defined instance id");
				for (InstanceBlockDeviceMapping b : describeInstances.getReservations().get(0).getInstances().get(0).getBlockDeviceMappings()) {
					if (b.getEbs() != null) {
						volumeIds.put(b.getEbs().getVolumeId(), b.getDeviceName());
					}
				}

				log.severe("Volumes of instance \"" + instanceId + "\":" + volumeIds);
				service = new EC2Service(awsCredentials,clientConfiguration,region, instanceId, volumeIds);
			} else {
				throw new IllegalArgumentException("Either <hostname> or <instanceID> must be specified");
			}
			return new Status(Status.StatusCode.Success);
		} catch (AmazonServiceException e) {
			log.log(Level.SEVERE, "Unexpected AWS Error:", e);
			return new Status(Status.StatusCode.ErrorTargetServiceExecutionFailed, "Could not setup due to AWS Error",
					e.getMessage(), e);
		}
	}

	public Status execute(MonitorEnvironment env)
			throws Exception
	{
		try
		{
			log.info("Execute Monitor: " + this.toString());
			service.execute(env);
		} catch (AmazonServiceException e) {
			log.log(Level.SEVERE, "Unexpected AWS Error:", e);
			return new Status(Status.StatusCode.ErrorTargetServiceExecutionFailed, "Could not execute due to AWS Error",
					e.getMessage(), e);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.log(Level.SEVERE, ex.toString(), ex);
			return new Status(Status.StatusCode.ErrorInternal, ex.toString());
		}
		return new Status(Status.StatusCode.Success);
	}

	public void teardown(MonitorEnvironment env)
			throws Exception
	{
		if (service != null)
			service.close();
		service = null;
	}
}