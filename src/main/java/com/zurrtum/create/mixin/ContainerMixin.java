package com.zurrtum.create.mixin;

import com.zurrtum.create.infrastructure.items.ContainerExtension;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(Container.class)
public interface ContainerMixin extends ContainerExtension {
    @Shadow
    void setItem(int slot, ItemStack itemStack);

    @Shadow
    int getContainerSize();

    @Shadow
    int getMaxStackSize(ItemStack itemStack);

    @Shadow
    ItemStack getItem(int slot);

    @Shadow
    boolean canPlaceItem(int slot, ItemStack itemStack);

    @Shadow
    void setChanged();

    @Override
    default int count(@NonNull ItemStack stack, int maxAmount) {
        int count = 0;
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack target = getItem(i);
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
        return count;
    }

    @Override
    default @NonNull ItemStack count(@NonNull Predicate<ItemStack> predicate) {
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack stack = getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (predicate.test(stack)) {
                return onExtract(directCopy(stack, stack.getCount()));
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    default @NonNull ItemStack count(@NonNull Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack findStack = getItem(i);
            if (findStack.isEmpty()) {
                continue;
            }
            if (predicate.test(findStack)) {
                int count = findStack.getCount();
                if (count >= maxAmount) {
                    return onExtract(directCopy(findStack, maxAmount));
                }
                for (i = i + 1; i < size; i++) {
                    ItemStack stack = getItem(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (matches(stack, findStack)) {
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
    default @NonNull ItemStack preciseCount(@NonNull Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        IntSet blackList = new IntOpenHashSet();
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack findStack = getItem(i);
            if (findStack.isEmpty() || blackList.contains(i)) {
                continue;
            }
            if (predicate.test(findStack)) {
                int count = findStack.getCount();
                if (count >= maxAmount) {
                    return onExtract(directCopy(findStack, maxAmount));
                }
                for (int j = i + 1; j < size; j++) {
                    ItemStack stack = getItem(j);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (matches(stack, findStack)) {
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
    default int countAll(@NonNull Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return 0;
        }
        int count = 0;
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack stack = getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (predicate.test(stack)) {
                count += stack.getCount();
                if (count >= maxAmount) {
                    return maxAmount;
                }
            }
        }
        return count;
    }

    @Override
    default @NonNull ItemStack countAny() {
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack target = getItem(i);
            if (target.isEmpty()) {
                continue;
            }
            return onExtract(directCopy(target, target.getCount()));
        }
        return ItemStack.EMPTY;
    }

    @Override
    default @NonNull ItemStack countAny(int maxAmount) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack findStack = getItem(i);
            if (findStack.isEmpty()) {
                continue;
            }
            int count = findStack.getCount();
            if (count >= maxAmount) {
                return onExtract(directCopy(findStack, maxAmount));
            }
            for (i = i + 1; i < size; i++) {
                ItemStack stack = getItem(i);
                if (stack.isEmpty()) {
                    continue;
                }
                if (matches(stack, findStack)) {
                    count += stack.getCount();
                    if (count < maxAmount) {
                        continue;
                    }
                    return onExtract(directCopy(findStack, maxAmount));
                }
            }
            return onExtract(directCopy(findStack, count));
        }
        return ItemStack.EMPTY;
    }

    @Override
    default int countSpace(@NonNull ItemStack stack, int maxAmount) {
        int count = 0;
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            if (canPlaceItem(i, stack)) {
                ItemStack target = getItem(i);
                if (target.isEmpty()) {
                    count += getMaxStackSize(stack);
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
    default int countSpace(@NonNull ItemStack stack, int maxAmount, int start, int end) {
        int count = 0;
        for (int i = start; i <= end; i++) {
            if (canPlaceItem(i, stack)) {
                ItemStack target = getItem(i);
                if (target.isEmpty()) {
                    count += getMaxStackSize(stack);
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
    default boolean countSpace(@NonNull List<ItemStack> stacks) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return true;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            return countSpace(stack, count) == count;
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
            return countSpace(stack, count) == count;
        }
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack target = getItem(i);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (canPlaceItem(i, stack)) {
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
    default boolean countSpace(@NonNull List<ItemStack> stacks, int start, int end) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return true;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            return countSpace(stack, count, start, end) == count;
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
            return countSpace(stack, count, start, end) == count;
        }
        for (int i = start; i <= end; i++) {
            ItemStack target = getItem(i);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (canPlaceItem(i, stack)) {
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
    default int extract(@NonNull ItemStack stack, int maxAmount) {
        int remaining = maxAmount;
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack target = getItem(i);
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
                setItem(i, ItemStack.EMPTY);
                if (count == remaining) {
                    setChanged();
                    return maxAmount;
                }
                remaining -= count;
            }
        }
        if (remaining == maxAmount) {
            return 0;
        }
        setChanged();
        return maxAmount - remaining;
    }

    @Override
    default @NonNull ItemStack extract(@NonNull Predicate<ItemStack> predicate) {
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack target = getItem(i);
            if (target.isEmpty()) {
                continue;
            }
            if (predicate.test(target)) {
                setItem(i, ItemStack.EMPTY);
                setChanged();
                return onExtract(target);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    default @NonNull ItemStack extract(@NonNull Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack findStack = getItem(i);
            if (findStack.isEmpty()) {
                continue;
            }
            if (predicate.test(findStack)) {
                int count = findStack.getCount();
                if (count > maxAmount) {
                    findStack.setCount(count - maxAmount);
                    setChanged();
                    return onExtract(directCopy(findStack, maxAmount));
                }
                setItem(i, ItemStack.EMPTY);
                if (count == maxAmount) {
                    setChanged();
                    return onExtract(findStack);
                }
                int remaining = maxAmount - count;
                for (i = i + 1; i < size; i++) {
                    ItemStack stack = getItem(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (matches(stack, findStack)) {
                        count = stack.getCount();
                        if (count < remaining) {
                            setItem(i, ItemStack.EMPTY);
                            remaining -= count;
                            continue;
                        }
                        if (count == remaining) {
                            setItem(i, ItemStack.EMPTY);
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
    default @NonNull List<ItemStack> extract(@NonNull List<ItemStack> stacks) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return stacks;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            int extract = extract(stack);
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
            int extract = extract(stack, count);
            if (count == extract) {
                return List.of();
            }
            if (extract == 0) {
                return stacks;
            }
            return List.of(directCopy(stack, count - extract));
        }
        boolean dirty = false;
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack target = getItem(i);
            if (target.isEmpty()) {
                continue;
            }
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (matches(target, stack)) {
                    int count = target.getCount();
                    int remaining = entry.getIntValue();
                    if (count < remaining) {
                        setItem(i, ItemStack.EMPTY);
                        entry.setValue(remaining - count);
                        break;
                    }
                    if (count == remaining) {
                        setItem(i, ItemStack.EMPTY);
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
    default int extractAll(@NonNull Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return 0;
        }
        int remaining = maxAmount;
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack stack = getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (predicate.test(stack)) {
                int count = stack.getCount();
                if (count < remaining) {
                    setItem(i, ItemStack.EMPTY);
                    remaining -= count;
                    continue;
                }
                if (count == remaining) {
                    setItem(i, ItemStack.EMPTY);
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
    default @NonNull ItemStack extractAny() {
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack target = getItem(i);
            if (target.isEmpty()) {
                continue;
            }
            setItem(i, ItemStack.EMPTY);
            setChanged();
            return onExtract(target);
        }
        return ItemStack.EMPTY;
    }

    @Override
    default @NonNull ItemStack extractAny(int maxAmount) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack findStack = getItem(i);
            if (findStack.isEmpty()) {
                continue;
            }
            int count = findStack.getCount();
            if (count > maxAmount) {
                findStack.setCount(count - maxAmount);
                setChanged();
                return onExtract(directCopy(findStack, maxAmount));
            }
            setItem(i, ItemStack.EMPTY);
            if (count == maxAmount) {
                setChanged();
                return onExtract(findStack);
            }
            int remaining = maxAmount - count;
            for (i = i + 1; i < size; i++) {
                ItemStack stack = getItem(i);
                if (stack.isEmpty()) {
                    continue;
                }
                if (matches(stack, findStack)) {
                    count = stack.getCount();
                    if (count < remaining) {
                        setItem(i, ItemStack.EMPTY);
                        remaining -= count;
                        continue;
                    }
                    if (count == remaining) {
                        setItem(i, ItemStack.EMPTY);
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
        return ItemStack.EMPTY;
    }

    @Override
    default @NonNull ItemStack extractAnyMax() {
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack findStack = getItem(i);
            if (findStack.isEmpty()) {
                continue;
            }
            int count = findStack.getCount();
            int maxAmount = findStack.getMaxStackSize();
            setItem(i, ItemStack.EMPTY);
            if (count == maxAmount) {
                setChanged();
                return onExtract(findStack);
            }
            int remaining = maxAmount - count;
            for (i = i + 1; i < size; i++) {
                ItemStack stack = getItem(i);
                if (stack.isEmpty()) {
                    continue;
                }
                if (matches(stack, findStack)) {
                    count = stack.getCount();
                    if (count < remaining) {
                        setItem(i, ItemStack.EMPTY);
                        remaining -= count;
                        continue;
                    }
                    if (count == remaining) {
                        setItem(i, ItemStack.EMPTY);
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
        return ItemStack.EMPTY;
    }

    @Override
    default @NonNull ItemStack extractMax(@NonNull Predicate<ItemStack> predicate) {
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack findStack = getItem(i);
            if (findStack.isEmpty()) {
                continue;
            }
            if (predicate.test(findStack)) {
                int count = findStack.getCount();
                int maxAmount = findStack.getMaxStackSize();
                setItem(i, ItemStack.EMPTY);
                if (count == maxAmount) {
                    setChanged();
                    return onExtract(findStack);
                }
                int remaining = maxAmount - count;
                for (i = i + 1; i < size; i++) {
                    ItemStack stack = getItem(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (matches(stack, findStack)) {
                        count = stack.getCount();
                        if (count < remaining) {
                            setItem(i, ItemStack.EMPTY);
                            remaining -= count;
                            continue;
                        }
                        if (count == remaining) {
                            setItem(i, ItemStack.EMPTY);
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
    default int insert(@NonNull ItemStack stack, int maxAmount) {
        int remaining = maxAmount;
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            if (canPlaceItem(i, stack)) {
                ItemStack target = getItem(i);
                if (target.isEmpty()) {
                    int insert = Math.min(remaining, getMaxStackSize(stack));
                    setItem(i, directCopy(stack, insert));
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
    default int insert(@NonNull ItemStack stack, int maxAmount, int start, int end) {
        int remaining = maxAmount;
        for (int i = start; i < end; i++) {
            if (canPlaceItem(i, stack)) {
                ItemStack target = getItem(i);
                if (target.isEmpty()) {
                    int insert = Math.min(remaining, getMaxStackSize(stack));
                    setItem(i, directCopy(stack, insert));
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
    default @NonNull List<ItemStack> insert(@NonNull List<ItemStack> stacks) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return stacks;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            int insert = insert(stack);
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
            int insert = insert(stack, count);
            if (count == insert) {
                return List.of();
            }
            if (insert == 0) {
                return stacks;
            }
            return List.of(directCopy(stack, count - insert));
        }
        boolean dirty = false;
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack target = getItem(i);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (canPlaceItem(i, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, getMaxStackSize(stack));
                        setItem(i, directCopy(stack, insert));
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
    default @NonNull List<ItemStack> insert(@NonNull List<ItemStack> stacks, int start, int end) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return stacks;
        }
        if (listSize == 1) {
            ItemStack stack = stacks.getFirst();
            int count = stack.getCount();
            int insert = insert(stack, count, start, end);
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
            int insert = insert(stack, count, start, end);
            if (count == insert) {
                return List.of();
            }
            if (insert == 0) {
                return stacks;
            }
            return List.of(directCopy(stack, count - insert));
        }
        boolean dirty = false;
        for (int i = start; i < end; i++) {
            ItemStack target = getItem(i);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (canPlaceItem(i, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, getMaxStackSize(stack));
                        setItem(i, directCopy(stack, insert));
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
    default int insertExist(@NonNull ItemStack stack, int maxAmount) {
        int remaining = maxAmount;
        IntList emptys = new IntArrayList();
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            if (canPlaceItem(i, stack)) {
                ItemStack target = getItem(i);
                if (target.isEmpty()) {
                    emptys.add(i);
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
    default boolean preciseExtract(@NonNull ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        int remaining = stack.getCount();
        int size = getContainerSize();
        IntList buffer = new IntArrayList(size);
        for (int i = 0; i < size; i++) {
            ItemStack target = getItem(i);
            if (target.isEmpty()) {
                continue;
            }
            if (matches(target, stack)) {
                int count = target.getCount();
                if (count > remaining) {
                    for (int j = 0, bSize = buffer.size(); j < bSize; j++) {
                        setItem(buffer.getInt(j), ItemStack.EMPTY);
                    }
                    target.setCount(count - remaining);
                    setChanged();
                    return true;
                }
                if (count == remaining) {
                    for (int j = 0, bSize = buffer.size(); j < bSize; j++) {
                        setItem(buffer.getInt(j), ItemStack.EMPTY);
                    }
                    setItem(i, ItemStack.EMPTY);
                    setChanged();
                    return true;
                }
                buffer.add(i);
                remaining -= count;
            }
        }
        return false;
    }

    @Override
    default @NonNull ItemStack preciseExtract(@NonNull Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount == 0) {
            return ItemStack.EMPTY;
        }
        int size = getContainerSize();
        IntList buffer = new IntArrayList(size);
        IntSet blackList = new IntOpenHashSet();
        for (int i = 0; i < size; i++) {
            ItemStack findStack = getItem(i);
            if (findStack.isEmpty() || blackList.contains(i)) {
                continue;
            }
            if (predicate.test(findStack)) {
                buffer.clear();
                int count = findStack.getCount();
                if (count > maxAmount) {
                    findStack.setCount(count - maxAmount);
                    setChanged();
                    return onExtract(directCopy(findStack, maxAmount));
                }
                if (count == maxAmount) {
                    setItem(i, ItemStack.EMPTY);
                    setChanged();
                    return onExtract(findStack);
                }
                buffer.add(i);
                int remaining = maxAmount - count;
                for (int j = i + 1; j < size; j++) {
                    ItemStack stack = getItem(j);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (matches(stack, findStack)) {
                        count = stack.getCount();
                        if (count < remaining) {
                            blackList.add(j);
                            buffer.add(j);
                            remaining -= count;
                            continue;
                        }
                        for (int k = 0, bSize = buffer.size(); k < bSize; k++) {
                            setItem(buffer.getInt(k), ItemStack.EMPTY);
                        }
                        if (count == remaining) {
                            setItem(j, ItemStack.EMPTY);
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
    default boolean preciseInsert(@NonNull ItemStack stack, int maxAmount) {
        List<Runnable> changes = new ArrayList<>();
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            if (canPlaceItem(i, stack)) {
                ItemStack target = getItem(i);
                if (target.isEmpty()) {
                    int insert = Math.min(maxAmount, getMaxStackSize(stack));
                    if (maxAmount == insert) {
                        changes.forEach(Runnable::run);
                        setItem(i, directCopy(stack, insert));
                        setChanged();
                        return true;
                    }
                    int slot = i;
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
    default boolean preciseInsert(@NonNull List<ItemStack> stacks) {
        int listSize = stacks.size();
        if (listSize == 0) {
            return true;
        }
        if (listSize == 1) {
            return preciseInsert(stacks.getFirst());
        }
        Object2IntLinkedOpenCustomHashMap<ItemStack> map = new Object2IntLinkedOpenCustomHashMap<>(
            ITEM_STACK_HASH_STRATEGY);
        for (ItemStack stack : stacks) {
            map.merge(stack, stack.getCount(), Integer::sum);
        }
        Object2IntSortedMap.FastSortedEntrySet<ItemStack> entries = map.object2IntEntrySet();
        if (entries.size() == 1) {
            Object2IntMap.Entry<ItemStack> entry = entries.first();
            return preciseInsert(entry.getKey(), entry.getIntValue());
        }
        List<Runnable> changes = new ArrayList<>();
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack target = getItem(i);
            boolean empty = target.isEmpty();
            ObjectIterator<Object2IntMap.Entry<ItemStack>> iterator = entries.fastIterator();
            do {
                Object2IntMap.Entry<ItemStack> entry = iterator.next();
                ItemStack stack = entry.getKey();
                if (canPlaceItem(i, stack)) {
                    if (empty) {
                        int remaining = entry.getIntValue();
                        int insert = Math.min(remaining, getMaxStackSize(stack));
                        if (remaining == insert) {
                            iterator.remove();
                            if (entries.isEmpty()) {
                                changes.forEach(Runnable::run);
                                setItem(i, directCopy(stack, insert));
                                setChanged();
                                return true;
                            }
                        } else {
                            int slot = i;
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
    default boolean update(@NonNull Predicate<ItemStack> predicate, @NonNull Function<ItemStack, ItemStack> update) {
        for (int i = 0, size = getContainerSize(); i < size; i++) {
            ItemStack stack = getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (predicate.test(stack)) {
                ItemStack replace = update.apply(stack);
                if (replace != stack) {
                    setItem(i, replace);
                }
                setChanged();
                return true;
            }
        }
        return false;
    }
}
