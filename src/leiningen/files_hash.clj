(ns leiningen.files-hash
  (:import [java.io File]
           [java.security MessageDigest])
  (:require [clojure.java.io :as io]
            [leiningen.files-hash.props :as props]))

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
                    (.listFiles d))))

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

(defn hash-paths [paths]
  (->> paths
       (mapv #(File. %))
       sha256hash
       hex))

(defn files-hash
  "Writes a SHA-256 Merkle tree of some file trees to a properties-file property.

  Example configuration in project.clj:

    :files-hash [{:properties-file \"resources/versions.properties\"
                  :property-key \"graph-hash\"
                  :paths [\"src/de/otto/nav/graph\"
                          \"src/de/otto/nav/feed\"]}]"
  [{:keys [files-hash] :as project} & args]
  (doseq [{:keys [properties-file property-key paths]} files-hash]
    (let [props (props/load-props properties-file)]
      (-> props
          (assoc property-key (hash-paths paths))
          (props/store-props properties-file :comment "Written by lein-files-hash.")))))
