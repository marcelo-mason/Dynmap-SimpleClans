package net.sacredlabyrinth.phaed.dynmap.simpleclans.entries;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans;
import org.bukkit.entity.Player;

public class PlayerEntry
{
    private String name;
    private boolean visible;

    public PlayerEntry(Player player)
    {
        this.name = player.getName();

        // start player not visible

        setVisible(false);
    }


    public Player getPlayer()
    {
        return DynmapSimpleClans.getInstance().getServer().getPlayer(name);
    }

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean visible)
    {
        Player player = getPlayer();

        if (player != null)
        {
            this.visible = visible;
            DynmapSimpleClans.getInstance().getDynmapApi().setPlayerVisiblity(player, visible);
        }
    }

    public String getName()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode() >> 13;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof PlayerEntry))
        {
            return false;
        }

        PlayerEntry other = (PlayerEntry) obj;
        return other.getName().equals(this.getName());
    }

    @Override
    public String toString()
    {
        return name;
    }

}
