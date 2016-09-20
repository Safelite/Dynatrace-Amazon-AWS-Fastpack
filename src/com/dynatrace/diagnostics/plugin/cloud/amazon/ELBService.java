package com.dynatrace.diagnostics.plugin.cloud.amazon;

import java.util.Collection;
import java.util.Collections;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.model.Dimension;

public class ELBService extends CloudWatchService
{
  private final Collection<Dimension> d;

  public ELBService(BasicAWSCredentials awsCredentials, ClientConfiguration clientConfiguration,String region, String instance)
  {
    super(awsCredentials,clientConfiguration,region);
    this.d = Collections.singletonList(new Dimension().withName("LoadBalancerName").withValue(instance));
  }

  @Override
protected Collection<Dimension> getDimensions(String ns)
  {
    if (ns.equals("AWS/ELB"))
      return this.d;
    return null;
  }
}