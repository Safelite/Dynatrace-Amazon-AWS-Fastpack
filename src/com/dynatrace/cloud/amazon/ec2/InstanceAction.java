package com.dynatrace.cloud.amazon.ec2;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.dynatrace.diagnostics.pdk.Action;
import com.dynatrace.diagnostics.pdk.ActionEnvironment;
import com.dynatrace.diagnostics.pdk.Status;

public class InstanceAction
  implements Action
{
  private String loadBalancerName;
  private ArrayList<String> securityGroups;
  private String amiName;
  private String keyName;
  private String actionType;
  private int instances;
  private boolean enableMonitoring;
  private BasicAWSCredentials awsCredentials;

  public InstanceAction()
  {
    this.securityGroups = new ArrayList<String>();
  }

  public Status setup(ActionEnvironment env)
    throws Exception
  {
    String accessKeyId = env.getConfigString("accessKeyID");
    if ((accessKeyId == null) || (accessKeyId.equals(""))) {
      throw new IllegalArgumentException("Parameter <accesKeyID> must not be empty");
    }

    String secretAccessKey = env.getConfigString("secretAccessKey");
    if ((secretAccessKey == null) || (secretAccessKey.equals(""))) {
      throw new IllegalArgumentException("Parameter <secretAccessKey> must not be empty");
    }

    this.awsCredentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);

    this.loadBalancerName = env.getConfigString("loadBalancerName");
    this.enableMonitoring = env.getConfigBoolean("enableMonitoring").booleanValue();

    this.actionType = env.getConfigString("actionType");

    this.amiName = env.getConfigString("amiName");
    this.keyName = env.getConfigString("keyName");
    this.instances = env.getConfigLong("instances").intValue();
    parseSecurityGroups(env.getConfigString("securityGroups"));

    if (this.actionType.equals("start")) {
      if ((this.amiName == null) || (this.amiName.equals(""))) throw new IllegalArgumentException("Parameter <amiName> must not be empty");
      if ((this.keyName == null) || (this.keyName.equals(""))) throw new IllegalArgumentException("Parameter <keyName> must not be empty");
      if (this.securityGroups.size() < 1) throw new IllegalArgumentException("At least one security group must be specified");
    }

    return new Status(Status.StatusCode.Success);
  }

  private void parseSecurityGroups(String groupString)
  {
    this.securityGroups.clear();
    String[] strings = groupString.split(",");
    for (String groupName : strings)
      this.securityGroups.add(groupName.trim());
  }

  public Status execute(ActionEnvironment env)
    throws Exception
  {
    if ("start".equals(this.actionType)) {
      runInstance();
    }
    else
    {
      terminateInstance();
    }

    return new Status(Status.StatusCode.Success);
  }

  private void runInstance() {
    AmazonEC2 service = new AmazonEC2Client(this.awsCredentials);
    RunInstancesRequest request = new RunInstancesRequest();
    request.setImageId(this.amiName);
    request.setKeyName(this.keyName);
    request.setMinCount(Integer.valueOf(this.instances));
    request.setMaxCount(Integer.valueOf(this.instances));
    request.setMonitoring(Boolean.valueOf(this.enableMonitoring));

    if (this.securityGroups.size() > 0) {
      request.setSecurityGroups(this.securityGroups);
    }
    RunInstancesResult response = service.runInstances(request);
    Reservation reservation = response.getReservation();

    ArrayList instanceIDs = new ArrayList();

    for (com.amazonaws.services.ec2.model.Instance instance : reservation.getInstances()) {
      instanceIDs.add(instance.getInstanceId());
    }
    if (this.loadBalancerName.length() > 0)
      registerAtLoadBalancer(instanceIDs);
  }

  private void terminateInstance()
  {
    List instanceIds = getInstancesForLoadBalancer(this.loadBalancerName);

    int instancetoDelete = instanceIds.size() - this.instances;
    while ((instanceIds.size() > 0) && (instancetoDelete > 0)) {
      instanceIds.remove(0);
      --instancetoDelete;
    }

    if (instanceIds.size() > 0) {
      AmazonEC2 ec2service = new AmazonEC2Client(this.awsCredentials);

      if (this.loadBalancerName.length() > 0) {
        unregisterAtLoadBalancer(instanceIds);
      }

      TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest();
      terminateRequest.setInstanceIds(instanceIds);
      ec2service.terminateInstances(terminateRequest);
    }
  }

  private void registerAtLoadBalancer(List<String> instanceIDs)
  {
    AmazonElasticLoadBalancing service = new AmazonElasticLoadBalancingClient(this.awsCredentials);
    RegisterInstancesWithLoadBalancerRequest request = new RegisterInstancesWithLoadBalancerRequest();

    ArrayList instances = convert(instanceIDs);

    request.setInstances(instances);
    request.setLoadBalancerName(this.loadBalancerName);
    service.registerInstancesWithLoadBalancer(request);
  }

  private ArrayList<com.amazonaws.services.elasticloadbalancing.model.Instance> convert(List<String> instanceIDs)
  {
    ArrayList instances = new ArrayList();
    for (String instanceIds : instanceIDs) {
      instances.add(new com.amazonaws.services.elasticloadbalancing.model.Instance(instanceIds));
    }
    return instances;
  }

  private void unregisterAtLoadBalancer(List<String> instanceIDs) {
    AmazonElasticLoadBalancing service = new AmazonElasticLoadBalancingClient(this.awsCredentials);
    DeregisterInstancesFromLoadBalancerRequest request = new DeregisterInstancesFromLoadBalancerRequest();

    request.setInstances(convert(instanceIDs));
    request.setLoadBalancerName(this.loadBalancerName);
    service.deregisterInstancesFromLoadBalancer(request);
  }

  private List<String> getInstancesForLoadBalancer(String loadBalancerName)
  {
    ArrayList instanceIds = new ArrayList();

    AmazonElasticLoadBalancing service = new AmazonElasticLoadBalancingClient(this.awsCredentials);
    DescribeLoadBalancersRequest request = new DescribeLoadBalancersRequest();
    ArrayList loadBalancerNames = new ArrayList();
    loadBalancerNames.add(loadBalancerName);
    request.setLoadBalancerNames(loadBalancerNames);
    DescribeLoadBalancersResult response = service.describeLoadBalancers(request);
    List loadBalancerDescriptions = response.getLoadBalancerDescriptions();
    if (loadBalancerDescriptions.size() > 0) {
      LoadBalancerDescription desc = (LoadBalancerDescription)loadBalancerDescriptions.get(0);
      for (com.amazonaws.services.elasticloadbalancing.model.Instance instance : desc.getInstances()) {
        instanceIds.add(instance.getInstanceId());
      }
    }
    return instanceIds;
  }

  public void teardown(ActionEnvironment env)
    throws Exception
  {
  }
}