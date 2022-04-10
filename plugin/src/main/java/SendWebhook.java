import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

public class SendWebhook implements Hook {
    private final String webhookUrl;

    public SendWebhook(final String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void sendHook(final JSONObject payload) {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // Construct the payload
        String postJsonString = payload.toString();

        try {
            // Create the request
            HttpPost httpPost = new HttpPost(this.webhookUrl);
            StringEntity stringEntity = new StringEntity(postJsonString, ContentType.APPLICATION_JSON);
            httpPost.setEntity(stringEntity);
            // Send
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                // Print response
                String responseString = EntityUtils.toString(response.getEntity());
//                 System.out.println(responseString);
            } catch (Exception e) {
                System.out.println("Failed to read webhook response.");
                System.out.println("URL: " + this.webhookUrl);
                System.out.println("Payload: " + postJsonString);
                e.printStackTrace();
            } finally {
                response.close();
            }
        } catch (Exception e) {
            System.out.println("Failed to POST webhook event.");
            System.out.println("URL: " + this.webhookUrl);
            System.out.println("Payload: " + postJsonString);
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                System.out.println("HttpClient failed to close");
                e.printStackTrace();
            }
        }
    }
}
