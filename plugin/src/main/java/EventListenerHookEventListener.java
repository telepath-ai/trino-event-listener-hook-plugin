import io.trino.spi.eventlistener.EventListener;
import io.trino.spi.eventlistener.QueryCompletedEvent;
import io.trino.spi.eventlistener.QueryCreatedEvent;
import io.trino.spi.eventlistener.SplitCompletedEvent;
import io.trino.spi.eventlistener.QueryContext;
import io.trino.spi.eventlistener.QueryMetadata;
import org.json.JSONObject;

public class EventListenerHookEventListener implements EventListener {
    private boolean hasWebhook = false;
    private boolean hasSNS = false;

    private SendWebhook sendWebhook;
    private SendSNS sendSNS;

    public EventListenerHookEventListener(final String webhookUrl, final String snsTopicArn) {
        if (!webhookUrl.isBlank()) {
            this.sendWebhook = new SendWebhook(webhookUrl);
            this.hasWebhook = true;
        }
        if (!snsTopicArn.isBlank()) {
            this.sendSNS = new SendSNS(snsTopicArn);
            this.hasSNS = true;
        }
    }

    @Override
    public void queryCreated(final QueryCreatedEvent queryCreatedEvent) {
        JSONObject hookData = new JSONObject()
            .put("metadata", this.makeHookMetadata(queryCreatedEvent.getMetadata()))
            .put("context", this.makeHookContext(queryCreatedEvent.getContext()))
            .put("createTime", queryCreatedEvent.getCreateTime());

        this.executeHook("QueryCreated", hookData);
    }

    @Override
    public void queryCompleted(final QueryCompletedEvent queryCompletedEvent) {
        JSONObject hookStatistics = new JSONObject()
            .put("cumulativeMemory", queryCompletedEvent.getStatistics().getCumulativeMemory())
            .put("totalBytes", queryCompletedEvent.getStatistics().getTotalBytes())
            .put("peakUserMemoryBytes", queryCompletedEvent.getStatistics().getPeakUserMemoryBytes())
            .put("totalRows", queryCompletedEvent.getStatistics().getTotalRows())
            .put("queuedTime", queryCompletedEvent.getStatistics().getQueuedTime().toMillis())
            .put("wallTime", queryCompletedEvent.getStatistics().getWallTime().toMillis())
            .put("cpuTime", queryCompletedEvent.getStatistics().getCpuTime().toMillis());

        JSONObject hookData = new JSONObject()
            .put("metadata", this.makeHookMetadata(queryCompletedEvent.getMetadata()))
            .put("context", this.makeHookContext(queryCompletedEvent.getContext()))
            .put("statistics", hookStatistics)
            .put("createTime", queryCompletedEvent.getCreateTime())
            .put("endTime", queryCompletedEvent.getEndTime())
            .put("executionStartTime", queryCompletedEvent.getExecutionStartTime())
            .put("failureInfo", queryCompletedEvent.getFailureInfo().orElse(null));

        this.executeHook("QueryCompleted", hookData);
    }

//     @Override
//     public void splitCompleted(final SplitCompletedEvent splitCompletedEvent) {
//         this.executeHook("SplitCompleted", new JSONObject(splitCompletedEvent));
//     }

    private void executeHook(final String eventName, final JSONObject data) {
        // Create the payload object
        JSONObject payload = new JSONObject()
            .put("event", eventName)
            .put("data", data);

        if (this.hasWebhook) {
            this.sendWebhook.sendHook(payload);
        }
        if (this.hasSNS) {
            this.sendSNS.sendHook(payload);
        }
    }

    private JSONObject makeHookContext(final QueryContext queryContext) {
        return new JSONObject()
            .put("schema", queryContext.getSchema().orElse(null))
            .put("catalog", queryContext.getCatalog().orElse(null))
            .put("serverAddress", queryContext.getServerAddress())
            .put("user", queryContext.getUser());
    }

    private JSONObject makeHookMetadata(final QueryMetadata queryMetadata) {
        return new JSONObject()
            .put("queryId", queryMetadata.getQueryId())
            .put("queryState", queryMetadata.getQueryState());
    }
}
