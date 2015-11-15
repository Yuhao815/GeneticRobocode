import genome

import numpy

from deap import algorithms
from deap import base
from deap import creator
from deap import tools
from deap import gp

pset = gp.PrimitiveSetTyped("main", [], types.NoneType)

#logical
pset.addPrimitive(Or, [bool, bool, types.NoneType], types.NoneType)
pset.addPrimitive(And, [bool, bool, types.NoneType], types.NoneType)
pset.addPrimitive(Not, [bool, types.NoneType], types.NoneType)
pset.addPrimitive(If_Then, [bool, types.NoneType], types.NoneType)

#actions
pset.addTerminal(TurnGunToEnemy, types.NoneType)
pset.addTerminal(TurnLeft5, types.NoneType)
pset.addTerminal(TurnLeft10, types.NoneType)
pset.addTerminal(TurnRight5, types.NoneType)
pset.addTerminal(TurnRight10, types.NoneType)
pset.addTerminal(Fire1, types.NoneType)
pset.addTerminal(Fire2, types.NoneType)
pset.addTerminal(Fire3,types.NoneType)

#tests
pset.addTerminal(TestEnemyEnergy, bool)
pset.addTerminal(TestEnemyEnergy0, bool)
pset.addTerminal(TestEnemyEnergyBelow10, bool)
pset.addTerminal(TestEnergyBelow10, bool)
pset.addTerminal(TestEnergyGreaterThanEnemys, bool)
pset.addTerminal(TestEnergyLessThanEnemys, bool)
pset.addTerminal(TestEnemyWithin10Ticks, bool
pset.addTerminal(TestEnemyWithin20Ticks, bool)
pset.addTerminal(TestEnemyWithin50Ticks, bool)
pset.addTerminal(TestGunIsHot, bool)
pset.addTerminal(TestGunWithin5Ticks, bool)
pset.addTerminal(TestTurnToEnemyWithin10Ticks, bool)
pset.addTerminal(TestTurnToEnemyWithin5TIcks, bool)

#begin genetic programming
#initialize population
creator.create("FitnessMin", base.Fitness, weights=(-1.0,))
creator.create("Individual", gp.PrimitiveTree, fitness=creator.FitnessMin, pset=pset)

toolbox = base.Toolbox()
toolbox.register("expr", gp.genFull, pset=pset, min_=1, max_=3)
toolbox.register("individual", tools.initIterate, creator.Individual, toolbox.expr)
toolbox.register("population", tools.initRepeat, list, toolbox.individual)

def evalRobot(individual):
    # Transform the tree expression in a callable function
    #func = toolbox.compile(expr=individual)
    
    result = 0 #call robocode
    return result

#genetic parameters
toolbox.register("evaluate", evalRobot)
toolbox.register("select", tools.selTournament, tournsize=3)
toolbox.register("mate", gp.cxOnePoint)
toolbox.register("expr_mut", gp.genFull, min_=0, max_=2)
toolbox.register("mutate", gp.mutUniform, expr=toolbox.expr_mut, pset=pset)

def main():
    random.seed(10)
    pop = toolbox.population(n=100)
    hof = tools.HallOfFame(1)
    stats = tools.Statistics(lambda ind: ind.fitness.values)
    stats.register("avg", numpy.mean)
    stats.register("std", numpy.std)
    stats.register("min", numpy.min)
    stats.register("max", numpy.max)
    
    algorithms.eaSimple(pop, toolbox, 0.5, 0.2, 40, stats, halloffame=hof)

    return pop, stats, hof