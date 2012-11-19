package net.sacredlabyrinth.phaed.dynmap.simpleclans.entries;


import com.p000ison.dev.simpleclans2.clanplayer.ClanPlayer;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.Helper;
import org.bukkit.Location;

import java.util.Date;

public class KillEntry
{
    private ClanPlayer victim;
    private ClanPlayer attacker;
    private Location location;
    private long timestamp;

    public KillEntry(ClanPlayer victim, ClanPlayer attacker, Location location)
    {
        this.victim = victim;
        this.attacker = attacker;
        this.timestamp = new Date().getTime();
        this.location = location;
    }

    public long getAgeSeconds()
    {
        return (System.currentTimeMillis() - timestamp) / 1000;
    }

    public ClanPlayer getVictim()
    {
        return victim;
    }

    public ClanPlayer getAttacker()
    {
        return attacker;
    }

    public Location getLocation()
    {
        return location;
    }

    @Override
    public String toString()
    {
        return victim.getName() + "." + attacker.getName() + "." + Helper.toLocationString(location);
    }
}
