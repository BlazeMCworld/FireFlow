package de.blazemcworld.fireflow;

import de.blazemcworld.fireflow.compiler.*;
import de.blazemcworld.fireflow.compiler.instruction.Instruction;
import de.blazemcworld.fireflow.node.NodeInput;
import de.blazemcworld.fireflow.node.NodeOutput;
import de.blazemcworld.fireflow.node.impl.AddNumbersNode;
import de.blazemcworld.fireflow.node.impl.struct.StructNode;
import de.blazemcworld.fireflow.node.impl.WhileNode;
import de.blazemcworld.fireflow.node.impl.lists.ListAppendNode;
import de.blazemcworld.fireflow.node.impl.struct.UnpackStructNode;
import de.blazemcworld.fireflow.node.impl.variable.GetVariableNode;
import de.blazemcworld.fireflow.node.impl.variable.LocalVariableScope;
import de.blazemcworld.fireflow.node.impl.variable.SetVariableNode;
import de.blazemcworld.fireflow.value.*;
import it.unimi.dsi.fastutil.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class CompileTest {

    public static Instruction listTest() {
        SetVariableNode initList = new SetVariableNode(LocalVariableScope.INSTANCE, ListValue.get(NumberValue.INSTANCE));
        Instruction entry = initList.inputs.get(0 /*Signal*/);
        initList.inputs.get(1 /*Name*/).inset("list");
        // Value omitted, will default to a new empty list

        GetVariableNode getCounter = new GetVariableNode(LocalVariableScope.INSTANCE, NumberValue.INSTANCE);
        getCounter.inputs.getFirst(/*Name*/).inset("counter");

        AddNumbersNode addCounter = new AddNumbersNode();
        addCounter.inputs.get(0 /*Left*/).connectValue(getCounter.outputs.getFirst(/*Value*/));
        addCounter.inputs.get(1 /*Right*/).inset(1);

        WhileNode repeat = new WhileNode();
        initList.outputs.getFirst(/*Next*/).connectSignal(repeat.inputs.getFirst(/*Signal*/));
        repeat.inputs.get(1 /*Condition*/).inset(true);

        SetVariableNode increaseCounter = new SetVariableNode(LocalVariableScope.INSTANCE, NumberValue.INSTANCE);
        repeat.outputs.getFirst(/*Loop*/).connectSignal(increaseCounter.inputs.getFirst(/*Signal*/));
        increaseCounter.inputs.get(1 /*Name*/).inset("counter");
        increaseCounter.inputs.get(2 /*Value*/).connectValue(addCounter.outputs.getFirst(/*Result*/));

        GetVariableNode getList = new GetVariableNode(LocalVariableScope.INSTANCE, ListValue.get(NumberValue.INSTANCE));
        getList.inputs.getFirst(/*Name*/).inset("list");

        ListAppendNode appender = new ListAppendNode(NumberValue.INSTANCE);
        increaseCounter.outputs.getFirst(/*Next*/).connectSignal(appender.inputs.get(0 /*Signal*/));
        appender.inputs.get(1 /*List*/).connectValue(getList.outputs.getFirst(/*Value*/));
        appender.inputs.get(2 /*Value*/).connectValue(getCounter.outputs.getFirst(/*Value*/));
        return entry;
    }

    public static Instruction tripleAdderFnTest() {
        FunctionDefinition definition = new FunctionDefinition(
                "Triple Adder", List.of(
                new NodeOutput("A", NumberValue.INSTANCE),
                new NodeOutput("B", NumberValue.INSTANCE),
                new NodeOutput("C", NumberValue.INSTANCE)
        ), List.of(
                new NodeInput("Result", NumberValue.INSTANCE)
        ));

        AddNumbersNode firstAdder = new AddNumbersNode();
        firstAdder.inputs.get(0).connectValue(definition.fnInputs.get(0));
        firstAdder.inputs.get(1).connectValue(definition.fnInputs.get(1));

        AddNumbersNode secondAdder = new AddNumbersNode();
        secondAdder.inputs.get(0).connectValue(firstAdder.outputs.getFirst());
        secondAdder.inputs.get(1).connectValue(definition.fnInputs.get(2));

        definition.fnOutputs.getFirst().connectValue(secondAdder.outputs.getFirst());

        FunctionDefinition.Call one = definition.createCall();
        one.inputs.get(0).inset(1);
        one.inputs.get(1).inset(2);
        one.inputs.get(2).inset(3);

        FunctionDefinition.Call two = definition.createCall();
        two.inputs.get(0).inset(4);
        two.inputs.get(1).inset(5);
        two.inputs.get(2).inset(6);

        SetVariableNode saveOne = new SetVariableNode(LocalVariableScope.INSTANCE, NumberValue.INSTANCE);
        Instruction entry = saveOne.inputs.getFirst();
        saveOne.inputs.get(1).inset("one");
        saveOne.inputs.get(2).connectValue(one.outputs.getFirst());

        SetVariableNode saveTwo = new SetVariableNode(LocalVariableScope.INSTANCE, NumberValue.INSTANCE);
        saveOne.outputs.getFirst().connectSignal(saveTwo.inputs.getFirst());
        saveTwo.inputs.get(1).inset("two");
        saveTwo.inputs.get(2).connectValue(two.outputs.getFirst());

        return entry;
    }

    public static Instruction incVarTest() {
        FunctionDefinition definition = new FunctionDefinition(
                "Increment Variable", List.of(
                new NodeOutput("Signal", SignalValue.INSTANCE),
                new NodeOutput("Name", TextValue.INSTANCE)
        ), List.of(
                new NodeInput("Next", SignalValue.INSTANCE)
        ));

        GetVariableNode getCurrent = new GetVariableNode(LocalVariableScope.INSTANCE, NumberValue.INSTANCE);
        getCurrent.inputs.getFirst().connectValue(definition.fnInputs.get(1));

        AddNumbersNode addOne = new AddNumbersNode();
        addOne.inputs.get(0).connectValue(getCurrent.outputs.getFirst());
        addOne.inputs.get(1).inset(1);

        SetVariableNode saveChange = new SetVariableNode(LocalVariableScope.INSTANCE, NumberValue.INSTANCE);
        definition.fnInputs.getFirst().connectSignal(saveChange.inputs.getFirst());
        saveChange.inputs.get(1).connectValue(definition.fnInputs.get(1));
        saveChange.inputs.get(2).connectValue(addOne.outputs.getFirst());
        saveChange.outputs.getFirst().connectSignal(definition.fnOutputs.getFirst());

        FunctionDefinition.Call oneInc = definition.createCall();
        Instruction entry = oneInc.inputs.get(0);
        oneInc.inputs.get(1).inset("one");

        FunctionDefinition.Call firstTwoInc = definition.createCall();
        oneInc.outputs.getFirst().connectSignal(firstTwoInc.inputs.getFirst());
        firstTwoInc.inputs.get(1).inset("two");

        FunctionDefinition.Call secondTwoInc = definition.createCall();
        firstTwoInc.outputs.getFirst().connectSignal(secondTwoInc.inputs.getFirst());
        secondTwoInc.inputs.get(1).inset("two");

        return entry;
    }

    public static Instruction structTest() {
        StructValue structType = StructValue.get("moner", List.of(
                Pair.of("aaa", TextValue.INSTANCE),
                Pair.of("bbb", NumberValue.INSTANCE),
                Pair.of("ccc", NumberValue.INSTANCE)
        ));

        FunctionDefinition doSomethingWithMoner = new FunctionDefinition("doSomethingWithMoner", List.of(
                new NodeOutput("moner", structType),
                new NodeOutput("ddd", NumberValue.INSTANCE)
        ), List.of(
                new NodeInput("newMoner", structType)
        ));

        UnpackStructNode unpacked = new UnpackStructNode(structType);
        unpacked.inputs.getFirst().connectValue(doSomethingWithMoner.fnInputs.getFirst());

        AddNumbersNode adder = new AddNumbersNode();
        adder.inputs.getFirst().connectValue(unpacked.outputs.get(1));
        adder.inputs.get(1).connectValue(unpacked.outputs.get(2));

        StructNode.CreateNode newStructNode = StructNode.get(structType).newCreateNode();
        newStructNode.inputs.getFirst().connectValue(unpacked.outputs.getFirst());
        newStructNode.inputs.get(1).connectValue(adder.outputs.getFirst());
        newStructNode.inputs.get(2).connectValue(doSomethingWithMoner.fnInputs.get(1));

        doSomethingWithMoner.fnOutputs.getFirst().connectValue(newStructNode.getOut());

        StructNode.CreateNode structNode = StructNode.get(structType).newCreateNode();
        structNode.inputs.getFirst().inset("bruh");
        structNode.inputs.get(1).inset(77);
        structNode.inputs.get(2).inset(88);

        SetVariableNode initStructVar = new SetVariableNode(LocalVariableScope.INSTANCE, structType);
        NodeInput entry = initStructVar.inputs.getFirst();
        initStructVar.inputs.get(1).inset("myMoner");
        initStructVar.inputs.get(2).connectValue(structNode.getOut());

        FunctionDefinition.Call call = doSomethingWithMoner.createCall();
        GetVariableNode getStructVar = new GetVariableNode(LocalVariableScope.INSTANCE, structType);
        getStructVar.inputs.getFirst().inset("myMoner");
        call.inputs.getFirst().connectValue(getStructVar.outputs.getFirst());
        call.inputs.get(1).inset(99);

        SetVariableNode resetStructVar = new SetVariableNode(LocalVariableScope.INSTANCE, structType);
        resetStructVar.inputs.get(1).inset("myMoner");
        resetStructVar.inputs.get(2).connectValue(call.outputs.getFirst());

        initStructVar.outputs.getFirst().connectSignal(resetStructVar.inputs.getFirst());
        return entry;
    }

    public static void main(String[] args) {
        NodeCompiler compiler = new NodeCompiler("Something");
        Instruction entry = structTest();

        compiler.prepare(entry);
        String entrypoint = compiler.markRoot(entry);

        byte[] bytes = compiler.compile();
        /*
        try (var stream = new java.io.FileOutputStream("Something.class")) {
            stream.write(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
         */


        ByteClassLoader loader = new ByteClassLoader(NodeCompiler.class.getClassLoader());
        Class<?> c = loader.define(compiler.className, bytes);

        try {
            CompiledNode inst = (CompiledNode) c.getDeclaredConstructor().newInstance();

            //inst.setCpu(10_000_000); TODO: add option for tests, or remove CompileTest
            try {
                c.getDeclaredMethod(entrypoint).invoke(inst);
            } catch (InvocationTargetException invoke) {
                if (invoke.getTargetException() instanceof CpuLimitException) {
                    System.out.println("Reached cpu limit!");
                } else {
                    System.err.println("Threw exception!");
                    invoke.getTargetException().printStackTrace(System.err);
                }
            }

            System.out.println(inst.locals);
        } catch (Exception err) {
            err.printStackTrace(System.err);
        }
    }
}
