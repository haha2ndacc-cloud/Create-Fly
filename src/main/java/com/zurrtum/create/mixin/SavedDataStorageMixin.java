package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SavedDataStorage.class)
public class SavedDataStorageMixin {
    @WrapOperation(method = "readTagFromDisk(Ljava/nio/file/Path;Lnet/minecraft/util/datafix/DataFixTypes;I)Lnet/minecraft/nbt/CompoundTag;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/datafix/DataFixTypes;update(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/CompoundTag;II)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag handleNullDataFixType(
        DataFixTypes dataFixTypes,
        DataFixer fixer,
        CompoundTag tag,
        int fromVersion,
        int toVersion,
        Operation<CompoundTag> original
    ) {
        if (dataFixTypes == null) {
            return tag;
        }

        return original.call(dataFixTypes, fixer, tag, fromVersion, toVersion);
    }
}
