package de.blazemcworld.fireflow.code;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.Node.Varargs;
import de.blazemcworld.fireflow.code.node.impl.event.*;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionCallNode;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionDefinition;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionInputsNode;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionOutputsNode;
import de.blazemcworld.fireflow.code.widget.NodeWidget;
import de.blazemcworld.fireflow.code.widget.Widget;
import de.blazemcworld.fireflow.space.PlayWorld;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.util.DummyPlayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CodeEvaluator {

    public final Space space;
    private boolean stopped = false;
    public final VariableStore sessionVariables = new VariableStore();
    public final Set<Node> nodes;
    public final PlayWorld world;
    private final Set<Runnable> tickTasks = new HashSet<>();
    private boolean initCalled = false;

    public CodeEvaluator(Space space) {
        this.space = space;
        world = space.playWorld;

        Set<Node> nodes = new HashSet<>();
        for (Widget widget : space.editor.rootWidgets) {
            if (widget instanceof NodeWidget nodeWidget) {
                nodes.add(nodeWidget.node);
            }
        }

        this.nodes = copyNodes(nodes);
    }

    public void stop() {
        stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }

    @SuppressWarnings("unchecked")
    private Set<Node> copyNodes(Set<Node> nodes) {
        HashMap<Node, Node> old2new = new HashMap<>();

        HashMap<String, FunctionDefinition> functions = new HashMap<>();

        for (FunctionDefinition old : space.editor.functions.values()) {
            FunctionDefinition copy = new FunctionDefinition(old.name, old.icon);
            for (Node.Output<?> input : old.inputsNode.outputs) {
                copy.addInput(input.id, input.type);
            }
            for (Node.Input<?> output : old.outputsNode.inputs) {
                copy.addOutput(output.id, output.type);
            }
            functions.put(old.name, copy);
        }

        for (Node node : nodes) {
            Node copy = null;
            
            if (node instanceof FunctionCallNode call) {
                copy = new FunctionCallNode(functions.get(call.function.name));
            }

            if (node instanceof FunctionInputsNode inputsNode) {
                copy = functions.get(inputsNode.function.name).inputsNode;
            }

            if (node instanceof FunctionOutputsNode outputsNode) {
                copy = functions.get(outputsNode.function.name).outputsNode;
            }

            if (copy == null) copy = node.copy();
            
            for (Varargs<?> base : node.varargs) {
                for (Varargs<?> next : copy.varargs) {
                    if (!base.id.equals(next.id)) continue;
                    next.ignoreUpdates = true;
                    copy.inputs.removeAll(next.children);
                    next.children.clear();

                    for (Node.Input<?> input : base.children) {
                        next.addInput(input.id);
                    }
                }
            }

            old2new.put(node, copy);
        }

        for (Node old : nodes) {
            Node copy = old2new.get(old);
            for (int i = 0; i < copy.inputs.size(); i++) {
                Node.Input<?> newInput = copy.inputs.get(i);
                Node.Output<?> oldTarget = old.inputs.get(i).connected;
                if (oldTarget == null) continue;
                Node.Output<?> newTarget = old2new.get(oldTarget.getNode()).outputs.get(oldTarget.getNode().outputs.indexOf(oldTarget));
                if (newTarget == null) continue;
                ((Node.Input<Object>) newInput).connect((Node.Output<Object>) newTarget);
            }

            for (int i = 0; i < copy.outputs.size(); i++) {
                Node.Output<?> newOutput = copy.outputs.get(i);
                Node.Input<?> oldTarget = old.outputs.get(i).connected;
                if (oldTarget == null) continue;
                Node.Input<?> newTarget = old2new.get(oldTarget.getNode()).inputs.get(oldTarget.getNode().inputs.indexOf(oldTarget));
                ((Node.Output<Object>) newOutput).connected = (Node.Input<Object>) newTarget;
            }

            for (int i = 0; i < copy.inputs.size(); i++) {
                Node.Input<?> newInput = copy.inputs.get(i);
                Node.Input<?> oldInput = old.inputs.get(i);
                if (oldInput.inset == null) continue;
                newInput.setInset(oldInput.inset);
            }
        }

        return new HashSet<>(old2new.values());
    }

    public CodeThread newCodeThread() {
        return new CodeThread(this);
    }

    public boolean onSwingHand(ServerPlayerEntity player, boolean isMainHand) {
        boolean cancel = false;
        for (Node node : nodes) {
            if (node instanceof OnPlayerSwingHandNode n) {
                cancel = n.onSwingHand(this, player, isMainHand, cancel);
            }
        }
        return cancel;
    }

    public boolean onSwapHands(ServerPlayerEntity player) {
        boolean cancel = false;
        for (Node node : nodes) {
            if (node instanceof OnPlayerSwapHandsNode n) {
                cancel = n.onSwapHands(this, player, cancel);
            }
        }
        return cancel;
    }

    public void tick() {
        if (stopped) return;
        Set<Runnable> tasks;
        synchronized (tickTasks) {
            tasks = new HashSet<>(tickTasks);
            tickTasks.clear();
        }
        for (Runnable task : tasks) task.run();
    }

    public boolean onUseItem(ServerPlayerEntity player, ItemStack stack, Hand hand) {
        boolean cancel = false;
        for (Node node : nodes) {
            if (node instanceof OnPlayerUseItemNode n) {
                cancel = n.onUseItem(this, player, stack, hand, cancel);
            }
        }
        return cancel;
    }

    public void exitPlay(ServerPlayerEntity player) {
        for (Node node : nodes) {
            if (node instanceof OnPlayerLeaveNode n) {
                n.onLeave(this, player);
            }
        }
        if (player instanceof DummyPlayer) {
            player.discard();
        }
    }

    public boolean onPlaceBlock(ItemPlacementContext context) {
        boolean cancel = false;
        for (Node node : nodes) {
            if (node instanceof OnPlayerPlaceBlockNode n) {
                cancel = n.onPlaceBlock(this, context, cancel);
            }
        }
        return cancel;
    }

    public boolean onChat(ServerPlayerEntity player, String message) {
        boolean cancel = false;
        for (Node node : nodes) {
            if (node instanceof OnPlayerChatNode n) {
                cancel = n.onChat(this, player, message, cancel);
            }
        }
        return cancel;
    }

    public boolean onBreakBlock(ServerPlayerEntity player, BlockPos pos) {
        boolean cancel = false;
        for (Node node : nodes) {
            if (node instanceof OnPlayerBreakBlockNode n) {
                cancel = n.onBreakBlock(this, player, pos, cancel);
            }
        }
        return cancel;
    }

    public boolean onDropItem(ServerPlayerEntity player) {
        boolean cancel = false;
        for (Node node : nodes) {
            if (node instanceof OnPlayerDropItemNode n) {
                cancel = n.onDropItem(this, player, cancel);
            }
        }
        return cancel;
    }

    public boolean allowDeath(LivingEntity target, DamageSource source, float damage) {
        boolean cancel = false;

        String type = source.getTypeRegistryEntry().getKey().map(k -> k.getValue().getPath()).orElse("unknown");

        for (Node node : nodes) {
            if (node instanceof OnPlayerDeathNode n && target instanceof ServerPlayerEntity pl) {
                cancel = n.onPlayerDeath(this, pl, damage, type, cancel);
            }

            if (node instanceof OnEntityDeathNode n && !(target instanceof ServerPlayerEntity)) {
                cancel = n.onEntityDeath(this, target, damage, type, cancel);
            }

            if (node instanceof OnPlayerKillPlayerNode n && target instanceof ServerPlayerEntity victim && source.getAttacker() instanceof ServerPlayerEntity attacker) {
                cancel = n.onPlayerKillPlayer(this, attacker, victim, damage, cancel);
            }

            if (node instanceof OnPlayerKillEntityNode n && !(target instanceof ServerPlayerEntity) && source.getAttacker() instanceof ServerPlayerEntity attacker) {
                cancel = n.onPlayerKillEntity(this, attacker, target, damage, cancel);
            }

            if (node instanceof OnEntityKillPlayerNode n && target instanceof ServerPlayerEntity victim && !(source.getAttacker() instanceof ServerPlayerEntity)) {
                cancel = n.onEntityKillPlayer(this, source.getAttacker(), victim, damage, cancel);
            }

            if (node instanceof OnEntityKillEntityNode n && !(target instanceof ServerPlayerEntity) && !(source.getAttacker() instanceof ServerPlayerEntity)) {
                cancel = n.onEntityKillEntity(this, source.getAttacker(), target, damage, cancel);
            }
        }
        return !cancel;
    }

    public void nextTick(Runnable r) {
        synchronized (tickTasks) {
            tickTasks.add(r);
        }
    }

    public void onJoin(ServerPlayerEntity player) {
        if (!initCalled) {
            initCalled = true;
            world.markStarted();

            for (Node node : nodes) {
                if (node instanceof OnInitializeNode init) {
                    init.emit(this);
                }
            }
        }

        for (Node n : nodes) {
            if (n instanceof OnPlayerJoinNode join) {
                join.onJoin(this, player);
            }
        }
    }

    public boolean shouldCancelFlight(ServerPlayerEntity player, boolean enabled) {
        boolean cancel = false;
        for (Node n : nodes) {
            if (enabled && n instanceof OnPlayerStartFlyingNode fly) {
                cancel = fly.onStartFlying(this, player, cancel);
            }
            if (!enabled && n instanceof OnPlayerStopFlyingNode fly) {
                cancel = fly.onStopFlying(this, player, cancel);
            }
        }
        return cancel;
    }

    public boolean allowDamage(LivingEntity target, DamageSource source, float damage) {
        boolean cancel = false;

        String type = source.getTypeRegistryEntry().getKey().map(k -> k.getValue().getPath()).orElse("unknown");

        for (Node node : nodes) {
            if (node instanceof OnPlayerHurtNode n && target instanceof ServerPlayerEntity pl) {
                cancel = n.onPlayerHurt(this, pl, damage, type, cancel);
            }

            if (node instanceof OnEntityHurtNode n && !(target instanceof ServerPlayerEntity)) {
                cancel = n.onEntityHurt(this, target, damage, type, cancel);
            }

            if (node instanceof OnPlayerAttackPlayerNode n && target instanceof ServerPlayerEntity victim && source.getAttacker() instanceof ServerPlayerEntity attacker) {
                cancel = n.onPlayerAttackPlayer(this, attacker, victim, damage, cancel);
            }

            if (node instanceof OnPlayerAttackEntityNode n && !(target instanceof ServerPlayerEntity) && source.getAttacker() instanceof ServerPlayerEntity attacker) {
                cancel = n.onPlayerAttackEntity(this, attacker, target, damage, cancel);
            }

            if (node instanceof OnEntityAttackPlayerNode n && target instanceof ServerPlayerEntity victim && !(source.getAttacker() instanceof ServerPlayerEntity)) {
                cancel = n.onEntityAttackPlayer(this, source.getAttacker(), victim, damage, cancel);
            }

            if (node instanceof OnEntityAttackEntityNode n && !(target instanceof ServerPlayerEntity) && !(source.getAttacker() instanceof ServerPlayerEntity)) {
                cancel = n.onEntityAttackEntity(this, source.getAttacker(), target, damage, cancel);
            }
        }
        return !cancel;
    }
}
