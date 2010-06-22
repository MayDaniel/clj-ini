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

(defn get-comments
  [source]
  (letfn [(extract-comments [src] (take-while #(= (first %) \#) src))]
    (with-meta {}
      (cond (not (or (every? string? source) (string? source)))
            (throw (Exception. "Source should be either a sequence of strings, or a file-name."))
            (string? source) {:comments (extract-comments (read-lines (create-file source)))}
            :else {:comments (extract-comments source)}))))

(defn read-map
  "Constructs a Clojure hash-map from write-map dump. Creates the file
if it does not exist. Any comment metadata will be included."
  [file]
  (let [contents (read-lines (create-file file))
        data-lines (remove #(not (includes? % \=)) contents)
        meta-comments (meta (get-comments contents))]
    (if (empty? data-lines) {}
        (loop [lines data-lines acc {}]
          (if (empty? lines) (with-meta acc meta-comments)
              (let [[x & more] lines
                    [key val] (re-split #" = " x 2)]
                (recur more (assoc acc (read-string key) (read-string val)))))))))

(defn write-map
  "Writes a (merge (read-map file) map) to file, and takes optional comments metadata to include in the dump."
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
