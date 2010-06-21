(ns clj-ini.core
  (:use [clojure.contrib.str-utils :only [re-split]]
        [clojure.contrib.duck-streams :only [read-lines append-spit spit]]
        [clojure.contrib.seq-utils :only [includes?]])
  (:import [java.io File]))

(defn create-file
  [name]
  (when-not (.exists (File. name)) (.createNewFile (File. name))) name)

(defn clean-file
  [name]
  (spit (create-file name) ""))

(defn read-map
  "Constructs a Clojure hash-map from write-map dump. Creates the file
if it does not exist."
  [file]
  (let [contents (read-lines (create-file file))
        file-map (remove #(not (includes? % \=)) contents)
        meta-comments (take-while #(= (first %) \#) contents)]
    (if (empty? file-map) {}
        (loop [lines file-map acc {}]
          (if (empty? lines) (with-meta acc {:comments meta-comments})
              (let [x (re-split #" = " (first lines) 2)]
                (recur (rest lines) (assoc acc (read-string (first x)) (read-string (second x))))))))))

(defn write-map
  "Writes a (merge (read-map file) map) to a file, and takes optional comments metadata
to include in the dump."
  [file map]
  (let [contents (read-map (create-file file))]
    (clean-file file)
    (if-let [meta-comments (:comments (meta map))]
      (do
        (doseq [line meta-comments]
          (append-spit file (str "# " line \newline)))
        (append-spit file \newline)))
    (doseq [kv (merge contents map)]
      (append-spit file (str (key kv) " = " (val kv) \newline)))))
