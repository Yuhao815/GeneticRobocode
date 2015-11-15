def TurnGunToEnemy():

def TurnGunLeft5():

def TurnGunLeft10():

def TurnGunRight5():

def TurnGunRight10():

def Fire1():

def Fire2():

def Fire3():

#Tests
def TestEnemyEnergy():

def TestEnemyEnergy0():

def TestEnemyEnergyBelow10():

def TestEnergyBelow10():

def TestEnergyGreaterThanEnemys():

def TestEnergyLessThanEnemys():

def TestEnemyWithin10Ticks():

def TestEnemyWithin20Ticks():

def TestEnemyWithin50Ticks():

def TestGunIsHot():

def TestGunWithin5Ticks():

def TestTurnToEnemyWithin10Ticks():

def TestTurnToEnemyWithin5tTcks():



def TurnRight10():

def Or(test1, test2, action): #returns bool

def And(test1, test2, antion):

def Not(test, action):

def If_Then(test, action):







pset = PrimitiveSetTyped("main", [], types.NoneType)

#logical
pset.addPrimitive("Or", [bool, bool, types.FunctionType], types.FunctionType)
pset.addPrimitive("And", [bool, bool, types.FunctionType], types.FunctionType)
pset.addPrimitive("Not", [bool, types.FunctionType], types.FunctionType)
pset.addPrimitive("If_Then", [bool, types.FunctionType], types.FunctionType)

#actions
pset.addPrimitive(TurnGunToEnemy, [], types.NoneType)
pset.addPrimitive(TurnLeft5, [], types.NoneType)
pset.addPrimitive(TurnLeft10, [], types.NoneType)
pset.addPrimitive(TurnRight5, [], types.NoneType)
pset.addPrimitive(TurnRight10, [], types.NoneType)
pset.addPrimitive(Fire1, [], types.NoneType)
pset.addPrimitive(Fire2, [], types.NoneType)
pset.addPrimitive(Fire3, [], types.NoneType)

#tests
