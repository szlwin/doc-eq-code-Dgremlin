package dec.core.context.model;
import java.util.List;
import java.util.Optional;
public interface DeferredRegistry {
    List<DeferredDefinition> requiredBy(RequiredStage stage);
    List<DeferredDefinition> ownedBy(DefinitionKey key);
    Optional<DeferredDefinition> find(DeferredKey key);
    List<DeferredKey> keys();
    int size();
}
