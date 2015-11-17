import genome

#import numpy

import subprocess

from deap import algorithms
from deap import base
from deap import creator
from deap import tools
from deap import gp

NoneType = type(None)

pset = gp.PrimitiveSetTyped("main", [], NoneType)

#logical
pset.addPrimitive(genome.Or, [bool, bool, NoneType], NoneType)
pset.addPrimitive(genome.And, [bool, bool, NoneType], NoneType)
pset.addPrimitive(genome.Not, [bool, NoneType], NoneType)
pset.addPrimitive(genome.If_Then, [bool, NoneType], NoneType)

#actions
pset.addPrimitive(genome.TurnGunToEnemy, [], NoneType)
pset.addPrimitive(genome.TurnGunLeft5, [], NoneType)
pset.addPrimitive(genome.TurnGunLeft10, [], NoneType)
pset.addPrimitive(genome.TurnGunRight5, [], NoneType)
pset.addPrimitive(genome.TurnGunRight10, [], NoneType)
pset.addPrimitive(genome.Fire1, [], NoneType)
pset.addPrimitive(genome.Fire2, [], NoneType)
pset.addPrimitive(genome.Fire3, [], NoneType)

#tests
pset.addPrimitive(genome.TestEnemyEnergy, [], bool)
pset.addPrimitive(genome.TestEnemyEnergy0, [], bool)
pset.addPrimitive(genome.TestEnemyEnergyBelow10, [], bool)
pset.addPrimitive(genome.TestEnergyBelow10, [], bool)
pset.addPrimitive(genome.TestEnergyGreaterThanEnemys, [], bool)
pset.addPrimitive(genome.TestEnergyLessThanEnemys, [], bool)
pset.addPrimitive(genome.TestEnemyWithin10Ticks, [], bool)
pset.addPrimitive(genome.TestEnemyWithin20Ticks, [], bool)
pset.addPrimitive(genome.TestEnemyWithin50Ticks, [], bool)
pset.addPrimitive(genome.TestGunIsHot, [], bool)
pset.addPrimitive(genome.TestGunWithin5Ticks, [], bool)
pset.addPrimitive(genome.TestTurnToEnemyWithin10Ticks, [], bool)
pset.addPrimitive(genome.TestTurnToEnemyWithin5TIcks, [], bool)

#"plugs" the holes at the end. Is this what we want?
pset.addTerminal(True, bool)
pset.addTerminal(False, bool)
pset.addTerminal(None, NoneType)

#begin genetic programming
#initialize population
creator.create("FitnessMin", base.Fitness, weights=(-1.0,))
creator.create("Individual", gp.PrimitiveTree, fitness=creator.FitnessMin, pset=pset)

toolbox = base.Toolbox()
toolbox.register("expr", gp.genFull, pset=pset, min_=10, max_=15) #the min and max depth for leaves?
toolbox.register("individual", tools.initIterate, creator.Individual, toolbox.expr)
toolbox.register("population", tools.initRepeat, list, toolbox.individual)

def writeGenome(filename, individual):
	outputFile = open(filename, 'w')
	outputFile.write(str(individual))
	outputFile.close()

def evalRobot(individual):
    # Transform the tree expression in a callable function
    #func = toolbox.compile(expr=individual)
    writeGenome("genome.txt", individual)
    robocodeCommand = "java -Xmx512M -DNOSECURITY=false -Dsun.io.useCanonCaches=false -cp C:/robocode/libs/robocode.jar robocode.Robocode -battle C:/robocode/battles/sample.battle -nodisplay -results results.txt"
    subprocess.call(robocodeCommand)
    result = 1.0 #call robocode
    return result,

#genetic parameters
toolbox.register("evaluate", evalRobot)
toolbox.register("select", tools.selTournament, tournsize=3)
toolbox.register("mate", gp.cxOnePoint)
toolbox.register("expr_mut", gp.genFull, min_=5, max_=10) #the min and max depth of new mutations?
toolbox.register("mutate", gp.mutUniform, expr=toolbox.expr_mut, pset=pset)

#random.seed(10)
pop = toolbox.population(n=100)
hof = tools.HallOfFame(1)
stats = tools.Statistics(lambda ind: ind.fitness.values)
#stats.register("avg", numpy.mean)
#stats.register("std", numpy.std)
#stats.register("min", numpy.min)
#stats.register("max", numpy.max)

algorithms.eaSimple(pop, toolbox, 0.5, 0.2, 40, stats, halloffame=hof)