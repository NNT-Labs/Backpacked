package com.mrcrayfish.backpacked.integration;

import com.mojang.serialization.DynamicOps;
import com.mrcrayfish.backpacked.BackpackHelper;
import com.mrcrayfish.backpacked.Config;
import com.mrcrayfish.backpacked.Constants;
import com.mrcrayfish.backpacked.common.augment.AugmentHandler;
import com.mrcrayfish.backpacked.common.augment.impl.RecallAugment;
import com.mrcrayfish.backpacked.core.ModAugmentTypes;
import com.mrcrayfish.backpacked.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.api.StackWithSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Integration with the Gravestones mod, which stores equipped backpacks inside the gravestone
 * instead of leaving them on the ground.
 * <p>
 * Gravestones creates the grave from the head of {@code LivingEntity#dropAllDeathLoot}, which runs
 * before {@code Player#dropEquipment} where Backpacked drops its own backpacks. Removing the
 * backpacks here is therefore enough to stop them being dropped as well; no cancelling required.
 */
public class GravestonesSupport
{
    public static void init()
    {
        GravestonesApi.registerDataType(Utils.rl("backpacks"), new BackpackedDataType());
    }

    private static class BackpackedDataType extends GravestoneDataType
    {
        private static final String KEY = "backpacks";

        @Override
        public void writeData(CompoundTag tag, DynamicOps<Tag> ops, Player player)
        {
            ListTag list = new ListTag();

            // Backpacked keeps the backpacks equipped when this is enabled, so leave them alone
            if(!Config.BACKPACK.equipable.keepOnDeath.get() && player instanceof ServerPlayer serverPlayer)
            {
                NonNullList<ItemStack> backpacks = BackpackHelper.getBackpacks(player);
                for(int index = 0; index < backpacks.size(); index++)
                {
                    ItemStack stack = backpacks.get(index);
                    if(stack.isEmpty())
                        continue;

                    // Lets other mods react to, or opt out of, the backpack being stored. Skipped
                    // backpacks are left equipped so they fall back to Backpacked's own handling.
                    ResourceLocation slotId = Utils.rl(Integer.toString(index));
                    GravestonesApi.onInsertItem(player, stack, slotId);
                    if(stack.isEmpty() || GravestonesApi.shouldSkipItem(player, stack, slotId))
                        continue;

                    // The recall augment takes priority over the grave, matching the normal death behaviour
                    RecallAugment augment = BackpackHelper.findAugment(stack, ModAugmentTypes.RECALL.get());
                    if(augment != null && AugmentHandler.recallBackpack(serverPlayer, index, stack, augment))
                    {
                        backpacks.set(index, ItemStack.EMPTY);
                        continue;
                    }

                    // If a backpack somehow fails to serialise, leave it equipped so that
                    // Backpacked drops it normally rather than losing it to the grave
                    Optional<Tag> encoded = StackWithSlot.CODEC.encodeStart(ops, new StackWithSlot(index, stack.copy())).resultOrPartial(Constants.LOG::error);
                    if(encoded.isEmpty())
                        continue;

                    list.add(encoded.get());
                    backpacks.set(index, ItemStack.EMPTY);
                }
            }

            tag.put(KEY, list);
        }

        @Override
        public void onBreak(CompoundTag tag, DynamicOps<Tag> ops, Level level, BlockPos pos, int decay)
        {
            this.readBackpacks(tag, ops).forEach(entry -> this.dropStack(level, pos, entry.stack()));
        }

        @Override
        public void onCollect(CompoundTag tag, DynamicOps<Tag> ops, Level level, BlockPos pos, Player player, int decay)
        {
            List<ItemStack> remaining = new ArrayList<>();
            for(StackWithSlot entry : this.readBackpacks(tag, ops))
            {
                ItemStack stack = entry.stack();
                if(stack.isEmpty())
                    continue;

                // Prefer the slot the backpack was equipped in when the player died
                int index = entry.slot();
                if(BackpackHelper.getBackpackStack(player, index).isEmpty() && BackpackHelper.setBackpackStack(player, stack.copy(), index))
                    continue;

                // Otherwise settle for any free slot, falling back to the inventory or the ground
                if(BackpackHelper.equipBackpack(player, stack))
                    continue;

                remaining.add(stack);
            }
            this.dropStacks(player, remaining);
        }

        private List<StackWithSlot> readBackpacks(CompoundTag tag, DynamicOps<Tag> ops)
        {
            List<StackWithSlot> backpacks = new ArrayList<>();
            ListTag list = tag.getList(KEY, Tag.TAG_COMPOUND);
            for(Tag element : list)
            {
                StackWithSlot.CODEC.decode(ops, element).resultOrPartial(Constants.LOG::error).ifPresent(pair -> backpacks.add(pair.getFirst()));
            }
            return backpacks;
        }
    }
}
