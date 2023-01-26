# Changelog

## 0.3.5

- Upgrade clj-kondo version

- Make babashka compatible by using the [clj-kondo-bb](https://github.com/clj-kondo/clj-kondo-bb) library

- Discontinue the `carve` binary in favor of invocation with babashka.
  Instead you can now install carve with [bbin](https://github.com/babashka/bbin):

  ```
  bbin install io.github.borkdude/carve
  ```

- Implement [babashka.cli](https://github.com/babashka/cli) integration
