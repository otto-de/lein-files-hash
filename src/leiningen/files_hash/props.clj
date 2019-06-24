(ns leiningen.files-hash.props
  (:import [java.util Properties])
  (:require [clojure.java.io :as io]))

(defn load-props [filename]
  (if (.exists (io/file filename))
    (with-open [r (io/reader filename)]
      (into {}
            (doto (Properties.)
              (.load r))))
    {}))

(defn store-props [m filename & {:keys [comment append?]}]
  (let [props (Properties.)]
    (doseq [[k v] m]
      (.setProperty props k v))
    (with-open [w (io/writer filename :append append?)]
      (.store props w comment))))
