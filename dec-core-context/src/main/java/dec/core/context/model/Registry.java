package dec.core.context.model;
import java.util.List;
import java.util.Optional;
public interface Registry<K extends DefinitionKey, V> {
    Optional<V> find(K key);
    V require(K key);
    List<K> keys();
    int size();
}
