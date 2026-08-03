# Publishing Notes

## GitHub Release

Title:

```text
Oritong's Diamond Core 1.0.9
```

Tag:

```text
v1.0.9
```

Release notes:

```markdown
## Oritong's Diamond Core 1.0.9

### Changes

- Updated BetterGTAE's large molecular assembler to batch queued pattern outputs without a parallel hatch, using a fixed `2147483647` run cap.
- Improved encoded AE2 smithing table and stonecutting pattern output handling for the large molecular assembler.
- Keeps the Industrial Foregoing subtype normalization, JEI startup fix, and BetterGTAE encoded pattern hatch support from 1.0.6/1.0.8.
- Includes the 1.0.5 Botania Pure Daisy EMI fluid-output fix and previous compatibility fixes.

### Compatibility

- Minecraft 1.20.1
- Forge 47+
- Java 17
```

Attach this file:

```text
build/libs/oritongsdiamondcore-1.0.9.jar
```

## Mod Hosting Page

Name:

```text
Oritong's Diamond Core
```

Summary:

```text
Compatibility and quality-of-life fixes for Oritong's Diamond modpack.
```

Description:

```markdown
Oritong's Diamond Core is a Forge 1.20.1 core compatibility mod for Oritong's Diamond modpack.

It focuses on small cross-mod fixes:

- AE2 and ExtendedAE amount limit adjustments
- GTCEu parallel hatch defaults
- Pipez energy routing compatibility with GTCEu and GTMThings
- EMI compatibility for Tinkers' Construct crafting stations
- EMI compatibility for Botania Pure Daisy fluid outputs
- ME Requester input length adjustment
- GTM QOL data stick crash guard
- Industrial Foregoing + JEI recipe lookup normalization
- BetterGTAE + AE2 crafting pattern hatch and large molecular assembler support

This mod is intended for a specific modpack-style environment. It uses mixins into several mods, so it should be tested together with the matching mod versions before being used in another pack.
```

Game versions:

```text
1.20.1
```

Loader:

```text
Forge
```

Required:

```text
Forge 47+
Minecraft 1.20.1
Java 17
Applied Energistics 2
JEI
Industrial Foregoing
BetterGTAE
```

Recommended environment:

```text
AE2, ExtendedAE, GTCEu, Pipez, GTMThings, EMI, JEI, Tinkers' Construct, Mantle, Botania, ME Requester, GTM QOL, Industrial Foregoing, BetterGTAE
```

License:

```text
GNU General Public License v3.0
```
