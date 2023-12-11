package hohserg.villager.tool.trading.mixin;

import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantOffer.class)
public class MerchantOfferMixin {

    @Inject(method = "isRequiredItem", at = @At("HEAD"), cancellable = true)
    public void isRequiredItem(ItemStack passed, ItemStack required, CallbackInfoReturnable<Boolean> ci) {
        if (required.isDamageableItem() && !required.isDamaged()) {
            if (passed.isDamaged()) {
                var itemStack = passed.copy();
                itemStack.setDamageValue(0);
                ci.setReturnValue(ItemStack.isSameItem(itemStack, required) &&
                        (!required.hasTag() || itemStack.hasTag() && NbtUtils.compareNbt(required.getTag(), itemStack.getTag(), false)));
            }
        }
    }
}
