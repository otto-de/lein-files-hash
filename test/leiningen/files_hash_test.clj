(ns leiningen.files-hash-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [clojure.test.check.generators :as gen]
            [leiningen.files-hash :as files-hash])
  (:import [java.nio.file CopyOption Files Paths StandardCopyOption]))

(defn gen-1 [g]
  (first (gen/sample g 1)))

(defn random-string []
  (gen-1 (gen/not-empty gen/string-alphanumeric)))

(defn ensure-dir [dirname]
  (let [file (io/file dirname)]
    (when-not (.exists file)
      (.mkdirs file))))

(declare create-random-tree)

(defn create-random-dir [dirname]
  (let [c (rand-int 3)
        names (repeatedly c random-string)]
    (ensure-dir dirname)
    (doseq [child names]
      (create-random-tree (str dirname "/" child)))))

(defn create-random-file [filename]
  (let [parent (.getParentFile (io/file filename))]
    (.mkdirs parent)
    (spit filename (random-string))))

(defn create-random-tree [filename]
  ((if (= (rand-int 3) 0)
     create-random-dir
     create-random-file)
   filename))

(defn delete-dir [n]
  (doseq [f (reverse (file-seq (io/file n)))]
    (io/delete-file f)))

(defmacro with-random-tree [filename & body]
  `(do (create-random-dir ~filename)
       (try ~@body
            (finally (.delete (io/file ~filename))))))

(defn change-random-char [s]
  (let [i (rand-int (count s))
        old-c (nth s i)
        new-c (gen-1 (gen/such-that (partial not= old-c) gen/char-alphanumeric))
        cs (char-array s)]
    (aset-char cs i new-c)
    (String. cs)))

(defn rename-file [f n]
  (let [source (Paths/get (.getPath f) (into-array String []))]
    (Files/move source
                (.resolveSibling source n)
                (into-array CopyOption [StandardCopyOption/REPLACE_EXISTING]))))

(defn report-dir-state [dir]
  (into {}
        (for [f (file-seq (io/file dir))]
          [(.getPath f) (if (.isDirectory f)
                          :dir
                          (slurp (.getPath f)))])))

(deftest test-files-hash
  (let [test-dir "tmp/testdir"
        hashfile "tmp/test.hash"
        make-hash (fn []
                    (files-hash/files-hash {:files-hash {hashfile [test-dir]}})
                    (slurp hashfile))]
    (dotimes [n 50]
      (with-random-tree test-dir
        (testing "creates a valid hash file"
          (let [hash (make-hash)]
            (is (= 64 (count hash))
                (str (report-dir-state test-dir)))
            (is (every? int?
                        (mapv #(Integer/parseInt (subs hash % (+ % 2)) 16)
                              (mapv (partial * 2) (range 32))))
                (str (report-dir-state test-dir)))))
        (testing "hashing again gives the same result"
          (is (= (make-hash) (make-hash))
              (str (report-dir-state test-dir))))
        (testing "modifying a file name changes the hash"
          (let [hash-before (make-hash)
                files (rest (file-seq (io/file test-dir)))]
            (when (not-empty files)
              (let [f (rand-nth files)]
                (rename-file f (change-random-char (.getName f)))
                (is (not= hash-before (make-hash))
                    (str (report-dir-state test-dir)))))))
        (testing "modifying file content changes the hash"
          (let [hash-before (make-hash)
                files (->> (file-seq (io/file test-dir))
                           (remove #(.isDirectory %)))]
            (when (not-empty files)
              (let [f (rand-nth files)]
                (spit f (change-random-char (slurp f)))
                (is (not= hash-before (make-hash))
                    (str (report-dir-state test-dir))))))))
      (delete-dir "tmp"))))
