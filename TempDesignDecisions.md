# Learning Framework #

All the models will need to have the ability to take some sort of input and compute posteriors based on this input.  The posteriors will be wrapped in the appropriate Distribution class.  Additionally, the models will need to implement the appropriate interface for the type of training they support.

## Questions ##

  * For the EM-Model, **who is responsible for updating the count table**?  The model, the count table or the distribution?  It seems that it should be the EM-Model.
  * Is TrainSupervised really needed?  There might be a variety of ways to do this: gradient, or table update.  Will we need to subclass the model to change supervised updates?  Maybe that's not too bad.

Interfaces:

## EM-Model ##

Update parameters (CountTable)

## OnlineEM ##
Following the definition from StepWise EM paper from Liang:
Online EM for Unsupervised Models

  * UpdateStepParameter(CountTable, SentenceDist)
  * And add a class called OnLineEM Training


## GradientBasedEM ##

Folowing the paper from Painless Unsupervised Learning with Features

  * GetOptimizationObjective
  * UpdateModelFromOptimizationObjective()

The optimization objective is passed to the optimization package.  Then we take an optimization objective and update the model with the parameters.

## TrainSupervised ##

For training the models in a supervised fashion....
This should receive an instance list and return the model trained with that list.
