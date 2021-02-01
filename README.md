# ece465-cloud-computing
This repo is for course work for ece465, Cloud Computing.

The depth-first search algorithm used to implement this multi-threaded Sudoku solver is adapted from Algorithm 6 of this paper:
https://shawnjzlee.me/dl/efficient-parallel-sudoku.pdf

## Algorithm Description

Separate threads analyze the game tree simultaneously via depth-first search. A globally-synchronized queue stores grids to be explored, so that all the threads
can work on the same subtree. Another globally-synchronized queue stores grids that have already been explored to ensure optimal use of the threads.
