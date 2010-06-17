(ns clj-ini.core
  (:use [clojure.contrib.str-utils2 :only [split]]
        [clojure.contrib.duck-streams :only [read-lines append-spit spit]]
        [clojure.contrib.seq-utils :only [includes? partition-all]])
  (:import [java.io File]))

;; Ignores lines that: have the prefix '#' (comment lines), contain only whitespace, are empty, do not include '='
;; Currently does not allow a key/value to be multiline.
;; Comment metadata can be a collection of strings or a string, depending on multiline or singleline

(defn read-map [file]
  (when-not (.exists (File. file)) (.createNewFile (File. file)))
  (if-not (empty? (read-lines file))
    (do
      (let [lines (remove #(or
                            (every? (partial = \space) %)
                            (some (partial = (first %)) [\# nil])
                            (not (includes? % \=))) (read-lines file)) file-str
            (if-not (empty? lines) (->> lines
                                        (interpose \space)
                                        (apply str)
                                        (remove #(= \= %))
                                        (apply str)
                                        (.replaceAll #"  " " ")
                                        (split #" ")
                                        (partition-all 2)) {})]
        (cond (-> file-str last count even?)
              (reduce merge (map #(hash-map (-> % first read-string keyword)
                                            (-> % second read-string)) file-str))
              (-> file-str last count odd?)
              (throw (Exception. "Exception in parsing. An uneven number of key/vals in the file."))
              (-> file-str empty?) {}))) {}))

(defn write-map [file map]
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
