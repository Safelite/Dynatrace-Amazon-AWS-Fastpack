package com.dynatrace.diagnostics.plugin.cloud.amazon;

import java.io.Closeable;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;

public abstract class CloudWatchService
		implements Closeable
{

	final Logger log = Logger.getLogger(getClass().getName());
	private AmazonCloudWatch service;
	protected long lastCheck = 0L;
	protected final AWSCredentials awsCredentials;
	protected long lastMeasureTimeInstance;
	private final Map<MetricCode, Long> Checks = new HashMap<MetricCode, Long>();
	private final ClientConfiguration clientConfiguration;
	private final String region;

	private static class MetricCode
	{
		private final String name;
		private final String group;
		public final String dimension;
		private final int hashCode;

		private MetricCode(String name, String group, String dimension) {
			super();
			this.name = name;
			this.group = group;
			this.dimension = dimension;
					int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result
					+ ((group == null) ? 0 : group.hashCode());
			result = prime * result
					+ ((dimension == null) ? 0 : dimension.hashCode());
			this.hashCode = result;

		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MetricCode other = (MetricCode) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (group == null) {
				if (other.group != null)
					return false;
			} else if (!group.equals(other.group))
				return false;
			if (dimension == null) {
				if (other.dimension != null)
					return false;
			} else if (!dimension.equals(other.dimension))
				return false;
			return true;
		}
	}

	public CloudWatchService(AWSCredentials awsCredentials, ClientConfiguration clientConfiguration, String region) {
		this.awsCredentials = awsCredentials;
		this.clientConfiguration = clientConfiguration;
		this.region = region;
	}

	private AmazonCloudWatch getService() {
		if (this.awsCredentials == null)
			throw new IllegalStateException("Credentials haven't been set yet");
		if (this.service == null)
			this.service = Utils.setEndPoint(new AmazonCloudWatchClient(this.awsCredentials,clientConfiguration),region);
		return this.service;
	}

	private Utils.MetricResult queryMetricAvg(GetMetricStatisticsRequest request, long lastMetricTime)
	{
		Utils.MetricResult result = null;
		GetMetricStatisticsResult response;
		try {
			response = getService().getMetricStatistics(request);
		} catch (AmazonClientException c) {
			log.log(Level.SEVERE,"Could not request metric: " + request.getNamespace() + '/' + request.getMetricName(),c);
			this.service = null;
			throw c;
		}

		List<Datapoint> dataPoints = response.getDatapoints();

		for (Datapoint data : dataPoints) {
			long dataPointTime = data.getTimestamp().getTime();
			if ((dataPointTime > lastMetricTime) && (((result == null) || (dataPointTime > result.measureTime)))) {
				if (result == null)
					result = new Utils.MetricResult();
				result.measureTime = dataPointTime;
				if (request.getStatistics().contains("Average"))
					result.value = data.getAverage().doubleValue();
				else if (request.getStatistics().contains("Sum"))
					result.value = data.getSum().doubleValue();
			}
		}
		return result;
	}

	public void close() throws IOException
	{
		if (this.service == null)
			return;
		this.service.shutdown();
		this.service = null;
	}

	public void execute(MonitorEnvironment env)
	{
		long now = System.currentTimeMillis() / 1000L;
		if (now - this.lastCheck < 60L) {
			return;
		}
		Calendar endTime = new GregorianCalendar(TimeZone.getTimeZone("UTC"));

		endTime.add(13, -1 * endTime.get(13));
		Calendar startTime = (Calendar) endTime.clone();
		startTime.add(12, -10);

		GetMetricStatisticsRequest request = new GetMetricStatisticsRequest();
		if (this.lastCheck != 0L)
			request.setPeriod(Integer.valueOf((int) (now - this.lastCheck) / 60 * 60)); // get a sum since the last call
		else
			request.setPeriod(Integer.valueOf(60));
		request.setStartTime(startTime.getTime());
		request.setEndTime(endTime.getTime());
		request.getDimensions().add(null);

		Map<MetricCode, Long> oldChecks = new HashMap<MetricCode, Long>(this.Checks);

		for (MonitorMeasure metric : env.getMonitorMeasures())
		{
			request.setStatistics(Collections.singleton(metric.getParameter("Statistics")));
			String ns = metric.getParameter("Namespace");
			if (ns == null)
				continue;
			Collection<Dimension> dimensions = getDimensions(ns);
			if (dimensions != null)
			{
				request.setNamespace(ns);
				request.setMetricName(metric.getMeasureName());
				for (Dimension d : dimensions)
				{
					request.getDimensions().set(0, d);

					final MetricCode key = new MetricCode(metric.getMeasureName(),metric.getMetricGroupName(),d.getValue());
					Long lastMetricTime = oldChecks.get(key);

					Utils.MetricResult result = queryMetricAvg(request,
							(lastMetricTime == null) ? 0L : lastMetricTime);
					if (result != null)
					{
						setValue(env, metric, d, result.value);

						if ((!(this.Checks.containsKey(d))) || (result.measureTime > this.Checks.get(d)))
							this.Checks.put(key, Long.valueOf(result.measureTime));
					}
				}
			}
		}
	}

	protected void setValue(MonitorEnvironment env, MonitorMeasure metric, Dimension d, double value)
	{
		metric.setValue(value);
		log.info("Set " + metric.getMeasureName() + " to " + value + " for " + d.toString());
	}

	protected abstract Collection<Dimension> getDimensions(String paramString);
}