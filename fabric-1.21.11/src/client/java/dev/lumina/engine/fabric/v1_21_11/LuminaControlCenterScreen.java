package dev.lumina.engine.fabric.v1_21_11;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.lumina.engine.common.ConfigStore;
import dev.lumina.engine.common.ControlCenterSession;
import dev.lumina.engine.common.LuminaConfig;
import dev.lumina.engine.common.ModStatus;
import dev.lumina.engine.common.QualityProfile;
import java.io.IOException;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class LuminaControlCenterScreen {
    private static final Logger LOGGER = LoggerFactory.getLogger(LuminaEngineClient.MOD_ID);

    private LuminaControlCenterScreen() {}

    static Screen create(Screen parent) {
        FabricPlatformAdapter platform = new FabricPlatformAdapter();
        ConfigStore store = new ConfigStore(platform.configDirectory().resolve(LuminaEngineClient.MOD_ID + ".json"));
        ControlCenterSession session;
        try {
            session = ControlCenterSession.open(store, platform);
        } catch (IOException exception) {
            LOGGER.error("Could not load configuration for the Lumina Control Center; showing safe defaults", exception);
            session = ControlCenterSession.withDefaults(store, platform);
        }

        Option<QualityProfile> profile = Option.<QualityProfile>createBuilder()
            .name(Text.translatable("lumina_engine.option.profile"))
            .description(OptionDescription.of(Text.translatable("lumina_engine.option.profile.description")))
            .binding(QualityProfile.BALANCED, session::profile, session::setProfile)
            .controller(option -> EnumControllerBuilder.create(option)
                .enumClass(QualityProfile.class)
                .formatValue(value -> Text.translatable(profileTranslationKey(value))))
            .build();

        Option<Integer> targetFps = Option.<Integer>createBuilder()
            .name(Text.translatable("lumina_engine.option.target_fps"))
            .description(OptionDescription.of(Text.translatable("lumina_engine.option.target_fps.description")))
            .binding(LuminaConfig.DEFAULT_TARGET_FPS, session::targetFps, session::setTargetFps)
            .controller(option -> IntegerSliderControllerBuilder.create(option)
                .range(LuminaConfig.MIN_TARGET_FPS, LuminaConfig.MAX_TARGET_FPS)
                .step(1)
                .formatValue(value -> Text.translatable("lumina_engine.option.target_fps.value", value)))
            .build();

        Option<Boolean> adaptive = Option.<Boolean>createBuilder()
            .name(Text.translatable("lumina_engine.option.adaptive"))
            .description(OptionDescription.of(Text.translatable("lumina_engine.option.adaptive.description")))
            .binding(false, session::adaptiveOptimizationEnabled, ignored -> {})
            .controller(BooleanControllerBuilder::create)
            .available(false)
            .build();

        ButtonOption restoreDefaults = ButtonOption.createBuilder()
            .name(Text.translatable("lumina_engine.option.restore_defaults"))
            .description(OptionDescription.of(Text.translatable("lumina_engine.option.restore_defaults.description")))
            .text(Text.translatable("lumina_engine.action.restore"))
            .action((screen, option) -> {
                profile.requestSetDefault();
                targetFps.requestSetDefault();
                adaptive.requestSetDefault();
            })
            .build();

        ConfigCategory.Builder diagnostics = ConfigCategory.createBuilder()
            .name(Text.translatable("lumina_engine.category.diagnostics"));
        for (ModStatus status : session.diagnostics().mods()) {
            diagnostics.option(LabelOption.create(diagnosticText(status)));
        }

        ControlCenterSession finalSession = session;
        return YetAnotherConfigLib.createBuilder()
            .title(Text.translatable("lumina_engine.control_center.title"))
            .category(ConfigCategory.createBuilder()
                .name(Text.translatable("lumina_engine.category.configuration"))
                .option(profile)
                .option(targetFps)
                .option(adaptive)
                .option(restoreDefaults)
                .build())
            .category(diagnostics.build())
            .save(() -> save(finalSession))
            .build()
            .generateScreen(parent);
    }

    private static void save(ControlCenterSession session) {
        try {
            session.save();
        } catch (IOException exception) {
            LOGGER.error("Could not save Lumina Engine configuration", exception);
        }
    }

    private static Text diagnosticText(ModStatus status) {
        return status.installed()
            ? Text.translatable("lumina_engine.diagnostic.installed", status.displayName(), status.version())
            : Text.translatable("lumina_engine.diagnostic.missing", status.displayName());
    }

    private static String profileTranslationKey(QualityProfile profile) {
        return "lumina_engine.profile." + profile.name().toLowerCase(java.util.Locale.ROOT);
    }
}
