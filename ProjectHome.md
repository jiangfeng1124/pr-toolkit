This project contains an implementation of an unsupervised machine learning framework called Posterior Regularization.

Posterior Regularization is a probabilistic framework for structured,
weakly supervised learning.  Our framework efficiently incorporates indirect
supervision via constraints on posterior distributions of probabilistic models
with latent variables. Posterior Regularization separates model
complexity from the complexity of structural constraints it is desired to
satisfy.  By directly imposing decomposable regularization on the posterior
moments of latent variables during learning, we retain the computational
efficiency of the unconstrained model while ensuring desired constraints hold
in expectation.

This code is based on several research projects and includes
implementations of code for part of speech and grammar induction, as
well as word alignment.  See the main page for a more detailed
description.