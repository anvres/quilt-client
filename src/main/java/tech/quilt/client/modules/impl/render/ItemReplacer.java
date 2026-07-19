package tech.quilt.client.modules.impl.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.item.SwordItem;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.BooleanSetting;
import tech.quilt.client.modules.api.setting.impl.ModeSetting;

@ModuleAnnotation(
    name = "ItemReplacer",
    category = Category.RENDER,
    description = "Заменяет модели мечей на кастомные"
)
public final class ItemReplacer extends Module {
    private static final String[] WEAPONS = {
        "Abominable Blade", "Abominable Great Saber", "Abominable Scythe", "Acidic Cleaver",
        "Amethyst Shuriken", "Ancient Royal Great Sword", "Aquatic Sacred Blade", "Arcanethyst",
        "Ashura's Blade", "Awakened Lichblade", "Blood Edge", "Bloody Death", "Bramblethorn",
        "Brimstone Claymore", "Carian Knight's Sword", "Chrono Blade", "Corrupted Mythic Blade",
        "Creation Splitter", "Crescent Rose", "Cyber Katana", "Cyber Mantis Blade", "Cyber Sword",
        "Cybernetic Chainsaw Blade", "Cybernetic Katana", "Cybernetic Knife", "Dainsleif",
        "Dark Blade", "Dark Cleaver", "Death Knight's Dagger", "Death Knight's Sword",
        "Demigod's Unholy Blade", "Demigod's Unholy Halberd", "Demon Lord's Great Axe",
        "Demon Lord's Sword", "Demonic Blade", "Demonic Cleaver", "Divine Axe Rhitta",
        "Divine Justice", "Divine Punisher", "Divine Reaper", "Dragon Slaying blade",
        "Edge Of The Astral Plane", "Emberblade", "Enigma", "Epic Sword", "Estoc",
        "Fallen God's Spear", "Fallen God's Sword", "Floral Longsword", "Floral Sabre",
        "Forest Guardian's Glaive", "Frost Axe", "Frost Blade", "Frost Scythe", "Hearthflame",
        "Hero Sword", "Holy Moonlight Sword", "Hornet's Needle", "Icewhisper", "Jade Halberd",
        "Katana", "Legendary Sword", "Longsword", "Magi Scythe", "Masamune", "Mjolnir",
        "Molten Blade", "Molten Sword", "Muramasa", "Mystical Spellblade", "Mythic Blade",
        "Ocean's Rage", "Partisan", "Pharaoh's Treasure", "Pheonix Grace", "Plague Longsword",
        "Power Fuse Hammer", "Power Fuse Sword", "Requiem of the Ninth Abyss", "Ribbon Cleaver",
        "Righteous Relic", "Rivers Of Blood", "Royal Chakram", "Royal Rapier", "Sabre",
        "Scissor Blade", "Sculk Cleaver", "Sculk Scythe", "Sculk Sword", "Sentinel's Will",
        "Silverine Blade", "Soul Claws", "Soul Collector", "Soul Devourer", "Soul Edge",
        "Soul Harvester", "Soul Stealer", "Soulrender", "Star's Edge", "Steel Sword",
        "Stop Sign", "Storm Bringer", "Storm's Edge", "Sunbreak", "Tengen's Blade",
        "Terra Blade", "Thousand Demon Daggers", "Thunder Bringer", "Thunderbrand",
        "True Excalibur", "Vampiric Needle", "Wakizashi", "Watcher Claymore",
        "Watching Warglaive", "Waxweaver", "Whisperwind", "Wickpiercer", "Wraith Scythe", "Yoru"
    };

    public static final ItemReplacer INSTANCE = new ItemReplacer();

    private final BooleanSetting selfOnly = new BooleanSetting("Только для себя", "Заменять модели только на своих предметах", true);
    private final ModeSetting weaponModel = new ModeSetting("Орудие", WEAPONS);

    private ItemReplacer() {}

    public static ItemReplacer getInstance() {
        return INSTANCE;
    }

    public boolean isSelfOnly() {
        return selfOnly.isEnabled();
    }

    public String getSelectedWeapon() {
        return weaponModel.get();
    }

    public boolean renderReplacement(ItemStack stack, ModelTransformationMode renderMode, MatrixStack matrices, net.minecraft.client.render.VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!isEnabled()) return false;
        if (!(stack.getItem() instanceof SwordItem)) return false;
        return true;
    }
}
