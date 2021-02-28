#!/bin/bash

declare -r  FILES_TO_DELETE="HYB_HIT*"
declare -ri MIN_DAYS_TO_PRESERVE=0
declare -ri NUM_ARGS=$#
declare -r  ARG1=$1
declare -i  days_to_preserve=$1

function match() {
	local -r str=$1
	local -r regex=$2
	matched=0
	echo $str | grep -i -E $regex > /dev/null
	[ $? -eq 0 ] && matched=1 || matched=0
}

function usage {
	echo "usage: takipi-cleanup.sh [number of days to preserve]"
}

function checkArgument {

	if [ $NUM_ARGS -gt 1 ]; then
		usage
		exit 1
	fi

	if [ $NUM_ARGS -eq 1 ]; then
		if [ "$days_to_preserve" != "$ARG1" ] || [ $days_to_preserve -lt $MIN_DAYS_TO_PRESERVE ]; then
			usage
			exit 1
		fi
	else
		days_to_preserve=-1
	fi
}

function readDaysToPreserve {

	while [ $days_to_preserve -lt $MIN_DAYS_TO_PRESERVE ]
	do
		echo -e "Please enter the number of days (>= $MIN_DAYS_TO_PRESERVE) to preserve: \c"
		read days_to_preserve
	done
}

function main {

	checkArgument

	readDaysToPreserve

	local -i file_count=$(find . -name "$FILES_TO_DELETE" -type f -mtime +$days_to_preserve | wc -l)
	if [ $file_count -eq 0 ]
	then
		echo "There are no files older than $days_to_preserve days to delete"
	else
		echo -e "Delete all $file_count files older than $days_to_preserve days? (y/n): \c"
		read proceed

		match $proceed "^y$|^yes$" 
		
		if [ -n "$proceed" ] && [ $matched -eq 1 ]
		then
			find . -name "$FILES_TO_DELETE" -type f -mtime +$days_to_preserve -delete
			echo "File deletion is complete."
		else
			exit 1
		fi
	fi
}

main

