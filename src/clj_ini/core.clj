(ns clj-ini.core
  (:use [clojure.contrib.str-utils :only [re-split]]
        [clojure.contrib.duck-streams :only [read-lines append-spit spit]]
        [clojure.contrib.seq-utils :only [includes?]])
  (:import [java.io File]))

(defn create-file
  [name]
  (when-not (.exists (File. name)) (.createNewFile (File. name))) name)

(defn read-map 
  "Constructs a Clojure hash-map from write-map dump. Creates the file
if it does not exist."
  [file]
  (let [contents (read-lines (create-file file))
        file-map (remove #(not (includes? % \=)) contents)]
    (if (empty? file-map) {}
        (loop [lines file-map acc {}]
          (if (empty? lines) acc
              (let [x (re-split #" = " (first lines) 2)]
                (recur (rest lines) (assoc acc (read-string (first x)) (read-string (second x))))))))))
