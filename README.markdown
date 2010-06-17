# Clj-Ini

Another small, superficial read/write key/val configuration utility.

## Usage

- `#` is the prefix for commented lines.
- `read-map` ignores lines that; are comments, of which every element is whitespace, are empty, or do not include '='.
- Key/values can not be multiline.
- Comment metadata is always stored at the top of a file.
- `:comments` may be a collection of strings, or a string, depending on whether multi-lines or single-lines are wanted, respectively.
- Written files should not contain multiple keys of the same name.

* `(write-map)`                  - Writes key/values to a file, in a readable format, with comments metadata accepted.
* `(read-map)`                   - Reads the key/values back into a Clojure data structure.

### Example

    (write-map "foo.bar" (with-meta 
                           {:a 1 :b 2 :c 3}
                           {:comments ["Comment, line 1" "Comment, line 2"]}))

### Results

File: [foo.bar](http://github.com/maydaniel/blob/master/foo.bar)

## Installation

- Leiningen.

## License

Clj-Ini is licensed under the Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
