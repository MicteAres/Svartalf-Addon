package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockPlaceEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.HashSet;
import java.util.Set;

public class ModuleExample extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgRender = this.settings.createGroup("Render");

    private final Setting<Boolean> notify = sgGeneral.add(new BoolSetting.Builder()
        .name("notify-chat")
        .description("Şart sağlandığında chat'e mesaj yazsın mı?")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("highlight-color")
        .description("Bulunan sütunun rengini ayarlar.")
        .defaultValue(new SettingColor(0, 255, 200, 150))
        .build()
    );

    // Glow yapılacak blokların pozisyonlarını tutacağız
    private final Set<BlockPos> highlightedBlocks = new HashSet<>();

    public ModuleExample() {
        super(AddonTemplate.CATEGORY, "cobble-tower-checker", "5+ cobble/deepslate sütunlarını işaretler.");
    }

    private boolean isCobble(BlockState state) {
        return state.isOf(Blocks.COBBLESTONE) || state.isOf(Blocks.COBBLED_DEEPSLATE);
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        if (mc.world == null) return;

        BlockPos placedPos = event.blockPos;

        if (!isCobble(mc.world.getBlockState(placedPos))) return;

        int count = 1;

        BlockPos up = placedPos.up();
        while (isCobble(mc.world.getBlockState(up))) {
            count++;
            up = up.up();
        }

        BlockPos down = placedPos.down();
        while (isCobble(mc.world.getBlockState(down))) {
            count++;
            down = down.down();
        }

        if (count >= 5) {
            boolean surrounded = true;
            BlockPos check = placedPos;
            Set<BlockPos> columnBlocks = new HashSet<>();

            for (int i = 0; i < count; i++) {
                columnBlocks.add(check);

                BlockPos[] neighbors = {
                    check.north(),
                    check.south(),
                    check.east(),
                    check.west()
                };

                for (BlockPos neighbor : neighbors) {
                    if (mc.world.getBlockState(neighbor).isAir()) {
                        surrounded = false;
                        break;
                    }
                }

                if (!surrounded) break;
                check = check.down();
            }

            if (surrounded) {
                highlightedBlocks.clear();
                highlightedBlocks.addAll(columnBlocks);

                if (notify.get()) {
                    info("5+ cobblestone/deepslate sütunu bulundu ve işaretlendi!");
                }
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null) return;

        for (BlockPos pos : highlightedBlocks) {
            Box box = new Box(pos);
            event.renderer.box(box, color.get(), color.get(), ShapeMode.Lines, 0);
        }
    }
}
