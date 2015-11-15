import genome

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
