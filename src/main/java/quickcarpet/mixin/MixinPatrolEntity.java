package quickcarpet.mixin;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PatrolEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.IWorld;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.logging.LoggerRegistry;
import quickcarpet.utils.Messenger;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Mixin(PatrolEntity.class)
public class MixinPatrolEntity extends HostileEntity {
    protected MixinPatrolEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "initialize", at = @At("HEAD"))
    private void onInitialize(IWorld world, LocalDifficulty localDifficulty, SpawnType spawnType, EntityData data, CompoundTag tag, CallbackInfoReturnable<EntityData> cir) {
        if (LoggerRegistry.__banner) {
            try {
                Field seedField = Random.class.getDeclaredField("seed");
                seedField.setAccessible(true);
                long seed = ((AtomicLong) seedField.get(this.random)).get();
                Random rand = new Random(seed ^ 0x5DEECE66DL);
                LoggerRegistry.getLogger("banner").logNoCommand(() -> new Component[]{Messenger.c(
                        "e pos: ", "c " + this.getPos() + " ",
                        "e spawnType: ", "c " + spawnType + " ",
                        "e seed: ", String.format("c 0x%012x ", seed),
                        "e nextFloat: ", "c " + rand.nextFloat())});
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
    }
}
