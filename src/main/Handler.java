import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

import java.lang.StringBuilder;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.*;

import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.contrib.awsxray.propagator.AwsXrayPropagator;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;

import java.lang.reflect.Field;

import io.opentelemetry.sdk.trace.SpanProcessor;

public class Handler implements RequestHandler<SQSEvent, String> {
  public Handler() {
  }

  @Override
  public String handleRequest(SQSEvent event, Context context) {
      Tracer tracer = GlobalOpenTelemetry.getTracer("instrumentation-library-name", "1.0.0");

      SpanBuilder spanBuilder = tracer.spanBuilder("lambda process");
      spanBuilder.setSpanKind(SpanKind.SERVER);

      for (SQSEvent.SQSMessage message: event.getRecords()) {
          System.out.println(message);
          System.out.println(extractSpanContext(message));
          spanBuilder.addLink(extractSpanContext(message), Attributes.builder()
                  .put(AttributeKey.stringKey("From"), "FromLambdaFunction").build());
      }

      Span span = spanBuilder.startSpan();
      span.setAttribute("Key", "MyValue");
      span.end();
      System.out.println("My span: " + span);

    return "Success";
  }

    public static SpanContext extractSpanContext(SQSEvent.SQSMessage message) {
        String parentHeader = message.getAttributes().get("AWSTraceHeader");

        io.opentelemetry.context.Context xrayContext =
            AwsXrayPropagator.getInstance()
                .extract(
                    io.opentelemetry.context.Context.root(), // We don't want the ambient context.
                    Collections.singletonMap("x-amzn-trace-id", parentHeader),
                    MapGetter.INSTANCE);

        return Span.fromContext(xrayContext).getSpanContext();
    }

    private enum MapGetter implements TextMapGetter<Map<String, String>> {
        INSTANCE;

        @Override
        public Iterable<String> keys(Map<String, String> map) {
            return map.keySet();
        }

        @Override
        public String get(Map<String, String> map, String s) {
            return map.get(s.toLowerCase(Locale.ROOT));
        }
    }
}
