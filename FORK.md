# Backpacked fork

Fork of [MrCrayfish/Backpacked](https://github.com/MrCrayfish/Backpacked) maintained by the studio
for its events (currently Chillcraft 2). It adds a complete Spanish translation (`es_mx`) and an
integration with **Gravestones**: equipped backpacks are stored inside the gravestone on death
instead of being left on the ground.

License: **GNU LGPL v2.1**, same as the original. Since the modified jar is redistributed to
players, this repo is public to provide its source code.

---

## Branches

| Branch | Upstream base | Version | Status |
|---|---|---|---|
| `chillcraft/1.21.1` | `multiloader/1.21.1` (Backpacked 3.0.5) | Fabric 1.21.1 | **Current.** Built and tested in game on 2026-11-09 |
| `chillcraft/1.20.1` | `multiloader/1.20.X` (Backpacked 3.0.9) | Fabric 1.20.1 | Obsolete (the event moved to 1.21.1); kept for reference |

The `multiloader/*` branches are upstream copies and are never modified.

## Remotes and local folders

```
upstream → git@github.com:MrCrayfish/Backpacked.git   (original, read only)
origin   → git@github.com:<org>/backpacked.git        (this fork)
```

Locally the repo lives in two folders (a `git worktree`, one branch each):

```
D:\modpacks\backpacked\          ← chillcraft/1.20.1 (main repository, contains .git/)
D:\modpacks\backpacked-1.21.1\   ← chillcraft/1.21.1 (worktree)
```

If the folders are moved: `git worktree repair <worktree-path>` from the main folder.

## Changes from the original

1. **Complete translation** of `common/src/main/resources/assets/backpacked/lang/es_mx.json`
   (205 keys; identical between 1.20.1 and 1.21.1).
2. **Gravestones integration** (`fabric/src/main/java/com/mrcrayfish/backpacked/integration/GravestonesSupport.java`),
   registered in `Backpacked.java` (fabric) only if the `gravestones` mod is loaded. Gravestones
   hooks into `dropAllDeathLoot` (HEAD), before Backpacked drops the backpacks in
   `dropEquipment`.
3. **`fabric/build.gradle`:** Modrinth Maven repository and a workaround to consume the Gravestones
   jar, which is built with a newer Loom version (1.17 vs 1.7): its `MANIFEST.MF` is stripped and
   it is used as a compile-only dependency. Nothing from Gravestones ends up in the resulting jar.
4. **`gradle.properties`:** `gravestones_version` (must match the jar in the pack:
   `1.4.2+1.21+A` on 1.21.1, `1.4.2+1.20+A` on 1.20.1) and, on 1.21.1, `packagesRepo` (MrCrayfish's
   GitHub Packages URL, which that branch does not define).

## Building (1.21.1)

From `D:\modpacks\backpacked-1.21.1\`, in PowerShell:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"; $env:TARGET_LOADER = "fabric"
.\gradlew :fabric:build
```

- Requires JDK 21 and a *classic* GitHub token with `read:packages` in
  `~/.gradle/gradle.properties` (`gpr.user` / `gpr.key`). Without the token, Framework cannot be
  downloaded (401). **The token never goes in the repo.**
- The first build takes ~7 minutes.
- The jar is written to `fabric/build/libs/`. It is renamed with the variant suffix:
  `backpacked-fabric-1.21.1-3.0.5-gravestones-integration.jar`, and published as a fork release.

**Runtime dependency:** `framework >= 0.13.10` for Fabric 1.21.1 (CurseForge only). Tested with
Framework `0.13.11`.

**In-game test** (2026-11-09): on death, the equipped backpack stays in the gravestone and is
re-equipped when collected.

## Pulling from upstream

```bash
git fetch upstream
git switch chillcraft/1.21.1
git merge upstream/multiloader/1.21.1     # merge, not rebase: the branch is already published
```

To port to another Minecraft version: create `chillcraft/<version>` from
`upstream/multiloader/<version>` and apply our own commits with `git cherry-pick`.
