# Introduction #

This package contains code for numerical optimization.

For Unconstrained optimization it contains an implementation of the following gradient based methods:
  * Gradient Descent
  * Conjugate Gradient
  * LBFGS
and the following Line Search methods:

  * Armijo backtracking line search
  * Wolfes rule

For Constrained Optimization contains the implementation of the following methods:
  * Projected Gradient Descent

and the following Line Search Methods:

  * Armijo Rule along the projection arc

and the projection code for the following constraints:

  * Box constraints
  * Simplex Constraints

# Details #

The library is organized in the following packages:
  * gradientBasedMethods - Methods that find a descent direction based on the gradient
  * linesearch - Unidimensional search methods
  * projections - Methods that perform the projection of a point into a set
  * stopCriteria - Different stooping criteria for  the optimization algorithm


Add your content here.  Format your content with:
  * Text in **bold** or _italic_
  * Headings, paragraphs, and lists
  * Automatic links to other wiki pages