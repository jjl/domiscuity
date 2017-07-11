The irresponsible clojure guild presents...

# domiscuity

[![Clojars Project](https://img.shields.io/clojars/v/irresponsible/domiscuity.svg)](https://clojars.org/irresponsible/domiscuity)
[![Jitpack Project](https://jitpack.io/v/irresponsible/domiscuity.svg)](https://jitpack.io/#irresponsible/domiscuity)
[![Build Status](https://travis-ci.org/irresponsible/domiscuity.svg?branch=master)](https://travis-ci.org/irresponsible/domiscuity)

Parse html5 and play with the DOM with a clean API.

Supports clojure, clojurescript support is still experimental.

## Usage

```clojure
(ns my.app
 (:require [domiscuity.parser :as p]
           [domiscuity.nav :as nav]
		   [domiscuity.convertor :as c]))

;; parsing a string of html is easy 
(def doc (p/doc (slurp "my-file.html")))
;; so is finding elements within it
(def scripts (nav/find-tags "script"))
;; converting to clojure and filtering is easy too
(def javascripts
  (into [] (comp (keep c/native->clojure)
                 (filter #(= "text/javascript" (:type (:attrs %)))))
        scripts))
```

## Contributors

* [James Laver](https://github.com/jjl)
* [Antonis Kalou](https://github.com/kalouantonis)
* [Kent Fredric](https://github.com/kentfredric)

## Copyright and License

Copyright (c) 2016 James Laver

MIT LICENSE

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
