# Tesseract MI bridge — Minecraft 26.1.2

Round 2, 2026-11-06. Branch `port/26.1.2-mi`.
Base: upstream 26.1.2 `2a38561ba8278911a8ca4f16af8db8a072748f8b`.
Bridge source: upstream 1.21.1 `6a9c981a6e6dc35ea58db3b4c8a919d893dcf744`.
MIT LICENSE retained. MI sources are read-only.

## Scope

Restored all 139 compat/mi Java sources, datagen/mi, MICommonCapabitilies,
MI fluid helpers, generated language keys and the enabled MI mixin config.
The published bridge is the Overdrive subset: material authoring, MIFluidHolder,
TexturesMIHookDatagenProvider and the obsolete FakedMachineModelBuilder remain
in source but are explicitly excluded from compilation. Overdrive does not use
them; the machine model generator replaces the old FakedMachineModelBuilder.
No machine core or Overdrive-required type is stubbed or removed.
The excluded authoring APIs are not claimed to work on 26.1.2.

## Migration

NeoGradle userdev 7.1.38 / Gradle 9.2.1 / Java 25 / NeoForge 26.1.2.126.
Version is read directly from `mod_version`; no Minecraft suffix is appended.
Target coordinates: `net.swedz:tesseract-api-neoforge:1.13.7-26.1.2-fleet.1`.
MI: `aztech:Modern-Industrialization:2.6.0-26.1.2-fleet01`,
loader range `[2.6.0-,2.7-)`. EMI-only, no JEI/REI dependency.
The generic Tesseract 1.0.2 API is embedded with JarJar.

Identifier, InteractionResult, ValueInput/ValueOutput, native item/fluid
resources and transaction API replace their old counterparts. MI energy uses
native EnergyHandler and ItemAccess, with a component-backed SimpleEnergyItem
contract and MI ItemEnergyHandler preserving long stored amounts/capacity.
Native energy moves are atomic; each move is capped at Integer.MAX_VALUE,
as required by the new transfer API. Recipe helpers use MI itemIn/itemOut and
fluidIn/fluidOut. Casing and machine state/item models use the 26.1 codecs.
The MI mixin is enabled in neoforge.mods.toml, with Java 25 compatibility;
its readNbt injection was migrated to ValueInput. Runtime mixin application
and gameplay have not yet been tested.

AT is explicitly wired to NeoGradle; interface-injected tree-growth calls
use explicit casts to the interfaces implemented by their existing mixins.

## Verification

All Gradle commands run serially with JAVA_HOME Java 25. Before heavy commands,
pgrep/free are recorded. Existing foreign daemons were idle; none were killed.
No opcScript is present. Full logs: `.port-tools/logs/` (ignored by Git),
with `.exitcode` and `.errors.txt` companions. Dependency resolution failure is
never considered success just because grep has no error lines.

- compile-01: FAIL (initial restored bridge).
- compile-02: FAIL, 282 javac errors.
- compile-03: FAIL, 46 javac errors; cold AT Minecraft recompile completed.
- compile-04: PASS, exit 0, warnings only.
- build/publish results are recorded below after completion.

- build.log: PASS, exit 0.
- build-final.log: PASS, exit 0, self-contained primary artifact (JarJar).
- publish.log: PASS, exit 0. Published primary jar/module/POM/sources verified in MavenLocal; API/runtime module variants point to the self-contained jar. All 20 classes in Overdrive missing-mi-bridge.txt are present.

## Integration follow-up

Overdrive client datagen loads the bridge and MI hooks without mixin failure,
and native casing/machine/item codecs generate successfully. Recipe datagen
exposed the need to pass the datagen HolderLookup.Provider to MIRecipeJson.
Added an explicit provider constructor; consumers must use it for tag inputs.
- build-integration.log: PASS, exit 0 after adding the datagen registry constructor.
- publish-integration.log: PASS, exit 0; same required fleet coordinates refreshed.

A further datagen check exposed unbound data-only component holders when
converting ItemStackTemplate to ItemStack. Shaped/shapeless recipe conversion
now retains ItemStackTemplate directly, including output component patches.
Recipe inversion accesses the template item without constructing a live stack.
- build-template.log and publish-template.log: PASS, exit 0 after retaining ItemStackTemplate.
