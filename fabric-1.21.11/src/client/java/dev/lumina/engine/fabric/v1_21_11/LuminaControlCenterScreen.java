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
import dev.lumina.engine.common.HudPosition;
import dev.lumina.engine.common.iris.IrisStatus;
import dev.lumina.engine.common.telemetry.FrameTimeSnapshot;
import dev.lumina.engine.common.benchmark.BenchmarkResult;
import dev.lumina.engine.common.benchmark.BenchmarkSnapshot;
import dev.lumina.engine.common.benchmark.BenchmarkComparison;
import dev.lumina.engine.common.compat.CompatibilityAdvisor;
import dev.lumina.engine.common.compat.CompatibilityAssessment;
import dev.lumina.engine.common.adaptive.RecommendationResult;
import dev.lumina.engine.common.adaptive.ActionableRecommendation;
import dev.lumina.engine.common.adaptive.QualityAdjustmentPlan;
import dev.lumina.engine.common.adaptive.PlannedAdjustment;
import java.io.IOException;
import java.time.Instant;
import net.minecraft.client.MinecraftClient;
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
        CompatibilityAssessment compatibility = CompatibilityAdvisor.assess("1.21.11",
            modVersion(session, "iris"), modVersion(session, "sodium"));

        Option<QualityProfile> profile = Option.<QualityProfile>createBuilder()
            .name(Text.translatable("lumina_engine.option.profile"))
            .description(OptionDescription.of(Text.translatable("lumina_engine.option.profile.description")))
            .binding(QualityProfile.BALANCED, session::profile, session::setProfile)
            .controller(option -> EnumControllerBuilder.create(option)
                .enumClass(QualityProfile.class)
                .formatValue(value -> Text.translatable(profileTranslationKey(value))))
            .build();
        profile.requestSet(pendingProfile);

        Option<Integer> targetFps = Option.<Integer>createBuilder()
            .name(Text.translatable("lumina_engine.option.target_fps"))
            .description(OptionDescription.of(Text.translatable("lumina_engine.option.target_fps.description")))
            .binding(LuminaConfig.DEFAULT_TARGET_FPS, session::targetFps, session::setTargetFps)
            .controller(option -> IntegerSliderControllerBuilder.create(option)
                .range(LuminaConfig.MIN_TARGET_FPS, LuminaConfig.MAX_TARGET_FPS)
                .step(1)
                .formatValue(value -> Text.translatable("lumina_engine.option.target_fps.value", value)))
            .build();
        targetFps.requestSet(pendingTargetFps);

        Option<Boolean> adaptive = Option.<Boolean>createBuilder()
            .name(Text.translatable("lumina_engine.option.adaptive"))
            .description(OptionDescription.of(Text.translatable("lumina_engine.option.adaptive.description")))
            .binding(false, session::adaptiveOptimizationEnabled, ignored -> {})
            .controller(BooleanControllerBuilder::create)
            .available(false)
            .build();

        Option<Boolean> performanceHud = Option.<Boolean>createBuilder()
            .name(Text.translatable("lumina_engine.option.hud"))
            .description(OptionDescription.of(Text.translatable("lumina_engine.option.hud.description")))
            .binding(false, session::performanceHudEnabled, session::setPerformanceHudEnabled)
            .controller(BooleanControllerBuilder::create).build();
        Option<HudPosition> hudPosition = Option.<HudPosition>createBuilder()
            .name(Text.translatable("lumina_engine.option.hud_position"))
            .binding(HudPosition.TOP_LEFT, session::performanceHudPosition, session::setPerformanceHudPosition)
            .controller(option -> EnumControllerBuilder.create(option).enumClass(HudPosition.class)
                .formatValue(value -> Text.translatable("lumina_engine.hud.position." + value.name().toLowerCase(java.util.Locale.ROOT))))
            .build();

        ButtonOption restoreDefaults = ButtonOption.createBuilder()
            .name(Text.translatable("lumina_engine.option.restore_defaults"))
            .description(OptionDescription.of(Text.translatable("lumina_engine.option.restore_defaults.description")))
            .text(Text.translatable("lumina_engine.action.restore"))
            .action((screen, option) -> {
                profile.requestSetDefault();
                targetFps.requestSetDefault();
                adaptive.requestSetDefault();
                performanceHud.requestSetDefault();
                hudPosition.requestSetDefault();
            })
            .build();

        ConfigCategory.Builder diagnostics = ConfigCategory.createBuilder()
            .name(Text.translatable("lumina_engine.category.diagnostics"));
        ButtonOption refreshDiagnostics = ButtonOption.createBuilder()
            .name(Text.translatable("lumina_engine.diagnostics.refresh"))
            .description(OptionDescription.of(Text.translatable("lumina_engine.diagnostics.refresh.description")))
            .text(Text.translatable("lumina_engine.action.refresh"))
            .action((screen, option) -> MinecraftClient.getInstance().setScreen(create(
                parent,
                session,
                profile.pendingValue(),
                targetFps.pendingValue()
            )))
            .build();
        FrameTimeSnapshot metrics = FrameTelemetryRuntime.latest();
        BenchmarkSnapshot benchmark = FrameTelemetryRuntime.benchmark();
        RecommendationResult recommendation = FrameTelemetryRuntime.recommendation();
        QualityAdjustmentPlan plan = FrameTelemetryRuntime.plan();
        diagnostics
            .option(refreshDiagnostics)
            .option(LabelOption.create(Text.translatable("lumina_engine.benchmark.status",
                Text.translatable("lumina_engine.benchmark.phase." + benchmark.phase().name().toLowerCase(java.util.Locale.ROOT)),
                Math.round(benchmark.progress() * 100))))
            .option(ButtonOption.createBuilder()
                .name(Text.translatable("lumina_engine.benchmark.start"))
                .text(Text.translatable("lumina_engine.action.start"))
                .available(!benchmark.phase().equals(dev.lumina.engine.common.benchmark.BenchmarkPhase.WARMING_UP)
                    && !benchmark.phase().equals(dev.lumina.engine.common.benchmark.BenchmarkPhase.MEASURING))
                .action((screen, option) -> {
                    FrameTelemetryRuntime.startBenchmark();
                    MinecraftClient.getInstance().setScreen(create(parent, session, profile.pendingValue(), targetFps.pendingValue()));
                }).build())
            .option(ButtonOption.createBuilder()
                .name(Text.translatable("lumina_engine.benchmark.cancel"))
                .text(Text.translatable("lumina_engine.action.cancel"))
                .available(benchmark.phase().equals(dev.lumina.engine.common.benchmark.BenchmarkPhase.WARMING_UP)
                    || benchmark.phase().equals(dev.lumina.engine.common.benchmark.BenchmarkPhase.MEASURING))
                .action((screen, option) -> {
                    FrameTelemetryRuntime.cancelBenchmark();
                    MinecraftClient.getInstance().setScreen(create(parent, session, profile.pendingValue(), targetFps.pendingValue()));
                }).build())
            .option(LabelOption.create(Text.translatable("lumina_engine.recommendation", recommendationText(recommendation))))
            .option(LabelOption.create(Text.translatable("lumina_engine.recommendation.reason", reasonText(recommendation))))
            .option(LabelOption.create(planText(plan)))
            .option(LabelOption.create(telemetryStatusText(metrics)))
            .option(LabelOption.create(Text.translatable("lumina_engine.telemetry.average_fps", format(metrics.averageFps()))))
            .option(LabelOption.create(Text.translatable("lumina_engine.telemetry.stable_minimum_fps", format(metrics.stableMinimumFps()))))
            .option(LabelOption.create(Text.translatable("lumina_engine.telemetry.one_percent_low", format(metrics.onePercentLowFps()))))
            .option(LabelOption.create(Text.translatable("lumina_engine.telemetry.frame_time", format(metrics.averageFrameTimeMillis()), format(metrics.p95FrameTimeMillis()))));
        benchmark.result().ifPresent(result -> addBenchmarkResult(diagnostics, result));
        FrameTelemetryRuntime.benchmarkComparison().ifPresent(comparison -> addBenchmarkComparison(diagnostics, comparison));
        for (PlannedAdjustment adjustment : plan.adjustments()) {
            diagnostics.option(LabelOption.create(adjustmentText(adjustment)));
        }
        FrameTelemetryRuntime.actionableRecommendation().ifPresent(actionable -> {
            diagnostics
                .option(LabelOption.create(Text.translatable("lumina_engine.actionable.title")))
                .option(LabelOption.create(Text.translatable("lumina_engine.actionable.profile",
                    Text.translatable(profileTranslationKey(actionable.suggestedProfile())))))
                .option(LabelOption.create(Text.translatable("lumina_engine.actionable.generated",
                    Instant.ofEpochMilli(actionable.generatedAtEpochMillis()).toString())))
                .option(LabelOption.create(Text.translatable("lumina_engine.actionable.cooldown",
                    Math.ceil(actionable.cooldownRemainingNanos(System.nanoTime()) / 1_000_000_000.0))));
            for (PlannedAdjustment adjustment : actionable.plan().adjustments()) {
                diagnostics.option(LabelOption.create(adjustmentText(adjustment)));
            }
            diagnostics.option(ButtonOption.createBuilder()
                .name(Text.translatable("lumina_engine.actionable.dismiss"))
                .text(Text.translatable("lumina_engine.action.dismiss"))
                .action((screen, option) -> {
                    FrameTelemetryRuntime.dismissActionableRecommendation();
                    MinecraftClient.getInstance().setScreen(create(parent, session,
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
            .option(LabelOption.create(Text.translatable("lumina_engine.compatibility.status",
                Text.translatable("lumina_engine.compatibility." + compatibility.level().name().toLowerCase(java.util.Locale.ROOT)))))
            .option(LabelOption.create(Text.translatable("lumina_engine.compatibility.expected",
                compatibility.expectedIris(), compatibility.expectedSodium())))
            .option(LabelOption.create(irisInstalledText(irisStatus)))
            .option(LabelOption.create(Text.translatable(
                "lumina_engine.iris.shaders_enabled",
                booleanText(irisStatus.shadersEnabled())
            )))
            .option(LabelOption.create(Text.translatable(
                "lumina_engine.iris.shader_active",
                booleanText(irisStatus.shaderActive())
            )))
            .option(ButtonOption.createBuilder()
                .name(Text.translatable("lumina_engine.iris.open"))
                .description(OptionDescription.of(Text.translatable("lumina_engine.iris.open.description")))
                .text(Text.translatable("lumina_engine.action.open_iris"))
                .available(irisStatus.installed())
                .action((screen, option) -> iris.openShaderScreen(screen))
                .build());

        ControlCenterSession finalSession = session;
        return YetAnotherConfigLib.createBuilder()
            .title(Text.translatable("lumina_engine.control_center.title"))
            .category(ConfigCategory.createBuilder()
                .name(Text.translatable("lumina_engine.category.configuration"))
                .option(profile)
                .option(targetFps)
                .option(adaptive)
                .option(performanceHud)
                .option(hudPosition)
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
            FrameTelemetryRuntime.setHud(session.performanceHudEnabled(), session.performanceHudPosition());
        } catch (IOException exception) {
            LOGGER.error("Could not save Lumina Engine configuration", exception);
        }
    }

    private static Text telemetryStatusText(FrameTimeSnapshot snapshot) {
        return Text.translatable("lumina_engine.telemetry.status." + snapshot.targetStatus().name().toLowerCase(java.util.Locale.ROOT), snapshot.targetFps());
    }

    private static String format(double value) { return String.format(java.util.Locale.ROOT, "%.1f", value); }

    private static void addBenchmarkResult(ConfigCategory.Builder diagnostics, BenchmarkResult result) {
        diagnostics
            .option(LabelOption.create(Text.translatable("lumina_engine.benchmark.result.fps", format(result.averageFps()), format(result.stableMinimumFps()), format(result.onePercentLowFps()))))
            .option(LabelOption.create(Text.translatable("lumina_engine.benchmark.result.frame_time", format(result.averageFrameTimeMillis()), format(result.p95FrameTimeMillis()))))
            .option(LabelOption.create(Text.translatable("lumina_engine.benchmark.result.target", format(result.targetMetPercent()), result.targetFps())));
    }

    private static void addBenchmarkComparison(ConfigCategory.Builder diagnostics, BenchmarkComparison value) {
        diagnostics
            .option(LabelOption.create(Text.translatable("lumina_engine.comparison.title", value.baseline().label(), value.candidate().label())))
            .option(LabelOption.create(Text.translatable("lumina_engine.comparison.average", format(value.averageFpsDelta()), format(value.averageFpsPercent()))))
            .option(LabelOption.create(Text.translatable("lumina_engine.comparison.low", format(value.onePercentLowDelta()), format(value.onePercentLowPercent()))))
            .option(LabelOption.create(Text.translatable("lumina_engine.comparison.stability", format(value.p95FrameTimeDelta()), format(value.p95FrameTimePercent()))));
    }

    private static Text recommendationText(RecommendationResult result) {
        return Text.translatable("lumina_engine.recommendation." + result.recommendation().name().toLowerCase(java.util.Locale.ROOT));
    }

    private static Text reasonText(RecommendationResult result) {
        return Text.translatable("lumina_engine.recommendation.reason." + result.reason().name().toLowerCase(java.util.Locale.ROOT));
    }

    private static Text planText(QualityAdjustmentPlan plan) {
        return plan.changesQuality()
            ? Text.translatable("lumina_engine.plan.transition", Text.translatable(profileTranslationKey(plan.currentProfile())), Text.translatable(profileTranslationKey(plan.suggestedProfile())))
            : Text.translatable("lumina_engine.plan.no_changes");
    }

    private static Text adjustmentText(PlannedAdjustment adjustment) {
        return Text.translatable("lumina_engine.plan.adjustment",
            Text.translatable("lumina_engine.plan.domain." + adjustment.domain().name().toLowerCase(java.util.Locale.ROOT)),
            Text.translatable("lumina_engine.plan.direction." + adjustment.direction().name().toLowerCase(java.util.Locale.ROOT)));
    }

    private static Text diagnosticText(ModStatus status) {
        return status.installed()
            ? Text.translatable("lumina_engine.diagnostic.installed", status.displayName(), status.version())
            : Text.translatable("lumina_engine.diagnostic.missing", status.displayName());
    }

    private static String modVersion(ControlCenterSession session, String id) {
        return session.diagnostics().mods().stream().filter(mod -> id.equals(mod.id()))
            .filter(ModStatus::installed).map(ModStatus::version).findFirst().orElse(null);
    }

    private static Text irisInstalledText(IrisStatus status) {
        return status.installed()
            ? Text.translatable("lumina_engine.iris.installed", status.version())
            : Text.translatable("lumina_engine.iris.missing");
    }

    private static Text booleanText(boolean value) {
        return Text.translatable(value ? "lumina_engine.value.yes" : "lumina_engine.value.no");
    }

    private static String profileTranslationKey(QualityProfile profile) {
        return "lumina_engine.profile." + profile.name().toLowerCase(java.util.Locale.ROOT);
    }
}
