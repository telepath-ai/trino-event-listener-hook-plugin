import io.trino.spi.Plugin;
import io.trino.spi.eventlistener.EventListenerFactory;

import java.util.Collections;

public class EventListenerHookPlugin implements Plugin {

    @Override
    public Iterable<EventListenerFactory> getEventListenerFactories() {
        return Collections.singletonList(new EventListenerHookEventListenerFactory());
    }
}