# Changelog

## 1.0.10

- Fixed O123456789 compatibility with EMI's recipe tree from the core mod by rendering recipe-tree amount labels with EMI's original layout before O123456789 can replace them.

## 1.0.9

- Updated BetterGTAE's large molecular assembler to consume its queued pattern outputs with a fixed `2147483647` run cap instead of relying on a parallel hatch.
- Removed the large molecular assembler parallel hatch recipe modifier and structure slot acceptance.
- Improved large molecular assembler output creation so AE2 smithing table and stonecutting pattern outputs keep their encoded item data.

## 1.0.8

- Fixed BetterGTAE crafting pattern hatch execution so encoded patterns register real output multipliers instead of zero-count output recipes.

## 1.0.7

- Fixed the Industrial Foregoing JEI subtype mixin crashing during JEI startup by splitting instance and static redirect handlers.

## 1.0.6

- Added Industrial Foregoing + JEI subtype normalization for NBT-initialized dissolution chamber recipe outputs.
- Added BetterGTAE + AE2 crafting pattern hatch support for encoded smithing table and stonecutting patterns.

## 1.0.5

- Added the mod logo to Forge metadata.
- Fixed Botania Pure Daisy EMI recipes when the output is a fluid block or fluid-backed block state.
- Fixed the first attempt at the Pure Daisy EMI patch so Botania's EMI plugin can load normally.
- Kept the 1.0.4 compatibility fixes for Pipez, GTMThings, TConstruct crafting station recipe filling, and GTMQOL data sticks.

## 1.0.4

- Removed the AE2 pattern encoding terminal quick multiplier feature.
- Added compatibility for TConstruct crafting station EMI shift-left-click recipe filling.
- Improved Pipez round-robin energy insertion into GTCEu energy inputs.
- Added Pipez compatibility for `gtmthings:wireless_energy_interface`.
- Fixed GTMQOL data stick rendering crashes caused by missing or invalid recipe data.
- Updated parallel hatch initialization so newly placed hatches start at their maximum parallel value.
