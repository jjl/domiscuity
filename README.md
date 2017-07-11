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
 (:require [irresponsible.domiscuity.parser :as parser]
           [irresponsible.domiscuity.dom :as dom]))

;; Parse HTML file
(def doc (parser/doc (slurp "my-file.html")))

;; Fetch all elements with class "small"
(dom/find-by-class doc :small)

;; Fetch all elements with attribute "value"
(dom/find-by-attr doc :value)
;; Fetch all elements with attribute "value" of 5
(dom/find-by-attr doc :value 5)

;; Get a single element with ID main-container
(def elem (dom/find-by-id doc :main-container))

;; Get all attributes
(dom/attrs elem)
;; Set attribute value
(dom/set-attr! elem :class "top-level")
;; Get the new attribute
(println (dom/attr elem :class)) => "top-level"

;; Get all children of the given element
;; (includes text and comments)
(dom/children elem)

;; Get all ONLY element children
(dom/child-elems elem)

;; Add a child element
(dom/append! elem (dom/make-element "div" {:class "row"}))

;; Detach children from parents
(dom/detach! elem)

;; Add a sibling element to the current one
(dom/insert-after! elem (dom/make-element "div" {:id "footer"}))
;; Check that the sibling is correct
(println (dom/attr (dom/next-sibling elem) "id")) => "footer"

;; Transucer for finding javascript elements
(def xform (filter #(= "text/javascript" (dom/attr % "type"))))
;; Find all javascript script tags
(def javascripts (dom/find-by-tag "script" xform))

;; Perform advanced queries
(dom/query-all doc "p.small#introduction")
;; Filter with custom predicate
(dom/find-attr doc #(and (= (dom/attr % :class) "small")
                         (= (dom/attr % :id) "introduction")))
                         
;; Reading and writing text
(def doc (parser/doc "<p>My text block</p>"))
(println (dom/text doc)) => "My text block"
(dom/set-text! doc "Another text block")
(println (dom/text doc)) => "Another text block"
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
