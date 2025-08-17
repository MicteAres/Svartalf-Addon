package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.BlockPlaceEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class ModuleExample extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    // Settings (ekstra ayar istersen buraya ekleyebilirsin)
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    private final Setting<Boolean> notify = sgGeneral.add(new BoolSetting.Builder()
        .name("notify-chat")
        .description("Şart sağlandığında chat'e mesaj yazsın mı?")
        .defaultValue(true)
        .build()
    );

    public ModuleExample() {
        super(AddonTemplate.CATEGORY, "cobble-tower-checker", "5+ cobble/deepslate sütunlarını kontrol eder.");
    }

    private boolean isCobble(BlockState state) {
        return state.isOf(Blocks.COBBLESTONE) || state.isOf(Blocks.COBBLED_DEEPSLATE);
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        if (mc.world == null) return;

        BlockPos placedPos = event.blockPos;

        // Sadece cobble / deepslate ise bak
        if (!isCobble(mc.world.getBlockState(placedPos))) return;

        // Yukarı ve aşağı doğru zinciri say
        int count = 1;

        // Yukarı
        BlockPos up = placedPos.up();
        while (isCobble(mc.world.getBlockState(up))) {
            count++;
            up = up.up();
        }

        // Aşağı
        BlockPos down = placedPos.down();
        while (isCobble(mc.world.getBlockState(down))) {
            count++;
            down = down.down();
        }

        // Eğer 5 veya daha fazlaysa etraf kontrolü
        if (count >= 5) {
            boolean surrounded = true;
            BlockPos check = placedPos;

            // Sütunun tüm bloklarını tara
            for (int i = 0; i < count; i++) {
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

            // Şartlar sağlanıyorsa
            if (surrounded && notify.get()) {
                info("5+ cobblestone/deepslate sütunu bulundu (tamamen kapalı)!");
            }
        }
    }
}
