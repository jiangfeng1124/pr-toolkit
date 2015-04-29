# Introduction #

We haven't made any releases yet, but you can get the code using subversion.  The command is:

```
svn checkout http://pr-toolkit.googlecode.com/svn/applications/postagging/trunk/ pr-toolkit-postagging-read-only
```

The postagging code is under `applications/postaggin/trunk` and the main class is `src/postagging/program/RunModel.java` The program requires a corpus parameters file (which tells it where the training and testing data are).   Here is an example `corpus.params` file:

```
name=penn-treebank
lowercase=true
transductive=true

unknown-threshold=1 
train-name=portuguese-train-conll.gz
train-file=/home1/k/kuzman/posTag-project/portuguese-train-conll.gz
test-name1=portuguese-test-conll.gz
test-file1=/home1/k/kuzman/posTag-project/portuguese-test-conll.gz
reader-type=posTag-project
```

The name is used when saving the model after training, and is useful for keeping track of which corpus you're dealing with.  `lowercase=true` means that we want to lowercase the corpus (i.e. "The" and "the" will be the same output symbol), `transductive=true` means that the unlabeled testing data is added to the training data.  Finally, `unknown-threshold=1` means that words that only occur once are replaced with a special "unk"
symbol.  Grouping rare words into a single emission tends to imprve
results for HMMs, but who knows what will happen for the languages you
try.  Also... `reader-type=posTag-project` means we expect the files to be in the format:

```
art     Um
n       revivalismo
adj     refrescante

art     O
prop    7_e_Meio
v-fin   é
art     um
n       ex-libris
prp     de
art     a
n       noite
adj     algarvia
punc    .                                                                            
```

where each line contains a POS tag and word, and sentences are split by
blank lines.  The other option is to use CoNLL X format.

There are options in the code for controlling the number of EM iterations, and whether to train using EM or L1LMax (sparse PR training).  All the options are declared in the `RunModel.java` file, which you can also access here:

```
http://code.google.com/p/pr-toolkit/source/browse/applications/postagging/trunk/src/postagging/programs/RunModel.java
```

For example you might run:

```
export WDIR=location-of-the-postagging-code-base-dir
export CLASSPATH="$WDIR/class/:$WDIR/lib/args4j-2.0.10.jar:$WDIR/lib/commons-math-2.0.jar"
export CLASSPATH="$CLASSPATH:$WDIR/lib/optimization-0.1.jar:$WDIR/lib/pr-toolkit-0.1.jar:$WDIR/lib/trove-2.0.2.jar"

java -cp $CLASSPATH -Xmx1800m postagging.programs.RunModel \
       -corpus-params "<corpus-params-file>" -base-output-dir "<save-model-here>" \
       -num-em-iters 100 -model-type HMMFinalState -model-init RANDOM

```

You can see other options in the `RunModel.java` file, they are declared with the `@Option` macro.



# Details #

Add your content here.  Format your content with:
  * Text in **bold** or _italic_
  * Headings, paragraphs, and lists
  * Automatic links to other wiki pages