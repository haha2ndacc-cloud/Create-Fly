package com.zurrtum.create.client.model;

import net.minecraft.client.resources.model.cuboid.CuboidModelElement;

public interface NormalsModelElement {
    static boolean calcNormals(CuboidModelElement element) {
        return ((NormalsModelElement) (Object) element).create$calcNormals();
    }

    static void markNormals(CuboidModelElement element) {
        ((NormalsModelElement) (Object) element).create$markNormals();
    }

    boolean create$calcNormals();

    void create$markNormals();
}
