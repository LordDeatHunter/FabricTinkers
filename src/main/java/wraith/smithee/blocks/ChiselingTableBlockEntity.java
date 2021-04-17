package wraith.smithee.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import wraith.smithee.Smithee;
import wraith.smithee.registry.BlockEntityRegistry;
import wraith.smithee.screens.ChiselingTableScreenHandler;
import wraith.smithee.screens.ImplementedInventory;

public class ChiselingTableBlockEntity extends LockableContainerBlockEntity implements ImplementedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
    private int tool = 0;
    private int part = 0;
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            if (index == 0) {
                return tool;
            } else {
                return part;
            }
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                tool = value;
            } else {
                part = value;
            }
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public ChiselingTableBlockEntity() {
        super(BlockEntityRegistry.BLOCK_ENTITIES.get("chiseling_table"));
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
        return new ChiselingTableScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    @Override
    public ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return this.createMenu(syncId, playerInventory, playerInventory.player);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("container." + Smithee.MOD_ID + ".chiseling_table");
    }

    @Override
    public Text getContainerName() {
        return this.getDisplayName();
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putInt("ToolPageNumber", this.tool);
        tag.putInt("PartPageNumber", this.part);
        Inventories.toTag(tag, this.inventory);
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.tool = tag.getInt("ToolPageNumber");
        this.part = tag.getInt("PartPageNumber");
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
        if (slot < 3 && !result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        markDirty();
    }

}