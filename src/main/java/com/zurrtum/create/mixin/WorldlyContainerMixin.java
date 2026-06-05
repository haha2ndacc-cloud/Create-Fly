package com.zurrtum.create.mixin;

import com.zurrtum.create.infrastructure.items.WorldlyContainerExtension;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(WorldlyContainer.class)
public interface WorldlyContainerMixin extends WorldlyContainerExtension {
    @Shadow
    int[] getSlotsForFace(Direction direction);

    @Shadow
    boolean canPlaceItemThroughFace(int slot, ItemStack itemStack, @Nullable Direction direction);

    @Shadow
    boolean canTakeItemThroughFace(int slot, ItemStack itemStack, Direction direction);

    @Override
    default int count(@NonNull ItemStack stack, int maxAmount, @Nullable Direction side) {
        int count = 0;
        for (int slot : getSlotsForFace(side)) {
            if (canTakeItemThroughFace(slot, stack, side)) {
                ItemStack target = getItem(slot);
                if (target.isEmpty()) {
                    continue;
                }
                if (matches(target, stack)) {
                    count += target.getCount();
                    if (count >= maxAmount) {
                        return maxAmount;
                    }
                }
            }
        }
        return count;
    }

    @Override
    default @NonNull ItemStack count(@NonNull Predicate<ItemStack> predicate, @Nullable Direction side) {
        for (int slot : getSlotsForFace(side)) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (canTakeItemThroughFace(slot, stack, side) && predicate.test(stack)) {
                return onExtract(directCopy(stack, stack.getCount()));
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    default @NonNull ItemStack count(@NonNull Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        int[] slots = getSlotsForFace(side);
        for (int i = 0, size = slots.length; i < size; i++) {
            int slot = slots[i];
            ItemStack findStack = getItem(slot);
            if (findStack.isEmpty()) {
                continue;
            }
            if (canTakeItemThroughFace(slot, findStack, side) && predicate.test(findStack)) {
                int count = findStack.getCount();
                if (count >= maxAmount) {
                    return onExtract(directCopy(findStack, maxAmount));
                }
                for (i = i + 1; i < size; i++) {
                    slot = slots[i];
                    ItemStack stack = getItem(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (canTakeItemThroughFace(slot, stack, side) && matches(stack, findStack)) {
                        count += stack.getCount();
                        if (count < maxAmount) {
                            continue;
                        }
                        return onExtract(directCopy(findStack, maxAmount));
                    }
                }
                return onExtract(directCopy(findStack, count));
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    default @NonNull ItemStack preciseCount(
        @NonNull Predicate<ItemStack> predicate,
        int maxAmount,
        @Nullable Direction side
    ) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        IntSet blackList = new IntOpenHashSet();
        int[] slots = getSlotsForFace(side);
        for (int i = 0, size = slots.length; i < size; i++) {
            int slot = slots[i];
            ItemStack findStack = getItem(slot);
            if (findStack.isEmpty() || blackList.contains(i)) {
                continue;
            }
            if (canTakeItemThroughFace(slot, findStack, side) && predicate.test(findStack)) {
                int count = findStack.getCount();
                if (count >= maxAmount) {
                    return onExtract(directCopy(findStack, maxAmount));
                }
                for (int j = i + 1; j < size; j++) {
                    slot = slots[j];
                    ItemStack stack = getItem(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (canTakeItemThroughFace(slot, stack, side) && matches(stack, findStack)) {
                        count += stack.getCount();
                        if (count < maxAmount) {
                            blackList.add(j);
                            continue;
                        }
                        return onExtract(directCopy(findStack, maxAmount));
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    default int countAll(@NonNull Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        if (maxAmount == 0) {
            return 0;
        }
        int count = 0;
        for (int slot : getSlotsForFace(side)) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (predicate.test(stack) && canTakeItemThroughFace(slot, stack, side)) {
                count += stack.getCount();
                if (count >= maxAmount) {
                    return maxAmount;
                }
            }
        }
        return count;
    }

    @Override
    default @NonNull ItemStack countAny(@Nullable Direction side) {
        for (int slot : getSlotsForFace(side)) {
            ItemStack target = getItem(slot);
            if (target.isEmpty()) {
                continue;
            }
            if (canTakeItemThroughFace(slot, target, side)) {
                return onExtract(directCopy(target, target.getCount()));
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    default @NonNull ItemStack countAny(int maxAmount, @Nullable Direction side) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        int[] slots = getSlotsForFace(side);
        for (int i = 0, size = slots.length; i < size; i++) {
            int slot = slots[i];
            ItemStack findStack = getItem(slot);
            if (findStack.isEmpty()) {
                continue;
            }
            if (canTakeItemThroughFace(slot, findStack, side)) {
                int count = findStack.getCount();
                if (count >= maxAmount) {
                    return onExtract(directCopy(findStack, maxAmount));
                }
                for (i = i + 1; i < size; i++) {
                    slot = slots[i];
                    ItemStack stack = getItem(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (canTakeItemThroughFace(slot, stack, side) && matches(stack, findStack)) {
                        count += stack.getCount();
                        if (count < maxAmount) {
                            continue;
                        }
                        return onExtract(directCopy(findStack, maxAmount));
                    }
                }
                return onExtract(directCopy(findStack, count));
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    @NonNull
    default int countSpace(ItemStack stack, @Nullable Direction side) {
        int maxAmount = stack.getCount();
        if (maxAmount == 0) {
            return 0;
        }
        return countSpace(stack, maxAmount, side);
    }

    @Override
    default int countSpace(@NonNull ItemStack stack, int maxAmount, @Nullable Direction side) {
        int count = 0;
        for (int slot : getSlotsForFace(side)) {
            if (canPlaceItemThroughFace(slot, stack, side) && canPlaceItem(slot, stack)) {
                ItemStack target = getItem(slot);
                if (target.isEmpty()) {
                    count += getMaxStackSize(stack) - target.getCount();
                    if (count >= maxAmount) {
                        return maxAmount;
                    }
                } else if (matches(target, stack)) {
                    count += target.getMaxStackSize() - target.getCount();
                    if (count >= maxAmount) {
                        return maxAmount;
                    }
                }
            }
        }
        return count;
    }

    @Override
    default int countSpace(@NonNull ItemStack stack, int maxAmount, int start, int end, @Nullable Direction side) {
        int count = 0;
        int[] slots = getSlotsForFace(side);
        start = WorldlyContainerExtension.findStartIndex(slots, start);
        if (start == -1) {
            return 0;
        }
        end = WorldlyContainerExtension.findEndIndex(slots, start, end);
        if (end == -1) {
            return 0;
        }
        for (int i = start; i <= end; i++) {
            int slot = slots[i];
            if (canPlaceItemThroughFace(slot, stack, side) && canPlaceItem(slot, stack)) {
                ItemStack target = getItem(slot);
                if (target.isEmpty()) {
                    count += getMaxStackSize(stack) - target.getCount();
                    if (count >= maxAmount) {
                        return maxAmount;
                    }
                } else if (matches(target, stack)) {
                    count += target.getMaxStackSize() - target.getCount();
                    if (count >= maxAmount) {
                        return maxAmount;
                    }
                }
            }
        }
        return count;
    }

    @Override
    @NonNull
    default boolean countSpace(List<ItemStack> stacks, @Nullable Direction side) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return true;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            return countSpace(stack, count, side) == count;
        }
        Object2IntLinkedOpenCustomHashMap<ItemStack> map = new Object2IntLinkedOpenCustomHashMap<>(
            ITEM_STACK_HASH_STRATEGY);
        for (ItemStack stack : stacks) {
            map.merge(stack, stack.getCount(), Integer::sum);
        }
        Object2IntSortedMap.FastSortedEntrySet<ItemStack> entries = map.object2IntEntrySet();
        if (entries.size() == 1) {
            Object2IntMap.Entry<ItemStack> entry = entries.first();
            ItemStack stack = entry.getKey();
            int count = entry.getIntValue();
            return countSpace(stack, count, side) == count;
        }
        for (int slot : getSlotsForFace(side)) {
            ItemStack target = getItem(slot);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (canPlaceItemThroughFace(slot, stack, side) && canPlaceItem(slot, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, getMaxStackSize(stack));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                return true;
                            }
                        } else {
                            entry.setValue(remaining - insert);
                        }
                        break;
                    }
                    if (matches(target, stack)) {
                        int maxCount = target.getMaxStackSize();
                        int count = target.getCount();
                        if (count != maxCount) {
                            int remaining = entry.getIntValue();
                            int insert = Math.min(remaining, maxCount - count);
                            if (remaining == insert) {
                                iterator.remove();
                                if (entries.isEmpty()) {
                                    return true;
                                }
                            } else {
                                entry.setValue(remaining - insert);
                            }
                        }
                        break;
                    }
                }
            } while (iterator.hasNext());
        }
        return false;
    }

    @Override
    @NonNull
    default boolean countSpace(List<ItemStack> stacks, int start, int end, @Nullable Direction side) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return true;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            return countSpace(stack, count, start, end, side) == count;
        }
        Object2IntLinkedOpenCustomHashMap<ItemStack> map = new Object2IntLinkedOpenCustomHashMap<>(
            ITEM_STACK_HASH_STRATEGY);
        for (ItemStack stack : stacks) {
            map.merge(stack, stack.getCount(), Integer::sum);
        }
        Object2IntSortedMap.FastSortedEntrySet<ItemStack> entries = map.object2IntEntrySet();
        if (entries.size() == 1) {
            Object2IntMap.Entry<ItemStack> entry = entries.first();
            ItemStack stack = entry.getKey();
            int count = entry.getIntValue();
            return countSpace(stack, count, start, end, side) == count;
        }
        int[] slots = getSlotsForFace(side);
        start = WorldlyContainerExtension.findStartIndex(slots, start);
        if (start == -1) {
            return false;
        }
        end = WorldlyContainerExtension.findEndIndex(slots, start, end);
        if (end == -1) {
            return false;
        }
        for (int i = start; i <= end; i++) {
            int slot = slots[i];
            ItemStack target = getItem(slot);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (canPlaceItemThroughFace(slot, stack, side) && canPlaceItem(slot, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, getMaxStackSize(stack));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                return true;
                            }
                        } else {
                            entry.setValue(remaining - insert);
                        }
                        break;
                    }
                    if (matches(target, stack)) {
                        int maxCount = target.getMaxStackSize();
                        int count = target.getCount();
                        if (count != maxCount) {
                            int remaining = entry.getIntValue();
                            int insert = Math.min(remaining, maxCount - count);
                            if (remaining == insert) {
                                iterator.remove();
                                if (entries.isEmpty()) {
                                    return true;
                                }
                            } else {
                                entry.setValue(remaining - insert);
                            }
                        }
                        break;
                    }
                }
            } while (iterator.hasNext());
        }
        return false;
    }

    @Override
    default int extract(@NonNull ItemStack stack, int maxAmount, @Nullable Direction side) {
        int remaining = maxAmount;
        for (int slot : getSlotsForFace(side)) {
            if (canTakeItemThroughFace(slot, stack, side)) {
                ItemStack target = getItem(slot);
                if (target.isEmpty()) {
                    continue;
                }
                if (matches(target, stack)) {
                    int count = target.getCount();
                    if (count > remaining) {
                        target.setCount(count - remaining);
                        setChanged();
                        return maxAmount;
                    }
                    setItem(slot, ItemStack.EMPTY);
                    if (count == remaining) {
                        setChanged();
                        return maxAmount;
                    }
                    remaining -= count;
                }
            }
        }
        if (remaining == maxAmount) {
            return 0;
        }
        setChanged();
        return maxAmount - remaining;
    }

    @Override
    default @NonNull ItemStack extract(
        @NonNull Predicate<ItemStack> predicate,
        int maxAmount,
        @Nullable Direction side
    ) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        int[] slots = getSlotsForFace(side);
        for (int i = 0, size = slots.length; i < size; i++) {
            int slot = slots[i];
            ItemStack findStack = getItem(slot);
            if (findStack.isEmpty()) {
                continue;
            }
            if (canTakeItemThroughFace(slot, findStack, side) && predicate.test(findStack)) {
                int count = findStack.getCount();
                if (count > maxAmount) {
                    findStack.setCount(count - maxAmount);
                    setChanged();
                    return onExtract(directCopy(findStack, maxAmount));
                }
                setItem(slot, ItemStack.EMPTY);
                if (count == maxAmount) {
                    setChanged();
                    return onExtract(findStack);
                }
                int remaining = maxAmount - count;
                for (i = i + 1; i < size; i++) {
                    slot = slots[i];
                    ItemStack stack = getItem(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (canTakeItemThroughFace(slot, stack, side) && matches(stack, findStack)) {
                        count = stack.getCount();
                        if (count < remaining) {
                            setItem(slot, ItemStack.EMPTY);
                            remaining -= count;
                            continue;
                        }
                        if (count == remaining) {
                            setItem(slot, ItemStack.EMPTY);
                        } else {
                            stack.setCount(count - remaining);
                        }
                        setChanged();
                        findStack.setCount(maxAmount);
                        return onExtract(findStack);
                    }
                }
                setChanged();
                findStack.setCount(maxAmount - remaining);
                return onExtract(findStack);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    default @NonNull ItemStack extract(@NonNull Predicate<ItemStack> predicate, @Nullable Direction side) {
        for (int slot : getSlotsForFace(side)) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (canTakeItemThroughFace(slot, stack, side) && predicate.test(stack)) {
                setItem(slot, ItemStack.EMPTY);
                setChanged();
                return onExtract(stack);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    @NonNull
    default List<ItemStack> extract(@NonNull List<ItemStack> stacks, @Nullable Direction side) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return stacks;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            int extract = extract(stack, side);
            if (count == extract) {
                return List.of();
            }
            if (extract == 0) {
                return stacks;
            }
            return List.of(directCopy(stack, count - extract));
        }
        Object2IntLinkedOpenCustomHashMap<ItemStack> map = new Object2IntLinkedOpenCustomHashMap<>(
            ITEM_STACK_HASH_STRATEGY);
        for (ItemStack stack : stacks) {
            map.merge(stack, stack.getCount(), Integer::sum);
        }
        Object2IntSortedMap.FastSortedEntrySet<ItemStack> entries = map.object2IntEntrySet();
        if (entries.size() == 1) {
            Object2IntMap.Entry<ItemStack> entry = entries.first();
            ItemStack stack = entry.getKey();
            int count = entry.getIntValue();
            int extract = extract(stack, count, side);
            if (count == extract) {
                return List.of();
            }
            if (extract == 0) {
                return stacks;
            }
            return List.of(directCopy(stack, count - extract));
        }
        boolean dirty = false;
        for (int slot : getSlotsForFace(side)) {
            ItemStack target = getItem(slot);
            if (target.isEmpty()) {
                continue;
            }
            if (canTakeItemThroughFace(slot, target, side)) {
                ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
                do {
                    Object2IntMap.Entry<ItemStack> entry = iterator.next();
                    ItemStack stack = entry.getKey();
                    if (matches(target, stack)) {
                        int count = target.getCount();
                        int remaining = entry.getIntValue();
                        if (count < remaining) {
                            setItem(slot, ItemStack.EMPTY);
                            entry.setValue(remaining - count);
                            break;
                        }
                        if (count == remaining) {
                            setItem(slot, ItemStack.EMPTY);
                        } else {
                            target.setCount(count - remaining);
                        }
                        iterator.remove();
                        if (entries.isEmpty()) {
                            setChanged();
                            return List.of();
                        }
                        dirty = true;
                        break;
                    }
                } while (iterator.hasNext());
            }
        }
        if (dirty) {
            List<ItemStack> result = new ArrayList<>(entries.size());
            for (Object2IntMap.Entry<ItemStack> entry : entries) {
                ItemStack stack = entry.getKey();
                int count = entry.getIntValue();
                if (stack.getCount() == count) {
                    result.add(stack);
                } else {
                    result.add(directCopy(stack, count));
                }
            }
            setChanged();
            return result;
        }
        return stacks;
    }

    @Override
    default int extractAll(@NonNull Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
        if (maxAmount == 0) {
            return 0;
        }
        int remaining = maxAmount;
        for (int slot : getSlotsForFace(side)) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (predicate.test(stack) && canTakeItemThroughFace(slot, stack, side)) {
                int count = stack.getCount();
                if (count < remaining) {
                    setItem(slot, ItemStack.EMPTY);
                    remaining -= count;
                    continue;
                }
                if (count == remaining) {
                    setItem(slot, ItemStack.EMPTY);
                } else {
                    stack.setCount(count - remaining);
                }
                setChanged();
                return maxAmount;
            }
        }
        if (remaining == maxAmount) {
            return 0;
        }
        setChanged();
        return maxAmount - remaining;
    }

    @Override
    default @NonNull ItemStack extractAny(@Nullable Direction side) {
        for (int slot : getSlotsForFace(side)) {
            ItemStack target = getItem(slot);
            if (target.isEmpty()) {
                continue;
            }
            if (canTakeItemThroughFace(slot, target, side)) {
                setItem(slot, ItemStack.EMPTY);
                setChanged();
                return onExtract(target);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    default @NonNull ItemStack extractAny(int maxAmount, @Nullable Direction side) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        int[] slots = getSlotsForFace(side);
        int size = slots.length;
        for (int i = 0; i < size; i++) {
            int slot = slots[i];
            ItemStack findStack = getItem(slot);
            if (findStack.isEmpty()) {
                continue;
            }
            if (canTakeItemThroughFace(slot, findStack, side)) {
                int count = findStack.getCount();
                if (count > maxAmount) {
                    findStack.setCount(count - maxAmount);
                    setChanged();
                    return onExtract(directCopy(findStack, maxAmount));
                }
                setItem(slot, ItemStack.EMPTY);
                if (count == maxAmount) {
                    setChanged();
                    return onExtract(findStack);
                }
                int remaining = maxAmount - count;
                for (i = i + 1; i < size; i++) {
                    slot = slots[i];
                    ItemStack stack = getItem(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (canTakeItemThroughFace(slot, stack, side) && matches(stack, findStack)) {
                        count = stack.getCount();
                        if (count < remaining) {
                            setItem(slot, ItemStack.EMPTY);
                            remaining -= count;
                            continue;
                        }
                        if (count == remaining) {
                            setItem(slot, ItemStack.EMPTY);
                        } else {
                            stack.setCount(count - remaining);
                        }
                        setChanged();
                        findStack.setCount(maxAmount);
                        return onExtract(findStack);
                    }
                }
                setChanged();
                findStack.setCount(maxAmount - remaining);
                return onExtract(findStack);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    @NonNull
    default ItemStack extractAnyMax(@Nullable Direction side) {
        int[] slots = getSlotsForFace(side);
        int size = slots.length;
        for (int i = 0; i < size; i++) {
            int slot = slots[i];
            ItemStack findStack = getItem(slot);
            if (findStack.isEmpty()) {
                continue;
            }
            if (canTakeItemThroughFace(slot, findStack, side)) {
                int count = findStack.getCount();
                int maxAmount = findStack.getMaxStackSize();
                setItem(slot, ItemStack.EMPTY);
                if (count == maxAmount) {
                    setChanged();
                    return onExtract(findStack);
                }
                int remaining = maxAmount - count;
                for (i = i + 1; i < size; i++) {
                    slot = slots[i];
                    ItemStack stack = getItem(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (canTakeItemThroughFace(slot, stack, side) && matches(stack, findStack)) {
                        count = stack.getCount();
                        if (count < remaining) {
                            setItem(slot, ItemStack.EMPTY);
                            remaining -= count;
                            continue;
                        }
                        if (count == remaining) {
                            setItem(slot, ItemStack.EMPTY);
                        } else {
                            stack.setCount(count - remaining);
                        }
                        setChanged();
                        findStack.setCount(maxAmount);
                        return onExtract(findStack);
                    }
                }
                setChanged();
                findStack.setCount(maxAmount - remaining);
                return onExtract(findStack);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    @NonNull
    default ItemStack extractMax(@NonNull Predicate<ItemStack> predicate, @Nullable Direction side) {
        int[] slots = getSlotsForFace(side);
        int size = slots.length;
        for (int i = 0; i < size; i++) {
            int slot = slots[i];
            ItemStack findStack = getItem(slot);
            if (findStack.isEmpty()) {
                continue;
            }
            if (predicate.test(findStack) && canTakeItemThroughFace(slot, findStack, side)) {
                int count = findStack.getCount();
                int maxAmount = findStack.getMaxStackSize();
                setItem(slot, ItemStack.EMPTY);
                if (count == maxAmount) {
                    setChanged();
                    return onExtract(findStack);
                }
                int remaining = maxAmount - count;
                for (i = i + 1; i < size; i++) {
                    slot = slots[i];
                    ItemStack stack = getItem(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (canTakeItemThroughFace(slot, stack, side) && matches(stack, findStack)) {
                        count = stack.getCount();
                        if (count < remaining) {
                            setItem(slot, ItemStack.EMPTY);
                            remaining -= count;
                            continue;
                        }
                        if (count == remaining) {
                            setItem(slot, ItemStack.EMPTY);
                        } else {
                            stack.setCount(count - remaining);
                        }
                        setChanged();
                        findStack.setCount(maxAmount);
                        return onExtract(findStack);
                    }
                }
                setChanged();
                findStack.setCount(maxAmount - remaining);
                return onExtract(findStack);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    default int insert(@NonNull ItemStack stack, int maxAmount, @Nullable Direction side) {
        int remaining = maxAmount;
        for (int slot : getSlotsForFace(side)) {
            if (canPlaceItemThroughFace(slot, stack, side) && canPlaceItem(slot, stack)) {
                ItemStack target = getItem(slot);
                if (target.isEmpty()) {
                    int insert = Math.min(remaining, getMaxStackSize(stack));
                    setItem(slot, directCopy(stack, insert));
                    if (remaining == insert) {
                        setChanged();
                        return maxAmount;
                    }
                    remaining -= insert;
                } else if (matches(target, stack)) {
                    int maxCount = target.getMaxStackSize();
                    int count = target.getCount();
                    if (count != maxCount) {
                        int insert = Math.min(remaining, maxCount - count);
                        target.setCount(count + insert);
                        if (remaining == insert) {
                            setChanged();
                            return maxAmount;
                        }
                        remaining -= insert;
                    }
                }
            }
        }
        if (remaining == maxAmount) {
            return 0;
        }
        setChanged();
        return maxAmount - remaining;
    }

    @Override
    default int insert(@NonNull ItemStack stack, int maxAmount, int start, int end, @Nullable Direction side) {
        int remaining = maxAmount;
        int[] slots = getSlotsForFace(side);
        start = WorldlyContainerExtension.findStartIndex(slots, start);
        if (start == -1) {
            return 0;
        }
        end = WorldlyContainerExtension.findEndIndex(slots, start, end);
        if (end == -1) {
            return 0;
        }
        for (int i = start; i <= end; i++) {
            int slot = slots[i];
            if (canPlaceItemThroughFace(slot, stack, side) && canPlaceItem(slot, stack)) {
                ItemStack target = getItem(slot);
                if (target.isEmpty()) {
                    int insert = Math.min(remaining, getMaxStackSize(stack));
                    setItem(slot, directCopy(stack, insert));
                    if (remaining == insert) {
                        setChanged();
                        return maxAmount;
                    }
                    remaining -= insert;
                } else if (matches(target, stack)) {
                    int maxCount = target.getMaxStackSize();
                    int count = target.getCount();
                    if (count != maxCount) {
                        int insert = Math.min(remaining, maxCount - count);
                        target.setCount(count + insert);
                        if (remaining == insert) {
                            setChanged();
                            return maxAmount;
                        }
                        remaining -= insert;
                    }
                }
            }
        }
        if (remaining == maxAmount) {
            return 0;
        }
        setChanged();
        return maxAmount - remaining;
    }

    @Override
    @NonNull
    default List<ItemStack> insert(@NonNull List<ItemStack> stacks, @Nullable Direction side) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return stacks;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            int insert = insert(stack, side);
            if (count == insert) {
                return List.of();
            }
            if (insert == 0) {
                return stacks;
            }
            return List.of(directCopy(stack, count - insert));
        }
        Object2IntLinkedOpenCustomHashMap<ItemStack> map = new Object2IntLinkedOpenCustomHashMap<>(
            ITEM_STACK_HASH_STRATEGY);
        for (ItemStack stack : stacks) {
            map.merge(stack, stack.getCount(), Integer::sum);
        }
        Object2IntSortedMap.FastSortedEntrySet<ItemStack> entries = map.object2IntEntrySet();
        if (entries.size() == 1) {
            Object2IntMap.Entry<ItemStack> entry = entries.first();
            ItemStack stack = entry.getKey();
            int count = entry.getIntValue();
            int insert = insert(stack, count, side);
            if (count == insert) {
                return List.of();
            }
            if (insert == 0) {
                return stacks;
            }
            return List.of(directCopy(stack, count - insert));
        }
        boolean dirty = false;
        for (int slot : getSlotsForFace(side)) {
            ItemStack target = getItem(slot);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (canPlaceItemThroughFace(slot, stack, side) && canPlaceItem(slot, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, getMaxStackSize(stack));
                        setItem(slot, directCopy(stack, insert));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                setChanged();
                                return List.of();
                            }
                        } else {
                            entry.setValue(remaining - insert);
                        }
                        dirty = true;
                        break;
                    }
                    if (matches(target, stack)) {
                        int maxCount = target.getMaxStackSize();
                        int count = target.getCount();
                        if (count != maxCount) {
                            int remaining = entry.getIntValue();
                            int insert = Math.min(remaining, maxCount - count);
                            target.setCount(count + insert);
                            if (remaining == insert) {
                                iterator.remove();
                                if (entries.isEmpty()) {
                                    setChanged();
                                    return List.of();
                                }
                            } else {
                                entry.setValue(remaining - insert);
                            }
                            dirty = true;
                        }
                        break;
                    }
                }
            } while (iterator.hasNext());
        }
        if (dirty) {
            List<ItemStack> result = new ArrayList<>(entries.size());
            for (Object2IntMap.Entry<ItemStack> entry : entries) {
                ItemStack stack = entry.getKey();
                int count = entry.getIntValue();
                if (stack.getCount() == count) {
                    result.add(stack);
                } else {
                    result.add(directCopy(stack, count));
                }
            }
            setChanged();
            return result;
        }
        return stacks;
    }

    @Override
    @NonNull
    default List<ItemStack> insert(@NonNull List<ItemStack> stacks, int start, int end, @Nullable Direction side) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return stacks;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            int insert = insert(stack, count, start, end, side);
            if (count == insert) {
                return List.of();
            }
            if (insert == 0) {
                return stacks;
            }
            return List.of(directCopy(stack, count - insert));
        }
        Object2IntLinkedOpenCustomHashMap<ItemStack> map = new Object2IntLinkedOpenCustomHashMap<>(
            ITEM_STACK_HASH_STRATEGY);
        for (ItemStack stack : stacks) {
            map.merge(stack, stack.getCount(), Integer::sum);
        }
        Object2IntSortedMap.FastSortedEntrySet<ItemStack> entries = map.object2IntEntrySet();
        if (entries.size() == 1) {
            Object2IntMap.Entry<ItemStack> entry = entries.first();
            ItemStack stack = entry.getKey();
            int count = entry.getIntValue();
            int insert = insert(stack, count, start, end, side);
            if (count == insert) {
                return List.of();
            }
            if (insert == 0) {
                return stacks;
            }
            return List.of(directCopy(stack, count - insert));
        }
        boolean dirty = false;
        int[] slots = getSlotsForFace(side);
        start = WorldlyContainerExtension.findStartIndex(slots, start);
        if (start == -1) {
            return stacks;
        }
        end = WorldlyContainerExtension.findEndIndex(slots, start, end);
        if (end == -1) {
            return stacks;
        }
        for (int i = start; i <= end; i++) {
            int slot = slots[i];
            ItemStack target = getItem(slot);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (canPlaceItemThroughFace(slot, stack, side) && canPlaceItem(slot, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, getMaxStackSize(stack));
                        setItem(slot, directCopy(stack, insert));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                setChanged();
                                return List.of();
                            }
                        } else {
                            entry.setValue(remaining - insert);
                        }
                        dirty = true;
                        break;
                    }
                    if (matches(target, stack)) {
                        int maxCount = target.getMaxStackSize();
                        int count = target.getCount();
                        if (count != maxCount) {
                            int remaining = entry.getIntValue();
                            int insert = Math.min(remaining, maxCount - count);
                            target.setCount(count + insert);
                            if (remaining == insert) {
                                iterator.remove();
                                if (entries.isEmpty()) {
                                    setChanged();
                                    return List.of();
                                }
                            } else {
                                entry.setValue(remaining - insert);
                            }
                            dirty = true;
                        }
                        break;
                    }
                }
            } while (iterator.hasNext());
        }
        if (dirty) {
            List<ItemStack> result = new ArrayList<>(entries.size());
            for (Object2IntMap.Entry<ItemStack> entry : entries) {
                ItemStack stack = entry.getKey();
                int count = entry.getIntValue();
                if (stack.getCount() == count) {
                    result.add(stack);
                } else {
                    result.add(directCopy(stack, count));
                }
            }
            setChanged();
            return result;
        }
        return stacks;
    }

    @Override
    default int insertExist(@NonNull ItemStack stack, int maxAmount, @Nullable Direction side) {
        int remaining = maxAmount;
        IntList emptys = new IntArrayList();
        for (int slot : getSlotsForFace(side)) {
            if (canPlaceItemThroughFace(slot, stack, side) && canPlaceItem(slot, stack)) {
                ItemStack target = getItem(slot);
                if (target.isEmpty()) {
                    emptys.add(slot);
                } else if (matches(target, stack)) {
                    int maxCount = target.getMaxStackSize();
                    int count = target.getCount();
                    if (count != maxCount) {
                        int insert = Math.min(remaining, maxCount - count);
                        target.setCount(count + insert);
                        if (remaining == insert) {
                            setChanged();
                            return maxAmount;
                        }
                        remaining -= insert;
                    }
                }
            }
        }
        for (int i = 0, size = emptys.size(); i < size; i++) {
            int insert = Math.min(remaining, getMaxStackSize(stack));
            setItem(emptys.getInt(i), directCopy(stack, insert));
            if (remaining == insert) {
                setChanged();
                return maxAmount;
            }
            remaining -= insert;
        }
        if (remaining == maxAmount) {
            return 0;
        }
        setChanged();
        return maxAmount - remaining;
    }

    @Override
    default boolean preciseExtract(@NonNull ItemStack stack, @Nullable Direction side) {
        if (stack.isEmpty()) {
            return true;
        }
        int remaining = stack.getCount();
        IntList buffer = new IntArrayList();
        for (int slot : getSlotsForFace(side)) {
            if (canTakeItemThroughFace(slot, stack, side)) {
                ItemStack target = getItem(slot);
                if (target.isEmpty()) {
                    continue;
                }
                if (matches(target, stack)) {
                    int count = target.getCount();
                    if (count > remaining) {
                        for (int i = 0, size = buffer.size(); i < size; i++) {
                            setItem(buffer.getInt(i), ItemStack.EMPTY);
                        }
                        target.setCount(count - remaining);
                        setChanged();
                        return true;
                    }
                    if (count == remaining) {
                        for (int i = 0, size = buffer.size(); i < size; i++) {
                            setItem(buffer.getInt(i), ItemStack.EMPTY);
                        }
                        setItem(slot, ItemStack.EMPTY);
                        setChanged();
                        return true;
                    }
                    buffer.add(slot);
                    remaining -= count;
                }
            }
        }
        return false;
    }

    @Override
    default @NonNull ItemStack preciseExtract(
        @NonNull Predicate<ItemStack> predicate,
        int maxAmount,
        @Nullable Direction side
    ) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        int[] slots = getSlotsForFace(side);
        int size = slots.length;
        IntList buffer = new IntArrayList(size);
        IntSet blackList = new IntOpenHashSet();
        for (int i = 0; i < size; i++) {
            int slot = slots[i];
            ItemStack findStack = getItem(slot);
            if (findStack.isEmpty() || blackList.contains(i)) {
                continue;
            }
            if (canTakeItemThroughFace(slot, findStack, side) && predicate.test(findStack)) {
                buffer.clear();
                int count = findStack.getCount();
                if (count > maxAmount) {
                    findStack.setCount(count - maxAmount);
                    setChanged();
                    return onExtract(directCopy(findStack, maxAmount));
                }
                if (count == maxAmount) {
                    setItem(slot, ItemStack.EMPTY);
                    setChanged();
                    return onExtract(findStack);
                }
                buffer.add(slot);
                int remaining = maxAmount - count;
                for (int j = i + 1; j < size; j++) {
                    slot = slots[j];
                    ItemStack stack = getItem(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (canTakeItemThroughFace(slot, stack, side) && matches(stack, findStack)) {
                        count = stack.getCount();
                        if (count < remaining) {
                            blackList.add(j);
                            buffer.add(slot);
                            remaining -= count;
                            continue;
                        }
                        for (int k = 0, bSize = buffer.size(); k < bSize; k++) {
                            setItem(buffer.getInt(k), ItemStack.EMPTY);
                        }
                        if (count == remaining) {
                            setItem(slot, ItemStack.EMPTY);
                        } else {
                            stack.setCount(count - remaining);
                        }
                        setChanged();
                        findStack.setCount(maxAmount);
                        return onExtract(findStack);
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    default boolean preciseInsert(@NonNull ItemStack stack, int maxAmount, @Nullable Direction side) {
        List<Runnable> changes = new ArrayList<>();
        for (int slot : getSlotsForFace(side)) {
            if (canPlaceItemThroughFace(slot, stack, side) && canPlaceItem(slot, stack)) {
                ItemStack target = getItem(slot);
                if (target.isEmpty()) {
                    int insert = Math.min(maxAmount, getMaxStackSize(stack));
                    if (maxAmount == insert) {
                        changes.forEach(Runnable::run);
                        setItem(slot, directCopy(stack, insert));
                        setChanged();
                        return true;
                    }
                    changes.add(() -> setItem(slot, directCopy(stack, insert)));
                    maxAmount -= insert;
                } else if (matches(target, stack)) {
                    int maxCount = target.getMaxStackSize();
                    int count = target.getCount();
                    if (count != maxCount) {
                        int insert = Math.min(maxAmount, maxCount - count);
                        if (maxAmount == insert) {
                            changes.forEach(Runnable::run);
                            target.setCount(count + insert);
                            setChanged();
                            return true;
                        }
                        changes.add(() -> target.setCount(count + insert));
                        maxAmount -= insert;
                    }
                }
            }
        }
        return false;
    }

    @Override
    default boolean preciseInsert(@NonNull List<ItemStack> stacks, @Nullable Direction side) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return true;
        }
        if (listSize == 1) {
            return preciseInsert(stacks.getFirst(), side);
        }
        Object2IntLinkedOpenCustomHashMap<ItemStack> map = new Object2IntLinkedOpenCustomHashMap<>(
            ITEM_STACK_HASH_STRATEGY);
        for (ItemStack stack : stacks) {
            map.merge(stack, stack.getCount(), Integer::sum);
        }
        Object2IntSortedMap.FastSortedEntrySet<ItemStack> entries = map.object2IntEntrySet();
        if (entries.size() == 1) {
            Object2IntMap.Entry<ItemStack> entry = entries.first();
            return preciseInsert(entry.getKey(), entry.getIntValue(), side);
        }
        List<Runnable> changes = new ArrayList<>();
        for (int slot : getSlotsForFace(side)) {
            ItemStack target = getItem(slot);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (canPlaceItemThroughFace(slot, stack, side) && canPlaceItem(slot, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, getMaxStackSize(stack));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                changes.forEach(Runnable::run);
                                setItem(slot, directCopy(stack, insert));
                                setChanged();
                                return true;
                            }
                        } else {
                            changes.add(() -> setItem(slot, directCopy(stack, insert)));
                            entry.setValue(remaining - insert);
                        }
                        break;
                    }
                    if (matches(target, stack)) {
                        int maxCount = target.getMaxStackSize();
                        int count = target.getCount();
                        if (count != maxCount) {
                            int remaining = entry.getIntValue();
                            int insert = Math.min(remaining, maxCount - count);
                            if (remaining == insert) {
                                iterator.remove();
                                if (entries.isEmpty()) {
                                    changes.forEach(Runnable::run);
                                    target.setCount(count + insert);
                                    setChanged();
                                    return true;
                                }
                            } else {
                                changes.add(() -> target.setCount(count + insert));
                                entry.setValue(remaining - insert);
                            }
                        }
                        break;
                    }
                }
            } while (iterator.hasNext());
        }
        return false;
    }

    @Override
    default boolean update(
        @NonNull Predicate<ItemStack> predicate,
        @NonNull Function<ItemStack, ItemStack> update,
        @Nullable Direction side
    ) {
        for (int slot : getSlotsForFace(side)) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (predicate.test(stack) && canTakeItemThroughFace(slot, stack, side)) {
                ItemStack replace = update.apply(stack);
                if (replace != stack) {
                    setItem(slot, replace);
                }
                setChanged();
                return true;
            }
        }
        return false;
    }
}
