All the classes in this folder implement the Navigator interface. They are interchangeable so that the simulator 
can choose which navigation method they want to use easily. Only one of these classes are used for an execution.

* AStar: Basic AStar implentation *
C (No Cuts):  	 	Disallows diagonal movement accross an obstacle
R (Repulsion):   	Repulsive force to prioritize avoiding obstacles (prioritises for selection)
RR(Repulsion Two):	Repulsive force is used to affect distance, not just for selection (room for optimisation)
S (Smoothing): 	 	Cuts out unneeded nodes from the path
SS(Smoothing Two):	Cuts out unneeded nodes but with a limit on how many in a row can be cut
W (Width): 	 	The smoothing cuts uses a wide line to detect collision rather than a thin one
T (Low Turning): 	Prioritize tiles that can be reached with the least turning (distance now includes turn)
TT(Low Turning Two): 	Only the first node takes into account turning

Review:
AStar:		Clings to obstacle edges, not smooth, path can ping pong
AStarC:		Slighly better at avoiding, not smooth, path can ping pong
AStarR:		Does a decent job of avoiding obstacles but sometimes cuts corners, not smooth, path can ping pong
AStarRR:	Less likely to cut corners, not smooth, path can ping pong
AStarS:		Much smoother, jerky around corners, sometimes passes through obstacles
AStarT:		Potentially enormous computation (~20 seconds)
AStarTT:	Paths are a little bit straighter, clings to edges, not smooth, path can ping pong

AStarCS:	Cuts through obstacles
AStarCSW:	Still cuts through obstacles
AStarRRSW:	Smooth, still clings to obstacles a bit but then tries to move away before continuing
AStarRRSSW:	Slightly less smooth but doesn't cling to obstacles as much

* Rapidly Exploring Random Tree implementation *
Review
RapidExploringRandomTree:	Creates a random new path each iteration, guarenteed ping pong

* Vector Field *
C (No Cuts):  	 	Disallows diagonal movement accross an obstacle
SS(Smoothing Two):	Cuts out unneeded nodes but with a limit on how many in a row can be cut
W (Width): 	 	The smoothing cuts uses a wide line to detect collision rather than a thin one

Review:
