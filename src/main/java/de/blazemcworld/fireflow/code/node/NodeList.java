package de.blazemcworld.fireflow.code.node;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.node.impl.condition.*;
import de.blazemcworld.fireflow.code.node.impl.dictionary.*;
import de.blazemcworld.fireflow.code.node.impl.entity.*;
import de.blazemcworld.fireflow.code.node.impl.event.*;
import de.blazemcworld.fireflow.code.node.impl.flow.*;
import de.blazemcworld.fireflow.code.node.impl.item.*;
import de.blazemcworld.fireflow.code.node.impl.list.*;
import de.blazemcworld.fireflow.code.node.impl.number.*;
import de.blazemcworld.fireflow.code.node.impl.player.effect.*;
import de.blazemcworld.fireflow.code.node.impl.player.info.*;
import de.blazemcworld.fireflow.code.node.impl.position.FacingVectorNode;
import de.blazemcworld.fireflow.code.node.impl.position.PackPositionNode;
import de.blazemcworld.fireflow.code.node.impl.position.PositionDistanceNode;
import de.blazemcworld.fireflow.code.node.impl.position.UnpackPositionNode;
import de.blazemcworld.fireflow.code.node.impl.string.*;
import de.blazemcworld.fireflow.code.node.impl.text.CombineTextsNode;
import de.blazemcworld.fireflow.code.node.impl.text.FormatToTextNode;
import de.blazemcworld.fireflow.code.node.impl.variable.*;
import de.blazemcworld.fireflow.code.node.impl.vector.*;
import de.blazemcworld.fireflow.code.node.impl.world.CpuUsageNode;
import de.blazemcworld.fireflow.code.node.impl.world.GetBlockNode;
import de.blazemcworld.fireflow.code.node.impl.world.SetBlockNode;
import de.blazemcworld.fireflow.code.node.impl.world.SetRegionNode;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class NodeList {

    public static Category root;

    public static void init() {
        root = new Category("root", null)
                .add(new Category("Condition", Items.COMPARATOR)
                        .add(new ConditionAndNode())
                        .add(new ConditionalChoiceNode<>(null))
                        .add(new ConditionOrNode())
                        .add(new InvertConditionNode())
                        .add(new ValuesEqualNode<>(null))
                )
                .add(new Category("Dictionary", Items.CHISELED_BOOKSHELF)
                        .add(new DictionaryForEach<>(null, null))
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
                        .add(new EntityListNode())
                        .add(new GiveEntityEffectNode())
                        .add(new RemoveEntityNode())
                        .add(new SetEntityGravityNode())
                        .add(new SetEntityVelocityNode())
                        .add(new SpawnEntityNode())
                        .add(new TakeEntityEffectNode())
                        .add(new TeleportEntityNode())
                )
                .add(new Category("Event", Items.OBSERVER)
                        .add(new CancelEventNode())
                        .add(new OnChunkLoadNode())
                        .add(new OnEntityDeathNode())
                        .add(new OnEntityKillEntityNode())
                        .add(new OnEntityKillPlayerNode())
                        .add(new OnInitializeNode())
                        .add(new OnPlayerBreakBlockNode())
                        .add(new OnPlayerChatNode())
                        .add(new OnPlayerDeathNode())
                        .add(new OnPlayerDropItemNode())
                        .add(new OnPlayerJoinNode())
                        .add(new OnPlayerKillEntityNode())
                        .add(new OnPlayerKillPlayerNode())
                        .add(new OnPlayerLeaveNode())
                        .add(new OnPlayerPlaceBlockNode())
                        .add(new OnPlayerSwapHandsNode())
                        .add(new OnPlayerSwingHandNode())
                        .add(new OnPlayerUseItemNode())
                )
                .add(new Category("Control", Items.REPEATER)
                        .add(new GridRepeatNode())
                        .add(new IfNode())
                        .add(new ListForEachNode<>(null))
                        .add(new PauseThreadNode())
                        .add(new RepeatNode())
                        .add(new ScheduleNode())
                        .add(new WhileNode())
                )
                .add(new Category("Item", Items.ITEM_FRAME)
                        .add(new ItemsEqualNode())
                        .add(new SetItemCountNode())
                        .add(new SetItemLoreNode())
                        .add(new SetItemMaterialNode())
                        .add(new SetItemNameNode())
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
                        .add(new Category("Effect", Items.STONE_SWORD)
                                .add(new BroadcastNode())
                                .add(new ClearInventoryNode())
                                .add(new ClearPlayerEffectsNode())
                                .add(new GivePlayerEffectNode())
                                .add(new GivePlayerItemNode())
                                .add(new KillPlayerNode())
                                .add(new PlaySoundNode())
                                .add(new PlayerAnimationNode())
                                .add(new SendActionbarNode())
                                .add(new SendBlockChangeNode())
                                .add(new SendMessageNode())
                                .add(new SendTitleNode())
                                .add(new SetAllowFlyingNode())
                                .add(new SetExperienceLevelNode())
                                .add(new SetGamemodeNode())
                                .add(new SetHeldSlotNode())
                                .add(new SetPlayerFlyingNode())
                                .add(new SetPlayerFoodNode())
                                .add(new SetPlayerHealthNode())
                                .add(new SetPlayerInventoryNode())
                                .add(new SetPlayerInvulnerableNode())
                                .add(new SetPlayerItemCooldownNode())
                                .add(new SetPlayerSaturationNode())
                                .add(new SetPlayerSkinNode())
                                .add(new SetPlayerSlotItemNode())
                                .add(new SetPlayerVelocityNode())
                                .add(new TakePlayerEffectNode())
                                .add(new TakePlayerItemNode())
                                .add(new TeleportPlayerNode())
                        )
                        .add(new Category("Info", Items.ENDER_PEARL)
                                .add(new GetExperienceLevelNode())
                                .add(new GetExperiencePercentageNode())
                                .add(new GetHeldSlotNode())
                                .add(new GetPlayerFoodNode())
                                .add(new GetPlayerHealthNode())
                                .add(new GetPlayerItemCooldownNode())
                                .add(new GetPlayerNameNode())
                                .add(new GetPlayerSaturationNode())
                                .add(new GetPlayerUUIDNode())
                                .add(new IsPlayerInvulnerableNode())
                                .add(new IsPlayerSneakingNode())
                                .add(new IsPlayingNode())
                                .add(new PlayerCanFlyNode())
                                .add(new PlayerFromNameNode())
                                .add(new PlayerFromUUIDNode())
                                .add(new PlayerHasItemNode())
                                .add(new PlayerIsFlyingNode())
                                .add(new PlayerItemHasCooldownNode())
                                .add(new PlayerListNode())
                                .add(new PlayerMainItemNode())
                                .add(new PlayerOffhandItemNode())
                                .add(new PlayerPositionNode())
                        )
                )
                .add(new Category("Position", Items.COMPASS)
                        .add(new PositionDistanceNode())
                        .add(new FacingVectorNode())
                        .add(new PackPositionNode())
                        .add(new UnpackPositionNode())
                )
                .add(new Category("String", Items.STRING)
                        .add(new CharacterAtNode())
                        .add(new CombineStringsNode())
                        .add(new ReplaceStringNode())
                        .add(new StringLengthNode())
                        .add(new SubstringNode())
                )
                .add(new Category("Vector", Items.ARROW)
                        .add(new AddVectorsNode())
                        .add(new GetVectorComponentNode())
                        .add(new PackVectorNode())
                        .add(new RoundVectorAxesNode())
                        .add(new SetVectorComponentNode())
                        .add(new SetVectorLengthNode())
                        .add(new SubtractVectorsNode())
                        .add(new UnpackVectorNode())
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
                        .add(new SetVariableNode<>(null))
                )
                .add(new Category("World", Items.GRASS_BLOCK)
                        .add(new CpuUsageNode())
                        .add(new GetBlockNode())
                        .add(new SetBlockNode())
                        .add(new SetRegionNode())
                )
                .add(new Category("Function", Items.COMMAND_BLOCK).markFunctions())
                .finish();

        FireFlow.LOGGER.info("Loaded {} node types", root.collectNodes().size());
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

        public List<Node> collectNodes() {
            List<Node> list = new ArrayList<>(nodes);
            for (Category category : categories) {
                list.addAll(category.collectNodes());
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
