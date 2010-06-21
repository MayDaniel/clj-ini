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

(defn write-map
  "Spits the map in a readable format. Takes optional comments metadata."
  [file map]
  (if-let [comments (:comments (meta map))]
    (do
      (spit file "")
      (if-not (sequential? comments)
        (spit file (str "#" \space comments \newline \newline))
        (doseq [x comments]
          (append-spit file (str "#" \space x \newline)))
        (append-spit file \newline)))
    (let [comments (filter #(= (first %) \#) (read-lines file))]
      (when-not (empty? comments)
        (spit file "")
        (doseq [x comments]
          (append-spit file (str x \newline)))
        (append-spit file \newline))))
  (doseq [kv (merge (read-map file) map)]
    (append-spit file (str (key kv) " = " (val kv) \newline))))
