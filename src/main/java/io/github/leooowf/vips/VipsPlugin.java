package io.github.leooowf.vips;

import com.henryfabio.sqlprovider.connector.SQLConnector;
import io.github.leooowf.vips.cache.KeysCache;
import io.github.leooowf.vips.commands.KeyCommand;
import io.github.leooowf.vips.commands.VipCommand;
import io.github.leooowf.vips.database.SQLProvider;
import io.github.leooowf.vips.database.dao.KeyDAO;
import lombok.Getter;
import me.saiintbrisson.bukkit.command.BukkitFrame;
import me.saiintbrisson.minecraft.command.message.MessageHolder;
import me.saiintbrisson.minecraft.command.message.MessageType;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class VipsPlugin extends JavaPlugin {

    @Getter private static VipsPlugin instance;

    private SQLConnector sqlConnector;
    private LuckPerms luckPerms;

    private KeyDAO keyDAO;

    private KeysCache keysCache;

    @Override
    public void onLoad() {

        instance = this;

        saveDefaultConfig();

        keysCache = new KeysCache();
    }

    @Override
    public void onEnable() {

        try {

            setupDatabase();

            setupLuckPerms();

            setupCommands();

            keysCache.populate();

        } catch (Throwable throwable) {
            throwable.printStackTrace();
            getLogger().severe("Houve um erro na inicialização do plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void setupDatabase() {
        sqlConnector = SQLProvider.createConnector(getConfig().getConfigurationSection("database"));

        keyDAO = new KeyDAO();
        keyDAO.createTable();
    }
    private void setupLuckPerms() {

        RegisteredServiceProvider<LuckPerms> registeredServiceProvider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

        if (registeredServiceProvider != null) {
            luckPerms = registeredServiceProvider.getProvider();
        }
    }

    private void setupCommands() {

        BukkitFrame bukkitFrame = new BukkitFrame(this);

        bukkitFrame.registerCommands(
                new VipCommand(),
                new KeyCommand()
        );

        MessageHolder messageHolder = bukkitFrame.getMessageHolder();
        messageHolder.setMessage(MessageType.NO_PERMISSION, "§cVocê não possui permissão para executar este comando.");
        messageHolder.setMessage(MessageType.ERROR, "§cUm erro ocorreu! {error}");
        messageHolder.setMessage(MessageType.INCORRECT_USAGE, "§cUtilize /{usage}");
        messageHolder.setMessage(MessageType.INCORRECT_TARGET, "§cVocê não pode utilizar este comando pois ele é direcioado apenas para {target}.");
    }
}
