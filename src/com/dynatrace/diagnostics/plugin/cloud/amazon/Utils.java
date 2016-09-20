package com.dynatrace.diagnostics.plugin.cloud.amazon;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;

public class Utils
{

	public static void setMeasureValues(String group, MonitorEnvironment env, String measureName, double value)
	{
		Collection<MonitorMeasure> measures = env.getMonitorMeasures(group, measureName);
		setValues(measures, value);
	}

	public static void setValues(Collection<MonitorMeasure> ms, double value)
	{
		for (MonitorMeasure m : ms)
		{
			m.setValue(value);
		}
	}

	public static Instance tryIdentifyEC2Instance(AmazonEC2Client amazonEC2Client, String address)
	{
		DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
		Filter f = null;
		try
		{
			CloudWatchMonitor.log.severe("Check EC2 Environment");
			if (!(InetAddress.getByAddress(new byte[] { -87, -2, -87, -2 }).getHostName().equals("169.254.169.254")))
			{
				InetAddress byName = InetAddress.getByName(address);
				f = new Filter("private-ip-address", Arrays.asList(new String[] { byName.getHostAddress() }));
			} else {
				CloudWatchMonitor.log.severe("Not In EC2 Environment, guess hostnames");
				f = guessHostNames(address);
			}
		} catch (UnknownHostException e)
		{
			CloudWatchMonitor.log.severe("In EC2 but could not resolve adress: " + address + ". Go guessing");
			f = guessHostNames(address);
		}

		describeInstancesRequest.setFilters(Arrays.asList(new Filter[] { f }));

		DescribeInstancesResult describeInstances = amazonEC2Client.describeInstances(describeInstancesRequest);
		CloudWatchMonitor.log.severe("Try Resolve hostname \"" + address + "\":" + describeInstancesRequest);

		if ((describeInstances.getReservations().isEmpty()) ||
				(describeInstances.getReservations().get(0).getInstances().isEmpty()))
			return null;
		if ((describeInstances.getReservations().size() > 1) ||
				(describeInstances.getReservations().get(0).getInstances().size() > 1))
		{
			throw new IllegalStateException("Found more than one Instance under the same hostname: " + f);
		}
		return describeInstances.getReservations().get(0).getInstances().get(0);
	}

	static Filter guessHostNames(String address)
	{
		Filter f;
		if (address.matches("ip-\\p{XDigit}{8}"))
		{
			f = new Filter("private-ip-address", Arrays.asList(new String[] { "" + Integer.parseInt(address.substring(3, 5), 16) +
					"." + Integer.parseInt(address.substring(5, 7), 16) + "." + Integer.parseInt(address.substring(7, 9), 16) +
					"." + Integer.parseInt(address.substring(9, 11), 16) }));
		}
		else
		{
			if (address.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))
			{
				f = new Filter("private-ip-address", Arrays.asList(new String[] { address }));
			} else {
				List<String> possible = new ArrayList<String>();
				possible.add(address);
				possible.add(address + ".*.internal");
				if (address.matches("dom[uU]-\\p{XDigit}{2}-\\p{XDigit}{2}-\\p{XDigit}{2}-\\p{XDigit}{2}-\\p{XDigit}{2}-\\p{XDigit}{2}$"))
					possible.add("dom" + address.substring(3).toUpperCase() + ".*.internal");
				else if (address.matches("dom[uU]-\\p{XDigit}{2}-\\p{XDigit}{2}-\\p{XDigit}{2}-\\p{XDigit}{2}-\\p{XDigit}{2}-\\p{XDigit}{2}.*.internal"))
					possible.add("dom" + address.substring(3, 22).toUpperCase() + address.substring(22));
				f = new Filter("private-dns-name", possible);
			}
		}
		return f;
	}

	public static class MetricResult
	{

		public double value;
		public long measureTime;

		public MetricResult()
		{
		}

		public MetricResult(double value, long time)
		{
			this.value = value;
			this.measureTime = time;
		}
	}

	public static <T extends AmazonWebServiceClient> T setEndPoint(T aws, String region)
	{
		if (region != null)
		{
			String reg;
			if (aws instanceof AmazonEC2Client)
				reg = "https://ec2." + region + ".amazonaws.com";
			else if (aws instanceof AmazonRDSClient)
				reg = "https://rds." + region + ".amazonaws.com";
			else if (aws instanceof AmazonElasticLoadBalancingClient)
				reg = "https://elasticloadbalancing." + region + ".amazonaws.com";
			else if (aws instanceof AmazonCloudWatchClient)
				reg = "https://monitoring." + region + ".amazonaws.com";
			else throw new IllegalArgumentException("unhandled AmazonService: " + aws.getClass()); // break off
			CloudWatchMonitor.log.info("Region is: " + reg);
			aws.setEndpoint(reg);
		}
		return aws;

	}


}