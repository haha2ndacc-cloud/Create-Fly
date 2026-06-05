package com.zurrtum.create.client.flywheel.lib.backend;

import com.zurrtum.create.client.flywheel.api.backend.Backend;
import com.zurrtum.create.client.flywheel.api.backend.Engine;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelAccessor;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;

public final class SimpleBackend implements Backend {
    private final Function<LevelAccessor, Engine> engineFactory;
    private final IntSupplier priority;
    private final BooleanSupplier isSupported;

    public SimpleBackend(
        Function<LevelAccessor, Engine> engineFactory,
        IntSupplier priority,
        BooleanSupplier isSupported
    ) {
        this.engineFactory = engineFactory;
        this.priority = priority;
        this.isSupported = isSupported;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Engine createEngine(LevelAccessor level) {
        return engineFactory.apply(level);
    }

    @Override
    public int priority() {
        return priority.getAsInt();
    }

    @Override
    public boolean isSupported() {
        return isSupported.getAsBoolean();
    }

    public static final class Builder {
        private @Nullable Function<LevelAccessor, Engine> engineFactory;
        private IntSupplier priority = () -> 0;
        private @Nullable BooleanSupplier isSupported;

        public Builder() {
        }

        public Builder engineFactory(Function<LevelAccessor, Engine> engineFactory) {
            this.engineFactory = engineFactory;
            return this;
        }

        public Builder priority(int priority) {
            return priority(() -> priority);
        }

        public Builder priority(IntSupplier priority) {
            this.priority = priority;
            return this;
        }

        public Builder supported(BooleanSupplier isSupported) {
            this.isSupported = isSupported;
            return this;
        }

        public Backend register(Identifier id) {
            Objects.requireNonNull(engineFactory);
            Objects.requireNonNull(isSupported);
            return REGISTRY.registerAndGet(id, new SimpleBackend(engineFactory, priority, isSupported));
        }
    }
}
