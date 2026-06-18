package de.blazemcworld.fireflow.code.node.impl.player.visual.bossbar;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.*;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import de.blazemcworld.fireflow.space.BossBarManager;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.List;

public class SetBossbarNode extends Node {

    public SetBossbarNode() {
        super("set_bossbar", "Set Bossbar", "Sets a bossbar for a player", Items.BIRCH_SIGN);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Text> title = new Input<>("title", "Title", TextType.INSTANCE);
        Input<Double> current_health = new Input<>("current_health", "Current Health", NumberType.INSTANCE);
        Input<Double> max_health = new Input<>("max_health", "Maximum Health", NumberType.INSTANCE);
        Input<Double> position = new Input<>("position", "Position", NumberType.INSTANCE);
        Input<String> barColor = new Input<>("bar_color", "Bar Color", StringType.INSTANCE).options("Red","Purple","Pink","Blue","Green","Yellow","White");
        Input<String> barStyle = new Input<>("bar_style", "Bar Style", StringType.INSTANCE).options("Solid","6 Segments","10 Segments","12 Segments","20 Segments");
        Input<String> skyEffect = new Input<>("sky_effect", "Sky Effect", StringType.INSTANCE).options("None","Thicken Fog","Darken Sky","Both");

        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                Space space = SpaceManager.getSpaceForPlayer(p);
                BossBarManager bossBarManager = space.bossBarManager;
                BossBar.Color color = switch(barColor.getValue(ctx)) {
                    case "Red" -> BossBar.Color.RED;
                    case "Purple" -> BossBar.Color.PURPLE;
                    case "Pink" -> BossBar.Color.PINK;
                    case "Blue" -> BossBar.Color.BLUE;
                    case "Green" -> BossBar.Color.GREEN;
                    case "Yellow" -> BossBar.Color.YELLOW;
                    default -> BossBar.Color.WHITE;
                };
                BossBar.Style style = switch(barStyle.getValue(ctx)) {
                    case "6 Segments" -> BossBar.Style.NOTCHED_6;
                    case "10 Segments" -> BossBar.Style.NOTCHED_10;
                    case "12 Segments" -> BossBar.Style.NOTCHED_12;
                    case "20 Segments" -> BossBar.Style.NOTCHED_20;
                    default -> BossBar.Style.PROGRESS;
                };
                ServerBossBar bossBar = new ServerBossBar(
                        title.getValue(ctx),
                        color,
                        style
                );
                switch(skyEffect.getValue(ctx)) {
                    case "Thicken Fog" -> bossBar.setThickenFog(true);
                    case "Darken Sky" -> bossBar.setDarkenSky(true);
                    case "Both" -> {
                        bossBar.setThickenFog(true);
                        bossBar.setDarkenSky(true);
                    }
                }
                bossBar.setPercent(current_health.getValue(ctx).floatValue()/max_health.getValue(ctx).floatValue());
                List<ServerBossBar> bossBars = bossBarManager.getBossBars(p);
                bossBarManager.setBossBarAtIndex(p,position.getValue(ctx).intValue(), bossBar);
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetBossbarNode();
    }
}
