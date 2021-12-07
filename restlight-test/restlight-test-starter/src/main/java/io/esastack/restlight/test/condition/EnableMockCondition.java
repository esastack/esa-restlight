package io.esastack.restlight.test.condition;

import io.esastack.restlight.starter.condition.ServerPortType;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class EnableMockCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final ServerPortType type = ServerPortType.get();
        if (ServerPortType.MOCK == type) {
            return new ConditionOutcome(true, "Current server is mocked!");
        }
        return new ConditionOutcome(false, "Current server(" + type + ") is not mocked!");
    }
}
