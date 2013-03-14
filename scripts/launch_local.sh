#!/bin/bash

if [ $# -lt 1 ]; then
	echo "Usage: $0 <Conf File>"
	echo "Runs acceptors and learners locally with config file"
	exit 1;
fi

#BASEDIR=~/branches/scala-paxos
BASEDIR=$PWD
CLASSDIR=$BASEDIR/target/classes
LOGDIR=$BASEDIR/logs
CONF=$BASEDIR/$1
cd $CLASSDIR

#scala ch.usi.tomica.paxos.simple.ProposerApp 1 ../../config/cluster_48.conf 1
echo "Launching acceptors"
scala ch.usi.tomica.paxos.simple.AcceptorApp 1 $CONF > $LOGDIR/a1.log &
sleep 0.5
scala ch.usi.tomica.paxos.simple.AcceptorApp 2 $CONF > $LOGDIR/a2.log &
sleep 0.5
scala ch.usi.tomica.paxos.simple.AcceptorApp 3 $CONF > $LOGDIR/a3.log &
sleep 0.5

echo "Launching learers"
scala ch.usi.tomica.paxos.simple.LearnerApp 1 $CONFDIR/$CONF > $LOGDIR/l1.log &

echo "Done. Start some proposals!"
wait
