(defproject lein-files-hash "0.1.1"
  :description "A Leiningen plugin to save hash trees of files and directories to other files."
  :url "https://github.com/otto-ec/nav_lein-files-hash"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :eval-in-leiningen true

  :dependencies [[org.clojure/clojure "1.10.0"]]

  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]]}})
