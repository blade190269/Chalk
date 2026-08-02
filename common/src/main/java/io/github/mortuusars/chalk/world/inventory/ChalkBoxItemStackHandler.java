package io.github.mortuusars.chalk.world.inventory;

public class ChalkBoxItemStackHandler /*extends ItemStackHandler*/ {
//    private final ItemStack chalkBoxStack;
//    private final ChalkBoxItem chalkBoxItem;
//
//    public ChalkBoxItemStackHandler(ItemStack chalkBoxStack) {
//        super(OldChalkBoxContents.SLOTS);
//        Preconditions.checkArgument(chalkBoxStack.getItem() instanceof ChalkBoxItem, "{} is not a ChalkBoxItem", chalkBoxStack);
//        this.chalkBoxStack = chalkBoxStack;
//        this.chalkBoxItem = ((ChalkBoxItem) chalkBoxStack.getItem());
//
//        List<ItemStack> items = chalkBoxItem.getContents(chalkBoxStack).items();
//
//        for (int index = 0; index < items.size(); index++) {
//            this.stacks.set(index, items.get(index));
//        }
//    }
//
//    public ItemStack getChalkBoxStack() {
//        return chalkBoxStack;
//    }
//
//    @Override
//    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
//        return chalkBoxItem.isItemValid(chalkBoxStack, slot, stack);
//    }
//
//    @Override
//    protected void onContentsChanged(int slot) {
//        int prevGlowingAmount = chalkBoxItem.getGlowAmount(chalkBoxStack);
//
//        chalkBoxItem.setItemInSlot(chalkBoxStack, slot, getStackInSlot(slot));
//
//        if (slot == ChalkBoxItem.GLOWINGS_SLOT_INDEX && chalkBoxItem.getGlowAmount(chalkBoxStack) > prevGlowingAmount) {
//            // Refresh glow stack:
//            this.stacks.set(slot, chalkBoxItem.getItemInSlot(chalkBoxStack, slot));
//        }
//    }
}
