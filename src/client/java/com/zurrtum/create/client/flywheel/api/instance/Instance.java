package com.zurrtum.create.client.flywheel.api.instance;

public interface Instance {
    InstanceType<?> type();

    InstanceHandle handle();

    default void setChanged() {
        handle().setChanged();
    }

    default void delete() {
        handle().setDeleted();
    }

    default void setVisible(boolean visible) {
        handle().setVisible(visible);
    }
}
