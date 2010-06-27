# Clj-Ini

Another small, superficial read/write key/val configuration utility.

## Usage

* `(write-map)`                  - Writes key/values to a file in a readable format.
* `(read-map)`                   - Returns the key/values to a Clojure data structure.

### Example

    (write-map "foo.bar" (with-meta 
                           {:a 1 :b [2 3] :c {:d 4, :e 5}}
                           {:comments ["Comment, line 1" "Comment, line 2"]}))

### Results

File: [foo.bar](http://github.com/MayDaniel/Clj-Ini/blob/master/foo.bar)

## Installation

- Leiningen.
- Add `[clj-ini "0.1.0"]` to your dependencies in project.clj.

## License

Clj-Ini is licensed under the Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
