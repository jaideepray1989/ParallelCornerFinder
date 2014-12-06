# makefile for edu.mit.edu.mit.sketch.toolkit

default: all

HOME_MAKE_DIR = ${DRG_DIR}/scripts
include ${HOME_MAKE_DIR}/make.defaults

JAVA_PACKAGE = edu.mit.sketch.toolkit
JAVA_PACKAGE_DIRECTORY = edu/mit/sketch/toolkit

# Fill this in with the directories that must be made _before_
# this directory (i.e., because of a code dependency).
# PREDIRS = dir1 dir2 dir3
# PREDIRS = 

# Fill this in with the subdirectories that you would like made
# when you do a make in this directory
# SUBDIRS = dir1 dir2 dir3
# SUBDIRS = 

CLASSDIR = $(WRITE_CLASS_FILES_TO)/$(JAVA_PACKAGE_DIRECTORY)

-include .dependencies

include $(HOME_MAKE_DIR)/make.rules
