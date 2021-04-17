package wraith.smithee.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import wraith.smithee.Smithee;
import wraith.smithee.registry.BlockEntityRegistry;
import wraith.smithee.screens.DisassemblyTableScreenHandler;
import wraith.smithee.screens.ImplementedInventory;

public class DisassemblyTableBlockEntity extends LockableContainerBlockEntity implements ImplementedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(4, ItemStack.EMPTY);
    private DisassemblyTableScreenHandler handler;

    public DisassemblyTableBlockEntity() {
        super(BlockEntityRegistry.BLOCK_ENTITIES.get("disassembly_table"));
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        this.handler = new DisassemblyTableScreenHandler(syncId, playerInventory, this);
        return handler;
    }

    @Override
    public ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return this.createMenu(syncId, playerInventory, playerInventory.player);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("container." + Smithee.MOD_ID + ".disassembly_table");
    }

    @Override
    public Text getContainerName() {
        return this.getDisplayName();
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        Inventories.toTag(tag, this.inventory);
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        Inventories.fromTag(tag, this.inventory);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (this.world.getBlockEntity(this.pos) != this) {
            return false;
        } else {
            return player.squaredDistanceTo((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public ItemStack removeStack(int slot, int count) {
        ItemStack result = Inventories.splitStack(getItems(), slot, count);
        boolean update = false;
        if (slot < 3) {
            if (!result.isEmpty()) {
                markDirty();
            } else {
                update = true;
            }
        }
        if (update || slot == 3) {
            handler.onContentChanged(this);
        }
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        if (slot == 0) {
            handler.onContentChanged(this);
        }
        markDirty();
    }

    public void setHandler(DisassemblyTableScreenHandler handler) {
        this.handler = handler;
    }

}
