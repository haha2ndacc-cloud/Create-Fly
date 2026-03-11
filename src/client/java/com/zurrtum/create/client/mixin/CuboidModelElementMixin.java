package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.model.NormalsModelElement;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CuboidModelElement.class)
public class CuboidModelElementMixin implements NormalsModelElement {
    @Unique
    private boolean normals;

    @Override
    public boolean create$calcNormals() {
        return normals;
    }

    @Override
    public void create$markNormals() {
        normals = true;
    }
}
