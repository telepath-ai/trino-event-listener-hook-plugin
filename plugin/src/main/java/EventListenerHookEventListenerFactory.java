import io.trino.spi.eventlistener.EventListener;
import io.trino.spi.eventlistener.EventListenerFactory;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public class EventListenerHookEventListenerFactory implements EventListenerFactory {
    // Config variable names
    public static final String PLUGIN_NAME = "event-listener-hook";
    public static final String WEBHOOK_URL_CONFIG_VAR = PLUGIN_NAME + ".webhookUrl";
    public static final String SNS_TOPIC_CONFIG_VAR = PLUGIN_NAME + ".snsTopicArn";

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public EventListener create(Map<String, String> config) {
        // Get the hook destination values from the Config
        String webhookUrl = config.getOrDefault(WEBHOOK_URL_CONFIG_VAR, "");
        String snsTopicArn = config.getOrDefault(SNS_TOPIC_CONFIG_VAR, "");

        if (!webhookUrl.isBlank()) {
            System.out.println("EventListenerHookPlugin configured with Webhook URL: " + webhookUrl);
        }
        if (!snsTopicArn.isBlank()) {
            System.out.println("EventListenerHookPlugin configured with SNS Topic: " + snsTopicArn);
        }

        // Instantiate the listener
        return new EventListenerHookEventListener(webhookUrl, snsTopicArn);
    }
}
