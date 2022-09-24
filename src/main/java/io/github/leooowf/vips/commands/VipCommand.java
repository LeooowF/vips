package io.github.leooowf.vips.commands;

import io.github.leooowf.vips.VipsPlugin;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.command.Context;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.github.paperspigot.Title;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VipCommand {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.UK)
            .withZone(ZoneId.systemDefault());

    @Command(
            name = "givevip",
            aliases = {"darvip"},
            permission = "vips.admin",
            usage = "darvip <jogador> <vip> <tempo>"
    )
    public void giveVipCommand(Context<CommandSender> context, Player target, String group, int time) {

        LuckPerms luckPerms = VipsPlugin.getInstance().getLuckPerms();

        Group luckPermsGroup = luckPerms.getGroupManager().getGroup(group);

        if (luckPermsGroup == null) {
            context.sendMessage("§cO grupo inserido é inválido.");
            return;
        }

        if (group.equals("master")
                || group.equals("supervisor")
                || group.equals("gerente")
                || group.equals("admin")
                || group.equals("moderador")
                || group.equals("suporte")) {

            context.sendMessage("§cO grupo informado não é um VIP.");
            return;
        }

        if (time <= 0) {
            context.sendMessage("§cO tempo inserido é inválido.");
            return;
        }

        User user = luckPerms.getPlayerAdapter(Player.class).getUser(target);

        DataMutateResult dataMutateResult = user.data().add(InheritanceNode.builder().group(luckPermsGroup).expiry(time, TimeUnit.DAYS).build());

        if (!dataMutateResult.wasSuccessful()) {
            context.sendMessage("§cNão foi possível adicionar esse vip para este jogador.");
            return;
        }

        Group primary = luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());

        if (primary.getWeight().getAsInt() < luckPermsGroup.getWeight().getAsInt()) {

            DataMutateResult dataMutateResult2 = user.setPrimaryGroup(group);

            if (!dataMutateResult2.wasSuccessful()) {
                context.sendMessage("§cNão foi possível adicionar esse vip para este jogador.");
                return;
            }
        }

        luckPerms.getUserManager().saveUser(user);
        luckPerms.getUserManager().cleanupUser(user);

        String prefix = ChatColor.translateAlternateColorCodes('&', luckPermsGroup.getCachedData().getMetaData().getPrefix());

        Title title = new Title(
                prefix.substring(0, 2) + target.getName(),
                "§ftornou-se " + prefix,
                20, 70, 20
        );

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendTitle(title);
            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.AMBIENCE_THUNDER, 3.0F, 6.0F);
        }

        context.sendMessage("§eVIP adicionado com êxito.");
    }

    @Command(
            name = "viptime",
            aliases = {"tempovip", "diasvip"}
    )
    public void vipTimeCommand(Context<Player> context) {

        Player player = context.getSender();

        LuckPerms luckPerms = VipsPlugin.getInstance().getLuckPerms();

        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);

        List<InheritanceNode> groups = new ArrayList<>();

        for (InheritanceNode inheritanceNode : user.getNodes(NodeType.INHERITANCE)) {

            if (inheritanceNode.hasExpiry()) {
                groups.add(inheritanceNode);
            }
        }

        if (groups.isEmpty()) {
            player.sendMessage("§cVocê não possui nenhum vip ativo.");
            return;
        }

        StringBuilder builder = new StringBuilder("\n");

        for (InheritanceNode node : groups) {

            Group group = luckPerms.getGroupManager().getGroup(node.getGroupName());

            if (group == null) {
                continue;
            }

            Instant expiry = node.getExpiry();

            if (expiry == null) {
                continue;
            }

            builder.append(ChatColor.translateAlternateColorCodes('&', group.getCachedData().getMetaData().getPrefix()))
                    .append(ChatColor.WHITE)
                    .append("Expira em: ")
                    .append(DATE_TIME_FORMATTER.format(expiry))
                    .append("\n");
        }

        builder.append("\n ");

        player.sendMessage(builder.toString());
    }
}