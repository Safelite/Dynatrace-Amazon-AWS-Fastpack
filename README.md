
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Amazon AWS Fastpack</title>
    <link type="text/css" rel="stylesheet" href="css/blueprint/liquid.css" media="screen, projection"/>
    <link type="text/css" rel="stylesheet" href="css/blueprint/print.css" media="print"/>
    <link type="text/css" rel="stylesheet" href="css/content-style.css" media="screen, projection, print"/>
    <link type="text/css" rel="stylesheet" href="css/screen.css" media="screen, projection"/>
    <link type="text/css" rel="stylesheet" href="css/print.css" media="print"/>
</head>
<body>
    <div class="container" style="min-width: 760px;">
        <div class="header block">
            <div class="header-left column span-6">
                
            </div>
            <div class="column span-18 header-right last">
                <h4>Amazon AWS Fastpack</h4>
            </div>
        </div>
      </div>
<div>
        <div class="block">
            <div class="toc column span-6 prepend-top">
                <h3>Table of Contents
                                        <span class="small">(<a href="Amazon_AWS_Fastpack.html">Start</a>)</span>
                                    </h3>
                
<ul class="toc">
</ul>
<div>
            </div>
            <div>
            <div id="77922354" class="content column span-18 last">
                <h1>Amazon AWS Fastpack</h1>
    <div class="section-2"  id="77922354_AmazonAWSFastpack-Overview"  >
        <h2>Overview</h2>
    
    <div class="section-3"  id="77922354_AmazonAWSFastpack-AmazonAWSFastpack"  >
        <h3>Amazon AWS Fastpack</h3>
    
    <p>
            <img src="images_community/download/attachments/77922354/icon.png" alt="images_community/download/attachments/77922354/icon.png" class="confluence-embedded-image" />
        The dynaTrace FastPack for Amazon AWS enables easy out-of-the-box monitoring of various AWS solutions via Cloud Watch. The Fastpack consists of a customer Monitor, several Dashboards and a System Profile.    </p>
    <p>
    </p>
    <div class="confbox admonition admonition-note">
        
    <p>
This Fastpack is for dynaTrace 4.2 and contains new features. for older dynaTrace versions please look for the <a href="https://community/display/DL/Amazon+EC2+FastPack">Amazon EC2 FastPack</a>    </p>
    </div>
    
    </div>
    
    </div>
    
    <div class="section-2"  id="77922354_AmazonAWSFastpack-FastPackDetails"  >
        <h2>Fast Pack Details</h2>
    
    <div class="tablewrap">
        <table>
<thead class=" "></thead><tfoot class=" "></tfoot><tbody class=" ">    <tr>
            <td rowspan="1" colspan="1">
        <p>
Name    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
<strong class=" ">Amazon AWS FastPack</strong>    </p>
            </td>
        </tr>
    <tr>
            <td rowspan="1" colspan="1">
        <p>
Version    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
1.0.0    </p>
            </td>
        </tr>
    <tr>
            <td rowspan="1" colspan="1">
        <p>
dynaTrace Version    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
4.2    </p>
            </td>
        </tr>
    <tr>
            <td rowspan="1" colspan="1">
        <p>
Author    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
dynaTrace - Michael Kopp    </p>
            </td>
        </tr>
    <tr>
            <td rowspan="1" colspan="1">
        <p>
License    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
<a href="attachments_5275722_2_dynaTraceBSD.txt">dynaTrace BSD</a>    </p>
            </td>
        </tr>
    <tr>
            <td rowspan="1" colspan="1">
        <p>
Support    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
<a href="https://community/display/DL/Support+Levels#SupportLevels-Community">Not Supported </a>    </p>
            </td>
        </tr>
    <tr>
            <td rowspan="1" colspan="1">
        <p>
FastPack Contents    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
<a href="attachments_89096418_1_dynaTrace_AWSFastpack.dtp">Fastpack Download</a> contains:    </p>
<ul class=" "><li class=" ">    <p>
Cloud Watch Monitor for EC2, ELB and RDS    </p>
</li><li class=" ">    <p>
Amazon Simple DB Sensor Support    </p>
</li><li class=" ">    <p>
Monitoring Dashboards    </p>
</li></ul>            </td>
        </tr>
</tbody>        </table>
            </div>
    </div>
    
    <div class="section-2"  id="77922354_AmazonAWSFastpack-MonitoringEC2Instances"  >
        <h2>Monitoring EC2 Instances</h2>
    
    <p>
            <img src="images_community/download/attachments/77922354/ec2.png" alt="images_community/download/attachments/77922354/ec2.png" class="" />
            </p>
    <p>
The Instance Monitoring provides an overview of the relevant CloudWatch metrics for a specific cloud instance. The dashboard shows live and historical data about    </p>
<ul class=" "><li class=" ">    <p>
CPU Consumption    </p>
</li><li class=" ">    <p>
Network Activity and    </p>
</li><li class=" ">    <p>
Disk Activity    </p>
</li></ul>    <p>
This dashboard assists you in the verification of the health of your Amazon EC2 Instance. The CloudWatch monitor delivers the metrics for all attached EBS volumes separately as dynamic measures, but the dashboard simply charts a summary.    </p>
    </div>
    
    <div class="section-2"  id="77922354_AmazonAWSFastpack-AmazonEC2LoadBalancerDashboard"  >
        <h2>Amazon EC2 Load Balancer Dashboard</h2>
    
    <p>
            <img src="images_community/download/attachments/76480998/ELB.png" alt="images_community/download/attachments/76480998/ELB.png" class="" />
            </p>
    <p>
