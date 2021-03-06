import genome

#import numpy

import subprocess

from deap import algorithms
from deap import base
from deap import creator
from deap import tools
from deap import gp

NoneType = type(None)
FunctionType = type(genome.Root)

pset = gp.PrimitiveSetTyped("main", [], FunctionType)

#add initial node
pset.addPrimitive(genome.Root, [NoneType, NoneType, NoneType, NoneType, NoneType], FunctionType)

#actions
pset.addPrimitive(genome.Fire1, [], NoneType)
pset.addPrimitive(genome.Fire2, [], NoneType)
pset.addPrimitive(genome.Fire3, [], NoneType)
pset.addPrimitive(genome.TurnGunToEnemy, [], NoneType)
pset.addPrimitive(genome.TurnGunLeft5, [], NoneType)
pset.addPrimitive(genome.TurnGunLeft10, [], NoneType)
pset.addPrimitive(genome.TurnGunRight5, [], NoneType)
pset.addPrimitive(genome.TurnGunRight10, [], NoneType)

#tests
pset.addPrimitive(genome.TestEnemyEnergy0, [], bool)
pset.addPrimitive(genome.TestEnemyEnergyBelow10, [], bool)
pset.addPrimitive(genome.TestEnergyBelow10, [], bool)
pset.addPrimitive(genome.TestEnergyGreaterThanEnemys, [], bool)
pset.addPrimitive(genome.TestEnergyLessThanEnemys, [], bool)
pset.addPrimitive(genome.TestEnemyWithin5Ticks, [], bool)
pset.addPrimitive(genome.TestEnemyWithin10Ticks, [], bool)
pset.addPrimitive(genome.TestEnemyWithin20Ticks, [], bool)
pset.addPrimitive(genome.TestEnemyWithin50Ticks, [], bool)
pset.addPrimitive(genome.TestEnemyWithin5TicksOfFire1, [], bool)
pset.addPrimitive(genome.TestEnemyWithin5TicksOfFire2, [], bool)
pset.addPrimitive(genome.TestEnemyWithin5TicksOfFire3, [], bool)
pset.addPrimitive(genome.TestEnemyWithin10TicksOfFire1, [], bool)
pset.addPrimitive(genome.TestEnemyWithin10TicksOfFire2, [], bool)
pset.addPrimitive(genome.TestEnemyWithin10TicksOfFire3, [], bool)
pset.addPrimitive(genome.TestEnemyWithin20TicksOfFire1, [], bool)
pset.addPrimitive(genome.TestEnemyWithin20TicksOfFire2, [], bool)
pset.addPrimitive(genome.TestEnemyWithin20TicksOfFire3, [], bool)
pset.addPrimitive(genome.TestEnemyWithin50TicksOfFire1, [], bool)
pset.addPrimitive(genome.TestEnemyWithin50TicksOfFire2, [], bool)
pset.addPrimitive(genome.TestEnemyWithin50TicksOfFire3, [], bool)
pset.addPrimitive(genome.TestGunIsHot, [], bool)
pset.addPrimitive(genome.TestGunWithin5Ticks, [], bool)
pset.addPrimitive(genome.TestTurnToEnemyWithin10Ticks, [], bool)
pset.addPrimitive(genome.TestTurnToEnemyWithin5Ticks, [], bool)

#logical
pset.addPrimitive(genome.Or, [bool, bool, NoneType], NoneType)
pset.addPrimitive(genome.And, [bool, bool, NoneType], NoneType)
pset.addPrimitive(genome.Not, [bool, NoneType], NoneType)
pset.addPrimitive(genome.If_Then, [bool, NoneType], NoneType)

#"plugs" the holes at the end. Is this what we want?
pset.addTerminal(True, bool)
pset.addTerminal(False, bool)
pset.addTerminal(None, NoneType)

counter = 0
#begin genetic programming
#initialize population
creator.create("FitnessMax", base.Fitness, weights=(1.0,))
creator.create("Individual", gp.PrimitiveTree, fitness=creator.FitnessMax, pset=pset)

toolbox = base.Toolbox()
toolbox.register("expr", gp.genGrow, pset=pset, min_=10, max_=15) #the min and max depth for leaves? #genGrow or genFull?
toolbox.register("individual", tools.initIterate, creator.Individual, toolbox.expr)
toolbox.register("population", tools.initRepeat, list, toolbox.individual)

def writeGenome(filename, individual):
    outputFile = open(filename, 'w')
    outputFile.write(str(individual))
    outputFile.close()

def readScores(filename):
    results = {}
    with open(filename, 'r') as resultsFile:
        scores = resultsFile.readlines()[2:]
        for scoreLine in scores:
            place, robotName, score = scoreLine.split()[:3]
            results[robotName] = float(score)
    print(results)
    return results

def evalRobot(individual):
    # Transform the tree expression in a callable function
    #func = toolbox.compile(expr=individual)
    writeGenome("genome.txt", individual)
    #print(individual)
    #print("_________________________")
    global counter
    counter += 1
    robocodeCommand = "java -Xmx512M -DNOSECURITY=true -Dsun.io.useCanonCaches=false -cp C:/Users/sam/Desktop/Github/RoboCode/libs/robocode.jar robocode.Robocode -battle C:/Users/sam/Desktop/Github/GeneticRobocode/battles/MyBattle.battle -nodisplay -results results.txt"
    subprocess.call(robocodeCommand)
    scores = readScores("results.txt")
    result = scores["genetic.Genetic*"] - scores["sample.Tracker"]
    #print("______" + result)
    #result = 1.0 #call robocode
    return result,

#genetic parameters
toolbox.register("evaluate", evalRobot)
toolbox.register("select", tools.selTournament, tournsize=3)
toolbox.register("mate", gp.cxOnePoint)
toolbox.register("expr_mut", gp.genFull, min_=5, max_=10) #the min and max depth of new mutations?
toolbox.register("mutate", gp.mutUniform, expr=toolbox.expr_mut, pset=pset)

popSize = 50
numGens = 20

#random.seed(10)
pop = toolbox.population(n=popSize)
hof = tools.HallOfFame(1)
stats = tools.Statistics(lambda ind: ind.fitness.values)
#stats.register("avg", numpy.mean)
#stats.register("std", numpy.std)
#stats.register("min", numpy.min)
#stats.register("max", numpy.max)

algorithms.eaSimple(pop, toolbox, 0.5, 0.2, numGens, stats, halloffame=hof)
print(counter)
#print the very best
print(hof[0])
writeGenome("finalGenome.txt", hof[0])