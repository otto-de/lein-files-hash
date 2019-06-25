# lein-files-hash

A Leiningen plugin to save hash trees of files and directories to properties
files.

## Usage

Put `[de.otto/lein-files-hash "0.2.3"]` into the `:plugins` vector of your project.clj.

Configure what should be hashed and saved where:

```
  :files-hash [{:properties-file "resources/versions.properties"
                :property-key "graph-hash"
                :paths ["src/de/otto/nav/graph"
                        "src/de/otto/nav/feed"]}]
```

This will then on invocation create a SHA-256 Merkle hash tree of all files and
their names under the given paths and save the resulting hash under the given
key in the given properties file.  As the format hints, you can have multiple
such configurations.  You can also refer to single file names in the paths
vector.

Call from shell:

    $ lein files-hash

You might want to do this in your project's build aliases:

    :aliases {"uberjar" ["do" ["clean"] ["files-hash"] "uberjar"]}

## License

Copyright © 2019 OTTO GmbH & Co. KG

This program and the accompanying materials are made available under the terms
of the Apache License 2.0, see https://www.apache.org/licenses/LICENSE-2.0.html
