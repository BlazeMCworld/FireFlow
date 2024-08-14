package de.blazemcworld.fireflow.node;

import de.blazemcworld.fireflow.compiler.CompiledNode;
import de.blazemcworld.fireflow.compiler.NodeCompiler;
import de.blazemcworld.fireflow.compiler.StructDefinition;
import de.blazemcworld.fireflow.compiler.instruction.Instruction;
import de.blazemcworld.fireflow.compiler.instruction.MultiInstruction;
import de.blazemcworld.fireflow.compiler.instruction.RawInstruction;
import de.blazemcworld.fireflow.evaluation.CodeEvaluator;
import de.blazemcworld.fireflow.node.annotation.*;
import de.blazemcworld.fireflow.value.AllValues;
import de.blazemcworld.fireflow.value.SignalValue;
import de.blazemcworld.fireflow.value.Value;
import net.minestom.server.network.NetworkBuffer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Node {

    public List<NodeInput> inputs = new ArrayList<>();
    public List<NodeOutput> outputs = new ArrayList<>();
    public final String name;

    public Node(String name) {
        this.name = name;
    }

    protected NodeInput input(String name, Value value) {
        NodeInput input = new NodeInput(name, value);
        inputs.add(input);
        return input;
    }

    protected NodeOutput output(String name, Value value) {
        NodeOutput output = new NodeOutput(name, value);
        outputs.add(output);
        return output;
    }

    public void register(CodeEvaluator evaluator) {}

    protected static long idCounter = 0;
    private final Map<String, String> myIds = new HashMap<>();

    public String allocateId(String generalId) {
        return myIds.computeIfAbsent(generalId, _id -> Long.toHexString(idCounter++));
    }

    public String getBaseName() {
        return name;
    }

    public void writeData(NetworkBuffer buffer) {
        List<Value> generics = generics();
        buffer.write(NetworkBuffer.INT, generics.size());
        for (Value generic : generics) {
            AllValues.writeValue(buffer, generic);
        }

        List<NodeInput> insetted = new ArrayList<>();
        for (NodeInput input : inputs) {
            if (input.getInset() != null) insetted.add(input);
        }
        buffer.write(NetworkBuffer.INT, insetted.size());
        for (NodeInput input : insetted) {
            buffer.write(NetworkBuffer.INT, inputs.indexOf(input));
            input.type.writeInset(buffer, input.getInset());
        }
    }

    public Node readData(NetworkBuffer buffer, List<StructDefinition> structs) {
        int genericsSize = buffer.read(NetworkBuffer.INT);
        List<Value> generics = new ArrayList<>();
        for (int i = 0; i < genericsSize; i++) {
            generics.add(AllValues.readValue(buffer, structs));
        }

        Node target = fromGenerics(generics);

        int size = buffer.read(NetworkBuffer.INT);
        for (int i = 0; i < size; i++) {
            int index = buffer.read(NetworkBuffer.INT);
            target.inputs.get(index).inset(target.inputs.get(index).type.readInset(buffer));
        }

        return target;
    }

    public List<Value> generics() {
        return List.of();
    }

    public List<Value.GenericParam> possibleGenerics(List<StructDefinition> structs) {
        return List.of();
    }

    public Node fromGenerics(List<Value> generics) {
        return this;
    }

    protected void loadJava(Class<?> clazz) {
        try {
            ClassReader reader = new ClassReader(clazz.getName());
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(FlowSignalInput.class)) {
                    String name = m.getAnnotation(FlowSignalInput.class).value();
                    for (NodeInput input : inputs) {
                        if (!input.getName().equals(name) || input.type != SignalValue.INSTANCE) continue;

                        input.setInstruction(convertJava(classNode, clazz, m));
                    }
                    continue;
                }
                if (m.isAnnotationPresent(FlowValueOutput.class)) {
                    String name = m.getAnnotation(FlowValueOutput.class).value();
                    for (NodeOutput output : outputs) {
                        if (!output.getName().equals(name) || output.type == SignalValue.INSTANCE) continue;

                        Instruction insn = convertJava(classNode, clazz, m);
                        if (m.getReturnType() == Object.class) {
                            insn = output.type.cast(insn);
                        }
                        output.setInstruction(new MultiInstruction(output.type.getType(), insn));
                    }
                }
            }
        } catch (Exception err) {
            if (err instanceof RuntimeException r) throw r;
            throw new RuntimeException(err);
        }
    }

    private Instruction convertJava(ClassNode classNode, Class<?> clazz, Method m) {
        String desc = Type.getType(m).getDescriptor();
        for (MethodNode mNode : classNode.methods) {
            if (!mNode.name.equals(m.getName()) || !mNode.desc.equals(desc)) continue;

            List<Instruction> all = new ArrayList<>();

            int returnCount = 0;
            int maxVar = -1;
            update:
            for (AbstractInsnNode insn : mNode.instructions) {
                if (insn instanceof MethodInsnNode invoke && invoke.owner.equals(classNode.name)) {
                    for (Method other : clazz.getDeclaredMethods()) {
                        if (!other.getName().equals(invoke.name) || !Type.getType(other).getDescriptor().equals(invoke.desc)) continue;
                        if (other.isAnnotationPresent(FlowSignalOutput.class)) {
                            String outputName = other.getAnnotation(FlowSignalOutput.class).value();
                            for (NodeOutput output : outputs) {
                                if (!output.getName().equals(outputName)) continue;
                                all.add(output);
                                continue update;
                            }
                        }
                        if (other.isAnnotationPresent(FlowValueInput.class)) {
                            String inputName = other.getAnnotation(FlowValueInput.class).value();
                            for (NodeInput input : inputs) {
                                if (!input.getName().equals(inputName)) continue;
                                if (other.getReturnType() == Object.class) {
                                    all.add(input.type.wrapPrimitive(input));
                                } else {
                                    all.add(input);
                                }
                                continue update;
                            }
                        }
                        if (other.isAnnotationPresent(FlowContext.class)) {
                            all.add(new RawInstruction(Type.getType(CompiledNode.class), new VarInsnNode(Opcodes.ALOAD, 0)));
                            continue update;
                        }
                    }
                }
                if (insn instanceof VarInsnNode v) {
                    int which = v.var;
                    maxVar = Math.max(maxVar, which);
                    all.add(new Instruction() {
                        @Override
                        public void prepare(NodeCompiler ctx) {}

                        @Override
                        public InsnList compile(NodeCompiler ctx, int usedVars) {
                            InsnList out = new InsnList();
                            v.var = which + usedVars;
                            out.add(v);
                            return out;
                        }

                        @Override
                        public Type returnType() {
                            return null;
                        }
                    });
                    continue;
                }
                if (insn instanceof IincInsnNode v) {
                    int which = v.var;
                    maxVar = Math.max(maxVar, which);
                    all.add(new Instruction() {
                        @Override
                        public void prepare(NodeCompiler ctx) {}

                        @Override
                        public InsnList compile(NodeCompiler ctx, int usedVars) {
                            InsnList out = new InsnList();
                            v.var = which + usedVars;
                            out.add(v);
                            return out;
                        }

                        @Override
                        public Type returnType() {
                            return null;
                        }
                    });
                    continue;
                }
                if (insn.getOpcode() >= Opcodes.IRETURN && insn.getOpcode() <= Opcodes.RETURN) {
                    returnCount++;
                    continue;
                }
                if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof String str && str.startsWith("ID$")) {
                    ldc.cst = allocateId(str.substring(3));
                }

                if (insn instanceof FrameNode || insn instanceof LineNumberNode) continue;

                all.add(new RawInstruction(null, insn));
            }

            if (returnCount > 1) {
                throw new RuntimeException("Multiple returns are not allowed!");
            }

            int reserved = maxVar + 1;
            return new Instruction() {
                @Override
                public void prepare(NodeCompiler ctx) {
                    for (Instruction i : all) {
                        ctx.prepare(i);
                    }
                }

                @Override
                public InsnList compile(NodeCompiler ctx, int usedVars) {
                    InsnList out = new InsnList();
                    for (Instruction i : all) {
                        out.add(i.compile(ctx, usedVars + reserved));
                    }
                    return out;
                }

                @Override
                public Type returnType() {
                    return Type.VOID_TYPE;
                }
            };
        }
        throw new RuntimeException("Failed to find method " + m + " in asm!");
    }
}
