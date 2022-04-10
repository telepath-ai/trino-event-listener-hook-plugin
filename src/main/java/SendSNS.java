import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;
import org.json.JSONObject;

public class SendSNS implements Hook {
    private final String snsTopicArn;

    public SendSNS(final String snsTopicArn) {
        this.snsTopicArn = snsTopicArn;
    }

    public void sendHook(final JSONObject payload) {
        String message = payload.toString();
        String topicArn = this.snsTopicArn;

        // Create the Client
        SnsClient snsClient = SnsClient.create();

        try {
            // Send the Message
            PublishRequest request = PublishRequest.builder()
                .message(message)
                .topicArn(topicArn)
                .build();
            PublishResponse result = snsClient.publish(request);
//             System.out.println(result.messageId() + " Message sent. Status is " + result.sdkHttpResponse().statusCode());

         } catch (SnsException e) {
            System.out.println("Failed to send SNS Message.");
            System.err.println(e.awsErrorDetails().errorMessage());
            System.out.println("TopicARN: " + topicArn);
            System.out.println("Message: " + message);
         }

         // Close the Client
         snsClient.close();
    }
}
