package wraith.smithee.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.smithee.properties.Trait;
import wraith.smithee.properties.TraitType;

import java.util.List;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "afterBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExhaustion(F)V"), cancellable = true)
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity entity, ItemStack stack, CallbackInfo ci) {

        ItemStack handStack = player.getMainHandStack();
        boolean cancelExhaustion = false; // TODO: add a exhaustion canceller
        boolean cancelDrops = Trait.hasTrait(handStack, TraitType.MAGNETIC);

        List<ItemStack> drops = Block.getDroppedStacks(state, (ServerWorld) world, pos, entity, player, stack);
        if (Trait.hasTrait(stack, TraitType.MIDAS_TOUCH)) {
            drops.add(Trait.getMidas(handStack));
        }

        if (cancelDrops) {
            for (ItemStack drop : drops) {
                player.inventory.offerOrDrop(world, drop);
            }
        }

        if (cancelExhaustion && cancelDrops) {
            ci.cancel();
        } else if (!cancelExhaustion && cancelDrops) {
            player.addExhaustion(0.005F);
            ci.cancel();
        } else {
            for (ItemStack drop : drops) {
                Block.dropStack(world, pos, drop);
            }
            ci.cancel();
        }

    }

}
