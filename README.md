# Oritong's Diamond Core

Oritong's Diamond Core is a Forge 1.20.1 compatibility core mod for Oritong's Diamond modpack.
It contains small, targeted mixins that patch cross-mod behavior, UI limits, and integration bugs.

## Features

- AE2: raises processing pattern amount limits for item stacks.
- ExtendedAE: expands Infinity Cell amount handling.
- GTCEu: makes parallel hatches initialize at their maximum parallel value and allows shared use.
- Pipez + GTCEu: improves round-robin energy distribution into GT energy containers.
- GTMThings: exposes the wireless energy interface as an FE receiver for Pipez compatibility.
- EMI + Tinkers' Construct: improves shift-left-click recipe filling in the TConstruct crafting station, especially recipes with reusable tools.
- EMI + Botania: fixes Pure Daisy EMI recipes whose outputs are fluids or fluid-backed block states.
- EMI + O123456789: keeps recipe-tree amount labels on EMI's original layout so fluid amounts remain readable.
- ME Requester: increases number field length.
- GTM QOL: guards data stick decoration against invalid recipe data.
- Industrial Foregoing + JEI: normalizes machine addon recipe lookup for NBT-initialized dissolution chamber outputs, which also helps EMI through the JEI bridge.
- BetterGTAE + AE2: lets the crafting pattern hatch accept, preview, and execute AE2 smithing table and stonecutting patterns, and lets the large molecular assembler batch queued pattern outputs without a parallel hatch.

## Requirements

- Minecraft 1.20.1
- Forge 47+
- Java 17

This mod is built for a heavily modded environment and expects the related target mods to be present.

## License

This project is licensed under the GNU General Public License v3.0.
