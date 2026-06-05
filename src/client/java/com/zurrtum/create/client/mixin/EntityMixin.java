package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.client.content.contraptions.ContraptionHandlerClient;
import com.zurrtum.create.client.content.trains.CameraDistanceModifier;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.ContraptionCollider;
import com.zurrtum.create.content.contraptions.ContraptionHandler;
import com.zurrtum.create.infrastructure.fluids.FluidInteractionPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.util.TriConsumer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.ref.Reference;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    private Level level;
    @Shadow
    private Vec3 position;
    @Shadow
    private float nextStep;
    @Shadow
    @Final
    protected RandomSource random;
    @Shadow
    private EntityDimensions dimensions;
    @Shadow
    @Final
    private EntityFluidInteraction fluidInteraction;

    @Shadow
    protected abstract void playStepSound(BlockPos pos, BlockState blockState);

    @Shadow
    protected abstract float nextStep();

    @Inject(method = "doWaterSplashEffect()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I"), cancellable = true)
    private void cancelEffect(CallbackInfo ci) {
        if (((FluidInteractionPredicate) fluidInteraction).create$inModFluid()) {
            ci.cancel();
        }
    }

    @Unique
    private Stream<AbstractContraptionEntity> create$getIntersectionContraptionsStream() {
        return (level.isClientSide() ? ContraptionHandlerClient.loadedContraptions :
            ContraptionHandler.loadedContraptions).get(level).values().stream().map(Reference::get)
            .filter(cEntity -> cEntity != null && cEntity.collidingEntities.containsKey((Entity) (Object) this));
    }

    @Unique
    private Set<AbstractContraptionEntity> create$getIntersectingContraptions() {
        Set<AbstractContraptionEntity> contraptions = create$getIntersectionContraptionsStream().collect(Collectors.toSet());

        contraptions.addAll(level.getEntitiesOfClass(
            AbstractContraptionEntity.class,
            ((Entity) (Object) this).getBoundingBox().inflate(1.0f)
        ));
        return contraptions;
    }

    @Unique
    private void create$forCollision(Vec3 worldPos, TriConsumer<Contraption, BlockState, BlockPos> action) {
        create$getIntersectingContraptions().forEach(cEntity -> {
            Vec3 localPos = ContraptionCollider.worldToLocalPos(worldPos, cEntity);

            BlockPos blockPos = BlockPos.containing(localPos);
            Contraption contraption = cEntity.getContraption();
            StructureTemplate.StructureBlockInfo info = contraption.getBlocks().get(blockPos);

            if (info != null) {
                BlockState blockstate = info.state();
                action.accept(contraption, blockstate, blockPos);
            }
        });
    }

    // involves block step sounds on contraptions
    // injecting before `!blockstate1.isAir(this.world, blockpos)`
    // `if (this.moveDist > this.nextStep && !blockstate1.isAir())
    @Inject(method = "applyMovementEmissionAndPlaySound(Lnet/minecraft/world/entity/Entity$MovementEmission;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z", ordinal = 0))
    private void create$contraptionStepSounds(
        Entity.MovementEmission emission,
        Vec3 clippedMovement,
        BlockPos effectPos,
        BlockState effectState,
        CallbackInfo ci
    ) {
        Vec3 worldPos = position.add(0, -0.2, 0);
        MutableBoolean stepped = new MutableBoolean(false);

        create$forCollision(
            worldPos, (contraption, state, pos) -> {
                playStepSound(pos, state);
                stepped.setTrue();
            }
        );

        if (stepped.booleanValue()) {
            nextStep = nextStep();
        }
    }

    // involves client-side view bobbing animation on contraptions
    @Inject(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", at = @At("TAIL"))
    private void create$onMove(MoverType moverType, Vec3 delta, CallbackInfo ci) {
        if (!level.isClientSide()) {
            return;
        }
        Entity self = (Entity) (Object) this;
        if (self.onGround()) {
            return;
        }
        if (self.isPassenger()) {
            return;
        }

        Vec3 worldPos = position.add(0, -0.2, 0);
        boolean onAtLeastOneContraption = create$getIntersectionContraptionsStream().anyMatch(cEntity -> {
            Vec3 localPos = ContraptionCollider.worldToLocalPos(worldPos, cEntity);

            BlockPos blockPos = BlockPos.containing(localPos);
            Contraption contraption = cEntity.getContraption();
            StructureTemplate.StructureBlockInfo info = contraption.getBlocks().get(blockPos);

            if (info == null) {
                return false;
            }

            cEntity.registerColliding(self);
            return true;
        });

        if (!onAtLeastOneContraption) {
            return;
        }

        self.setOnGround(true);
        AllSynchedDatas.CONTRAPTION_GROUNDED.set(self, true);
    }

    @Inject(method = "spawnSprintParticle()V", at = @At("TAIL"))
    private void create$onSpawnSprintParticle(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        Vec3 worldPos = position.add(0, -0.2, 0);

        create$forCollision(
            worldPos, (contraption, state, pos) -> {
                if (state.getRenderShape() != RenderShape.INVISIBLE) {
                    Vec3 speed = self.getDeltaMovement();
                    level.addParticle(
                        new BlockParticleOption(ParticleTypes.BLOCK, state),
                        self.getX() + (random.nextFloat() - 0.5D) * dimensions.width(),
                        self.getY() + 0.1D,
                        self.getZ() + (random.nextFloat() - 0.5D) * dimensions.height(),
                        speed.x * -4.0D,
                        1.5D,
                        speed.z * -4.0D
                    );
                }
            }
        );
    }

    @WrapOperation(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;canSimulateMovement()Z"))
    private boolean move(Entity instance, Operation<Boolean> original) {
        if (original.call(instance)) {
            return true;
        }
        return level instanceof PonderLevel;
    }

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isPassenger()Z"))
    private void onMount(
        Entity entityToRide,
        boolean force,
        boolean sendEventAndTriggers,
        CallbackInfoReturnable<Boolean> cir
    ) {
        CameraDistanceModifier.onMount((Entity) (Object) this, entityToRide, true);
    }

    @WrapOperation(method = "removeVehicle()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;removePassenger(Lnet/minecraft/world/entity/Entity;)V"))
    private void onDismount(Entity instance, Entity passenger, Operation<Void> original) {
        CameraDistanceModifier.onMount(passenger, instance, false);
        original.call(instance, passenger);
    }
}