The load balancer dashboard provides an overview of load balancer specific metrics like Latency, the number of healthy and unhealthy hosts and the number of errors produced by the load balancer as opposed to the application itself.    </p>
    </div>
    
    <div class="section-2"  id="77922354_AmazonAWSFastpack-MonitoringRDSInstances"  >
        <h2>Monitoring RDS Instances</h2>
    
    <p>
            <img src="images_community/download/attachments/76480998/database.png" alt="images_community/download/attachments/76480998/database.png" class="" />
            </p>
    <p>
The RDS Monitoring provides an overview of the relevant metrics of a specific RDS instances. It is meant to be viewed for a single RDS instance . The dashboard shows live and historical data about    </p>
<ul class=" "><li class=" ">    <p>
CPU Consumption    </p>
</li><li class=" ">    <p>
Average I/O operations load and throughput    </p>
</li><li class=" ">    <p>
Average I/O latency    </p>
</li><li class=" ">    <p>
Number of Connections    </p>
</li><li class=" ">    <p>
Memory and Swap usage    </p>
</li><li class=" ">    <p>
Time lag of the Replicas    </p>
</li></ul>    <p>
This dashboard assists you in the verification of the health of your Amazon RDS Instance and can help explain why SQL statements executed against that instance might have been slow.    </p>
    </div>
    
    <div class="section-2"  id="77922354_AmazonAWSFastpack-AmazonCloudWatchMonitor"  >
        <h2>Amazon CloudWatch Monitor</h2>
    
    <p>
The Amazon CloudWatch Monitor supports monitoring of instances EC2 and RDS Instances as well as load balancers. If need it to be extended to any other CloudWatch provider please ask so in the forum.<br/>You will receive data even if you do not enable cloud watch specifically, but for most types of data it will have only a 5 minute granularity. If you enable detailed monitoring you will receive data every minute. If you have detailed monitoring for only some of the instances, set it up to be scheduled once a minute and the monitor will do the rest.    </p>
    <p>
The Monitor is configured by supplying the following parameters:    </p>
<ul class=" "><li class=" ">    <p>
Your Amazon AWS AccessKeyID.    </p>
</li><li class=" ">    <p>
Your Amazon AWS SecretAccesskey.    </p>
</li><li class=" ">    <p>
Http Proxy and port if that is necessary in your environment    </p>
</li><li class=" ">    <p>
Additional Amazon Zones to check, separated by a semi colon (for a list of regions look <a href="http://docs.amazonwebservices.com/general/latest/gr/rande.html">here</a>).<br/>On default the Monitor only checks the default the us-east-1 region, with this setting you can add other zones to check as well.    </p>
</li></ul>    <p>
The monitor will use the supplied host (or host list) to figure out the instances to monitor. In case you run completely in the EC2 Cloud you can just add the hostgroup &quot;Local&quot; to the monitor and it will automatically monitor all your ec2 instances that have a dynaTrace agent placed.<br/>In order to monitor RDS and ELB instances simply add the hostname to the infrastructure section of the server configuration and make sure it is within the monitored host group.<br/>You do not have to setup a separate monitor for EC2 or RDS, the monitor will figure this out on its own.    </p>
    <p>
The monitor uses the auto discovered hostnames to identify the EC2 instance. While this works from outside the EC2 cloud, it works best if the collector that does the monitoring sits itself in the same EC2 region as the instances it monitors.    </p>
    <p>
In addition to using the monitor we recommend using the dynaTrace Agent and Collector to get more detailed system and application metrics.    </p>
    </div>
    
    <div class="section-2"  id="77922354_AmazonAWSFastpack-FastPackInformation"  >
        <h2>FastPack Information</h2>
    
    <p>
The Amazon AWS FastPack contains everything to get started with Amazon cloud monitoring.    </p>
<ul class=" "><li class=" ">    <p>
A system profile with preconfigured schedules for monitoring EC2 and RDS Instances as well as Elastic Load Balancers.    </p>
</li><li class=" ">    <p>
The EC2 Instance monitoring dashboard    </p>
</li><li class=" ">    <p>
The RDS Instance monitoring dashboard    </p>
</li><li class=" ">    <p>
The Load Balancer monitoring dashboard.    </p>
</li></ul>    </div>
    
    <div class="section-2"  id="77922354_AmazonAWSFastpack-Installation"  >
        <h2>Installation</h2>
    
<ol class=" "><li class=" ">    <p>
Download and install the Fastpack to your dynaTrace Server    </p>
</li></ol>    </div>
    
            </div>
        </div>

        <div class="footer">
            Created with <a href="http://k15t.com/display/en/Scroll-Wiki-HTML-Exporter-for-Confluence-Overview">Scroll Wiki HTML Exporter for Confluence</a>.
        </div>
    </div>
</body>
</html>
