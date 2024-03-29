# Shuffling Cards

Visualizing various real-world card shuffles.

[Online Demo](https://shuffling.rafal.dittwald.com/)

[Slides](https://docs.google.com/presentation/d/1lDe9viL12U0NWVxYQ6KWsbhWnSoLQkkqgllo33r9E1Y/edit?usp=sharing)

[Notes](https://docs.google.com/document/d/1XstrEP83JLsOATRzZdjgwMXDpTrffgDSLpNFuj-YkcA/edit?usp=sharing)

![Preview Image of Bias Matrixes of Identity, Overhand, Milk, Riffle and Fisher-Yates Shuffles](/docs/preview-image-1.png)
![Preview Image of Bias Matrixes of a Combined Overhand, Milk and Riffle Shuffle](/docs/preview-image-2.png)

## Getting Started

- Have Java & Leiningen
- `git clone`
- `cd shuffling-cards`
- `lein repl`
- `(start!)`
- visit `http://localhost:9399` in the browser

`src/shuffleviz/shuffles.cljs` implements various shuffles and has various helper functions

`src/shuffleviz/slides.cljs` is what renders the UI; add new shuffles to display by editing the `shuffles` list

