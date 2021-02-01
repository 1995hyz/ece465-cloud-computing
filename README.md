# ece465-cloud-computing
This repo is for course work for ece465, Cloud Computing.

The depth-first search algorithm used to implement this multi-threaded Sudoku solver is adapted from Algorithm 6 of this paper:
https://shawnjzlee.me/dl/efficient-parallel-sudoku.pdf

## Algorithm Description

Separate threads analyze the game tree simultaneously via depth-first search. A globally-synchronized queue ("fringe") stores grids to be explored, so that all the threads can work on the same subtree. Another globally-synchronized queue ("explored") stores grids that have already been explored to ensure optimal use of the threads. The threads analyze grids in the fringe by first determining which cell to test with a value, then updating the board to maintain the constraints of the puzzle, then checking if the grid is valid. If the grid is valid and fully solved, then the solution is found. If the grid is valid but not completed, the grid is added to the fringe and the explored queues. If the grid is invalid, it is added to the explored queue before the thread pops off another grid from the fringe to analyze. This process occurs until a solution is found, at which point an atomic boolean is set to true to alert all threads to terminate.
