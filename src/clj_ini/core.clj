(ns clj-ini.core
  (:use [clojure.contrib.str-utils2 :only [split]]
        [clojure.contrib.duck-streams :only [read-lines append-spit spit]]
        [clojure.contrib.seq-utils :only [includes? partition-all]])
  (:import [java.io File]))

(defn create-file
  [name]
  (when-not (.exists (File. name)) (.createNewFile (File. name))) nil)

(defn replace-str 
  [a b s]
  (.replace s a b))

(defn split-
  [re s]
  (split s re))

(defn read-map
  "Constructs a Clojure hash-map from write-map dump. Returns an
empty map and creates the file if the file did not exist."
  [file]
  (create-file file)
  (if-not (empty? (read-lines file))
    (let [lines (remove #(or
                          (every? (partial = \space) %)
                          (some (partial = (first %)) [\# nil])
                          (not (includes? % \=)))
                        (read-lines file))
          file-str (if-not (empty? lines) 
                     (->> lines
                          (interpose \space)
                          (apply str)
                          (remove #(= \= %))
                          (apply str)
                          (replace-str "  " " ")
                          (split- #" ")
                          (partition-all 2)) {})]
      (cond (-> file-str last count even?)
            (reduce merge (map #(hash-map (-> % first read-string keyword)
                                          (-> % second read-string)) file-str))
            (-> file-str last count odd?)
            (throw (Exception.
                    "Exception in parsing. An uneven number of key/vals were found.
                       Remember a key/val should be one line."))
            (-> file-str empty?) {})) {}))

(defn write-map
  "Spits the map in a readable format. Takes optional comments metadata."
  [file map]
  (if-let [comments (:comments (meta map))]
    (do
      (spit file "")
      (if-not (sequential? comments)
        (spit file (str "#" \space comments \newline \newline))
        (do
          (doseq [x comments]
            (append-spit file (str "#" \space x \newline)))
          (append-spit file \newline))))
    (let [comments (filter #(= (first %) \#) (read-lines file))]
      (when-not (empty? comments)
        (spit file "")
        (doseq [x comments]
          (append-spit file (str x \newline)))
        (append-spit file \newline))))
  (doseq [kv (merge (read-map file) map)]
    (append-spit file (str (key kv) " = " (val kv) \newline))))
