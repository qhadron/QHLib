# QHLib

A Java library for math, 2D graphics, and 3D graphics.

## qh.math

This package contains classes for working with matrices and vectors in 3D computer graphics.

## qh.q3d

This package contains classes for working with 3D objects, including a parser able to parse JSON formatted objects (see qh.q3d.Parser3D).

## qh.qwindow

This package contains 2 types of windows: 

1. QWindow
  This is a class that is modelled based on Turing's rendering pipeline. Instead of being forced to draw everything in an update function, this window allows the user to draw elements on the screen any time they wanted. This class also handles both keyboard and mouse input.

2. Window3D
  This class allows users to add objects (qh.q3d.Object3D) and will draw them automatically. The calculations are done on a seperate thread. This class handles input by moving the camera around similar to a flight simulator.
