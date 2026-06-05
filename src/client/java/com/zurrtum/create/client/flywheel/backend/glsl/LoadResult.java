package com.zurrtum.create.client.flywheel.backend.glsl;

import com.zurrtum.create.client.flywheel.backend.compile.core.ShaderException;

public sealed interface LoadResult {
    SourceFile unwrap();

    record Success(SourceFile source) implements LoadResult {
        @Override
        public SourceFile unwrap() {
            return source;
        }
    }

    record Failure(LoadError error) implements LoadResult {
        @Override
        public SourceFile unwrap() {
            throw new ShaderException.Load(error.generateMessage().build());
        }
    }
}
