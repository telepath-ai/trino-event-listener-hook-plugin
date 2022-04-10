# trino-event-listener-hook-plugin
A very basic plugin for Trino that sends information from the EventListener to a webhook URL and/or an AWS SNS topic.

## Build
```
mvn clean package
```

## Deploy

### Copy artifacts to Trino container
After building the package, copy the following file to the Trino plugin directory (<path_to_trino>/plugin/event-listener-hook/)
```
target/event-listener-hook.jar
```

### Enable and configure plugin
Trino looks for EventListener plugins in a specific properties file. Create `<path_to_trino>/etc/event-listener.properties` file with the contents below to enable the plugin:

```
event-listener.name=event-listener-hook
```

#### Send events to a Webhook
If you want to send the events to a webhook URL, then you'll need an extra config value in your `event-listener.properties` file:
```
event-listener.name=event-listener-hook
event-listener-hook.webhookUrl=http://example.com/my-webhook
```

#### Publish events to an SNS topic
```
event-listener.name=event-listener-hook
event-listener-hook.snsTopicArn=<SNS_TOPIC_ARN>
```

*Note:* Trino will need `sns:Publish` IAM permissions for the given topic. How you do this will vary depending on your Trino deployment. 
If your deployment is running in Kubernetes, then you can grant the permissions to the Service Account that's running the pod. 
If you have an access token and secret, you can pass them as environmental variables on the Trino container, and the AWS SDK will 
automatically use them when it creates the SNS client. Here's a basic example if you were running Trino in a local container:
```
docker run -p 8080:8080 ---env AWS_ACCESS_KEY_ID=<KEY> --env AWS_SECRET_ACCESS_KEY=<SECRET> --env AWS_REGION=us-west-2 trinodb/trino
```
