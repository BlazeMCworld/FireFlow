package de.blazemcworld.fireflow.code;

import com.google.gson.JsonObject;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.Node.Varargs;
import de.blazemcworld.fireflow.code.node.impl.event.OnPlayerLoseFoodNode;
import de.blazemcworld.fireflow.code.node.impl.event.OnPlayerLoseSaturationNode;
import de.blazemcworld.fireflow.code.node.impl.event.action.*;
import de.blazemcworld.fireflow.code.node.impl.event.combat.OnPlayerAttackPlayerNode;
import de.blazemcworld.fireflow.code.node.impl.event.combat.OnPlayerDeathNode;
import de.blazemcworld.fireflow.code.node.impl.event.combat.OnPlayerHurtNode;
import de.blazemcworld.fireflow.code.node.impl.event.combat.OnPlayerKillPlayerNode;
import de.blazemcworld.fireflow.code.node.impl.event.combat.entity.*;
import de.blazemcworld.fireflow.code.node.impl.event.meta.*;
import de.blazemcworld.fireflow.code.node.impl.event.world.*;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionCallNode;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionDefinition;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionInputsNode;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionOutputsNode;
import de.blazemcworld.fireflow.code.widget.NodeWidget;
import de.blazemcworld.fireflow.code.widget.Widget;
import de.blazemcworld.fireflow.code.widget.WidgetVec;
import de.blazemcworld.fireflow.space.PlayLevel;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.util.DummyPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CodeEvaluator {

    public final Space space;
    private boolean stopped = false;
    public final VariableStore sessionVariables = new VariableStore();
    public Set<Node> nodes;
    public final PlayLevel level;
    private final Set<Runnable> tickTasks = new HashSet<>();
    private boolean initCalled = false;
    private int revision = 0; // Incremented after each live reload

    public CodeEvaluator(Space space) {
        this.space = space;
        level = space.playLevel;

        Set<NodeWidget> nodes = new HashSet<>();
        for (Widget widget : space.editor.rootWidgets) {
            if (widget instanceof NodeWidget nodeWidget) {
                nodes.add(nodeWidget);
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

    public Set<ServerPlayer> players() {
        return space.playersPlayMode();
    }


    private Set<Node> copyNodes(Set<NodeWidget> nodes) {
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

        for (NodeWidget nodeWidget : nodes) {
            Node node = nodeWidget.node;
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
            copy.originWidget = new WeakReference<>(nodeWidget);
            copy.evalUUID = node.evalUUID;
            copy.evalRevision = revision;
        }

        for (NodeWidget oldWidget : nodes) {
            Node old = oldWidget.node;
            Node copy = old2new.get(old);
            for (int i = 0; i < copy.inputs.size(); i++) {
                Node.Input<?> newInput = copy.inputs.get(i);
                Node.Output<?> oldTarget = old.inputs.get(i).connected;
                if (oldTarget == null) continue;
                Node.Output<?> newTarget = old2new.get(oldTarget.getNode()).outputs.get(oldTarget.getNode().outputs.indexOf(oldTarget));
                if (newTarget == null) continue;
                newInput.connect(newTarget);
            }

            for (int i = 0; i < copy.outputs.size(); i++) {
                Node.Output<?> newOutput = copy.outputs.get(i);
                Node.Input<?> oldTarget = old.outputs.get(i).connected;
                if (oldTarget == null) continue;
                newOutput.connected = old2new.get(oldTarget.getNode()).inputs.get(oldTarget.getNode().inputs.indexOf(oldTarget));
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

    public boolean onSwingHand(ServerPlayer player, boolean isMainHand) {
        boolean cancel = false;
        for (Node node : nodes) {
            if (node instanceof OnPlayerSwingHandNode n) {
                cancel = n.onSwingHand(this, player, isMainHand, cancel);
            }
        }
        return cancel;
    }

    public boolean onSwapHands(ServerPlayer player) {
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

    public boolean onInteractBlock(ServerPlayer player, BlockPos pos, Direction side, InteractionHand hand) {
        boolean cancel = false;
        for (Node node : nodes) {
            if (node instanceof OnPlayerInteractBlockNode n) {
                cancel = n.onInteractBlock(this, player, pos, side, hand, cancel);
            }
        }
        return cancel;
    }

    public boolean onUseItem(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        boolean cancel = false;
        for (Node node : nodes) {
            if (node instanceof OnPlayerUseItemNode n) {
                cancel = n.onUseItem(this, player, stack, hand, cancel);
            }
        }
        return cancel;
    }

    public void exitPlay(ServerPlayer player) {
        for (Node node : nodes) {
            if (node instanceof OnPlayerLeaveNode n) {
                n.onLeave(this, player);
            }
        }
        if (player instanceof DummyPlayer dummy && !dummy.exitCalled) {
            dummy.exitCalled = true;
            dummy.discard();
        }
    }

    public boolean onPlaceBlock(BlockPlaceContext context) {
        boolean cancel = false;
        for (Node node : nodes) {
            if (node instanceof OnPlayerPlaceBlockNode n) {
                cancel = n.onPlaceBlock(this, context, cancel);
            }
        }
        return cancel;
    }

    public boolean onChat(ServerPlayer player, String message) {
        boolean cancel = false;
        for (Node node : nodes) {
            if (node instanceof OnPlayerChatNode n) {
                cancel = n.onChat(this, player, message, cancel);
            }
        }
        return cancel;
    }

    public boolean onBreakBlock(ServerPlayer player, BlockPos pos) {
        boolean cancel = false;
        for (Node node : nodes) {
            if (node instanceof OnPlayerBreakBlockNode n) {
                cancel = n.onBreakBlock(this, player, pos, cancel);
            }
        }
        return cancel;
    }

    public boolean onDropItem(ServerPlayer player) {
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

        String type = source.typeHolder().unwrapKey().map(k -> k.identifier().getPath()).orElse("unknown");

        for (Node node : nodes) {
            if (node instanceof OnPlayerDeathNode n && target instanceof ServerPlayer pl) {
                cancel = n.onPlayerDeath(this, pl, damage, type, cancel);
            }

            if (node instanceof OnEntityDeathNode n && !(target instanceof ServerPlayer)) {
                cancel = n.onEntityDeath(this, target, damage, type, cancel);
            }

            if (node instanceof OnPlayerKillPlayerNode n && target instanceof ServerPlayer victim && source.getEntity() instanceof ServerPlayer attacker) {
                cancel = n.onPlayerKillPlayer(this, attacker, victim, damage, cancel);
            }

            if (node instanceof OnPlayerKillEntityNode n && !(target instanceof ServerPlayer) && source.getEntity() instanceof ServerPlayer attacker) {
                cancel = n.onPlayerKillEntity(this, attacker, target, damage, cancel);
            }

            if (node instanceof OnEntityKillPlayerNode n && target instanceof ServerPlayer victim && !(source.getEntity() instanceof ServerPlayer)) {
                cancel = n.onEntityKillPlayer(this, source.getEntity(), victim, damage, cancel);
            }

            if (node instanceof OnEntityKillEntityNode n && !(target instanceof ServerPlayer) && !(source.getEntity() instanceof ServerPlayer)) {
                cancel = n.onEntityKillEntity(this, source.getEntity(), target, damage, cancel);
            }
        }
        return !cancel;
    }

    public void nextTick(Runnable r) {
        synchronized (tickTasks) {
            tickTasks.add(r);
        }
    }

    private void ensureInit() {
        if (!initCalled) {
            initCalled = true;
            level.markStarted();

            for (Node node : nodes) {
                if (node instanceof OnInitializeNode init) {
                    init.emit(this);
                }
            }
        }
    }

    public void onJoin(ServerPlayer player) {
        for (Node n : nodes) {
            if (n instanceof OnPlayerJoinNode join) {
                join.onJoin(this, player);
            }
        }
    }

    public boolean shouldCancelFlight(ServerPlayer player, boolean enabled) {
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

    public float adjustDamage(LivingEntity target, DamageSource source, float damage) {
        String type = source.typeHolder().unwrapKey().map(k -> k.identifier().getPath()).orElse("unknown");
        CodeThread.EventContext ctx = new CodeThread.EventContext(CodeThread.EventType.DAMAGE_EVENT);
        ctx.eventNumber = damage;

        for (Node node : nodes) {
            if (node instanceof OnPlayerHurtNode n && target instanceof ServerPlayer pl) {
                n.onPlayerHurt(this, pl, damage, type, ctx);
            }

            if (node instanceof OnEntityHurtNode n && !(target instanceof ServerPlayer)) {
                n.onEntityHurt(this, target, damage, type, ctx);
            }

            if (node instanceof OnPlayerAttackPlayerNode n && target instanceof ServerPlayer victim && source.getEntity() instanceof ServerPlayer attacker) {
                n.onPlayerAttackPlayer(this, attacker, victim, damage, ctx);
            }

            if (node instanceof OnPlayerAttackEntityNode n && !(target instanceof ServerPlayer) && source.getEntity() instanceof ServerPlayer attacker) {
                n.onPlayerAttackEntity(this, attacker, target, damage, ctx);
            }

            if (node instanceof OnEntityAttackPlayerNode n && target instanceof ServerPlayer victim && !(source.getEntity() instanceof ServerPlayer)) {
                n.onEntityAttackPlayer(this, source.getEntity(), victim, damage, ctx);
            }

            if (node instanceof OnEntityAttackEntityNode n && !(target instanceof ServerPlayer) && !(source.getEntity() instanceof ServerPlayer)) {
                n.onEntityAttackEntity(this, source.getEntity(), target, damage, ctx);
            }
        }

        if (ctx.cancelled) return -1;
        return (float) ctx.eventNumber;
    }

    public void onChunkLoad(int x, int z) {
        ensureInit();
        for (Node node : nodes) {
            if (node instanceof OnChunkLoadNode onChunkLoadNode) {
                onChunkLoadNode.emit(this, x, z);
            }
        }
    }

    public void triggerDebug(String id, EditOrigin origin) {
        ensureInit();
        boolean found = false;
        for (Node node : nodes) {
            if (node instanceof DebugEventNode debugEventNode) {
                found = debugEventNode.trigger(this, id) || found;
            }
        }
        if (!found) origin.sendError("No debug event with id '" + id + "' found.");
    }

    public void visualizeDebug(Node node) {
        space.editor.nextTick(() -> {
            NodeWidget widget = node.originWidget.get();
            if (widget == null) return;

            WidgetVec pos = widget.pos();
            ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(
                    new DustParticleOptions(0xFFFF00, 1f),
                    false, false, pos.x(), pos.y(), 15.9,
                    0, 0, 0, 0, 1
            );

            for (ServerPlayer player : space.editor.level.players()) {
                player.connection.send(packet);
            }

            JsonObject json = new JsonObject();
            json.addProperty("type", "debug");
            json.addProperty("x", pos.x());
            json.addProperty("y", pos.y());
            space.editor.webBroadcast(json);
        });
    }

    public void liveReload() {
        nextTick(() -> {
            revision++;
            Set<NodeWidget> widgets = new HashSet<>();
            for (Widget widget : space.editor.rootWidgets) {
                if (widget instanceof NodeWidget nodeWidget) {
                    widgets.add(nodeWidget);
                }
            }
            this.nodes = copyNodes(widgets);
        });
    }

    public void syncRevision(Node old) {
        if (old.evalRevision == revision) return;

        for (Node current : nodes) {
            if (current.originWidget.get() != old.originWidget.get()) continue;
            if (old.originWidget.get() == null) continue;

            for (Node.Varargs<?> oldVarargs : old.varargs) {
                oldVarargs.ignoreUpdates = true;

                for (Node.Varargs<?> currentVarargs : current.varargs) {
                    if (!oldVarargs.id.equals(currentVarargs.id)) continue;
                    old.inputs.removeAll(oldVarargs.children);
                    oldVarargs.children.clear();

                    for (Node.Input<?> input : currentVarargs.children) {
                        oldVarargs.addInput(input.id);
                    }
                }
            }

            for (Node.Input<?> oldInput : old.inputs) {
                for (Node.Input<?> currentInput : current.inputs) {
                    if (!oldInput.id.equals(currentInput.id)) continue;

                    oldInput.connected = currentInput.connected;
                    oldInput.inset = currentInput.inset;
                    break;
                }
            }

            for (Node.Output<?> oldOutput : old.outputs) {
                for (Node.Output<?> currentOutput : current.outputs) {
                    if (!oldOutput.id.equals(currentOutput.id)) continue;

                    oldOutput.connected = currentOutput.connected;
                    break;
                }
            }

            for (Node.Varargs<?> oldVarargs : old.varargs) {
                oldVarargs.ignoreUpdates = false;
            }
            break;
        }

        old.evalRevision = revision;
    }

    public boolean onLoseFood(ServerPlayer player, int oldValue, int newValue) {
        CodeThread.EventContext ctx = new CodeThread.EventContext(CodeThread.EventType.UNSPECIFIED);

        for (Node node : nodes) {
            if (node instanceof OnPlayerLoseFoodNode onLoseFoodNode) {
                onLoseFoodNode.emit(this, player, oldValue, newValue, ctx);
            }
        }

        return ctx.cancelled;
    }

    public boolean onLoseSaturation(ServerPlayer player, float oldValue, float newValue) {
        CodeThread.EventContext ctx = new CodeThread.EventContext(CodeThread.EventType.UNSPECIFIED);

        for (Node node : nodes) {
            if (node instanceof OnPlayerLoseSaturationNode onLoseSaturationNode) {
                onLoseSaturationNode.emit(this, player, oldValue, newValue, ctx);
            }
        }

        return ctx.cancelled;
    }

    public void onStartSneaking(ServerPlayer player) {
        for (Node node : nodes) {
            if (node instanceof OnPlayerStartSneakingNode sneak) {
                sneak.emit(this, player);
            }
        }
    }

    public void onStopSneaking(ServerPlayer player) {
        for (Node node : nodes) {
            if (node instanceof OnPlayerStopSneakingNode sneak) {
                sneak.emit(this, player);
            }
        }
    }

    public void onStartSprinting(ServerPlayer player) {
        for (Node node : nodes) {
            if (node instanceof OnPlayerStartSprintingNode sprint) {
                sprint.emit(this, player);
            }
        }
    }

    public void onStopSprinting(ServerPlayer player) {
        for (Node node : nodes) {
            if (node instanceof OnPlayerStopSprintingNode sprint) {
                sprint.emit(this, player);
            }
        }
    }
}
