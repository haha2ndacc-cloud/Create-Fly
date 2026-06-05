package com.zurrtum.create.impl.registry;

import com.zurrtum.create.api.registry.SimpleRegistry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.StateHolder;
import org.jspecify.annotations.Nullable;

import java.util.*;

// methods are synchronized since registrations can happen during parallel mod loading
public abstract sealed class SimpleRegistryImpl<K, V> implements SimpleRegistry<K, V> permits SimpleRegistryImpl.MultiImpl, SimpleRegistryImpl.SingleImpl {
    protected final Map<K, V> registrations = new IdentityHashMap<>();
    protected final List<Provider<K, V>> providers = new ArrayList<>();

    public static <K, V> SimpleRegistry<K, V> single() {
        return new SingleImpl<>();
    }

    public static <K, V> SimpleRegistry.Multi<K, V> multi() {
        return new MultiImpl<>();
    }

    @Override
    public synchronized void register(K object, V value) {
        Objects.requireNonNull(object, "object");
        Objects.requireNonNull(value, "value");

        V existing = registrations.get(object);
        if (existing != null) {
            throw new IllegalArgumentException(String.format(
                "Tried to register duplicate values for object %s (%s): old=%s, new=%s",
                object,
                object.getClass(),
                existing,
                value
            ));
        }

        registrations.put(object, value);
    }

    @Override
    public synchronized void registerProvider(Provider<K, V> provider) {
        Objects.requireNonNull(provider);
        if (providers.contains(provider)) {
            throw new IllegalArgumentException("Tried to register provider twice: " + provider);
        }

        // add to start of list so it's queried first
        providers.addFirst(provider);
        provider.onRegister(this::invalidate);
    }

    @Override
    @Nullable
    public synchronized V get(StateHolder<K, ?> state) {
        Objects.requireNonNull(state, "state");
        return get(state.owner);
    }

    static final class SingleImpl<K, V> extends SimpleRegistryImpl<K, V> {
        private static final Object nullMarker = new Object();

        private final Map<K, V> providedValues = new IdentityHashMap<>();

        @SuppressWarnings("unchecked")
        private static <T> T nullMarker() {
            return (T) nullMarker;
        }

        @Override
        @Nullable
        public synchronized V get(K object, Level world) {
            Objects.requireNonNull(object, "object");
            if (registrations.containsKey(object)) {
                return registrations.get(object);
            }
            if (providedValues.containsKey(object)) {
                V provided = providedValues.get(object);
                return provided == nullMarker ? null : provided;
            }

            // no value known, check providers
            // new providers are added to the start, so normal iteration is reverse-registration order
            for (Provider<K, V> provider : providers) {
                V value = provider.get(object, world);
                if (value != null) {
                    providedValues.put(object, value);
                    return value;
                }
            }

            // no provider returned non-null
            providedValues.put(object, nullMarker());
            return null;
        }

        @Override
        @Nullable
        public synchronized V get(K object) {
            Objects.requireNonNull(object, "object");
            if (registrations.containsKey(object)) {
                return registrations.get(object);
            }
            if (providedValues.containsKey(object)) {
                V provided = providedValues.get(object);
                return provided == nullMarker ? null : provided;
            }

            // no value known, check providers
            // new providers are added to the start, so normal iteration is reverse-registration order
            for (Provider<K, V> provider : providers) {
                V value = provider.get(object);
                if (value != null) {
                    providedValues.put(object, value);
                    return value;
                }
            }

            // no provider returned non-null
            providedValues.put(object, nullMarker());
            return null;
        }

        @Override
        public void invalidate() {
            providedValues.clear();
        }
    }

    static final class MultiImpl<K, V> extends SimpleRegistryImpl<K, List<V>> implements SimpleRegistry.Multi<K, V> {
        private final Map<K, List<V>> totals = new IdentityHashMap<>();

        @Override
        public synchronized void add(K object, V value) {
            Objects.requireNonNull(object, "object");
            Objects.requireNonNull(value, "value");

            if (!registrations.containsKey(object)) {
                registrations.put(object, new ArrayList<>());
            }

            registrations.get(object).add(value);
        }

        @Override
        public void addProvider(Provider<K, V> provider) {
            registerProvider(new ProviderWrapper<>(provider));
        }

        @Override
        public synchronized void invalidate() {
            totals.clear();
        }

        @Override
        public synchronized List<V> get(K object, Level world) {
            Objects.requireNonNull(object, "object");
            if (!totals.containsKey(object)) {
                totals.put(object, calculateTotal(object, world));
            }

            return totals.get(object);
        }

        private List<V> calculateTotal(K object, Level world) {
            List<V> registrations = this.registrations.getOrDefault(object, List.of());
            List<V> total = new ArrayList<>(registrations);

            for (Provider<K, List<V>> provider : providers) {
                List<V> values = provider.get(object, world);
                if (values != null) {
                    total.addAll(values);
                }
            }

            return total.isEmpty() ? List.of() : Collections.unmodifiableList(total);
        }

        @Override
        public synchronized List<V> get(K object) {
            Objects.requireNonNull(object, "object");
            if (!totals.containsKey(object)) {
                totals.put(object, calculateTotal(object));
            }

            return totals.get(object);
        }

        private List<V> calculateTotal(K object) {
            List<V> registrations = this.registrations.getOrDefault(object, List.of());
            List<V> total = new ArrayList<>(registrations);

            for (Provider<K, List<V>> provider : providers) {
                List<V> values = provider.get(object);
                if (values != null) {
                    total.addAll(values);
                }
            }

            return total.isEmpty() ? List.of() : Collections.unmodifiableList(total);
        }

        // remove nullable
        @Override
        @Nullable
        public synchronized List<V> get(StateHolder<K, ?> state) {
            return super.get(state);
        }

        private record ProviderWrapper<K, V>(Provider<K, V> wrapped) implements Provider<K, List<V>> {
            @Override
            @Nullable
            public List<V> get(K object) {
                V value = wrapped.get(object);
                return value == null ? null : List.of(value);
            }

            @Override
            public void onRegister(Runnable invalidate) {
                wrapped.onRegister(invalidate);
            }
        }
    }
}