# Documentation Audit (RIO Android - Aug 2025)

This audit flags inconsistencies and provides concrete fixes to align documentation with the current codebase.

## Summary
- Overall documentation coverage is rich. However, some files describe a fully productionized, Hilt-enabled, Navigation-XML app, while the code currently runs a Compose-based shell with Hilt disabled and simplified Room schema. This audit proposes precise edits and deprecations.

## Key Inconsistencies

1) UI Framework & Navigation
- Issue: docs/android-architecture-overview.md and -summary.md reference Android Views + ViewBinding + Navigation Component XML, while app uses Jetpack Compose and Navigation Compose.
- Fix: Update both docs to state "UI: Jetpack Compose (Material3); Navigation: Navigation Compose" and remove references to XML nav graphs and ViewBinding.

2) Dependency Injection (Hilt)
- Issue: Several docs imply Hilt is enabled end-to-end (@HiltAndroidApp, DI modules). In code, app/build.gradle.kts has Hilt plugin commented out, and RIOApplication removes @HiltAndroidApp.
- Fix: Add a note in architecture docs and onboarding that Hilt is planned; show steps to re-enable. Update any guarantees of "Hilt everywhere" to "manual providers until Hilt is re-enabled".

3) Module Enablement
- Issue: Docs often list modules (core:network, core:payment, core:notifications, core:sync, features:*) as active. settings.gradle.kts shows many disabled.
- Fix: Clarify which modules are enabled now vs. planned. Add a module matrix in docs/technical-blueprint.md. Remove "100% working" claims for disabled modules.

4) Production-Ready Status Claims
- Issue: PRODUCTION_READY_STATUS.md claims 100% readiness across all features, which conflicts with disabled modules and .disabled files in repositories.
- Fix: Add a disclaimer or revise status to "Core authentication, Room cache, basic repositories are functional; advanced modules will be enabled progressively." Alternatively, split into "Phase status" with checkboxes.

5) BaseViewModel + DI
- Issue: BaseViewModel uses @Inject fields (UserRepository, ErrorHandler). With Hilt disabled, using BaseViewModel directly will fail injection.
- Fix: Document that ViewModels should accept dependencies via constructor until Hilt is re-enabled, or re-enable Hilt.

6) Navigation File References
- Issue: ONBOARDING.md points to app/.../navigation/RIONavigation.kt as active. The file is suffixed .disabled.
- Fix: Update paths to mention RIONavigation.kt.disabled and guidance to enable.

7) Tech Versions
- Issue: Some docs list older library versions (Kotlin 1.9.x, older Compose/Firebase) while gradle/libs.versions.toml has Kotlin 2.0.21, compose BOM 2024.09.00, Firebase BOM 33.7.0.
- Fix: Synchronize stated versions with version catalog.

## Proposed Edits

- Update docs/android-architecture-overview.md and docs/android-architecture-summary.md to reflect Compose + Nav Compose, current versions, and module status.
- Add docs/technical-blueprint.md (added) and docs/application-flow.md (added) as the authoritative sources.
- Amend docs/ONBOARDING.md sections 37-41 and 112-119 to remove assumptions about enabled Hilt and active navigation; link to developer-onboarding-guide.md.
- Either revise PRODUCTION_READY_STATUS.md language, or add a banner at top indicating progressive rollout with currently enabled modules.

## Obsolete/High-Risk Statements to Remove or Reword
- "100% Working" across features (messaging, notifications, payments) — reword to roadmapped/partially implemented.
- "Navigation Component with deep linking" (XML) — replace with Navigation Compose plan.

## Next Actions
- Approve the above changes.
- I will update the affected docs via PR with tracked diffs and cross-links to new documents.

