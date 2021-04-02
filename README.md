# ece465-cloud-computing
This repo is for course work for ece465, Cloud Computing.

The depth-first search algorithm used to implement this multi-threaded Sudoku solver is adapted from Algorithm 6 of this paper:
https://shawnjzlee.me/dl/efficient-parallel-sudoku.pdf

## Algorithm Description

Separate threads analyze the game tree simultaneously via depth-first search. A globally-synchronized queue ("fringe") stores grids to be explored, so that all the threads can work on the same subtree. Another globally-synchronized queue ("explored") stores grids that have already been explored to ensure optimal use of the threads. The threads analyze grids in the fringe by first determining which cell to test with a value, then updating the board to maintain the constraints of the puzzle, then checking if the grid is valid. If the grid is valid and fully solved, then the solution is found. If the grid is valid but not completed, the grid is added to the fringe and the explored queues. If the grid is invalid, it is added to the explored queue before the thread pops off another grid from the fringe to analyze. This process occurs until a solution is found, at which point an atomic boolean is set to true to alert all threads to terminate.

## Revision 1b

The algorithm was sped up by introducing pruning, i.e. ignoring branches that created invalid grids. The heuristics applied were sourced from: https://github.com/Shivanshu-Gupta/Parallel-Sudoku-Solver/blob/master/doc/DesignDoc.pdf.
Specifically, grids off of the fringe that had cells with no possible values were pruned, in addition to grids where there was a desired number not in the possible values of a row, column, or grid. 

Another speed improvement came from the removal of the explored queue, which was realized to be unnecessary as the algorithm never duplicated grids. Now, a thread attempting to add to the fringe does not need to check the explored queue first.

Lastly, a hard Sudoku puzzle was used to test the performance of this revision versus the first. The results can be found in runtime_stats.txt.

## Revision 2a
The algorithm was updated to support running in multi-node multi-thread. A "Manager" node and several "Client" nodes can be spawned. The "Manager" node is responsible for sending an initial graph to "Client" nodes and maintain a history of explored graphs. " Each "Client" is responsible for exploring unsolved graphs. To ensure each "Client" isn't working on solving the same graph as other "Client" nodes, before actually starting to work on a graph, the "Client" needs to check with the "Manager" if the graph has been explored or not.