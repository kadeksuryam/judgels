package judgels.gabriel.engines.functional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import java.util.List;
import judgels.gabriel.api.Subtask;
import judgels.gabriel.engines.MultipleSourceFilesGradingConfig;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableFunctionalGradingConfig.class)
public interface FunctionalGradingConfig extends MultipleSourceFilesGradingConfig {
    String getCustomScorer();

    @Override
    default List<Subtask> getSubtasks() {
        return ImmutableList.of(Subtask.of(-1, 100));
    }

    class Builder extends ImmutableFunctionalGradingConfig.Builder {}
}
