# Modding (v2)

## Goals

- Not supported (i.e. no backwards compatibility)
  - basic api, everything else is unsafe
- inject main class
- kotlin support
- mixins (with java)
- jar loading or folder loading with jcl
- build system (gradle?): easy debugging, executing and hot reloading
- soft dependency management
  - depend on other mods by package (with version)
  - depend on minosoft version (i.e. minosoft is a fake mod)
  - no libraries (aka bundle that with your jar)
- basic api
  - connect events (by regex, port, hostname, ip, …)
- load minosoft api for now from jitpack, then publish stable versions to maven central
- gui with list of mods
- loading procedure:
  - priority
  - load meta -> inject to classpath -> initialize main class
- main class template (with logging, assets, ...)
