Scala-Paxos
===========

This is a toy implementation of Paxos in pure Scala. It was mostly a fun little project for me to learn scala and actor programming, and, now, to get familiar with github and git (I'm still an old-school SVN person). Since I am unlikely to have much more time to improve it in the next few months, I figured I'd throw it up here.

If you don't know what Paxos is, take a look at Leslie Lamport's paper "Paxos made simple", and if you are still lost, "Paxos for system builders" is a nice discussion of many of the things that must be considered when actually attempting to build a system.

My goal at the beginning was to create a simple platform for playing with different Paxos variations/implementations. So if you need bandwidth efficiency, you can instantiate a set of ring-paxos classes and the calling code should use the same interface. It is not clear to what extent this is possible, and for now the only implementation is a basic, 3-phase simple paxos. 

At the moment integration with the Java edition of Berkeley DB to provide stable storage is incomplete, but this shouldn't be too much work to integrate.

Usage
-----
The project is maven-enabled, so you should be able to run in the root directory:

mvn compile

and

mvn assembly:assembly

if you want to produce a jar with all the dependencies included. BDB JE is included although is not being used yet.

To get an idea for how to instantiate and run the various parts, take a look at scripts/launch\_local.sh

Usual Disclaimer
----------------

I make *no* claims about the following:
- this code is correct
- that I fully understand paxos (in general, distrust anyone that claims that they do)
- that this is an exemplary use of the Scala language
- that this code is worth more than the economic value of the electricity used to send this README to you

Alex Tomic
