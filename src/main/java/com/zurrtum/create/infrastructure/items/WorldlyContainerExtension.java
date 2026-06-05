package com.zurrtum.create.infrastructure.items;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

public interface WorldlyContainerExtension extends Container {
    static int findEndIndex(int[] slots, int start, int end) {
        for (int i = slots.length - 1; i >= start; i--) {
            if (slots[i] <= end) {
                return i;
            }
        }
        return -1;
    }

    static int findStartIndex(int[] slots, int start) {
        for (int i = 0, size = slots.length; i < size; i++) {
            if (slots[i] >= start) {
                return i;
            }
        }
        return -1;
    }

    @Override
    default int count(ItemStack stack) {
        return count(stack, null);
    }

    @Override
    default int count(ItemStack stack, @Nullable Direction side) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return count(stack, maxAmount, side);
    }

    @Override
    default int count(ItemStack stack, int maxAmount) {
        return count(stack, maxAmount, null);
    }

    @Override
    default int count(ItemStack stack, int maxAmount, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default ItemStack count(Predicate<ItemStack> predicate) {
        return count(predicate, null);
    }

    @Override
    default ItemStack count(Predicate<ItemStack> predicate, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default ItemStack count(Predicate<ItemStack> predicate, int maxAmount) {
        return count(predicate, maxAmount, null);
    }

    @Override
    default ItemStack count(Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default ItemStack preciseCount(Predicate<ItemStack> predicate, int maxAmount) {
        return preciseCount(predicate, maxAmount, null);
    }

    @Override
    default ItemStack preciseCount(Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default int countAll(Predicate<ItemStack> predicate, int maxAmount) {
        return countAll(predicate, maxAmount, null);
    }

    @Override
    default int countAll(Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default ItemStack countAny() {
        return countAny(null);
    }

    @Override
    default ItemStack countAny(@Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default ItemStack countAny(int maxAmount) {
        return countAny(maxAmount, null);
    }

    @Override
    default ItemStack countAny(int maxAmount, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default int countSpace(ItemStack stack) {
        return countSpace(stack, null);
    }

    @Override
    default int countSpace(ItemStack stack, @Nullable Direction side) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return countSpace(stack, maxAmount, side);
    }

    @Override
    default int countSpace(ItemStack stack, int maxAmount) {
        return countSpace(stack, maxAmount, null);
    }

    @Override
    default int countSpace(ItemStack stack, int maxAmount, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default int countSpace(ItemStack stack, int maxAmount, int start, int end) {
        return countSpace(stack, maxAmount, start, end, null);
    }

    @Override
    default int countSpace(ItemStack stack, int maxAmount, int start, int end, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default boolean countSpace(List<ItemStack> stacks) {
        return countSpace(stacks, null);
    }

    @Override
    default boolean countSpace(List<ItemStack> stacks, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default boolean countSpace(List<ItemStack> stacks, int start, int end) {
        return countSpace(stacks, start, end, null);
    }

    @Override
    default boolean countSpace(List<ItemStack> stacks, int start, int end, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default int extract(ItemStack stack) {
        return extract(stack, null);
    }

    @Override
    default int extract(ItemStack stack, @Nullable Direction side) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return extract(stack, maxAmount, side);
    }

    @Override
    default int extract(ItemStack stack, int maxAmount) {
        return extract(stack, maxAmount, null);
    }

    @Override
    default int extract(ItemStack stack, int maxAmount, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default ItemStack extract(Predicate<ItemStack> predicate, int maxAmount) {
        return extract(predicate, maxAmount, null);
    }

    @Override
    default ItemStack extract(Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default ItemStack extract(Predicate<ItemStack> predicate) {
        return extract(predicate, null);
    }

    @Override
    default ItemStack extract(Predicate<ItemStack> predicate, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default List<ItemStack> extract(List<ItemStack> stacks) {
        return extract(stacks, null);
    }

    @Override
    default List<ItemStack> extract(List<ItemStack> stacks, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default int extractAll(Predicate<ItemStack> predicate, int maxAmount) {
        return extractAll(predicate, maxAmount, null);
    }

    @Override
    default int extractAll(Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default ItemStack extractAny() {
        return extractAny(null);
    }

    @Override
    default ItemStack extractAny(@Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default ItemStack extractAny(int maxAmount) {
        return extractAny(maxAmount, null);
    }

    @Override
    default ItemStack extractAny(int maxAmount, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default ItemStack extractAnyMax() {
        return extractAnyMax(null);
    }

    @Override
    default ItemStack extractAnyMax(@Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default ItemStack extractMax(Predicate<ItemStack> predicate) {
        return extractMax(predicate, null);
    }

    @Override
    default ItemStack extractMax(Predicate<ItemStack> predicate, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default int insert(ItemStack stack) {
        return insert(stack, null);
    }

    @Override
    default int insert(ItemStack stack, int maxAmount) {
        return insert(stack, maxAmount, null);
    }

    @Override
    default int insert(ItemStack stack, @Nullable Direction side) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return insert(stack, maxAmount, side);
    }

    @Override
    default int insert(ItemStack stack, int maxAmount, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default int insert(ItemStack stack, int maxAmount, int start, int end) {
        return insert(stack, maxAmount, start, end, null);
    }

    @Override
    default int insert(ItemStack stack, int maxAmount, int start, int end, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default List<ItemStack> insert(List<ItemStack> stacks) {
        return insert(stacks, null);
    }

    @Override
    default List<ItemStack> insert(List<ItemStack> stacks, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default List<ItemStack> insert(List<ItemStack> stacks, int start, int end) {
        return insert(stacks, start, end, null);
    }

    @Override
    default List<ItemStack> insert(List<ItemStack> stacks, int start, int end, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default int insertExist(ItemStack stack) {
        return insertExist(stack, null);
    }

    @Override
    default int insertExist(ItemStack stack, int maxAmount) {
        return insertExist(stack, maxAmount, null);
    }

    @Override
    default int insertExist(ItemStack stack, @Nullable Direction side) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return insertExist(stack, maxAmount, side);
    }

    @Override
    default int insertExist(ItemStack stack, int maxAmount, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default java.util.Iterator<ItemStack> iterator() {
        return iterator(null);
    }

    @Override
    default java.util.Iterator<ItemStack> iterator(@Nullable Direction side) {
        return new Iterator((WorldlyContainer) this, side);
    }

    @Override
    default boolean preciseExtract(ItemStack stack) {
        return preciseExtract(stack, null);
    }

    @Override
    default boolean preciseExtract(ItemStack stack, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default ItemStack preciseExtract(Predicate<ItemStack> predicate, int maxAmount) {
        return preciseExtract(predicate, maxAmount, null);
    }

    @Override
    default ItemStack preciseExtract(Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default boolean preciseInsert(ItemStack stack) {
        return preciseInsert(stack, null);
    }

    @Override
    default boolean preciseInsert(ItemStack stack, @Nullable Direction side) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return true;
        }
        return preciseInsert(stack, maxAmount, side);
    }

    @Override
    default boolean preciseInsert(ItemStack stack, int maxAmount) {
        return preciseInsert(stack, maxAmount, null);
    }

    @Override
    default boolean preciseInsert(ItemStack stack, int maxAmount, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default boolean preciseInsert(List<ItemStack> stacks) {
        return preciseInsert(stacks, null);
    }

    @Override
    default boolean preciseInsert(List<ItemStack> stacks, @Nullable Direction side) {
        throw new RuntimeException("Implemented via Mixin");
    }

    @Override
    default boolean update(Predicate<ItemStack> predicate, Function<ItemStack, ItemStack> update) {
        return update(predicate, update, null);
    }

    @Override
    default boolean update(
        Predicate<ItemStack> predicate,
        Function<ItemStack, ItemStack> update,
        @Nullable Direction side
    ) {
        throw new RuntimeException("Implemented via Mixin");
    }

    class Iterator implements java.util.Iterator<ItemStack> {
        private final WorldlyContainer inventory;
        private final @Nullable Direction side;
        private final int[] slots;
        private int index;
        private int current = -1;

        public Iterator(WorldlyContainer inventory, @Nullable Direction side) {
            this.inventory = inventory;
            this.side = side;
            slots = inventory.getSlotsForFace(side);
        }

        @Override
        public boolean hasNext() {
            if (current >= 0) {
                return true;
            }
            if (current == -2) {
                return false;
            }
            for (; index < slots.length; index++) {
                ItemStack stack = inventory.getItem(slots[index]);
                if (inventory.canTakeItemThroughFace(index, stack, side)) {
                    current = index;
                    index++;
                    return true;
                }
            }
            current = -2;
            return false;
        }

        @Override
        public ItemStack next() {
            if (hasNext()) {
                ItemStack result = inventory.getItem(slots[current]);
                current = -1;
                return result;
            }
            throw new NoSuchElementException();
        }
    }
}
