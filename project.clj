(defproject de.otto/lein-files-hash "0.2.6"
  :description "A Leiningen plugin to save hash trees of files and directories to other files."
  :url "https://github.com/otto-de/lein-files-hash"
  :license {:name "Apache License 2.0"
            :url  "http://www.apache.org/license/LICENSE-2.0.html"}
  :min-lein-version "2.9.1"
  :eval-in-leiningen true

  :dependencies [[org.clojure/clojure "1.10.1"]]

  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]]}})
