package de.blazemcworld.fireflow.node;

import de.blazemcworld.fireflow.node.impl.IfNode;
import de.blazemcworld.fireflow.node.impl.ScheduleNode;
import de.blazemcworld.fireflow.node.impl.ValuesEqualNode;
import de.blazemcworld.fireflow.node.impl.WhileNode;
import de.blazemcworld.fireflow.node.impl.dictionary.DictionaryGetNode;
import de.blazemcworld.fireflow.node.impl.dictionary.DictionaryKeysNode;
import de.blazemcworld.fireflow.node.impl.dictionary.DictionarySetNode;
import de.blazemcworld.fireflow.node.impl.dictionary.EmptyDictionaryNode;
import de.blazemcworld.fireflow.node.impl.event.*;
import de.blazemcworld.fireflow.node.impl.extraction.list.ListSizeNode;
import de.blazemcworld.fireflow.node.impl.extraction.number.NumberToTextNode;
import de.blazemcworld.fireflow.node.impl.extraction.player.*;
import de.blazemcworld.fireflow.node.impl.extraction.position.*;
import de.blazemcworld.fireflow.node.impl.extraction.text.FormatTextToMessageNode;
import de.blazemcworld.fireflow.node.impl.extraction.text.TextToMessageNode;
import de.blazemcworld.fireflow.node.impl.extraction.vector.*;
import de.blazemcworld.fireflow.node.impl.list.*;
import de.blazemcworld.fireflow.node.impl.number.*;
import de.blazemcworld.fireflow.node.impl.number.comparison.GreaterEqualThanNode;
import de.blazemcworld.fireflow.node.impl.number.comparison.GreaterThanNode;
import de.blazemcworld.fireflow.node.impl.number.comparison.LessEqualThanNode;
import de.blazemcworld.fireflow.node.impl.number.comparison.LessThanNode;
import de.blazemcworld.fireflow.node.impl.player.*;
import de.blazemcworld.fireflow.node.impl.position.CreatePositionNode;
import de.blazemcworld.fireflow.node.impl.position.PositionToVectorNode;
import de.blazemcworld.fireflow.node.impl.position.ShiftPositionVectorNode;
import de.blazemcworld.fireflow.node.impl.position.ShiftPositionXYZNode;
import de.blazemcworld.fireflow.node.impl.struct.UnpackStructNode;
import de.blazemcworld.fireflow.node.impl.text.ConcatTextsNode;
import de.blazemcworld.fireflow.node.impl.variable.*;
import de.blazemcworld.fireflow.node.impl.vector.CreateVectorNode;
import de.blazemcworld.fireflow.node.impl.vector.ScaleVectorNode;
import de.blazemcworld.fireflow.node.impl.vector.VectorToPositionNode;
import de.blazemcworld.fireflow.value.ListValue;
import de.blazemcworld.fireflow.value.NumberValue;
import de.blazemcworld.fireflow.value.StructValue;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class NodeList {

    public static final HashMap<String, Supplier<Node>> nodes = new HashMap<>();

    static {
        List<Supplier<Node>> all = List.<Supplier<Node>>of(
                // Sorted alphabetically
                // TIP: Sort the lines automatically using your ide
                () -> new DictionaryGetNode(NumberValue.INSTANCE, NumberValue.INSTANCE),
                () -> new DictionaryKeysNode(NumberValue.INSTANCE, NumberValue.INSTANCE),
                () -> new DictionarySetNode(NumberValue.INSTANCE, NumberValue.INSTANCE),
                () -> new EmptyDictionaryNode(NumberValue.INSTANCE, NumberValue.INSTANCE),
                () -> new EmptyListNode(NumberValue.INSTANCE),
                () -> new ForeachNode(NumberValue.INSTANCE),
                () -> new GetVariableNode(LocalVariableScope.INSTANCE, NumberValue.INSTANCE),
                () -> new GetVariableNode(PersistentVariableScope.INSTANCE, NumberValue.INSTANCE),
                () -> new GetVariableNode(SpaceVariableScope.INSTANCE, NumberValue.INSTANCE),
                () -> new ListAppendNode(NumberValue.INSTANCE),
                () -> new ListContainsNode(NumberValue.INSTANCE),
                () -> new ListFindValueNode(NumberValue.INSTANCE),
                () -> new ListGetNode(NumberValue.INSTANCE),
                () -> new ListGetNode(NumberValue.INSTANCE),
                () -> new ListInsertNode(NumberValue.INSTANCE),
                () -> new ListRemoveAtNode(NumberValue.INSTANCE),
                () -> new ListRemoveValueNode(NumberValue.INSTANCE),
                () -> new ListSizeNode(ListValue.get(NumberValue.INSTANCE)),
                () -> new RandomListValueNode(NumberValue.INSTANCE),
                () -> new SetVariableNode(LocalVariableScope.INSTANCE, NumberValue.INSTANCE),
                () -> new SetVariableNode(PersistentVariableScope.INSTANCE, NumberValue.INSTANCE),
                () -> new SetVariableNode(SpaceVariableScope.INSTANCE, NumberValue.INSTANCE),
                () -> new UnpackStructNode(StructValue.UNKNOWN),
                () -> new ValuesEqualNode(NumberValue.INSTANCE),
                AddNumbersNode::new,
                ClearTitleNode::new,
                ConcatTextsNode::new,
                CreatePositionNode::new,
                CreateVectorNode::new,
                DisplayPlayerDamageAnimationNode::new,
                DivideNumbersNode::new,
                FormatTextToMessageNode::new,
                GreaterEqualThanNode::new,
                GreaterThanNode::new,
                IfNode::new,
                KillPlayerNode::new,
                KnockBackPlayerNode::new,
                LessEqualThanNode::new,
                LessThanNode::new,
                MultiplyNumbersNode::new,
                NormalizedVectorNode::new,
                NumberToTextNode::new,
                PlayerChatEventNode::new,
                PlayerInteractEventNode::new,
                PlayerIsOnGroundNode::new,
                PlayerIsPlayingNode::new,
                PlayerIsSneakingNode::new,
                PlayerJoinEventNode::new,
                PlayerLeaveEventNode::new,
                PlayerNameNode::new,
                PlayerPositionNode::new,
                PlayerPunchPlayerEventNode::new,
                PlayerSneakEventNode::new,
                PlayerStartFlyingEventNode::new,
                PlayerStopFlyingEventNode::new,
                PlayerUUIDNode::new,
                PlayerUnsneakEventNode::new,
                PositionFacingDirectionNode::new,
                PositionPitchNode::new,
                PositionToVectorNode::new,
                PositionXNode::new,
                PositionYNode::new,
                PositionYawNode::new,
                PositionZNode::new,
                RandomNumberNode::new,
                ScaleVectorNode::new,
                ScheduleNode::new,
                SendActionBarNode::new,
                SendMessageNode::new,
                SendTitleNode::new,
                SetAllowPlayerFlyingNode::new,
                SetExperienceNode::new,
                SetGamemodeNode::new,
                SetLevelNode::new,
                SetPlayerElytraFlyingNode::new,
                SetPlayerFireTicksNode::new,
                SetPlayerFlyingNode::new,
                SetPlayerFoodNode::new,
                SetPlayerHealthNode::new,
                SetPlayerSaturationNode::new,
                SetPlayerVelocityNode::new,
                ShiftPositionVectorNode::new,
                ShiftPositionXYZNode::new,
                SubtractNumbersNode::new,
                TeleportPlayerNode::new,
                TextToMessageNode::new,
                VectorLengthNode::new,
                VectorToPositionNode::new,
                VectorXNode::new,
                VectorYNode::new,
                VectorZNode::new,
                WhileNode::new
        );

        for (Supplier<Node> each : all) {
            nodes.put(each.get().getBaseName(), each);
        }
    }

}
