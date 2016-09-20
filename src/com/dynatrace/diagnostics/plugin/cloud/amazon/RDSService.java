package com.dynatrace.diagnostics.plugin.cloud.amazon;

import java.util.Collection;
import java.util.Collections;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.model.Dimension;

public class RDSService extends CloudWatchService
{
  private final Collection<Dimension> dbInstance;

  public RDSService(BasicAWSCredentials awsCredentials, ClientConfiguration clientConfiguration,String region, String dbInstanceIdentifier)
  {
    super(awsCredentials,clientConfiguration,region);
    this.dbInstance = Collections.singletonList(new Dimension().withName("DBInstanceIdentifier").withValue(dbInstanceIdentifier));
  }

  @Override
protected Collection<Dimension> getDimensions(String namespace)
  {
    if (namespace.equals("AWS/RDS"))
      return this.dbInstance;
    return null;
  }
}