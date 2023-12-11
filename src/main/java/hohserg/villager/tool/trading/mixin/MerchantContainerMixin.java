package hohserg.villager.tool.trading.mixin;

import static java.lang.Math.max;

import java.util.Map;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MerchantContainer.class)
public class MerchantContainerMixin {

    @Inject(method = "updateSellItem",
            at = @At(value = "INVOKE",
                    shift = Shift.AFTER,
                    target = "Lnet/minecraft/world/item/trading/MerchantOffer;getXp()I"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void transferEnchantments(CallbackInfo ci, ItemStack input1, ItemStack input2, MerchantOffers merchantoffers, MerchantOffer merchantoffer) {
        MerchantContainer self = (MerchantContainer) ((Object) this);
        var result = merchantoffer.assemble();
        if (result.isDamageableItem()) {
            var damage = input1.getDamageValue() + input2.getDamageValue();
            result.setDamageValue(damage);

            Map<Enchantment, Integer> firstEnchants = result.getAllEnchantments();
            Map<Enchantment, Integer> secondEnchants = input1.getAllEnchantments();
            Map<Enchantment, Integer> thirdEnchants = input2.getAllEnchantments();

            addEnchants(firstEnchants, secondEnchants);
            addEnchants(firstEnchants, thirdEnchants);

            ListTag enchNbt = new ListTag();
            result.getOrCreateTag().put("Enchantments", enchNbt);
            firstEnchants.forEach((e, lvl) -> enchNbt.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(e), lvl.byteValue())));

            self.setItem(2, result);
        }
    }

    private void addEnchants(Map<Enchantment, Integer> firstEnchants, Map<Enchantment, Integer> secondEnchants) {
        if (firstEnchants.isEmpty()) {
            firstEnchants.putAll(secondEnchants);

        } else {
            secondEnchants.forEach((e, lvl) -> {
                if (firstEnchants.containsKey(e)) {
                    firstEnchants.put(e, max(lvl, firstEnchants.get(e)));

                } else if (firstEnchants.keySet().stream().allMatch(e::isCompatibleWith)) {
                    firstEnchants.put(e, lvl);
                }
            });
        }
    }
}
