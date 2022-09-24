package io.github.leooowf.vips.commands;

import io.github.leooowf.vips.VipsPlugin;
import io.github.leooowf.vips.model.Key;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.command.Context;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.github.paperspigot.Title;

import java.util.concurrent.TimeUnit;

public class KeyCommand {

    @Command(
            name = "createkey",
            aliases = {"criarkey", "gerarkey"},
            usage = "gerarkey <grupo> <tempo>",
            permission = "vips.admin"
    )
    public void createKeyCommand(Context<CommandSender> context, String group, int time) {

        if (VipsPlugin.getInstance().getLuckPerms().getGroupManager().getGroup(group) == null) {
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

        long now = System.currentTimeMillis();
        long end = now + TimeUnit.DAYS.toMillis(time);

        Key key = new Key(RandomStringUtils.randomAlphabetic(13), group, now, end);

        VipsPlugin.getInstance().getKeyDAO().insertOne(key);
        VipsPlugin.getInstance().getKeysCache().put(key);

        String message = String.format(
                "§fKey: §7%s §8(%s) §7- §f%sD.",
                key.getId(),
                key.getVip().toUpperCase(),
                time
        );

        context.sendMessage(message);
    }

    @Command(
            name = "deletekey",
            aliases = {"deletarkey", "removerkey"},
            usage = "deletarkey <id>",
            permission = "vips.admin"
    )
    public void deleteKeyCommand(Context<CommandSender> context, String id) {

        Key key = VipsPlugin.getInstance().getKeysCache().find(id);

        if (key == null) {
            context.sendMessage("§cA key inserida é inválida.");
            return;
        }

        VipsPlugin.getInstance().getKeysCache().invalidate(key.getId());
        VipsPlugin.getInstance().getKeyDAO().deleteOne(key.getId());

        context.sendMessage("§eKey deletada com êxito.");
    }

    @Command(
            name = "usekey",
            aliases = {"usarkey"},
            usage = "usarkey <id>"
    )
    public void useKeyCommand(Context<Player> context, String id) {

        Player player = context.getSender();

        Key key = VipsPlugin.getInstance().getKeysCache().find(id);

        if (key == null) {
            player.sendMessage("§cA key inserida é inválida.");
            return;
        }

        VipsPlugin.getInstance().getKeysCache().invalidate(key.getId());
        VipsPlugin.getInstance().getKeyDAO().deleteOne(key.getId());

        LuckPerms luckPerms = VipsPlugin.getInstance().getLuckPerms();

        Group group = luckPerms.getGroupManager().getGroup(key.getVip());

        if (group == null) {
            player.sendMessage("§cO grupo inserido é inválido.");
            return;
        }

        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);

        int days = (int) TimeUnit.MILLISECONDS.toDays(key.getEnd() - key.getGenerated());

        DataMutateResult dataMutateResult = user.data().add(InheritanceNode.builder().group(group).expiry(days, TimeUnit.DAYS).build());

        if (!dataMutateResult.wasSuccessful()) {
            context.sendMessage("§cNão foi possível adicionar esse vip para este jogador.");
            return;
        }

        Group primary = luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());

        if (primary.getWeight().getAsInt() < group.getWeight().getAsInt()) {

            DataMutateResult dataMutateResult2 = user.setPrimaryGroup(key.getVip());

            if (!dataMutateResult2.wasSuccessful()) {
                context.sendMessage("§cNão foi possível adicionar esse vip para este jogador.");
                return;
            }
        }

        luckPerms.getUserManager().saveUser(user);
        luckPerms.getUserManager().cleanupUser(user);

        String prefix = ChatColor.translateAlternateColorCodes('&', group.getCachedData().getMetaData().getPrefix());

        Title title = new Title(
                prefix.substring(0, 2) + player.getName(),
                "§ftornou-se " + prefix,
                20, 70, 20
        );

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendTitle(title);
            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.AMBIENCE_THUNDER, 3.0F, 6.0F);
        }
    }
}