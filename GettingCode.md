# Structure of the Subversion Repository #

This project is divided into three parts:
  * an **optimization** library (CodeOptimization), that is used to optimize the various objectives we use
  * a **pr-toolkit** (CodePrToolkit) library that contains shared code
  * a set of **applications** (CodeApplications) that use the pr-toolkit and optimization library for a variety of applications

# How to get the code #

You can get the code from subversion.  The Source tab at the top of this page describes how to get the entire subversion tree, but if you are interested in only one part, e.g. the part of speech (POS) tagging application, you should follow these instructions:

Find the part of the code you are interested in getting by browsing in

```
http://code.google.com/p/pr-toolkit/source/browse/
```

Suppose you decide that you want to get the POS tagging application which you found in `applications/postagging/trunk`.

## Checking out so you can commit changes ##

If you plan to make changes, use this command to check out the code as yourself using HTTPS:
```
# Project members authenticate over HTTPS to allow committing changes.
svn checkout https://pr-toolkit.googlecode.com/svn/applications/postagging/trunk pr-toolkit-applications --username <googlecode-user>
```

When prompted, enter your generated googlecode.com password.

## Checking out anonymously ##

Use this command to anonymously check out the latest project source code:
```
# Non-members may check out a read-only working copy anonymously over HTTP.
svn checkout http://pr-toolkit.googlecode.com/svn/applications/postagging/trunk pr-toolkit-applications-read-only
```

You can similarly get just the trunk of optizimation or pr-toolkit