package com.dumptruckman.bartersignsplus.sign;

import com.dumptruckman.bartersignsplus.BarterSignsManager;
import com.dumptruckman.bartersignsplus.config.Config;
import com.dumptruckman.bartersignsplus.locale.Language;
import com.dumptruckman.bartersignsplus.persistence.DB;
import com.dumptruckman.bartersignsplus.util.Logging;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dumptruckman
 */
public class BarterSign implements ConfigurationSerializable {

    private final Block block;
    private final String id;

    private Boolean indestructible;
    private Boolean dropsContents;
    private OfflinePlayer owner;
    private Boolean active;
    private Location location;

    public BarterSign(final Block block) {
        this.block = block;
        this.location = block.getLocation();
        this.id = BarterSignsManager.getSignId(block);
        this.indestructible = Config.SIGN_INDESTRUCTIBLE.getBoolean();
        this.dropsContents = Config.SIGN_DROPS_ITEMS.getBoolean();
        this.active = false;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        result.put("world", this.getBlock().getWorld());
        result.put("x", this.getBlock().getX());
        result.put("y", this.getBlock().getY());
        result.put("z", this.getBlock().getZ());

        result.put("owner", this.getOwner());
        result.put("active", this.isActive());


        result.put("indestructible", this.isIndestructible());
        result.put("dropsContents", this.isDroppingItemsOnBreak());
        //@TODO

        return result;
    }

    public static BarterSign deserialize(Map<String, Object> args) {
        World world = Bukkit.getWorld(args.get("world").toString());
        BarterSign sign = new BarterSign(world.getBlockAt((Integer)args.get("x"), (Integer)args.get("y"), (Integer)args.get("z")));
        sign.setOwner((OfflinePlayer) args.get("owner"));
        sign.setActive((Boolean) args.get("active"));
        if (!sign.isActive()) return sign;
        

        return sign;
    }

    public void save() {
        DB.getData().set(id, this);
    }

    public void initialize(Player owner) {
        this.setOwner(owner);
        this.setActive(false);
        this.setSignText(owner, Language.getStrings(Language.SIGN_SETUP_TEXT, owner.getName()));
        Language.SIGN_SETUP_MESSAGE.info(owner);

        this.save();
    }

    public void setSignText(Player player, List<String> message) {
        Sign sign = this.getSign();
        for (int i = 0; i < 4; i++) {
            sign.setLine(i, message.get(i));
        }
        if (player != null) {
            player.sendBlockChange(this.getLocation(), 0, (byte) 0);
        }
        sign.update(true);
    }

    public Sign getSign() {
        BlockState sign = this.getBlock().getState();
        if (sign instanceof Sign) return (Sign) sign;
        Logging.severe("Tried to get a sign that is no longer a sign!");
        return null;
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Block getBlock() {
        return block;
    }

    public Location getLocation() {
        return location;
    }

    /**
     * Tells whether this sign should be indestructible
     * @return true if sign should be indestructible
     */
    public Boolean isIndestructible() {
        return this.indestructible;
    }

    /**
     * Tells whether the items contained within this sign should drop when the sign breaks
     * @return true if items should drop on sign break
     */
    public Boolean isDroppingItemsOnBreak() {
        return this.dropsContents;
    }

    /**
     * Causes the contents of this sign to drop to the ground
     */
    public void dropContents() {
        
    }

    /**
     * Sets the owner of this sign
     * @param player owner of sign
     */
    public void setOwner(OfflinePlayer player) {
        this.owner = player;
    }

    /**
     * Returns the id of this sign
     * @return id of this sign
     */
    public String getId() {
        return this.id;
    }

    /**
     * Does everything necessary to remove this sign
     */
    public void destroy() {
        if (this.isDroppingItemsOnBreak()) this.dropContents();
    }
}
