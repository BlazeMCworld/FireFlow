package de.blazemcworld.fireflow.code.node;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.CodeEditor;
import de.blazemcworld.fireflow.code.node.impl.condition.*;
import de.blazemcworld.fireflow.code.node.impl.control.*;
import de.blazemcworld.fireflow.code.node.impl.dictionary.*;
import de.blazemcworld.fireflow.code.node.impl.entity.*;
import de.blazemcworld.fireflow.code.node.impl.event.OnPlayerLoseFoodNode;
import de.blazemcworld.fireflow.code.node.impl.event.OnPlayerLoseSaturationNode;
import de.blazemcworld.fireflow.code.node.impl.event.action.*;
import de.blazemcworld.fireflow.code.node.impl.event.combat.*;
import de.blazemcworld.fireflow.code.node.impl.event.combat.entity.*;
import de.blazemcworld.fireflow.code.node.impl.event.meta.*;
import de.blazemcworld.fireflow.code.node.impl.event.world.*;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionCallNode;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionDefinition;
import de.blazemcworld.fireflow.code.node.impl.event.action.OnPlayerStartSneakingNode;
import de.blazemcworld.fireflow.code.node.impl.item.*;
import de.blazemcworld.fireflow.code.node.impl.list.*;
import de.blazemcworld.fireflow.code.node.impl.number.*;
import de.blazemcworld.fireflow.code.node.impl.player.gameplay.*;
import de.blazemcworld.fireflow.code.node.impl.player.inventory.*;
import de.blazemcworld.fireflow.code.node.impl.player.meta.*;
import de.blazemcworld.fireflow.code.node.impl.player.movement.*;
import de.blazemcworld.fireflow.code.node.impl.player.statistic.*;
import de.blazemcworld.fireflow.code.node.impl.player.visual.*;
import de.blazemcworld.fireflow.code.node.impl.position.*;
import de.blazemcworld.fireflow.code.node.impl.string.*;
import de.blazemcworld.fireflow.code.node.impl.text.CombineTextsNode;
import de.blazemcworld.fireflow.code.node.impl.text.FormatToTextNode;
import de.blazemcworld.fireflow.code.node.impl.variable.*;
import de.blazemcworld.fireflow.code.node.impl.vector.*;
import de.blazemcworld.fireflow.code.node.impl.world.*;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class NodeList {

    public static Category root;

    public static void init() {
        root = new Category("All", null)
                .add(new Category("Condition", Items.COMPARATOR)
                        .add(new ConditionAndNode())
                        .add(new ConditionOrNode())
                        .add(new ConditionalChoiceNode<>(null))
                        .add(new InvertConditionNode())
                        .add(new ValuesEqualNode<>(null))
                )
                .add(new Category("Dictionary", Items.CHISELED_BOOKSHELF)
                        .add(new DictionaryGetNode<>(null, null))
                        .add(new DictionaryHasNode<>(null, null))
                        .add(new DictionaryKeysNode<>(null, null))
                        .add(new DictionaryPutNode<>(null, null))
                        .add(new DictionaryRemoveNode<>(null, null))
                        .add(new DictionarySizeNode<>(null, null))
                        .add(new DictionaryValuesNode<>(null, null))
                )
                .add(new Category("Entity", Items.ZOMBIE_HEAD)
                        .add(new ClearEntityEffectsNode())
                        .add(new EntityIsValidNode())
                        .add(new EntityListNode())
                        .add(new EntityPositionNode())
                        .add(new EntityTypeNode())
                        .add(new GiveEntityEffectNode())
                        .add(new RemoveEntityNode())
                        .add(new SetEntityGravityNode())
                        .add(new SetEntityVelocityNode())
                        .add(new SetTNTFuseNode())
                        .add(new SpawnEntityNode())
                        .add(new TakeEntityEffectNode())
                        .add(new TeleportEntityNode())
                )
                .add(new Category("Event", Items.OBSERVER)
                        .add(new Category("Action", Items.STICK)
                                .add(new OnPlayerStartFlyingNode())
                                .add(new OnPlayerStartSneakingNode())
                                .add(new OnPlayerStartSprintingNode())
                                .add(new OnPlayerStopFlyingNode())
                                .add(new OnPlayerStopSneakingNode())
                                .add(new OnPlayerStopSprintingNode())
                                .add(new OnPlayerSwapHandsNode())
                                .add(new OnPlayerSwingHandNode())
                                .add(new OnPlayerUseItemNode())
                        )
                        .add(new Category("Combat", Items.IRON_SWORD)
                                .add(new Category("Entity", Items.ZOMBIE_HEAD)
                                        .add(new OnEntityAttackEntityNode())
                                        .add(new OnEntityAttackPlayerNode())
                                        .add(new OnEntityDeathNode())
                                        .add(new OnEntityHurtNode())
                                        .add(new OnEntityKillEntityNode())
                                        .add(new OnEntityKillPlayerNode())
                                        .add(new OnPlayerAttackEntityNode())
                                        .add(new OnPlayerKillEntityNode())
                                )
                                .add(new OnPlayerAttackPlayerNode())
                                .add(new OnPlayerKillPlayerNode())
                                .add(new OnPlayerDeathNode())
                                .add(new SetEventDamageNode())
                                .add(new OnPlayerHurtNode())
                        )
                        .add(new Category("Meta", Items.COMMAND_BLOCK)
                                .add(new CancelEventNode())
                                .add(new DebugEventNode())
                                .add(new OnInitializeNode())
                                .add(new OnPlayerChatNode())
                                .add(new OnPlayerJoinNode())
                                .add(new OnPlayerLeaveNode())
                        )
                        .add(new Category("World", Items.GRASS_BLOCK)
                                .add(new OnChunkLoadNode())
                                .add(new OnPlayerBreakBlockNode())
                                .add(new OnPlayerDropItemNode())
                                .add(new OnPlayerInteractBlockNode())
                                .add(new OnPlayerPlaceBlockNode())
                                .add(new OnPlayerRespawnNode())
                        )
                        .add(new OnPlayerLoseFoodNode())
                        .add(new OnPlayerLoseSaturationNode())
                )
                .add(new Category("Control", Items.REPEATER)
                        .add(new DictionaryForEach<>(null, null))
                        .add(new GridRepeatNode())
                        .add(new IfNode())
                        .add(new ListForEachNode<>(null))
                        .add(new PauseThreadNode())
                        .add(new RepeatNode())
                        .add(new ScheduleNode())
                        .add(new WhileNode())
                )
                .add(new Category("Item", Items.ITEM_FRAME)
                        .add(new EnchantItemNode())
                        .add(new GetHiddenItemInfoNode())
                        .add(new GetItemCountNode())
                        .add(new GetItemEnchantmentsNode())
                        .add(new GetItemMaterialNode())
                        .add(new IsItemUnbreakableNode())
                        .add(new ItemIdListNode())
                        .add(new ItemsEqualNode())
                        .add(new SetHiddenItemInfoNode())
                        .add(new SetItemCountNode())
                        .add(new SetItemLoreNode())
                        .add(new SetItemMaterialNode())
                        .add(new SetItemNameNode())
                        .add(new SetItemUnbreakableNode())
                )
                .add(new Category("List", Items.BOOKSHELF)
                        .add(new CreateListNode<>(null))
                        .add(new FlattenListNode<>(null))
                        .add(new GetListValueNode<>(null))
                        .add(new IndexInListNode<>(null))
                        .add(new ListAppendNode<>(null))
                        .add(new ListContainsNode<>(null))
                        .add(new ListInsertNode<>(null))
                        .add(new ListIsEmptyNode<>(null))
                        .add(new ListLengthNode<>(null))
                        .add(new ListValuesEqualsNode<>(null))
                        .add(new RemoveListDuplicatesNode<>(null))
                        .add(new RemoveListIndexNode<>(null))
                        .add(new RemoveListValueNode<>(null))
                        .add(new ReverseListNode<>(null))
                        .add(new SetListValueNode<>(null))
                        .add(new ShuffleListNode<>(null))
                        .add(new TrimListNode<>(null))
                )
                .add(new Category("Number", Items.CLOCK)
                        .add(new AbsoluteNumberNode())
                        .add(new AddNumbersNode())
                        .add(new BasicNoiseNode())
                        .add(new ClampNumberNode())
                        .add(new DivideNumbersNode())
                        .add(new GreaterEqualNode())
                        .add(new GreaterThanNode())
                        .add(new LessEqualNode())
                        .add(new LessThanNode())
                        .add(new ModuloNode())
                        .add(new MultiplyNumbersNode())
                        .add(new ParseNumberNode())
                        .add(new RandomNumberNode())
                        .add(new RemainderNode())
                        .add(new RoundNumberNode())
                        .add(new SetToExponentialNode())
                        .add(new SquareRootNode())
                        .add(new SubtractNumbersNode())
                )
                .add(new Category("Player", Items.PLAYER_HEAD)
                        .add(new Category("Gameplay", Items.GRASS_BLOCK)
                                .add(new ClearPlayerEffectsNode())
                                .add(new DamagePlayerNode())
                                .add(new GivePlayerEffectNode())
                                .add(new IsPlayerInvulnerableNode())
                                .add(new KillPlayerNode())
                                .add(new PlaySoundNode())
                                .add(new PlayerAnimationNode())
                                .add(new SetGamemodeNode())
                                .add(new SetPlayerInvulnerableNode())
                                .add(new TakePlayerEffectNode())
                        )
                        .add(new Category("Inventory", Items.ITEM_FRAME)
                                .add(new ClearInventoryNode())
                                .add(new GetHeldSlotNode())
                                .add(new GetPlayerEquipmentNode())
                                .add(new GetPlayerInventoryNode())
                                .add(new GetPlayerItemCooldownNode())
                                .add(new GivePlayerItemNode())
                                .add(new PlayerHandItemsNode())
                                .add(new PlayerHasItemNode())
                                .add(new PlayerItemHasCooldownNode())
                                .add(new SetHeldSlotNode())
                                .add(new SetPlayerInventoryNode())
                                .add(new SetPlayerInventorySlotNode())
                                .add(new SetPlayerItemCooldownNode())
                                .add(new SetPlayerSlotItemNode())
                                .add(new TakePlayerItemNode())
                        )
                        .add(new Category("Meta", Items.COMMAND_BLOCK)
                                .add(new GetPlayerNameNode())
                                .add(new GetPlayerUUIDNode())
                                .add(new IsPlayingNode())
                                .add(new KickPlayerNode())
                                .add(new PlayerFromNameNode())
                                .add(new PlayerFromUUIDNode())
                                .add(new PlayerHasPermissionNode())
                                .add(new PlayerListNode())
                        )
                        .add(new Category("Movement", Items.FEATHER)
                                .add(new GetPlayerVelocityNode())
                                .add(new IsPlayerSneakingNode())
                                .add(new IsPlayerSprintingNode())
                                .add(new PlayerCanFlyNode())
                                .add(new PlayerCrosshairTargetNode())
                                .add(new PlayerIsFlyingNode())
                                .add(new PlayerPositionNode())
                                .add(new PlayerStandingBlockNode())
                                .add(new SetAllowFlyingNode())
                                .add(new SetPlayerFlyingNode())
                                .add(new SetPlayerGlidingNode())
                                .add(new SetPlayerVelocityNode())
                                .add(new TeleportPlayerNode())
                        )
                        .add(new Category("Statistic", Items.EXPERIENCE_BOTTLE)
                                .add(new GetExperienceLevelNode())
                                .add(new GetExperiencePercentageNode())
                                .add(new GetPlayerFoodNode())
                                .add(new GetPlayerHealthNode())
                                .add(new GetPlayerSaturationNode())
                                .add(new SetExperienceLevelNode())
                                .add(new SetPlayerFoodNode())
                                .add(new SetPlayerHealthNode())
                                .add(new SetPlayerSaturationNode())
                        )
                        .add(new Category("Visual", Items.ENDER_PEARL)
                                .add(new BroadcastNode())
                                .add(new SendActionbarNode())
                                .add(new SendBlockChangeNode())
                                .add(new SendMessageNode())
                                .add(new SendTitleNode())
                                .add(new SetPlayerPoseNode())
                                .add(new SetPlayerSkinNode())
                        )
                )
                .add(new Category("Position", Items.COMPASS)
                        .add(new FacingVectorNode())
                        .add(new PackPositionNode())
                        .add(new PositionDistanceNode())
                        .add(new SetPositionComponentNode())
                        .add(new UnpackPositionNode())
                )
                .add(new Category("String", Items.STRING)
                        .add(new CharacterAtNode())
                        .add(new CombineStringsNode())
                        .add(new LowercaseNode())
                        .add(new MatchRegExpNode())
                        .add(new ReplaceStringNode())
                        .add(new SplitStringNode())
                        .add(new StringContainsNode())
                        .add(new StringLayoutNode())
                        .add(new StringLengthNode())
                        .add(new SubstringNode())
                        .add(new UppercaseNode())
                )
                .add(new Category("Text", Items.WRITABLE_BOOK)
                        .add(new CombineTextsNode())
                        .add(new FormatToTextNode())
                )
                .add(new Category("Variable", Items.ENDER_CHEST)
                        .add(new CacheValueNode<>(null))
                        .add(new DecrementVariableNode())
                        .add(new GetVariableNode<>(null))
                        .add(new IncrementVariableNode())
                        .add(new RandomChoiceNode<>(null))
                        .add(new RemoveVariableNode())
                        .add(new SetVariableNode<>(null))
                        .add(new VariableExistsNode())
                )
                .add(new Category("Vector", Items.ARROW)
                        .add(new AddVectorsNode())
                        .add(new GetVectorComponentNode())
                        .add(new PackVectorNode())
                        .add(new ReflectVectorNode())
                        .add(new RoundVectorAxesNode())
                        .add(new SetVectorComponentNode())
                        .add(new SetVectorLengthNode())
                        .add(new SubtractVectorsNode())
                        .add(new UnpackVectorNode())
                )
                .add(new Category("World", Items.GRASS_BLOCK)
                        .add(new BlockIdListNode())
                        .add(new CpuUsageNode())
                        .add(new DebugMessageNode())
                        .add(new GetBlockLightNode())
                        .add(new GetBlockNode())
                        .add(new GetBlockTagInfoNode<>(null))
                        .add(new GetBlockTagListNode())
                        .add(new RaycastNode())
                        .add(new SetBlockNode())
                        .add(new SetBlockTagNode<>(null))
                        .add(new SetRegionNode())
                )
                .add(new Category("Function", Items.COMMAND_BLOCK).markFunctions())
                .finish();

        FireFlow.LOGGER.info("Loaded {} node types", root.collectNodes(null).size());
    }

    public static class Category {
        public final String name;
        public final Item icon;

        public final List<Category> categories = new ArrayList<>();
        public final List<Node> nodes = new ArrayList<>();
        public boolean isFunctions = false;
        public Predicate<Node> filter;

        public Category(String name, Item icon) {
            this.name = name;
            this.icon = icon;
        }

        public Category(Category copy) {
            name = copy.name;
            icon = copy.icon;
            isFunctions = copy.isFunctions;
        }

        public Category add(Node node) {
            nodes.add(node);
            return this;
        }

        public Category add(Category category) {
            categories.add(category);
            return this;
        }

        public Category finish() {
            for (Category category : categories) {
                category.finish();
            }
            categories.sort(Comparator.comparing(c -> c.name));
            nodes.sort(Comparator.comparing(n -> n.name));
            return this;
        }

        public List<Node> collectNodes(CodeEditor editor) {
            List<Node> list = new ArrayList<>(nodes);
            for (Category category : categories) {
                list.addAll(category.collectNodes(editor));
            }
            if (editor != null && isFunctions) {
                for (FunctionDefinition fn : editor.functions.values()) {
                    FunctionCallNode fnNode = new FunctionCallNode(fn);
                    fn.callNodes.remove(fnNode); // Remove since it's not actually a real node
                    if (filter == null || filter.test(fnNode)) nodes.add(fnNode);
                }
            }
            return list;
        }

        public Category markFunctions() {
            isFunctions = true;
            return this;
        }

        public Category filtered(Predicate<Node> filter) {
            Category filtered = new Category(this);
            for (Node n : nodes) {
                if (filter.test(n)) filtered.add(n);
            }
            for (Category c : categories) {
                Category fc = c.filtered(filter);
                if (fc.isFunctions) {
                    fc.filter = filter;
                    filtered.categories.add(fc);
                    continue;
                }
                if (fc.nodes.isEmpty() && fc.categories.isEmpty()) continue;
                filtered.categories.add(fc);
            }
            return filtered;
        }
    }
}
