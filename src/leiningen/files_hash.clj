(ns leiningen.files-hash
  (:import [java.io File]
           [java.security MessageDigest])
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as spec]
            [leiningen.core.main :as main]
            [leiningen.core.project :as project]
            [leiningen.files-hash.props :as props]
            [clojure.string :as str]))

(defn type-key [thing]
  (cond (string? thing) :string
        (bytes? thing) :bytes
        (vector? thing) :vector
        (instance? File thing) (if (.isDirectory thing)
                                 :directory
                                 :file)))

(defmulti sha256hash type-key)

(defmethod sha256hash :bytes [b]
  (-> (doto (MessageDigest/getInstance "SHA-256")
        (.update b))
      (.digest)))

(defmethod sha256hash :string [s]
  (sha256hash (.getBytes s)))

(defmethod sha256hash :vector [v]
  (sha256hash (byte-array (mapcat sha256hash v))))

(defmethod sha256hash :file [f]
  (sha256hash [(.getName f)
               (slurp f :encoding "UTF-8")]))

(defmethod sha256hash :directory [d]
  (sha256hash (into [(.getName d)]
                    (sort (.listFiles d)))))

(defn hex [bytes]
  (->> bytes
       (mapv (partial format "%02x"))
       (apply str)))

(defn unhex [s]
  (->> (range (count s))
       (mapv (partial * 2))
       (mapv #(subs s % (+ % 2)))
       (mapv #(Byte/parseByte % 16))
       (into-array Byte/TYPE)))

(defn path->hashable [paths]
  (->> (mapv io/file paths)
       (sort)))

(defn deps->hashable [deps]
  (let [deps-set (set deps)]
    (->> (mapv (fn [[name version]]
                 [(str name) (str version)])
               (:dependencies (project/read)))
         (filterv (fn [[name]] (contains? deps-set name)))
         (mapv (partial str/join ":"))
         (sort))))

(defn hash [& args]
  (->> (apply concat args)
       vec
       sha256hash
       hex))

(spec/def ::properties-file string?)
(spec/def ::property-key string?)
(spec/def ::paths (spec/coll-of string?))
(spec/def ::deps (spec/coll-of string?))
(spec/def ::config (spec/keys :req-un [::properties-file ::property-key ::paths ::deps]))
(spec/def ::configs (spec/coll-of ::config))

(defn files-hash
  "Writes a SHA-256 Merkle tree of some file trees to a properties-file property.

  Example configuration in project.clj:

    :files-hash [{:properties-file \"resources/versions.properties\"
                  :property-key \"graph-hash\"
                  :deps  [\"org.some.dependency/dependency\"
                          \"org.some.other.dependency/other-dependency\"]
                  :paths [\"src/de/otto/some-package\"
                          \"src/de/otto/some-other-package\"]}]"
  [{configs :files-hash} & args]
  (if (spec/valid? ::configs configs)
    (doseq [{:keys [properties-file property-key paths deps]} configs]
      (let [props (props/load-props properties-file)]
        (-> props
            (assoc property-key (hash (path->hashable paths) (deps->hashable deps)))
            (props/store-props properties-file :comment "Last written by lein-files-hash."))))
    (do (spec/explain ::configs configs)
        (flush)
        (main/abort "Invalid configuration for files-hash"))))
