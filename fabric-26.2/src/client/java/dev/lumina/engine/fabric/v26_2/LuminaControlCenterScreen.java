package dev.lumina.engine.fabric.v26_2;

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
import dev.lumina.engine.common.iris.IrisStatus;
import dev.lumina.engine.common.telemetry.FrameTimeSnapshot;
import dev.lumina.engine.common.adaptive.RecommendationResult;
import dev.lumina.engine.common.adaptive.ActionableRecommendation;
import dev.lumina.engine.common.adaptive.QualityAdjustmentPlan;
import dev.lumina.engine.common.adaptive.PlannedAdjustment;
import java.io.IOException;
import java.time.Instant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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
        return create(parent, session, session.profile(), session.targetFps());
    }

    private static Screen create(
        Screen parent,
        ControlCenterSession session,
        QualityProfile pendingProfile,
        int pendingTargetFps
    ) {
        FabricPlatformAdapter platform = new FabricPlatformAdapter();
        IrisIntegration iris = new IrisIntegration(platform);
        IrisStatus irisStatus = iris.status();

        Option<QualityProfile> profile = Option.<QualityProfile>createBuilder()
            .name(Component.translatable("lumina_engine.option.profile"))
            .description(OptionDescription.of(Component.translatable("lumina_engine.option.profile.description")))
            .binding(QualityProfile.BALANCED, session::profile, session::setProfile)
            .controller(option -> EnumControllerBuilder.create(option)
                .enumClass(QualityProfile.class)
                .formatValue(value -> Component.translatable(profileTranslationKey(value))))
            .build();
        profile.requestSet(pendingProfile);

        Option<Integer> targetFps = Option.<Integer>createBuilder()
            .name(Component.translatable("lumina_engine.option.target_fps"))
            .description(OptionDescription.of(Component.translatable("lumina_engine.option.target_fps.description")))
            .binding(LuminaConfig.DEFAULT_TARGET_FPS, session::targetFps, session::setTargetFps)
            .controller(option -> IntegerSliderControllerBuilder.create(option)
                .range(LuminaConfig.MIN_TARGET_FPS, LuminaConfig.MAX_TARGET_FPS)
                .step(1)
                .formatValue(value -> Component.translatable("lumina_engine.option.target_fps.value", value)))
            .build();
        targetFps.requestSet(pendingTargetFps);

        Option<Boolean> adaptive = Option.<Boolean>createBuilder()
            .name(Component.translatable("lumina_engine.option.adaptive"))
            .description(OptionDescription.of(Component.translatable("lumina_engine.option.adaptive.description")))
            .binding(false, session::adaptiveOptimizationEnabled, ignored -> {})
            .controller(BooleanControllerBuilder::create)
            .available(false)
            .build();

        ButtonOption restoreDefaults = ButtonOption.createBuilder()
            .name(Component.translatable("lumina_engine.option.restore_defaults"))
            .description(OptionDescription.of(Component.translatable("lumina_engine.option.restore_defaults.description")))
            .text(Component.translatable("lumina_engine.action.restore"))
            .action((screen, option) -> {
                profile.requestSetDefault();
                targetFps.requestSetDefault();
                adaptive.requestSetDefault();
            })
            .build();

        ConfigCategory.Builder diagnostics = ConfigCategory.createBuilder()
            .name(Component.translatable("lumina_engine.category.diagnostics"));
        ButtonOption refreshDiagnostics = ButtonOption.createBuilder()
            .name(Component.translatable("lumina_engine.diagnostics.refresh"))
            .description(OptionDescription.of(Component.translatable("lumina_engine.diagnostics.refresh.description")))
            .text(Component.translatable("lumina_engine.action.refresh"))
            .action((screen, option) -> Minecraft.getInstance().setScreenAndShow(create(
                parent,
                session,
                profile.pendingValue(),
                targetFps.pendingValue()
            )))
            .build();
        FrameTimeSnapshot metrics = FrameTelemetryRuntime.latest();
        RecommendationResult recommendation = FrameTelemetryRuntime.recommendation();
        QualityAdjustmentPlan plan = FrameTelemetryRuntime.plan();
        diagnostics
            .option(refreshDiagnostics)
            .option(LabelOption.create(Component.translatable("lumina_engine.recommendation", recommendationText(recommendation))))
            .option(LabelOption.create(Component.translatable("lumina_engine.recommendation.reason", reasonText(recommendation))))
            .option(LabelOption.create(planText(plan)))
            .option(LabelOption.create(telemetryStatusText(metrics)))
            .option(LabelOption.create(Component.translatable("lumina_engine.telemetry.average_fps", format(metrics.averageFps()))))
            .option(LabelOption.create(Component.translatable("lumina_engine.telemetry.stable_minimum_fps", format(metrics.stableMinimumFps()))))
            .option(LabelOption.create(Component.translatable("lumina_engine.telemetry.one_percent_low", format(metrics.onePercentLowFps()))))
            .option(LabelOption.create(Component.translatable("lumina_engine.telemetry.frame_time", format(metrics.averageFrameTimeMillis()), format(metrics.p95FrameTimeMillis()))));
        for (PlannedAdjustment adjustment : plan.adjustments()) {
            diagnostics.option(LabelOption.create(adjustmentText(adjustment)));
        }
        FrameTelemetryRuntime.actionableRecommendation().ifPresent(actionable -> {
            diagnostics
                .option(LabelOption.create(Component.translatable("lumina_engine.actionable.title")))
                .option(LabelOption.create(Component.translatable("lumina_engine.actionable.profile",
                    Component.translatable(profileTranslationKey(actionable.suggestedProfile())))))
                .option(LabelOption.create(Component.translatable("lumina_engine.actionable.generated",
                    Instant.ofEpochMilli(actionable.generatedAtEpochMillis()).toString())))
                .option(LabelOption.create(Component.translatable("lumina_engine.actionable.cooldown",
                    Math.ceil(actionable.cooldownRemainingNanos(System.nanoTime()) / 1_000_000_000.0))));
            for (PlannedAdjustment adjustment : actionable.plan().adjustments()) {
                diagnostics.option(LabelOption.create(adjustmentText(adjustment)));
            }
            diagnostics.option(ButtonOption.createBuilder()
                .name(Component.translatable("lumina_engine.actionable.dismiss"))
                .text(Component.translatable("lumina_engine.action.dismiss"))
                .action((screen, option) -> {
                    FrameTelemetryRuntime.dismissActionableRecommendation();
                    Minecraft.getInstance().setScreenAndShow(create(parent, session,
                        profile.pendingValue(), targetFps.pendingValue()));
                })
                .build());
        });
        for (ModStatus status : session.diagnostics().mods()) {
            if (!"iris".equals(status.id())) {
                diagnostics.option(LabelOption.create(diagnosticText(status)));
            }
        }
        diagnostics
            .option(LabelOption.create(irisInstalledText(irisStatus)))
            .option(LabelOption.create(Component.translatable(
                "lumina_engine.iris.shaders_enabled",
                booleanText(irisStatus.shadersEnabled())
            )))
            .option(LabelOption.create(Component.translatable(
                "lumina_engine.iris.shader_active",
                booleanText(irisStatus.shaderActive())
            )))
            .option(ButtonOption.createBuilder()
                .name(Component.translatable("lumina_engine.iris.open"))
                .description(OptionDescription.of(Component.translatable("lumina_engine.iris.open.description")))
                .text(Component.translatable("lumina_engine.action.open_iris"))
                .available(irisStatus.installed())
                .action((screen, option) -> iris.openShaderScreen(screen))
                .build());

        ControlCenterSession finalSession = session;
        return YetAnotherConfigLib.createBuilder()
            .title(Component.translatable("lumina_engine.control_center.title"))
            .category(ConfigCategory.createBuilder()
                .name(Component.translatable("lumina_engine.category.configuration"))
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
            FrameTelemetryRuntime.setTargetFps(session.targetFps());
            FrameTelemetryRuntime.setProfile(session.profile());
        } catch (IOException exception) {
            LOGGER.error("Could not save Lumina Engine configuration", exception);
        }
    }

    private static Component telemetryStatusText(FrameTimeSnapshot snapshot) {
        return Component.translatable("lumina_engine.telemetry.status." + snapshot.targetStatus().name().toLowerCase(java.util.Locale.ROOT), snapshot.targetFps());
    }

    private static String format(double value) { return String.format(java.util.Locale.ROOT, "%.1f", value); }

    private static Component recommendationText(RecommendationResult result) {
        return Component.translatable("lumina_engine.recommendation." + result.recommendation().name().toLowerCase(java.util.Locale.ROOT));
    }

    private static Component reasonText(RecommendationResult result) {
        return Component.translatable("lumina_engine.recommendation.reason." + result.reason().name().toLowerCase(java.util.Locale.ROOT));
    }

    private static Component planText(QualityAdjustmentPlan plan) {
        return plan.changesQuality()
            ? Component.translatable("lumina_engine.plan.transition", Component.translatable(profileTranslationKey(plan.currentProfile())), Component.translatable(profileTranslationKey(plan.suggestedProfile())))
            : Component.translatable("lumina_engine.plan.no_changes");
    }

    private static Component adjustmentText(PlannedAdjustment adjustment) {
        return Component.translatable("lumina_engine.plan.adjustment",
            Component.translatable("lumina_engine.plan.domain." + adjustment.domain().name().toLowerCase(java.util.Locale.ROOT)),
            Component.translatable("lumina_engine.plan.direction." + adjustment.direction().name().toLowerCase(java.util.Locale.ROOT)));
    }

    private static Component diagnosticText(ModStatus status) {
        return status.installed()
            ? Component.translatable("lumina_engine.diagnostic.installed", status.displayName(), status.version())
            : Component.translatable("lumina_engine.diagnostic.missing", status.displayName());
    }

    private static Component irisInstalledText(IrisStatus status) {
        return status.installed()
            ? Component.translatable("lumina_engine.iris.installed", status.version())
            : Component.translatable("lumina_engine.iris.missing");
    }

    private static Component booleanText(boolean value) {
        return Component.translatable(value ? "lumina_engine.value.yes" : "lumina_engine.value.no");
    }

    private static String profileTranslationKey(QualityProfile profile) {
        return "lumina_engine.profile." + profile.name().toLowerCase(java.util.Locale.ROOT);
    }
}
