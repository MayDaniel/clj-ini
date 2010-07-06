(ns clj-ini.core
  (:use [clojure.contrib.str-utils :only [re-split]]
        [clojure.contrib.duck-streams :only [read-lines append-spit spit]]
        [clojure.contrib.seq-utils :only [includes?]])
  (:import [java.io File]))

(defn check-create
  [name]
  (when-not (.exists (File. name)) (.createNewFile (File. name))) name)

(defn clean-file
  [name]
  (when (and (.exists (File. name)) (.canWrite (File. name)))
    (.delete (File. name)))
  (.createNewFile (File. name)) nil)

(defn get-comments
  [source]
  (letfn [(extract-comments [src] (take-while #(= (first %) \#) src))]
    (with-meta {}
      (cond (string? source) {:comments (extract-comments (read-lines (check-create source)))}
            (and (coll? source) (every? string? source)) {:comments (extract-comments source)}
            :else (throw (Exception. "Source should be either a sequence of strings, or a file-name."))))))

(defn read-map
  "Constructs a Clojure hash-map from write-map dump. Creates the file
if it does not exist. Any comment metadata will be included."
  [file]
  (let [contents (read-lines (check-create file))
        data-lines (remove #(not (includes? % \=)) contents)
        meta-comments (meta (get-comments contents))]
    (if (empty? data-lines) {}
        (loop [lines data-lines acc {}]
          (if lines (with-meta acc meta-comments)
              (let [[x & more] lines
                    [key val] (re-split #" = " x 2)]
                (recur more (assoc acc (read-string key) (read-string val)))))))))

(defn write-map
  "Writes a (merge (read-map file) map) to file, and takes optional comments
metadata to include in the dump."
  [file map]
  (let [contents (read-map (check-create file))]
    (clean-file file)
    (when-let [meta-comments (:comments (meta map))]
      (doseq [line meta-comments]
        (append-spit file (str "# " line \newline)))
      (append-spit file \newline))
    (when (some seq [contents map])
      (loop [out-map (merge contents map)]
        (when out-map
          (let [kv (first out-map)]
            (append-spit file (str (key kv) " = " (val kv) \newline)))
          (recur (next out-map))))))
