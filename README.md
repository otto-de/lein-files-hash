# lein-files-hash

A Leiningen plugin to save hash trees of files and directories to other files.

## Usage

Put `[lein-files-hash "0.1.0"]` into the `:plugins` vector of your project.clj.

Configure what should be hashed and saved where:

    :files-hash {"some-place/relevant-foo-files.hash" ["/src/foo/bar"
                                                       "/src/foo/quux"]}

This will then on invocation create a SHA-256 Merkle hash tree of all files and
their names under the given paths and save the resulting hash in the file given
as key.  You can have multiple such keys and corresponding pathnames, and you
can also refer to single file names in the pathnames vector.

Call from shell:

    $ lein files-hash

You might want to do this in your project's build aliases:

    :aliases {"uberjar" ["do" ["clean"] ["files-hash"] "uberjar"]}

## License

Copyright © 2019 OTTO GmbH & Co. KG

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
